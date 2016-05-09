package org.csc.phynixx.loggersystem.logger.channellogger.lock;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeoutException;

import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.channellogger.TAEnabledRandomAccessFile;


/**
 * locks a FileChannelLogger by aquiring a FileLock on the channel 
 * @author te_zf4iks2
 * 
 * @see FileChannel#tryLock()
 *
 */
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
             LOG.error("Kein Filelock gesetzt");
         }
		return true;
	}

}
