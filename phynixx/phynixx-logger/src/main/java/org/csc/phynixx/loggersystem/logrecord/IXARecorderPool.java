package org.csc.phynixx.loggersystem.logrecord;

import java.util.NoSuchElementException;

public interface IXARecorderPool {

	/**
	 * Obtains an instance from this pool for the specified <code>key</code>.
	 * <p>
	 * Instances returned from this method will have been either newly created
	 * with {@link KeyedPooledObjectFactory#makeObject makeObject} or will be a
	 * previously idle object and have been activated with
	 * {@link KeyedPooledObjectFactory#activateObject activateObject} and then
	 * (optionally) validated with
	 * {@link KeyedPooledObjectFactory#validateObject validateObject}.
	 * <p>
	 * By contract, clients <strong>must</strong> return the borrowed object
	 * using {@link #returnObject returnObject}, {@link #invalidateObject
	 * invalidateObject}, or a related method as defined in an implementation or
	 * sub-interface, using a <code>key</code> that is {@link Object#equals
	 * equivalent} to the one used to borrow the instance in the first place.
	 * <p>
	 * The behaviour of this method when the pool has been exhausted is not
	 * strictly specified (although it may be specified by implementations).
	 *
	 * @param key
	 *            the key used to obtain the object
	 *
	 * @return an instance from this pool.
	 *
	 * @throws IllegalStateException
	 *             after {@link #close close} has been called on this pool
	 * @throws Exception
	 *             when {@link KeyedPooledObjectFactory#makeObject makeObject}
	 *             throws an exception
	 * @throws NoSuchElementException
	 *             when the pool is exhausted and cannot or will not return
	 *             another instance
	 */
	IXADataRecorder borrowObject() throws Exception,NoSuchElementException;

	/**
	 * Return an instance to the pool. By contract, <code>obj</code>
	 * <strong>must</strong> have been obtained using {@link #borrowObject
	 * borrowObject} or a related method as defined in an implementation or
	 * sub-interface using a <code>key</code> that is equivalent to the one used
	 * to borrow the instance in the first place.
	 *
	 * @param key
	 *            the key used to obtain the object
	 * @param obj
	 *            a {@link #borrowObject borrowed} instance to be returned.
	 *
	 * @throws IllegalStateException
	 *             if an attempt is made to return an object to the pool that is
	 *             in any state other than allocated (i.e. borrowed). Attempting
	 *             to return an object more than once or attempting to return an
	 *             object that was never borrowed from the pool will trigger
	 *             this exception.
	 *
	 * @throws Exception
	 *             if an instance cannot be returned to the pool
	 */
	void returnObject(IXADataRecorder dataRecorder) throws Exception;

	/**
	 * Invalidates an object from the pool.
	 * <p>
	 * By contract, <code>obj</code> <strong>must</strong> have been obtained
	 * using {@link #borrowObject borrowObject} or a related method as defined
	 * in an implementation or sub-interface using a <code>key</code> that is
	 * equivalent to the one used to borrow the <code>Object</code> in the first
	 * place.
	 * <p>
	 * This method should be used when an object that has been borrowed is
	 * determined (due to an exception or other problem) to be invalid.
	 *
	 * @param key
	 *            the key used to obtain the object
	 * @param obj
	 *            a {@link #borrowObject borrowed} instance to be returned.
	 *
	 * @throws Exception
	 *             if the instance cannot be invalidated
	 */
	void invalidateObject(IXADataRecorder dataRecorder) throws Exception;

	/**
	 * Clears the pool, removing all pooled instances (optional operation).
	 *
	 * @throws UnsupportedOperationException
	 *             when this implementation doesn't support the operation
	 *
	 * @throws Exception
	 *             if the pool cannot be cleared
	 */
	void clear() throws Exception, UnsupportedOperationException;

	/**
	 * Close this pool, and free any resources associated with it.
	 * <p>
	 * Calling {@link #addObject addObject} or {@link #borrowObject
	 * borrowObject} after invoking this method on a pool will cause them to
	 * throw an {@link IllegalStateException}.
	 * <p>
	 * Implementations should silently fail if not all resources can be freed.
	 */
	void close();

	boolean isClosed();

	void destroy();

}
