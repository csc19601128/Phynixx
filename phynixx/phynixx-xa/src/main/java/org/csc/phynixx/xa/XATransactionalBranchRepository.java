package org.csc.phynixx.xa;

/*
 * #%L
 * phynixx-xa
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

import java.util.HashMap;
import java.util.Map;

import javax.transaction.xa.Xid;

import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;

/**
 * Created by Christoph Schmidt-Casdorff on 10.02.14.
 */
class XATransactionalBranchRepository<C extends IPhynixxConnection> implements
		IXATransactionalBranchRepository<C> {

	private static final IPhynixxLogger LOG = PhynixxLogManager
			.getLogger(XATransactionalBranchRepository.class);

	private Map<Xid, XATransactionalBranch<C>> branches = new HashMap<Xid, XATransactionalBranch<C>>();

	XATransactionalBranchRepository() {
	}

	@Override
	public XATransactionalBranch<C> instanciateTransactionalBranch(Xid xid,
			IPhynixxManagedConnection<C> physicalConnection) {
		synchronized (branches) {
			XATransactionalBranch<C> branch = branches.get(xid);
			if (branch == null) {
				branch = new XATransactionalBranch<C>(xid, physicalConnection);
				branches.put(xid, branch);
			}
			return branch;
		}

	}

	@Override
	public void releaseTransactionalBranch(Xid xid) {
		synchronized (branches) {
			if (branches.containsKey(xid)) {
				branches.remove(xid);
			}
		}
	}

	@Override
	public XATransactionalBranch<C> findTransactionalBranch(Xid xid) {
		synchronized (branches) {
			return branches.get(xid);
		}
	}

	@Override
	public void close() {
		synchronized (branches) {
			for (XATransactionalBranch<C> branch : branches.values()) {
				branch.close();
			}
		}
	}

}