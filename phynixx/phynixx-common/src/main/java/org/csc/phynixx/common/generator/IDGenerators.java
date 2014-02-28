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
public class IDGenerators {


    public static <T> IDGenerator<T> synchronizeGenerator(IDGenerator<T> generator) {
        return new SynchronizedGenerator<T>(generator);
    }



    public static IDGenerator<Long> createLongGenerator(long seed) {

        return new IDLongGenerator(seed);
    }

    public static IDGenerator<Long> createLongGenerator(long seed, boolean synchronize) {

        IDGenerator<Long> generator=new IDLongGenerator(seed);
        if(synchronize) {
            generator= synchronizeGenerator(generator);
        }

        return generator;
    }

}
