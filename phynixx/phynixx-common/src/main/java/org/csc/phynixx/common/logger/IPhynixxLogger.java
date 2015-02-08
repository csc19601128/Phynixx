package org.csc.phynixx.common.logger;

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

    public boolean isTraceEnabled();

	public boolean isInfoEnabled();

	public boolean isDebugEnabled();

	public void trace(String o, Throwable t);

	public void trace(String o);

	public void debug(String o);

	public void debug(String o, Throwable t);

    public void info(String o);

    public void info(String o, Throwable t);

    public void warn(String o);

    public void warn(String o, Throwable t);

    public void error(String o);

    public void error(String o, Throwable t);

    public void fatal(String o);

    public void fatal(String o, Throwable t);

}
