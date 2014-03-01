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


import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IXADataRecorderAware;
import org.csc.phynixx.connection.RequiresTransaction;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by zf4iks2 on 04.02.14.
 */
public interface TAEnabledUTFWriter extends IPhynixxConnection, IXADataRecorderAware {

    /**
     * resets the content of the file associated with die current transaction
     * @throws IOException
     */
    @RequiresTransaction
    void resetContent() throws IOException;

    @RequiresTransaction
    /**
     * opens a file and associates it with the current transaction
     */
    void open(File file);

    @RequiresTransaction
    TAEnabledUTFWriter write(String value) throws IOException;

    List<String> getContent();
}
