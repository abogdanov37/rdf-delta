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

package org.seaborne.delta.server.local.patchstores.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.jena.atlas.logging.Log;
import org.seaborne.delta.DeltaConfigException;
import org.seaborne.delta.server.local.PatchStore;
import org.seaborne.delta.server.local.PatchStoreProvider;

public class PatchStoreProviderZk implements PatchStoreProvider {

    public PatchStoreProviderZk() {}
    
    @Override
    public PatchStore create() {
        // initFromPersistent(LocalServerConfig config)
        
        RetryPolicy policy = new ExponentialBackoffRetry(10000, 5);
        // comma separated host:port pairs
        String connectString = System.getProperty("delta.zk");
        try {
            CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                //.connectionHandlingPolicy(ConnectionHandlingPolicy.)
                .retryPolicy(policy)
                .build();
            client.start();
            client.blockUntilConnected();
            return new PatchStoreZk(client);
        }
        catch (Exception ex) {
            Log.error(this,  "Failed to setup zookeeper backed PatchStore", ex);
            throw new DeltaConfigException("Failed to setup zookeeper backed PatchStore", ex);
        }
    }
    
    @Override
    public String getShortName() {
        return "zk";
    }
}