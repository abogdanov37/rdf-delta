/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seaborne.delta;

import java.net.BindException;

import org.apache.curator.test.TestingServer;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.riot.web.HttpOp;
import org.seaborne.delta.client.DeltaLinkHTTP;
import org.seaborne.delta.lib.IOX;
import org.seaborne.delta.link.DeltaLink;
import org.seaborne.delta.server.ZkT;
import org.seaborne.delta.server.http.PatchLogServer;
import org.seaborne.delta.server.local.*;

public class Setup {
    static { DPS.init(); }

    public interface LinkSetup {
//        public void beforeSuite();
//        public void afterSuite();
        public void beforeClass();
        public void afterClass();
        public void beforeTest();
        public void afterTest();
        
        public void relink();       // Same server, new link.
        public void restart();      // Different server, same state.

        public DeltaLink getLink();
        public DeltaLink createLink();  // New link every time.
        public boolean restartable();
    }
    
    public static class LocalSetup implements LinkSetup {
        protected LocalServer lserver = null;
        protected DeltaLink dlink = null;
        private final Creator<LocalServer> builder;
        private final boolean restartable;
        
        private LocalSetup(Creator<LocalServer> builder, boolean restartable) {
            this.builder = builder;
            this.restartable = restartable;
        }
        
        public static LinkSetup createMem() {
            return new LocalSetup(()->LocalServers.createMem(), false);
        }
        
        public static LinkSetup createFile() {
            return new LocalSetup(()->DeltaTestLib.createEmptyTestServer(), true);
        }
        
        public static LinkSetup createZkMem() {
            return new LocalSetup(()->{
                TestingServer server = ZkT.localServer();
                DataRegistry dataRegistry = new DataRegistry("Zk-LocalServer");
                String connectionString = server.getConnectString();
                LocalServerConfig config = LocalServers.configZk(connectionString);
                PatchStore patchStore = PatchStoreMgr.getPatchStoreProvider(DPS.PatchStoreZkProvider).create(config);
                patchStore.initialize(dataRegistry, config);
                LocalServer localServer = LocalServer.create(patchStore, config);
                return localServer;
            }, false);
        }
        
        @Override
        public void beforeClass() { DPS.init(); }

        @Override
        public void afterClass() {}

        @Override
        public DeltaLink getLink() {
            return dlink;
        }
        
        @Override
        public DeltaLink createLink() {
            return DeltaLinkLocal.connect(lserver); 
        }
        
        @Override
        public void beforeTest() {
            lserver = builder.create();
            dlink = createLink();
        }
        
        @Override
        public void afterTest() {
            if ( lserver != null )
                LocalServer.release(lserver);
        }

        @Override
        public void relink() {
            dlink =  DeltaLinkLocal.connect(lserver);
        }
        
        @Override
        public void restart() {
            if ( lserver == null )
                lserver = builder.create();
            else {
                LocalServerConfig config = lserver.getConfig() ;
                LocalServer.release(lserver);
                lserver = LocalServer.create(config);
            }
            relink();
        }

        @Override
        public boolean restartable() {
            return restartable;
        }
    }

    public static class RemoteSetup implements LinkSetup {

        private static int TEST_PORT=1086;
        
        /** Start a server - this server has no backing local DeltaLink
         * which is reset for each test. This enables the server to be reused 
         * (problems starting and stopping the background server
         * synchronous to the tests otherwise).   
         */
        public static PatchLogServer startPatchServer() {
            PatchLogServer dps = PatchLogServer.create(TEST_PORT, null) ;
            try { dps.start(); }
            catch (BindException ex) { throw IOX.exception(ex); }
            return dps;
        }
        
        public static void stopPatchServer(PatchLogServer dps) {
            dps.stop();
            // Clear cached connections.
            resetDefaultHttpClient();
        }
        
        // Local server of the patch server.
        private LocalServer localServer = null;
        private static PatchLogServer server = null;
        private DeltaLink dlink = null;
        
        @Override
        public void beforeClass() {
            if ( server == null )
                server = startPatchServer();
        }
        
        @Override
        public void afterClass() {
            stopPatchServer(server);
            server = null ;
            
        }

        @Override
        public void beforeTest() {
            localServer = DeltaTestLib.createEmptyTestServer();
            DeltaLink localLink = DeltaLinkLocal.connect(localServer);
            server.setEngine(localLink);
            dlink = createLink();
        }

        @Override
        public void afterTest() {
            LocalServer.release(localServer);
            server.setEngine(null);
            dlink = null;
        }
        
        @Override
        public void relink() {
            resetDefaultHttpClient();
            dlink = createLink();
        }
        
        
        @Override
        public void restart() {
            LocalServerConfig config = localServer.getConfig() ;
            LocalServer.release(localServer);
            localServer = LocalServer.create(config);
            resetDefaultHttpClient();
            DeltaLink localLink = DeltaLinkLocal.connect(localServer);
            server.setEngine(localLink);
            relink();
        }
        
        @Override
        public boolean restartable() {
            return true;
        }

        @Override
        public DeltaLink getLink() {
            return dlink;
        }
        
        @Override
        public DeltaLink createLink() {
            return DeltaLinkHTTP.connect("http://localhost:"+TEST_PORT+"/");
        }

        private static void resetDefaultHttpClient() {
            setHttpClient(HttpOp.createDefaultHttpClient());
        }
        
        /** Set the HttpClient - close the old one if appropriate */
        /*package*/ static void setHttpClient(HttpClient newHttpClient) {
            HttpClient hc = HttpOp.getDefaultHttpClient() ;
            if ( hc instanceof CloseableHttpClient )
                IO.close((CloseableHttpClient)hc) ;
            HttpOp.setDefaultHttpClient(newHttpClient) ;

        }
    }
}
