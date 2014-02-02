package org.csc.phynixx.xa;

/*
 * #%L
 * phynixx-xa
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


import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.connection.IPhynixxManagedConnection;
import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.test_connection.ITestConnection;
import org.csc.phynixx.test_connection.TestConnectionStatusManager;
import org.csc.phynixx.test_connection.TestStatusStack;
import org.objectweb.jotm.Jotm;

import javax.transaction.RollbackException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;


public class XAResourceTest extends TestCase {


    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private Jotm jotm = null;
    private TestResourceFactory factory1 = null;
    private TestResourceFactory factory2 = null;

    protected void setUp() throws Exception {

        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        TestConnectionStatusManager.clear();
        this.jotm = new Jotm(true, false);

        this.factory1 = new TestResourceFactory(
                "RF1",
                this.jotm.getTransactionManager());

        this.factory2 = new TestResourceFactory(
                "RF2",
                this.jotm.getTransactionManager());
    }

    protected void tearDown() throws Exception {
        if (this.jotm != null) {
            this.jotm.stop();
            this.jotm = null;
        }
        if (this.factory1 != null) {
            this.factory1.close();
            this.factory1 = null;
        }
        if (this.factory2 != null) {
            this.factory2.close();
            this.factory2 = null;
        }
        TestConnectionStatusManager.clear();
    }

    private TransactionManager getTransactionManager() {
        return this.jotm.getTransactionManager();
    }

    /**
     * there 's just one XAResource enlisted in a resource and the TM
     * performs a 1-phase commit
     *
     * @throws Exception
     */
    public void testOnePhaseCommit1() throws Exception {
        IPhynixxXAConnection xaCon = factory1.getXAConnection();

        // u are just interfacing the proxy.

        // ... the real core connection is hidden by the proxy
        ITestConnection con = (ITestConnection) xaCon.getConnection();
        Object conId = con.getId();

        try {
            this.getTransactionManager().begin();

            // act transactional and enlist the current resource
            con.act(1);

            this.getTransactionManager().commit();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(!statusStack.isPrepared());
        TestCase.assertTrue(factory1.isFreeConnection(con));
    }

    /**
     * two different Resource Factories ( == resourceManagers)
     * <p/>
     * There are two XAResource created but only one resource is enlisted
     * in the transaction
     *
     * @throws Exception
     */
    public void testOnePhaseCommit2() throws Exception {
        IPhynixxXAConnection xaCon1 = factory1.getXAConnection();
        ITestConnection con1 = (ITestConnection) xaCon1.getConnection();
        ITestConnection coreCon1 = (ITestConnection) ((IPhynixxManagedConnection) con1).getConnection();
        Object conId1 = con1.getId();

        IPhynixxXAConnection xaCon2 = factory2.getXAConnection();
        ITestConnection con2 = (ITestConnection) xaCon2.getConnection();
        ITestConnection coreCon2 = (ITestConnection) ((IPhynixxManagedConnection) con1).getConnection();
        Object conId2 = con2.getId();

        try {
            this.getTransactionManager().begin();

            // act transactional and enlist the current resource
            con1.act(1);
            // no act on the second  resource ...
            // .... con2.act();

            this.getTransactionManager().commit();
        } finally {
            if (con1 != null) {
                con1.close();
            }
            if (con2 != null) {
                con2.close();
            }
        }

        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId1);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(!statusStack.isPrepared());
        TestCase.assertTrue(factory1.isFreeConnection(coreCon1));


        // assert that the con2 has not been enlisted in the TX

        statusStack = TestConnectionStatusManager.getStatusStack(conId2);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(!statusStack.isCommitted());
        TestCase.assertTrue(!statusStack.isPrepared());
        TestCase.assertTrue(!statusStack.isRollbacked());
        TestCase.assertTrue(factory2.isFreeConnection(coreCon2));

    }

    /**
     * there 's just one XAResource enlisted in a resource but the
     * nothing has to be committed.
     * As the Transaction managers performs a 1 phase commit the resource is committed correctly
     *
     * @throws Exception
     */
    public void testReadOnly() throws Exception {
        IPhynixxXAConnection xaCon = factory1.getXAConnection();

        // u are just interfacing the proxy.
        ITestConnection con = (ITestConnection) xaCon.getConnection();

        // ... the real core connection is hidden by the proxy
        ITestConnection coreCon = (ITestConnection) ((IPhynixxManagedConnection) con).getConnection();
        Object conId = con.getId();

        try {
            this.getTransactionManager().begin();

            this.getTransactionManager().getTransaction().enlistResource(xaCon.getXAResource());

            // act transactional and enlist the current resource
            // conProxy.act();

            this.getTransactionManager().commit();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(!statusStack.isRollbacked());
        TestCase.assertTrue(factory1.isFreeConnection(coreCon));
    }

    /**
     * two different Resource Factories ( == resourceManagers)
     *
     * @throws Exception
     */
    public void testTwoPhaseCommit() throws Exception {
        IPhynixxXAConnection xaCon1 = factory1.getXAConnection();
        ITestConnection con1 = (ITestConnection) xaCon1.getConnection();
        ITestConnection coreCon1 = (ITestConnection) ((IPhynixxManagedConnection) con1).getConnection();
        Object conId1 = con1.getId();

        IPhynixxXAConnection xaCon2 = factory2.getXAConnection();
        ITestConnection con2 = (ITestConnection) xaCon2.getConnection();
        ITestConnection coreCon2 = (ITestConnection) ((IPhynixxManagedConnection) con2).getConnection();
        Object conId2 = con2.getId();

        try {
            this.getTransactionManager().begin();

            // act transactional and enlist the current resource
            con1.act(1);
            con2.act(1);

            this.getTransactionManager().commit();
        } finally {
            if (con1 != null) {
                con1.close();
            }
            if (con2 != null) {
                con2.close();
            }
        }

        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId1);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(statusStack.isPrepared());
        TestCase.assertTrue(factory1.isFreeConnection(coreCon1));


        statusStack = TestConnectionStatusManager.getStatusStack(conId2);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(statusStack.isPrepared());
        TestCase.assertTrue(factory2.isFreeConnection(coreCon2));
    }


    /**
     * one Resource Factory ( == resourceManagers) but two Connections.
     * The connections are joined and the transaction ends up in a one-phase-commit
     *
     * @throws Exception
     */
    public void testOnePhaseCommitOneRM_1() throws Exception {
        IPhynixxXAConnection xaCon1 = factory1.getXAConnection();
        ITestConnection con1 = (ITestConnection) xaCon1.getConnection();
        ITestConnection coreCon1 = (ITestConnection) ((IPhynixxManagedConnection) con1).getConnection();
        Object conId1 = con1.getId();


        IPhynixxXAConnection xaCon2 = factory1.getXAConnection();
        ITestConnection con2 = (ITestConnection) xaCon2.getConnection();
        ITestConnection coreCon2 = (ITestConnection) ((IPhynixxManagedConnection) con2).getConnection();
        Object conId2 = con2.getId();


        try {
            this.getTransactionManager().begin();

            // act transactional and enlist the current resource
            con1.act(1);
            con2.act(1);

            // the two XAresources are joined and con2 is released
            TestCase.assertTrue(factory1.isFreeConnection(coreCon2));

            // Exception, da con1 con2 unterschiedliche Connections sind, die aber via TMJOIN bei gleichem
            // RM zusammengefuehrt werden muessen
            // derzeit gibt es aber keinen meschanismus dafuer
            this.getTransactionManager().commit();
        } finally {
            if (con1 != null) {
                con1.close();
            }
            if (con2 != null) {
                con2.close();
            }
        }

        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId1);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        // one Phase commit -> no prepare
        TestCase.assertTrue(!statusStack.isPrepared());
        TestCase.assertTrue(factory1.isFreeConnection(coreCon1));


        statusStack = TestConnectionStatusManager.getStatusStack(conId2);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(!statusStack.isCommitted());
        TestCase.assertTrue(!statusStack.isPrepared());

    }

    /**
     * @throws Exception
     */
    public void testTwoPhaseCommitOneRM_2() throws Exception {
        PhynixxXAResource xares1 = (PhynixxXAResource) factory1.getXAResource();
        PhynixxXAResource xares2 = (PhynixxXAResource) factory2.getXAResource();

        ITestConnection con1 = null;
        ITestConnection con2 = null;

        ITestConnection coreCon1 = null;
        ITestConnection coreCon2 = null;

        Object conId1 = null;
        Object conId2 = null;

        try {
            this.getTransactionManager().begin();

            this.getTransactionManager().getTransaction().enlistResource(xares1);
            this.getTransactionManager().getTransaction().enlistResource(xares2);


            con1 = (ITestConnection) xares1.getXAConnection().getConnection();
            coreCon1 = (ITestConnection) ((IPhynixxManagedConnection) con1).getConnection();
            conId1 = con1.getId();

            con2 = (ITestConnection) xares2.getXAConnection().getConnection();
            coreCon2 = (ITestConnection) ((IPhynixxManagedConnection) con1).getConnection();
            conId2 = con2.getId();

            // act transactional and enlist the current resource
            ((ITestConnection) (xares1.getXAConnection().getConnection())).act(1);
            ((ITestConnection) (xares2.getXAConnection().getConnection())).act(1);

            this.getTransactionManager().commit();
        } finally {
            if (con1 != null) {
                con1.close();
            }
            if (con2 != null) {
                con2.close();
            }
        }
        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId1);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(statusStack.isPrepared());
        TestCase.assertTrue(factory1.isFreeConnection(coreCon1));


        statusStack = TestConnectionStatusManager.getStatusStack(conId2);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(statusStack.isPrepared());
        TestCase.assertTrue(factory2.isFreeConnection(coreCon2));
    }

    /**
     * two different Resource Factories ( == resourceManagers)
     * <p/>
     * There are two XAResource created and both resources are enlisted in the TX
     * but only one has anything to commit.
     *
     * @throws Exception
     */
    public void testTwoPhaseCommitOneRM_3() throws Exception {
        PhynixxXAResource xares1 = (PhynixxXAResource) factory1.getXAResource();
        PhynixxXAResource xares2 = (PhynixxXAResource) factory2.getXAResource();

        ITestConnection con1 = null;
        ITestConnection con2 = null;

        ITestConnection coreCon1 = null;
        ITestConnection coreCon2 = null;

        Object conId1 = null;
        Object conId2 = null;

        try {
            this.getTransactionManager().begin();

            this.getTransactionManager().getTransaction().enlistResource(xares1);
            this.getTransactionManager().getTransaction().enlistResource(xares2);


            con1 = (ITestConnection) xares1.getXAConnection().getConnection();
            coreCon1 = (ITestConnection) ((IPhynixxManagedConnection) con1).getConnection();
            conId1 = coreCon1.getId();

            con2 = (ITestConnection) xares2.getXAConnection().getConnection();
            coreCon2 = (ITestConnection) ((IPhynixxManagedConnection) con2).getConnection();
            conId2 = coreCon2.getId();

            // act transactional and enlist the current resource
            ((ITestConnection) (xares1.getXAConnection().getConnection())).act(1);

            this.getTransactionManager().commit();
        } finally {
            if (con1 != null) {
                con1.close();
            }
            if (con2 != null) {
                con2.close();
            }
        }
        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId1);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(statusStack.isPrepared());
        TestCase.assertTrue(factory1.isFreeConnection(coreCon1));


        statusStack = TestConnectionStatusManager.getStatusStack(conId2);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(!statusStack.isCommitted());
        TestCase.assertTrue(!statusStack.isPrepared());
        TestCase.assertTrue(factory2.isFreeConnection(coreCon2));
    }

    /**
     * one Resource Factory ( == resourceManagers) but 3 Connections.
     * The connections are joined and the transaction ends up in a one-phase-commit
     *
     * @throws Exception
     */
    public void testRollbackTwoRM_2() throws Exception {
        IPhynixxXAConnection xaCon1 = factory1.getXAConnection();
        ITestConnection con1 = (ITestConnection) xaCon1.getConnection();

        IPhynixxXAConnection xaCon2 = factory1.getXAConnection();
        ITestConnection con2 = (ITestConnection) xaCon2.getConnection();

        IPhynixxXAConnection xaCon3 = factory1.getXAConnection();
        ITestConnection con3 = (ITestConnection) xaCon3.getConnection();

        try {
            this.getTransactionManager().begin();

            // act transactional and enlist the current resource
            con1.act(1);
            con2.act(1);
            con3.act(1);

            this.getTransactionManager().commit();
        } finally {
            if (con1 != null) {
                con1.close();
            }
            if (con2 != null) {
                con2.close();
            }
            if (con3 == null) {
                con3.close();
            }
        }
    }

    /**
     * one Resource Factory ( == resourceManagers) but two Connections.
     * The connections are joined and the transaction end sup in a one-phase-commit
     *
     * @throws Exception
     */
    public void testTwoPhaseCommitTwoRM_2() throws Exception {
        IPhynixxXAConnection xaCon1 = factory1.getXAConnection();
        ITestConnection con1 = (ITestConnection) xaCon1.getConnection();

        IPhynixxXAConnection xaCon2 = factory1.getXAConnection();
        ITestConnection con2 = (ITestConnection) xaCon2.getConnection();

        IPhynixxXAConnection xaCon3 = factory1.getXAConnection();
        ITestConnection con3 = (ITestConnection) xaCon3.getConnection();

        try {
            this.getTransactionManager().begin();

            // act transactional and enlist the current resource
            con1.act(1);
            con2.act(1);
            con3.act(1);

            this.getTransactionManager().rollback();
        } finally {
            if (con1 != null) {
                con1.close();
            }
            if (con2 != null) {
                con2.close();
            }
            if (con3 == null) {
                con3.close();
            }
        }
    }

    public void testSuspend() throws Exception {

        IPhynixxXAConnection xaCon1 = factory1.getXAConnection();
        ITestConnection con1 = (ITestConnection) xaCon1.getConnection();

        IPhynixxXAConnection xaCon2 = factory1.getXAConnection();
        ITestConnection con2 = (ITestConnection) xaCon2.getConnection();

        IPhynixxXAConnection xaCon3 = factory1.getXAConnection();
        ITestConnection con3 = (ITestConnection) xaCon3.getConnection();
        try {

            this.getTransactionManager().begin();
            con1.act(1);
            con2.act(1);

            Transaction tx = this.getTransactionManager().suspend();

            this.getTransactionManager().begin();
            con3.act(1);
            this.getTransactionManager().commit();

            this.getTransactionManager().resume(tx);

            this.getTransactionManager().commit();


        } finally {
            if (con1 != null) {
                con1.close();
            }
            if (con2 != null) {
                con2.close();
            }
            if (con3 == null) {
                con3.close();
            }
        }
    }


    /**
     * two different Resource Factories ( == resourceManagers) instanciate two connections
     * <p/>
     * If one of connection is closed, the 'commit' has to fail and
     * the XAResources have to be rollbacked
     *
     * @throws Exception
     */
    public void testHeuristicRollback() throws Exception {

        int freeConnection1 = factory1.freeConnectionSize();
        int freeConnection2 = factory2.freeConnectionSize();

        IPhynixxXAConnection xaCon1 = factory1.getXAConnection();
        ITestConnection con1 = (ITestConnection) xaCon1.getConnection();
        Object conId1 = con1.getId();

        IPhynixxXAConnection xaCon2 = factory2.getXAConnection();
        ITestConnection con2 = (ITestConnection) xaCon2.getConnection();
        Object conId2 = con2.getId();

        log.debug("Con1 with ID=" + conId1);
        log.debug("Con2 with ID=" + conId2);

        TestCase.assertEquals(freeConnection1 - 1, factory1.freeConnectionSize());
        TestCase.assertEquals(freeConnection2 - 1, factory2.freeConnectionSize());

        try {
            this.getTransactionManager().begin();

            // act transactional and enlist the current resource
            con1.act(1);
            con2.act(1);

            // con1 is closed and the TX is marked as rollback
            con1.close();
            con1 = null;

            try {
                this.getTransactionManager().commit();
                throw new AssertionFailedError("Exception expected");
            } catch (RollbackException e) {
            }
        } finally {
            if (con1 != null) {
                con1.close();
            }
            if (con2 != null) {
                con2.close();
            }
        }


        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId1);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(!statusStack.isCommitted());
        TestCase.assertTrue(!statusStack.isPrepared());
        // rollback has not been performed explicitly
        TestCase.assertTrue(!statusStack.isRollbacked());
        //TestCase.assertTrue(statusStack.isClosed());


        statusStack = TestConnectionStatusManager.getStatusStack(conId2);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(!statusStack.isCommitted());
        TestCase.assertTrue(!statusStack.isPrepared());

        // con1 is not any longer available, threrefore no rollback
        TestCase.assertTrue(statusStack.isRollbacked());
        //TestCase.assertTrue(statusStack.isClosed());
    }

    /**
     * scenario:
     * transaction timeout is set to 2 secs.
     * The commit has to fail,when the XAResource expired
     *
     * @throws Exception
     */
    public void testTimeout1() throws Exception {
        IPhynixxXAConnection xaCon = factory1.getXAConnection();
        ITestConnection con = (ITestConnection) xaCon.getConnection();
        XAResource xaresource = xaCon.getXAResource();
        xaresource.setTransactionTimeout(2);

        try {
            this.getTransactionManager().begin();

            // act transactional and enlist the current resource
            con.act(1);

            //  sleeping 7 secs to provoke timeout
            TestUtils.sleep(3 * 1000);

            try {
                this.getTransactionManager().commit();
                throw new AssertionFailedError("RollbackedException expected");
            } catch (javax.transaction.RollbackException e) {
            }
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }

    /**
     * scenario:
     * transaction timeout is set to 2 secs.
     * The call of a method of the connection has to fail, when the XAResource expired
     *
     * @throws Exception
     */
    public void testTimeout2() throws Exception {
        IPhynixxXAConnection xaCon = factory1.getXAConnection();
        ITestConnection con = (ITestConnection) xaCon.getConnection();
        XAResource xaresource = xaCon.getXAResource();
        xaresource.setTransactionTimeout(2);

        try {
            this.getTransactionManager().begin();

            // act transactional and enlist the current resource
            con.act(1);

            //  sleeping 7 secs to provoke timeout
            TestUtils.sleep(3 * 1000);

            con.act(1);

            try {
                this.getTransactionManager().commit();
                throw new AssertionFailedError("RollbackedException expected");
            } catch (javax.transaction.RollbackException e) {
            }
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }

}
