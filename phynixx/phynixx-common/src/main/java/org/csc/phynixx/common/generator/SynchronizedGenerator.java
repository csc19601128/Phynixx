package org.csc.phynixx.common.generator;

/**
 * Created by christoph on 23.02.14.
 */
class SynchronizedGenerator<T> implements IDGenerator<T> {
    private IDGenerator<T> delegates;

    SynchronizedGenerator(IDGenerator<T> delegates) {
        this.delegates = delegates;
        if(delegates==null) {
            throw new IllegalArgumentException("delegates must be defined");
        }
    }

    @Override
    public T getCurrent() {
        synchronized (delegates) {
            return delegates.getCurrent();
        }
    }

    @Override
    public T generate() {

        synchronized (delegates) {
            return delegates.generate();
        }
    }
}
