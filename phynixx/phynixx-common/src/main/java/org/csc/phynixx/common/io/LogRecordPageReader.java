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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by zf4iks2 on 13.01.14.
 */
public class LogRecordPageReader {

    private List<LogRecordReader> logReaders = new ArrayList<LogRecordReader>();


    public LogRecordPageReader(byte[][] records) {
        if (records == null || records.length == 0) {
            return;
        }

        for (int i = 0; i < records.length; i++) {
            LogRecordReader reader = new LogRecordReader(records[i]);
            this.logReaders.add(reader);
        }
    }

    /**
     * @return number of lines
     */
    public int size() {
        return logReaders.size();
    }

    public List<LogRecordReader> getLogReaders() {
        return Collections.unmodifiableList(this.logReaders);
    }


}
