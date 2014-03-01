package org.csc.phynixx.xa;

/*
 * #%L
 * phynixx-xa
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


public class IDGenerator implements IResourceIDGenerator {

    private Long current = new Long(1);

    public IDGenerator() {
        this(0);
    }

    public IDGenerator(long start) {
        super();
        this.current = new Long(start);
    }

    public long getCurrentLong() {
        return this.current.longValue();
    }

    public long generateLong() {
        this.generate();
        return this.getCurrentLong();
    }


    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.IResourceIDGenerator#getCurrent()
     */
    public Object getCurrent() {
        return this.current.toString();
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.IResourceIDGenerator#generate()
     */
    public Object generate() {
        long cc = current.longValue();
        this.current = new Long(++cc);
        return this.getCurrent();
    }


}
