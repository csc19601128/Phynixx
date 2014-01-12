package org.csc.phynixx.io;

/*
 * #%L
 * phynixx-common
 * %%
 * Copyright (C) 2014 Christoph Schmidt-Casdorff
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


import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by christoph on 12.01.14.
 */
public class LogRecordWriter {


    ByteArrayOutputStream byteInput;

    DataOutputStream io;

    public LogRecordWriter() {
        this.byteInput = new ByteArrayOutputStream();
        this.io = new DataOutputStream(this.byteInput);
    }


    public byte[] toByteArray() throws IOException {
        this.io.flush();
        return byteInput.toByteArray();
    }

    public void writeChar(int v) throws IOException {
        io.writeChar(v);
    }

    public void writeShort(int v) throws IOException {
        io.writeShort(v);
    }

    public void writeInt(int v) throws IOException {
        io.writeInt(v);
    }

    public void writeLong(long v) throws IOException {
        io.writeLong(v);
    }

    public void writeFloat(float v) throws IOException {
        io.writeFloat(v);
    }

    public void writeDouble(double v) throws IOException {
        io.writeDouble(v);
    }

    public void writeUTF(String str) throws IOException {
        io.writeUTF(str);
    }

    public void writeBoolean(boolean v) throws IOException {
        io.writeBoolean(v);
    }

    public void close() {
        if (this.byteInput != null) {
            try {
                this.io.flush();
            } catch (IOException e) {
            }
            IOUtils.closeQuietly(this.byteInput);
        }
    }

}
