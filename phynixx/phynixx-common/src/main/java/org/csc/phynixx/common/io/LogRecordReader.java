package org.csc.phynixx.common.io;

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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by christoph on 12.01.14.
 */
public class LogRecordReader {

    ByteArrayInputStream byteInput;

    DataInputStream io;

    public LogRecordReader(byte[] content) {
        this.byteInput = new ByteArrayInputStream(content);
        this.io = new DataInputStream(this.byteInput);

    }

    public String readUTF() throws IOException {
        return this.io.readUTF();
    }

    public long readLong() throws IOException {
        return this.io.readLong();
    }


    public boolean readBoolean() throws IOException {
        return this.io.readBoolean();
    }

    public int readInt() throws IOException {
        return this.io.readInt();
    }


    public float readFloat() throws IOException {
        return this.io.readFloat();
    }

    public double readDouble() throws IOException {
        return this.io.readDouble();
    }

    public short readShort() throws IOException {
        return io.readShort();
    }

    public void close() {
        if (this.byteInput != null) {
            IOUtils.closeQuietly(this.byteInput);
        }
    }


}
