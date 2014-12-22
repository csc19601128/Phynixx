package org.csc.phynixx.common.generator;

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


class IDLongGenerator implements IDGenerator<Long>{


    private Long current = (long) 1;

    IDLongGenerator() {
        this(0l);
    }


    IDLongGenerator(Long seed) {
        current=seed;
    }


    public long getCurrentLong() {
        return this.current;
    }

    public long generateLong() {
        this.generate();
        return this.getCurrentLong();
    }


    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.IResourceIDGenerator#getCurrent()
     */
    @Override
    public Long getCurrent() {
        return this.current;
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.IResourceIDGenerator#generate()
     */
    @Override
    public Long generate() {
        long cc = current;
        this.current = cc + 1;
        return this.getCurrent();
    }


}
