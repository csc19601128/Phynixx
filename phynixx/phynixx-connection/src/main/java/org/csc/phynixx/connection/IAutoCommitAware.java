package org.csc.phynixx.connection;

/**
 * Created by christoph on 03.03.14.
 */
public interface  IAutoCommitAware {


    /**
     * set Autocommit
     */
    void setAutoCommit(boolean autocommit);


    /**
     * @return
     */
    boolean isAutoCommit();

}
