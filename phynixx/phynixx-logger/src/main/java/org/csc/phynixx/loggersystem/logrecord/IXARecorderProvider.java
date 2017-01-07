package org.csc.phynixx.loggersystem.logrecord;

/*
 * #%L
 * phynixx-logger
 * %%
 * Copyright (C) 2014 - 2017 Christoph Schmidt-Casdorff
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
 * 
 * provides fresh and ready for use {@link IXADataRecorder}
 * 
 * @author te_zf4iks2
 *
 */
public interface IXARecorderProvider {

   /**
    * provides a fresh DataLogger.
    * 
    * This logger is unleashed and the provided doesn't take care of the
    * lifecycle of the dataLogger.
    * 
    * @return
    */
   IXADataRecorder  provideXADataRecorder();

   /**
    * Close this pool, and free any resources associated with it.
    * Implementations should silently fail if not all resources can be freed.
    */
   void close();

   boolean isClosed();

   void destroy();

}
