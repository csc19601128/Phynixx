package org.csc.phynixx.common.generator;

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


/**
 * Created by christoph on 23.02.14.
 */
class SynchronizedGenerator<T> implements IDGenerator<T> {

    private final IDGenerator<T> delegates;

    SynchronizedGenerator(IDGenerator<T> delegates) {
        this.delegates = delegates;
        if(delegates==null) {
            throw new IllegalArgumentException("delegates must be defined");
        }
    }

    @Override
    public T getCurrent() {
        synchronized (delegates) {
            return delegates.getCurrent();
        }
    }

    @Override
    public T generate() {

        synchronized (delegates) {
            return delegates.generate();
        }
    }
}
