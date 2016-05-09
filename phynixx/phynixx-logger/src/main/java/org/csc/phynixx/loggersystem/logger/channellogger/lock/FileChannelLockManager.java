package org.csc.phynixx.loggersystem.logger.channellogger.lock;

import java.io.File;
import java.io.RandomAccessFile;

public class FileChannelLockManager {

	static IFileChannelLockStrategy fileChannelLockManagerStrategy= new FileLockAccessGuardStrategy();

	public static IAccessGuard lock(File file, RandomAccessFile raf) {

		return fileChannelLockManagerStrategy.lock(file, raf);
	}

}
