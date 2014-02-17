package org.csc.phynixx.xa.recovery;

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


import org.objectweb.jotm.TransactionResourceManager;

import javax.transaction.xa.XAResource;

/**
 * Dummy implememntation of JOTM's TransactionresourceManager
 * <p/>
 * It's used by JOTM to record the Mapping of XAResourceProgressState's name to XAResource
 *
 * @author christoph
 */

public class SampleTransactionResourceManager implements TransactionResourceManager {

    public void returnXAResource(String rmName, XAResource rmXares) {
        return;

    }

}
