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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christoph Schmidt-Casdorff on 13.01.14.
 */
public class LogRecordPageWriter {

    private static final byte[][] EMPTY_DATA = new byte[][]{};

    private final List<LogRecordWriter> logWriters = new ArrayList<LogRecordWriter>();

    /**
     * @return number of lines
     */
    public int size() {
        return logWriters.size();
    }

    public List<LogRecordWriter> getLogWriters() {
        return Collections.unmodifiableList(this.logWriters);
    }

    public LogRecordWriter newLine() {
        LogRecordWriter logRecordWriter = new LogRecordWriter();
        logWriters.add(logRecordWriter);
        return (logRecordWriter);
    }


    public byte[][] toByteByte() throws IOException {
        if (this.logWriters.isEmpty()) {
            return EMPTY_DATA;
        }

        byte[][] records = new byte[logWriters.size()][];

        for (int i = 0; i < logWriters.size(); i++) {
            records[i] = logWriters.get(i).toByteArray();
        }
        return records;

    }


}
