package org.csc.phynixx.phynixx.evaluation.howl;

/*
 * #%L
 * phynixx-howl
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


import java.util.ArrayList;
import java.util.List;

public class LogMessage {

    private Long messageId = null;

    private List messageChunks = new ArrayList();

    private transient byte[] data = null; //tmp array for data

    public LogMessage(long messageId) {
        this.messageId = new Long(messageId);
    }

    public LogMessage(Long messageId) {
        this(messageId.longValue());
    }

    public void addMessageChunk(MessageChunk chunk) {
        this.messageChunks.add(chunk);
    }

    public List getMessageChunks() {
        return messageChunks;
    }

    public byte[][] getData() {

        byte[][] data = new byte[messageChunks.size()][];
        for (int i = 0; i < this.messageChunks.size(); i++) {
            data[i] = ((MessageChunk) this.messageChunks.get(i)).getData();
        }
        return data;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof LogMessage)) {
            return false;
        }
        return this.messageId.longValue() == ((LogMessage) obj).getMessageId().longValue();
    }

    public Long getMessageId() {
        return this.messageId;
    }

    public int hashCode() {
        return this.messageId.hashCode();
    }


}
