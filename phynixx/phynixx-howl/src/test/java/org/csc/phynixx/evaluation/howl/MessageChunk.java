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


public class MessageChunk {

    private long backwardReference = -1L;
    private Long messageId = null;
    private byte[] data = null;


    public MessageChunk(long backwardReference, long messageId, byte[] data) {
        super();
        this.backwardReference = backwardReference;
        this.messageId = new Long(messageId);
        this.data = data;
    }

    public long getBackwardReference() {
        return backwardReference;
    }

    public Long getMessageId() {
        return messageId;
    }

    public byte[] getData() {
        return data;
    }


}
