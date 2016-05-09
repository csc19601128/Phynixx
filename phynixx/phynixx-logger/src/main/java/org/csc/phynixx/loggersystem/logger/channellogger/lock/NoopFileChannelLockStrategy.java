package org.csc.phynixx.loggersystem.logger.channellogger.lock;

import java.io.File;
import java.io.RandomAccessFile;

public class NoopFileChannelLockStrategy implements IFileChannelLockStrategy {

	private static class NoopAccessGuard implements IAccessGuard {

		@Override
		public void acquire() {
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public boolean release() {
			return true;
		}

	}

	private final static IAccessGuard NOOP_ACCESS_GUARD = new NoopAccessGuard();

	@Override
	public IAccessGuard lock(File file, RandomAccessFile randomAccessFile) {
		return NOOP_ACCESS_GUARD;
	}

}
