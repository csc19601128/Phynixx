package org.csc.phynixx.loggersystem.logger.channellogger.lock;

import java.io.File;
import java.io.RandomAccessFile;

public interface IFileChannelLockStrategy {

	IAccessGuard lock(File file, RandomAccessFile randomAccessFile);

}
