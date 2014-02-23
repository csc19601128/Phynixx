package org.csc.phynixx.common.generator;

/**
 * Created by christoph on 23.02.14.
 */
public interface IDGenerator<T> {
    /* (non-Javadoc)
         * @see de.csc.xaresource.sample.IResourceIDGenerator#getCurrent()
         */
    T getCurrent();

    /* (non-Javadoc)
         * @see de.csc.xaresource.sample.IResourceIDGenerator#generate()
         */
    T generate();
}
