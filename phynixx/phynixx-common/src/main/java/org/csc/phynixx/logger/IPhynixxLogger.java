package org.csc.phynixx.logger;

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


public interface IPhynixxLogger {

    public void debug(Object o);

    public void debug(Object o, Throwable t);

    public void info(Object o);

    public void info(Object o, Throwable t);

    public void warn(Object o);

    public void warn(Object o, Throwable t);

    public void error(Object o);

    public void error(Object o, Throwable t);

    public void fatal(Object o);

    public void fatal(Object o, Throwable t);

    public boolean isInfoEnabled();

    public boolean isDebugEnabled();

}
