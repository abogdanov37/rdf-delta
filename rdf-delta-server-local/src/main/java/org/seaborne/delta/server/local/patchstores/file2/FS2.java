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

package org.seaborne.delta.server.local.patchstores.file2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Iterator;
import java.util.Map;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.ext.com.google.common.collect.BiMap;
import org.apache.jena.ext.com.google.common.collect.HashBiMap;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.seaborne.delta.Id;
import org.seaborne.delta.Version;
import org.seaborne.delta.lib.IOX;
import org.seaborne.delta.server.local.filestore.FileStore;
import org.seaborne.patch.PatchHeader;
import org.seaborne.patch.text.RDFPatchReaderText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FS2 {
    private static Logger LOG = LoggerFactory.getLogger(FilePatchIdx.class);

    /**
     * Inspect a {@link FileStore} and build the in-memory maps.
     * Return the latest version or null if no patches.
     */
    public static FilePatchIdx initFromFileStore(FileStore fileStore) {
        BiMap<Id, Version> idToVersion = Maps.synchronizedBiMap(HashBiMap.create());
        Map<Id, PatchHeader> headers = null; //new ConcurrentHashMap<>();

        // Iterator is sorted.
        Iterator<Long> iter = fileStore.getIndexes().iterator();

        Version earliestVersion = null;     // Not found yet.
        Version currentPreviousVersion = null;
        Version currentVersion = null;      // Not found yet.
        PatchHeader previous = null;
        boolean first = true;

        for ( ; iter.hasNext() ; ) {
            long idx = iter.next();
            try ( InputStream in = fileStore.open(idx) ) {
                PatchHeader patchHeader = RDFPatchReaderText.readerHeader(in);
                if ( patchHeader == null ) {
                    FmtLog.error(LOG, "Can't read header: idx=%d", idx);
                    continue;
                }
                Id id = Id.fromNode(patchHeader.getId());
                if ( id == null ) {
                    FmtLog.error(LOG, "Can't find id: idx=%d: id=%s", idx, id);
                    continue;
                }
                else {
                    if ( idToVersion.containsKey(id) ) {
                        FmtLog.error(LOG, "Duplicate: idx=%d: id=%s", idx, id);
                    }
                }

                Id prev = Id.fromNode(patchHeader.getPrevious());
                if ( prev != null ) {
                    // We process entries in order so we should have seen previous by now.
                    if ( ! idToVersion.containsKey(prev) ) {
                        FmtLog.error(LOG, "Can't find previous: idx=%d: id=%s, prev=%s", idx, id, prev);
                        continue;
                    }
                }
                if ( headers != null )
                    headers.put(id, patchHeader);
                Version ver = Version.create(idx);
                idToVersion.put(id, ver);
                if ( earliestVersion == null )
                    earliestVersion = ver;
                currentPreviousVersion = currentVersion;
                currentVersion = ver;
            }
            catch (NoSuchFileException ex) { throw IOX.exception(ex); }
            catch (IOException ex)  { throw IOX.exception(ex); }
        }
        return new FilePatchIdx(fileStore, idToVersion, currentVersion, currentPreviousVersion, earliestVersion);
    }

}
