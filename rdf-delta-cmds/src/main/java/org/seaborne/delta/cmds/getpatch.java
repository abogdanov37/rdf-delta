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

package org.seaborne.delta.cmds;

import jena.cmd.CmdException ;
import org.seaborne.delta.client.DeltaConnection ;

/** Create a new log */
public class getpatch extends DeltaCmd {
    
    public static void main(String... args) {
        new getpatch(args).mainRun();
    }

    public getpatch(String[] argv) {
        super(argv) ;
        super.add(argDataSourceName);
        super.add(argDataSourceURI);
    }

    @Override
    protected String getSummary() {
        return getCommandName()+"--server URL --dsrc NAME id";
    }
    
    @Override
    protected void execCmd() {
        
        DeltaConnection dConn = DeltaConnection.connect(null, null, null, null, dLink);
        
        throw new CmdException(getCommandName()+" : Not implemented"); 
    }

    @Override
    protected void checkForMandatoryArgs() {
        if ( !contains(argDataSourceName) && ! contains(argDataSourceURI) ) 
            throw new CmdException("Required: one of --"+argDataSourceName.getKeyName()+" or --"+argDataSourceURI.getKeyName());
    }
}