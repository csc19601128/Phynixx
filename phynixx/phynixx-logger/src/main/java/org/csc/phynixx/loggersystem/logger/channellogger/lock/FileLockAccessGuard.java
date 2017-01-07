package org.csc.phynixx.loggersystem.logger.channellogger.lock;

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


import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeoutException;

import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.channellogger.TAEnabledRandomAccessFile;

public class FileLockAccessGuard implements IAccessGuard {
	
	 private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(FileLockAccessGuard.class);

	FileLock fileLock;
	private RandomAccessFile raf;	

	public FileLockAccessGuard(RandomAccessFile raf) {
		this.raf=raf;
	}

	@Override
	public void acquire() throws InterruptedException, TimeoutException, IOException {
		this.release(false);
		this.fileLock= raf.getChannel().tryLock(0,TAEnabledRandomAccessFile.HEADER_LENGTH, false);
		if(this.fileLock==null) {
			throw new IllegalStateException("Lock held by an other program");
		}
	}
	
	

	@Override
	public boolean isValid() {
		if (this.fileLock != null) {
			return this.fileLock.isValid();
		} else {
			return false;
		}
	}

	@Override
	public boolean release() throws IOException {
		return this.release(true);
	}
	
	private boolean release(boolean verbose) throws IOException {
		 if (this.fileLock != null) {
             if( fileLock.isValid()) {
              this.fileLock.release();
             } else if(verbose) {
            LOG.error("Filelock not valid");
             }
         } else if(verbose) {
         LOG.error("No Filelock set");
         }
		return true;
	}

}
