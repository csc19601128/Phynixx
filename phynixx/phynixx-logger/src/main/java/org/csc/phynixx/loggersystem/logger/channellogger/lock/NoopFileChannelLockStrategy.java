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
