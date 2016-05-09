package org.csc.phynixx.loggersystem.logger.channellogger.lock;

import java.io.File;
import java.io.RandomAccessFile;

public class FileLockAccessGuardStrategy implements IFileChannelLockStrategy {

	@Override
	public IAccessGuard lock(File file, RandomAccessFile randomAccessFile) {
		return new FileLockAccessGuard(randomAccessFile);
	}

	
}
