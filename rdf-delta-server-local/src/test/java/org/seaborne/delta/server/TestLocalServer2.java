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

package org.seaborne.delta.server;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.tdb.base.file.Location;
import org.junit.*;
import org.seaborne.delta.DeltaException;
import org.seaborne.delta.Id;
import org.seaborne.delta.server.local.LocalServer;

/**
 * Tests of {@link LocalServer} for creating and 
 * deleteing a {@link LocalServer} area.
 * 
 * See {@link TestLocalServer1} for tests involving
 * a static setup of data sources.
 */

public class TestLocalServer2 {

    // Testing area that is created and modified by tests. 
    private static String DIR = "target/testing/delta";

    private static void initialize() {
        FileOps.ensureDir(DIR);
        FileOps.clearAll(DIR);
        // copy in setup.
        try { FileUtils.copyDirectory(new File(TestLocalServer1.SERVER_DIR), new File(DIR)); }
        catch (IOException ex) { IO.exception(ex); }
    }
    
    @Before public void beforeTest() {
        initialize();
    }
    
    // Create does not overwrite
    @Test public void local_server_01() {
        Location loc = Location.create(DIR);
        LocalServer server = LocalServer.attach(loc);
        Id newId = server.createDataSource(false, "XYZ", "http://example/xyz");
        assertNotNull(newId);
    }
    
    // Create does not overwrite
    @Test public void local_server_02() {
        Location loc = Location.create(DIR);
        LocalServer server = LocalServer.attach(loc);
        
        Id newId1 = server.createDataSource(false, "XYZ", "http://example/xyz");
        try {
            Id newId2 = server.createDataSource(false, "XYZ", "http://example/xyz");
            fail("Expected createDataSource to fail");
        } catch (DeltaException ex) {}
    }
}
