package org.csc.phynixx.loggersystem.logger.channellogger.lock;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Das vorliegende IF schuetzt und synchronisiert den Zugriff auf Ressourcen.
 * 
 * Ein Aufrufer kann sich einen exklusiven Zugriff auf die Ressource besorgen
 * {@link #acquire(long)}. Gelingt dies, so kann die Ressource nur noch durch
 * den aktuellen Thread, in die Ressource geblockt wurde, genutzt werden.
 * 
 * Die Ressource muss nach Gebrauch wieder freigegeben werden (
 * {@link #release()}), da ansonsten kein Zugriff auf diese Ressource mehr
 * moeglich ist
 * 
 * @author te_zf4iks2
 *
 * @param <T>
 */
public interface IAccessGuard {

	/**
	 * 
	 * 
	 * @return die Resource , auf das der exklusive Lock erzielt wurde
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws IOException 
	 * @see {@link #acquire(long)} mit timeout=
	 *      {@link IAccessGuard#DEFAULT_TIMEOUT}
	 */
	void acquire() throws InterruptedException, TimeoutException, IOException;

	/**
	 * checks if the lock is still valid
	 * @return
	 */
	boolean isValid() ;

	/**
	 * Gibt einen via {@link #acquire(long)} erzhielten exklusiven Zugriff auf
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
