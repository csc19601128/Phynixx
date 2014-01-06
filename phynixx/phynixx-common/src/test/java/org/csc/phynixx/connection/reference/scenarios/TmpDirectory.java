/**
 *
 */
package org.csc.phynixx.connection.reference.scenarios;

/*
 * #%L
 * phynixx-common
 * %%
 * Copyright (C) 2014 csc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.File;


public class TmpDirectory {
    private static final String MY_TMP = "scenarios";
    private File dir = null;


    public TmpDirectory(String relDirectory) {
        String tmpDir = System.getProperty("java.io.tmpdir");
        dir = new File(tmpDir + File.separator + relDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public TmpDirectory() {
        this(MY_TMP);
    }

    public File getDirectory() {
        return this.dir;
    }

    /**
     * removes all files but keeps the directories ...
     */
    public void clear() {

        if (dir == null) {
            return;
        }
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
    }

    /**
     * removes all files and directories relative to java.io.tmpdir ...
     */
    public void rmdir() {

        if (dir == null) {
            return;
        }
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
        this.dir.delete();
        this.dir = null;
    }


}
