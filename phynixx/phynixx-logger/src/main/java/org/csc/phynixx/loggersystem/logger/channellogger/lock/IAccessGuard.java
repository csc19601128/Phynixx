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
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Das vorliegende IF schuetzt und synchronisiert den Zugriff auf Ressourcen.
 * 
 * Ein Aufrufer kann sich einen exklusiven Zugriff auf die Ressource besorgen
 * {@link #acquire()}. Gelingt dies, so kann die Ressource nur noch durch
 * den aktuellen Thread, in die Ressource geblockt wurde, genutzt werden.
 * 
 * Die Ressource muss nach Gebrauch wieder freigegeben werden (
 * {@link #release()}), da ansonsten kein Zugriff auf diese Ressource mehr
 * moeglich ist
 * 
 * @author te_zf4iks2
 *
 */
public interface IAccessGuard {

	/**
	 * 
	 * 
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws IOException 
	 */
	void acquire() throws InterruptedException, TimeoutException, IOException;

	/**
	 * checks if the lock is still valid
	 * @return
	 */
	boolean isValid() ;

	/**
	 * Gibt einen via {@link #acquire()} erzielten exklusiven Zugriff auf
	 * eine Resource wieder frei.
	 * 
	 * Falls niemand die aktuelle Ressource geblockt hat, so passiert nichts.
	 * 
	 * Falls der aktuelle Thread <b>nicht</b> den Zugriff auf die Ressource hat,
	 * so wird eine Exception geworfen.
	 * 
	 * @return true g.d.w. unlock wurde geloest
	 * @throws IOException 
	 * 
	 * @see ReentrantLock#unlock()
	 */
	boolean release() throws IOException;


}
