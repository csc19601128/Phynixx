/**
 *
 */
package org.csc.phynixx.common;

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


import org.apache.commons.io.FilenameUtils;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;

import java.io.File;
import java.io.IOException;


public class TmpDirectory {
    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(TmpDirectory.class);
    private static final String MY_TMP = "de_csc_xaresource";
    private File dir = null;


    public TmpDirectory(String relDirectory) throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        dir = new File(tmpDir + File.separator + relDirectory);
        if (!dir.exists()) {
            if(!dir.mkdirs()) {
                throw new IOException("Cannot mddirs for Directory "+dir);
            }
        }
        dir.deleteOnExit();
    }

    public TmpDirectory() throws IOException {
        this(MY_TMP);
    }

    public File getDirectory() {
        return this.dir;
    }

    public void delete() {
        this.clear();
        if(!this.dir.delete()) {
            LOG.error("Deletion of "+this.dir+" failed");
        }
        this.dir = null;
    }

    public void clear() {
        this.clearDirectory(dir);
    }

    private void clearDirectory(File dir) {

        if (dir == null) {
            return;
        }

        if (!dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                this.clearDirectory(file);
            }
            if (!file.delete()) {
                LOG.error("deleting " + file + " fails");
            }
        }
    }

    public File assertExitsFile(String filename) throws IOException {
        File parentDir = this.assertExitsDirectory(FilenameUtils.getPath(filename));

        String name = FilenameUtils.getName(filename);
        String fullname = FilenameUtils.normalize(parentDir.getAbsolutePath() + File.separator + name);
        File file = new File(fullname);
        if(!file.createNewFile()) {
            throw new IOException("Creation of "+ filename+ " in Tmp-Directory failed");
        }

        return file;

    }

    public File assertExitsDirectory(String dirname) throws IOException {

        File directory = new File(this.dir.getAbsolutePath() + File.separator + dirname);
        if (directory.exists() && !directory.isDirectory()) {
            throw new IllegalStateException(dirname + " is not a directory");
        }
        if (!directory.exists()) {
            if(!directory.mkdirs()) {
                throw new IOException("Deletion of "+ dirname+ " in Tmp-Directory failed");
            }
        }
        return directory;

    }


}
