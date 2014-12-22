package org.csc.phynixx.spring.jta;

/**
 * Created by christoph on 14.12.2014.
 */
public interface IConnectionDelegate<C> {

    C getTargetConnection();
}
