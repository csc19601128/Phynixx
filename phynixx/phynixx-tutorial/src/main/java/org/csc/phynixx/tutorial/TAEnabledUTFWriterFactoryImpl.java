package org.csc.phynixx.tutorial;

/*
 * #%L
 * phynixx-tutorial
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


import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.generator.IDGenerator;
import org.csc.phynixx.common.generator.IDGenerators;

import java.io.File;

/**
 * Created by Christoph Schmidt-Casdorff on 04.02.14.
 */
public class TAEnabledUTFWriterFactoryImpl implements TAEnabledUTFWriterFactory {

    private static final IDGenerator<Long> idGenerator= IDGenerators.createLongGenerator(1,true);

    private final UTFWriter sharedWriter;


    public TAEnabledUTFWriterFactoryImpl(File sharedFile) {
        this.sharedWriter = new UTFWriterImpl(sharedFile);
    }

    @Override
    public TAEnabledUTFWriter getConnection() {
        try {
            return new TAEnabledUTFWriterImpl(Long.toString(idGenerator.generate()), this.sharedWriter);
        } catch (Exception e) {
           throw new DelegatedRuntimeException(e);
        }
    }

    @Override
    public Class<TAEnabledUTFWriter> getConnectionInterface() {
        return TAEnabledUTFWriter.class;
    }

    @Override
    public void close() {
        this.sharedWriter.close();

    }
}
