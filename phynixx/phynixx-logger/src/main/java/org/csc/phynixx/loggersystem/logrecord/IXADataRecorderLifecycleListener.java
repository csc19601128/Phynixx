package org.csc.phynixx.loggersystem.logrecord;

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


/**
 * Notification concering the sates of an XARecorder
 */
public interface IXADataRecorderLifecycleListener {

	/**
	 * indicates a closed XADataRecorder 
	 * @see IXADataRecorder#disqualify()
	 */
    void recorderDataRecorderClosed(IXADataRecorder xaDataRecorder);

    /**
	 * indicates a opended XADataRecorder 
	 * @see IXADataRecorder#opened()
	 */
    void recorderDataRecorderOpened(IXADataRecorder xaDataRecorder);

    /**
	 * indicates a reset XADataRecorder 
	 * @see IXADataRecorder#release()
	 */
	void recorderDataRecorderReleased(IXADataRecorder phynixxXADataRecorder);

	/**
	 * indicates a destroyed XADataRecorder. The content of the logger is already destroyed. 
	 * @see IXADataRecorder#destroyed()
	 */
	void recorderDataRecorderDestroyed(IXADataRecorder phynixxXADataRecorder);

}
