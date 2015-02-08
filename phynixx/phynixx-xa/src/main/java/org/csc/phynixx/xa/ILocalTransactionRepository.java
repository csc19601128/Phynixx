package org.csc.phynixx.xa;

/*
 * #%L
 * phynixx-xa
 * %%
 * Copyright (C) 2014 - 2015 Christoph Schmidt-Casdorff
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
import org.csc.phynixx.connection.IPhynixxManagedConnection;

public interface ILocalTransactionRepository<C extends IPhynixxConnection> {

	public abstract void close();

	public abstract LocalTransactionProxy<C> findLocalTransactionProxy(Lid lid);

	public abstract void releaseLocalTransactionProxy(Lid lid);

	public abstract LocalTransactionProxy<C> instanciateLocalTransactionProxy(Lid lid,
			IPhynixxManagedConnection<C> physicalConnection); 

}
