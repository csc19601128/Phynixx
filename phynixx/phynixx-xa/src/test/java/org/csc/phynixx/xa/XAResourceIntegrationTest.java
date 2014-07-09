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
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.phynixx.testconnection.ITestConnection;
import org.csc.phynixx.phynixx.testconnection.TestConnectionStatus;
import org.csc.phynixx.phynixx.testconnection.TestConnectionStatusManager;
import org.csc.phynixx.phynixx.testconnection.TestStatusStack;
import org.csc.phynixx.xa.recovery.XidWrapper;
import org.junit.Assert;
import org.objectweb.jotm.Jotm;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;


public class XAResourceIntegrationTest extends TestCase {

    {
        System.setProperty("log4j_level", "INFO");
    }

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private Jotm jotm = null;
    private TestXAResourceFactory factory1 = null;
    private TestXAResourceFactory factory2 = null;

    protected void setUp() throws Exception {

        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        TestConnectionStatusManager.clear();
        this.jotm = new Jotm(true, false);

        this.factory1 = new TestXAResourceFactory("RF1", null,   this.jotm.getTransactionManager());

        this.factory2 = new TestXAResourceFactory(
                "RF2", null,
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
     * there 's just one XAResource enlisted in a resource but this resource hasn't changed. The TM
     * performs a 1-phase read-only commit
     *
     * @throws Exception
     */
    public void testOnePhaseReadOnly() throws Exception {
        IPhynixxXAConnection<ITestConnection> xaCon = factory1.getXAConnection();

        // u are just interfacing the proxy.


        // ... the real core connection is hidden by the proxy
        final Object conId;
        this.getTransactionManager().begin();

        // join the transaction
        conId = xaCon.getConnection().getConnectionId();


        this.getTransactionManager().commit();

        log.info(TestConnectionStatusManager.toDebugString());
        log.info(TestConnectionStatusManager.toDebugString());
        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(!statusStack.isRequiresTransaction());

        // con hasn't changed, so commit ism performed on the physical connection
        TestCase.assertTrue(!statusStack.isCommitted());
        TestCase.assertTrue(!statusStack.isPrepared());
        TestCase.assertTrue(statusStack.isReleased());
        // TestCase.assertTrue(factory1.isFreeConnection(con));
    }

    /**
     * there 's just one XAResource enlisted in a resource and the TM
     * performs a 1-phase commit
     *
     * @throws Exception
     */
    public void testOnePhaseCommit1() throws Exception {

        // get a XAResource
        IPhynixxXAConnection<ITestConnection> xaCon = factory1.getXAConnection();

        // u are just interfacing the proxy.


        this.getTransactionManager().begin();

        // ... the real core connection is hidden by the proxy
        final Object conId;
        ITestConnection con = xaCon.getConnection();
        conId = con.getConnectionId();

        // act transactional and enlist the current resource
        con.act(1);

        this.getTransactionManager().commit();

        log.info(TestConnectionStatusManager.toDebugString());
        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(!statusStack.isPrepared());
        TestCase.assertTrue(statusStack.isReleased());
        // TestCase.assertTrue(factory1.isFreeConnection(con));
    }

    /**
     * there 's just one XAResource enlisted in a resource and the TM
     * performs a 1-phase commit
     *
     * @throws Exception
     */
    public void testTransactionMigration() throws Exception {
        IPhynixxXAConnection<ITestConnection> xaCon = factory1.getXAConnection();

        // u are just interfacing the proxy.

        // ... the real core connection is hidden by the proxy
        ITestConnection con = xaCon.getConnection();
        Object conId = con.getConnectionId();

        try {
            this.getTransactionManager().begin();

            // refresh transaction binding
            con = xaCon.getConnection();

            // act transactional and enlist the current resource
            con.act(1);

            this.getTransactionManager().commit();
        } finally {
            if (con != null) {
                con.close();
            }
        }

        log.info(TestConnectionStatusManager.toDebugString());
        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(!statusStack.isPrepared());
    }

    /**
     * two different XAResourceProgressState Factories ( == resourceManagers)
     * <p/>
     * There are two XAResource created but only one resource is enlisted
     * in the transaction
     *
     * @throws Exception
     */
    public void testTwoPhaseReadOnly() throws Exception {


        Object conId1 = null;
        ITestConnection con1 = null;
        ITestConnection con2 = null;
        Object conId2 = null;
        this.getTransactionManager().begin();

        IPhynixxXAConnection<ITestConnection> xaCon1 = factory1.getXAConnection();
        con1 = xaCon1.getConnection();
        conId1 = con1.getConnectionId();

        IPhynixxXAConnection<ITestConnection> xaCon2 = factory2.getXAConnection();
        con2 = xaCon2.getConnection();
        conId2 = con2.getConnectionId();


        // act transactional and enlist the current resource
        con1.act(1);


        // no act on the second  resource ...
        // .... con2.act();

        this.getTransactionManager().commit();


        log.info(TestConnectionStatusManager.toDebugString());


        // conj1 has been changed an the XA protocol is performed
        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId1);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(statusStack.isPrepared());
        //TestCase.assertTrue(factory1.isFreeConnection(coreCon1));


        // assert that the con2
        // con2 has not been changed , so no rollback/prepare/commit was performed
        statusStack = TestConnectionStatusManager.getStatusStack(conId2);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(!statusStack.isCommitted());
        TestCase.assertTrue(!statusStack.isPrepared());
        TestCase.assertTrue(!statusStack.isRolledback());
        TestCase.assertTrue(statusStack.isReleased());
        //TestCase.assertTrue(factory2.isFreeConnection(coreCon2));

    }


    /**
     * two XAResource of the same Factory are joining the same underlying connection and the TZX is rolled back
     *
     * @throws Exception
     */
    public void testJoinedRollback() throws Exception {
        IPhynixxXAConnection<ITestConnection> xaCon1 = factory1.getXAConnection();

        IPhynixxXAConnection<ITestConnection> xaCon2 = factory1.getXAConnection();

        // u are just interfacing the proxy.
        ITestConnection con1 = null;
        ITestConnection con2 = null;

        Object conId2 = null;

        // ... the real core connection is hidden by the proxy

            this.getTransactionManager().begin();

            con1 = xaCon1.getConnection();
            con2 = xaCon2.getConnection();

            con1.act(1);
            con2.act(2);

            Object conId1 = con1.getConnectionId();
            conId2 = con2.getConnectionId();

            // same physical connection
            Assert.assertTrue("same physical connection", conId2 == conId1);

            // act transactional and enlist the current resource
            // conProxy.act();

            this.getTransactionManager().rollback();

            log.info(TestConnectionStatusManager.toDebugString());

        // @RequiredTransaction was requested twice
        Assert.assertEquals(2, TestConnectionStatusManager.getStatusStack(conId2).countStatus(TestConnectionStatus.REQUIRES_TRANSACTION));
        Assert.assertEquals(1, TestConnectionStatusManager.getStatusStack(conId2).countStatus(TestConnectionStatus.ROLLEDBACK));
        Assert.assertEquals(1, TestConnectionStatusManager.getStatusStack(conId2).countStatus(TestConnectionStatus.RELEASED));
    }

    /**
     * two XAResource of the same Factory are joining the same underlying connection and the TZX is rolled back
     *
     * @throws Exception
     */
    public void testJoinedCommit() throws Exception {
        IPhynixxXAConnection<ITestConnection> xaCon1 = factory1.getXAConnection();

        IPhynixxXAConnection<ITestConnection> xaCon2 = factory1.getXAConnection();

        // u are just interfacing the proxy.
        ITestConnection con1 = null;
        ITestConnection con2 = null;

        Object conId2 = null;

        // ... the real core connection is hidden by the proxy

        this.getTransactionManager().begin();

        con1 = xaCon1.getConnection();
        con2 = xaCon2.getConnection();

        con1.act(1);
        con2.act(2);

        Object conId1 = con1.getConnectionId();
        conId2 = con2.getConnectionId();

        // same physical connection
        Assert.assertTrue("same physical connection", conId2 == conId1);

        // act transactional and enlist the current resource
        // conProxy.act();

        this.getTransactionManager().commit();

        log.info(TestConnectionStatusManager.toDebugString());

        // @RequiredTransaction was requested twice
        Assert.assertEquals(2, TestConnectionStatusManager.getStatusStack(conId2).countStatus(TestConnectionStatus.REQUIRES_TRANSACTION));


        // onePhase Commit ist performed , so no prepare
        Assert.assertEquals(0, TestConnectionStatusManager.getStatusStack(conId2).countStatus(TestConnectionStatus.PREPARED));
        Assert.assertEquals(1, TestConnectionStatusManager.getStatusStack(conId2).countStatus(TestConnectionStatus.COMMITTED));
        Assert.assertEquals(1, TestConnectionStatusManager.getStatusStack(conId2).countStatus(TestConnectionStatus.RELEASED));
    }

    /**
     * there 's just one XAResource enlisted in a resource but the
     * nothing has to be committed.
     * As the Transaction managers performs a 1 phase commit the resource is committed correctly
     *
     * @throws Exception
     */
    public void testExplicitEnlistment1() throws Exception {
        IPhynixxXAConnection<ITestConnection> xaCon = factory1.getXAConnection();

            this.getTransactionManager().begin();

        this.getTransactionManager().getTransaction().enlistResource(xaCon.getXAResource());

        // u are just interfacing the proxy.
        ITestConnection con = xaCon.getConnection();

        // ... the real core connection is hidden by the proxy
        Object conId = con.getConnectionId();

            // act transactional and enlist the current resource
            // conProxy.act();

            this.getTransactionManager().commit();

        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(!statusStack.isCommitted());
        TestCase.assertTrue(!statusStack.isRolledback());
        //TestCase.assertTrue(factory1.isFreeConnection(coreCon));
    }

    /**
     * there 's just one XAResource enlisted in a resource but the
     * nothing has to be committed.
     * As the Transaction managers performs a 1 phase commit the resource is committed correctly
     *
     * @throws Exception
     */
    public void testExplicitEnlistment2() throws Exception {
        IPhynixxXAConnection<ITestConnection> xaCon = factory1.getXAConnection();


        // ... the real core connection is hidden by the proxy
        this.getTransactionManager().begin();


        this.getTransactionManager().getTransaction().enlistResource(xaCon.getXAResource());

        // u are just interfacing the proxy.
        ITestConnection con = xaCon.getConnection();

        // act transactional and enlist the current resource
        Object conId = con.getConnectionId();
        con.act(2);

        this.getTransactionManager().commit();

        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(!statusStack.isRolledback());
        //TestCase.assertTrue(factory1.isFreeConnection(coreCon));
    }

    /**
     * two different XAResourceProgressState Factories ( == resourceManagers)
     *
     * @throws Exception
     */
    public void testTwoPhaseCommit() throws Exception {
        IPhynixxXAConnection<ITestConnection> xaCon1 = factory1.getXAConnection();

        IPhynixxXAConnection<ITestConnection> xaCon2 = factory2.getXAConnection();

            this.getTransactionManager().begin();

            ITestConnection con1 = xaCon1.getConnection();
            Object conId1 = con1.getConnectionId();
            ITestConnection con2 = xaCon2.getConnection();
            Object conId2 = con2.getConnectionId();
            // act transactional and enlist the current resource
            con1.act(1);
            con2.act(1);

        Assert.assertTrue(conId1 != conId2);

            this.getTransactionManager().commit();

        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId1);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(statusStack.isPrepared());
        //TestCase.assertTrue(factory1.isFreeConnection(coreCon1));


        statusStack = TestConnectionStatusManager.getStatusStack(conId2);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(statusStack.isPrepared());
        //TestCase.assertTrue(factory2.isFreeConnection(coreCon2));
    }


    /**
     * one XAResourceProgressState Factory ( == resourceManagers) but two Connections.
     * The connections are joined and the transaction ends up in a one-phase-commit
     *
     * @throws Exception
     */
    public void testOnePhaseCommitOneRM_1() throws Exception {
        IPhynixxXAConnection<ITestConnection> xaCon1 = factory1.getXAConnection();

        IPhynixxXAConnection<ITestConnection> xaCon2 = factory1.getXAConnection();

        this.getTransactionManager().begin();

        ITestConnection con1 = xaCon1.getConnection();
        Object conId1 = con1.getConnectionId();
        ITestConnection con2 = xaCon2.getConnection();
        Object conId2 = con2.getConnectionId();

        Assert.assertTrue(conId1== conId2);

            // act transactional and enlist the current resource
            con1.act(1);
            con2.act(1);

            // the two XAresources are joined and con2 is released
            // TestCase.assertTrue(factory1.isFreeConnection(coreCon2));

            // Exception, da con1 con2 unterschiedliche Connections sind, die aber via TMJOIN bei gleichem
            // RM zusammengefuehrt werden muessen
            // derzeit gibt es aber keinen meschanismus dafuer
            this.getTransactionManager().commit();

        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId1);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        // one Phase commit -> no prepare
        TestCase.assertTrue(!statusStack.isPrepared());
        // TestCase.assertTrue(factory1.isFreeConnection(coreCon1));

    }

    /**
     * @throws Exception
     */
    public void testExplicitEnlistmentTwoPhaseCommitTwoRM() throws Exception {
        IPhynixxXAResource<ITestConnection> xares1 =  factory1.getXAResource();
        IPhynixxXAResource<ITestConnection> xares2 =  factory2.getXAResource();

        ITestConnection con1 = null;
        ITestConnection con2 = null;

        ITestConnection coreCon1 = null;
        ITestConnection coreCon2 = null;

        Object conId1 = null;
        Object conId2 = null;

            this.getTransactionManager().begin();

            this.getTransactionManager().getTransaction().enlistResource(xares1);
            this.getTransactionManager().getTransaction().enlistResource(xares2);


            con1 = xares1.getXAConnection().getConnection();
            //coreCon1 = (ITestConnection) ((IPhynixxManagedConnection) con1).getConnection();
            conId1 = con1.getConnectionId();

            con2 = xares2.getXAConnection().getConnection();
            //coreCon2 = (ITestConnection) ((IPhynixxManagedConnection) con1).getConnection();
            conId2 = con2.getConnectionId();
        Assert.assertTrue(conId1!= conId2);
            // act transactional and enlist the current resource
            con1.act(1);
            con2.act(1);

            this.getTransactionManager().commit();

        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId1);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(statusStack.isPrepared());
        //TestCase.assertTrue(factory1.isFreeConnection(coreCon1));


        statusStack = TestConnectionStatusManager.getStatusStack(conId2);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(statusStack.isPrepared());
        //TestCase.assertTrue(factory2.isFreeConnection(coreCon2));
    }

    /**
     * two different XAResourceProgressState Factories ( == resourceManagers)
     * <p/>
     * There are two XAResource created and both resources are enlisted in the TX
     * but only one has anything to commit.
     *
     * @throws Exception
     */
    public void testTwoPhaseCommitOneRM_3() throws Exception {

        IPhynixxXAResource<ITestConnection> xares1 = factory1.getXAResource();
        IPhynixxXAResource<ITestConnection> xares2 = factory2.getXAResource();


        this.getTransactionManager().begin();

        this.getTransactionManager().getTransaction().enlistResource(xares1);
        this.getTransactionManager().getTransaction().enlistResource(xares2);

        ITestConnection con1 = xares1.getXAConnection().getConnection();
        ITestConnection con2 = xares2.getXAConnection().getConnection();

        Object conId1 = con1.getConnectionId();
        Object conId2 = con2.getConnectionId();

        // act transactional and enlist the current resource
        con1.act(1);

        this.getTransactionManager().commit();


        log.info(TestConnectionStatusManager.toDebugString());

        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(conId1);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());
        TestCase.assertTrue(statusStack.isPrepared());
        TestCase.assertTrue(statusStack.isReleased());


        statusStack = TestConnectionStatusManager.getStatusStack(conId2);
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(!statusStack.isCommitted());
        TestCase.assertTrue(!statusStack.isPrepared());
        TestCase.assertTrue(statusStack.isReleased());
    }

    /**
     * one XAResourceProgressState Factory ( == resourceManagers) but 3 Connections.
     * The connections are joined and the transaction ends up in a one-phase-commit
     *
     * @throws Exception
     */
    public void testCommit3Connections1RM() throws Exception {
        IPhynixxXAConnection<ITestConnection> xaCon1 = factory1.getXAConnection();

        IPhynixxXAConnection<ITestConnection> xaCon2 = factory1.getXAConnection();

        IPhynixxXAConnection<ITestConnection> xaCon3 = factory1.getXAConnection();

        this.getTransactionManager().begin();
        ITestConnection con1 = xaCon1.getConnection();
        ITestConnection con2 = xaCon2.getConnection();
        ITestConnection con3 = xaCon3.getConnection();

            // act transactional and enlist the current resource
            con1.act(1);
            con2.act(1);
            con3.act(1);

            this.getTransactionManager().commit();

        log.info(TestConnectionStatusManager.toDebugString());

        Assert.assertTrue(con1.getConnectionId() == con2.getConnectionId());

        Assert.assertTrue(con1.getConnectionId()== con3.getConnectionId());

        TestStatusStack statusStack = TestConnectionStatusManager.getStatusStack(con3.getConnectionId());
        TestCase.assertTrue(statusStack != null);
        TestCase.assertTrue(statusStack.isCommitted());

        // one-phae commit -> no prepare
        TestCase.assertTrue(!statusStack.isPrepared());
        TestCase.assertTrue(statusStack.isReleased());

    }

    /**
     * one XAResourceProgressState Factory ( == resourceManagers) but two Connections.
     * The connections are joined and the transaction end sup in a one-phase-commit
     *
     * @throws Exception
     */
    public void testTwoPhaseCommitTwoRM_2() throws Exception {
        IPhynixxXAConnection<ITestConnection> xaCon1 = factory1.getXAConnection();
        ITestConnection con1 = (ITestConnection) xaCon1.getConnection();

        IPhynixxXAConnection<ITestConnection> xaCon2 = factory1.getXAConnection();
        ITestConnection con2 = (ITestConnection) xaCon2.getConnection();

        IPhynixxXAConnection<ITestConnection> xaCon3 = factory1.getXAConnection();
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

        IPhynixxXAConnection<ITestConnection> xaCon1 = factory1.getXAConnection();

        IPhynixxXAConnection<ITestConnection> xaCon2 = factory1.getXAConnection();

        IPhynixxXAConnection<ITestConnection> xaCon3 = factory1.getXAConnection();
            this.getTransactionManager().begin();

            ITestConnection con1 = (ITestConnection) xaCon1.getConnection();
            con1.act(1);

            Transaction tx = this.getTransactionManager().suspend();


            this.getTransactionManager().begin();
            ITestConnection con2 = (ITestConnection) xaCon3.getConnection();
            con2.act(1);
            this.getTransactionManager().commit();

            this.getTransactionManager().resume(tx);

            this.getTransactionManager().rollback();

        TestStatusStack statusStack1 = TestConnectionStatusManager.getStatusStack(con1.getConnectionId());
        TestCase.assertTrue(statusStack1 != null);
        TestCase.assertTrue(statusStack1.isRolledback());
        TestCase.assertTrue(statusStack1.isReleased());



        TestStatusStack statusStack2 = TestConnectionStatusManager.getStatusStack(con2.getConnectionId());
        TestCase.assertTrue(statusStack2 != null);
        TestCase.assertTrue(statusStack2.isCommitted());
        // one-phase commit -> no prepare
        TestCase.assertTrue(!statusStack2.isPrepared());
        TestCase.assertTrue(statusStack2.isReleased());
    }

    /**
     *
     * Suspending one XAConnection opens a second transactional branch and therefore a second physical connection.
     *
     * remember: the connection are associated to a TX by the call of xaConn.getConnection
     *
     * @throws Exception
     */
    public void testSuspendOneXAConnection() throws Exception {

        IPhynixxXAConnection<ITestConnection> xaCon = factory1.getXAConnection();

        this.getTransactionManager().begin();

        ITestConnection outCon = xaCon.getConnection();
        outCon.act(1);
        Transaction tx = this.getTransactionManager().suspend();


        this.getTransactionManager().begin();

        // associate connection to current transactional branch
        ITestConnection innerCon = xaCon.getConnection();
        innerCon.act(1);
        this.getTransactionManager().commit();

        this.getTransactionManager().resume(tx);

        this.getTransactionManager().rollback();

        // different transactional branches
        Assert.assertTrue(outCon.getConnectionId() != innerCon.getConnectionId());

        log.info(TestConnectionStatusManager.toDebugString());

        TestStatusStack statusStack1 = TestConnectionStatusManager.getStatusStack(outCon.getConnectionId());
        TestCase.assertTrue(statusStack1 != null);
        TestCase.assertTrue(statusStack1.isRolledback());
        TestCase.assertFalse(statusStack1.isCommitted());
        TestCase.assertTrue(statusStack1.isReleased());

        TestStatusStack statusStack2 = TestConnectionStatusManager.getStatusStack(innerCon.getConnectionId());
        TestCase.assertTrue(statusStack2 != null);
        TestCase.assertTrue(statusStack2.isCommitted());
        // one-phase commit -> no prepare
        TestCase.assertTrue(!statusStack2.isPrepared());
        TestCase.assertFalse(statusStack2.isRolledback());
        TestCase.assertTrue(statusStack2.isReleased());
    }

    /**
     * a transaction context bound to TX1. TX 1 is suspended and n new TX2 is started.
     * Any modifications on the transactional context is attached to TX1.
     *
     * Any rollback/commit on TZX2 doesn't effect the transactional context.
     *
     * @throws Exception
     */
    public void testSuspendInvalidTransactionalContext() throws Exception {

        IPhynixxXAConnection<ITestConnection> xaCon1 = factory1.getXAConnection();

        // stat TX1
        this.getTransactionManager().begin();

        ITestConnection con1 = xaCon1.getConnection();
        con1.act(1);

        // suspend TX1
        Transaction tx = this.getTransactionManager().suspend();

        // start TX2
        this.getTransactionManager().begin();

        // con1 remains asociated to the suspended TX as xaCon.getConnection() is not called
        con1.act(1);

        // commit on TX2 has no effect
        this.getTransactionManager().commit();

        this.getTransactionManager().resume(tx);

        // rollback on TX1 effects con1
        this.getTransactionManager().rollback();

        log.info(TestConnectionStatusManager.toDebugString());

        TestStatusStack statusStack1 = TestConnectionStatusManager.getStatusStack(con1.getConnectionId());
        TestCase.assertTrue(statusStack1 != null);
        TestCase.assertTrue(statusStack1.isRolledback());

        // connection is associated to the first TX, the second TX has no effect on the transaction context
        TestCase.assertTrue(!statusStack1.isCommitted());



    }

    /**
     *
     * Suspending one XAConnection opens a second transactional branch and therefore a second physical connection.
     *
     * remember: the connection are associated to a TX by the call of xaConn.getConnection
     *
     * @throws Exception
     */
    public void testMixedLocalGlobalTransaction() throws Exception {

        IPhynixxXAConnection<ITestConnection> xaCon = factory1.getXAConnection();

        // con1 on local transaction
        ITestConnection con1 = xaCon.getConnection();
        con1.act(1);
        con1.act(2);
        con1.commit();
        con1.close();


        // con2 on global transaction
        this.getTransactionManager().begin();
        ITestConnection con2 = xaCon.getConnection();
        con2.act(1);

        this.getTransactionManager().rollback();


        log.info(TestConnectionStatusManager.toDebugString());

        TestStatusStack statusStack1 = TestConnectionStatusManager.getStatusStack(con1.getConnectionId());
        TestCase.assertTrue(statusStack1 != null);
        TestCase.assertTrue(statusStack1.isCommitted());
        TestCase.assertTrue(statusStack1.isReleased());

        TestStatusStack statusStack2 = TestConnectionStatusManager.getStatusStack(con2.getConnectionId());
        TestCase.assertTrue(statusStack2 != null);
        TestCase.assertTrue(statusStack2.isRolledback());
        // one-phase commit -> no prepare
        TestCase.assertTrue(!statusStack2.isPrepared());
        TestCase.assertTrue(statusStack2.isReleased());
    }

    /**
     * A local transaction is opened and transactional context is modified and left open.
     *
     * Therefore no new Transaction (local or global) may be started on the XAConnection
     *
     *
     * @throws Exception
     */
    public void testMixedLocalGlobalTransactionNested1() throws Exception {

        IPhynixxXAConnection<ITestConnection> xaCon1 = factory1.getXAConnection();

        // con1 on local transaction
        ITestConnection con1 = xaCon1.getConnection();
        con1.act(1);
        con1.act(2);


        // con2 on global transaction
        this.getTransactionManager().begin();
        try {
            ITestConnection con2 = xaCon1.getConnection();
            throw new AssertionFailedError("Nested Transactions are not permitted");
        } catch (Exception e) {}
    }


    /**
     *
     * A local transaction is opened, transactional context is modified but rolled back at least.
     * Therefore the transactional context is closed and a new global transaction can be start on this XAConncgtion
     *
     * @throws Exception
     */
    public void testMixedLocalGlobalTransactionNested3() throws Exception {

        IPhynixxXAConnection<ITestConnection> xaCon = factory1.getXAConnection();

        // con 1in lo´cal transaction. Transactional context has transactional data
        ITestConnection con1 = xaCon.getConnection();
        Object conId1= con1.getConnectionId();
        con1.act(1);
        con1.act(2);

        con1.commit();

        // con1 on global transaction
        this.getTransactionManager().begin();

        // con2 shared con1 on global transaction
            ITestConnection con2 = xaCon.getConnection();
        Object conId2= con2.getConnectionId();
        con2.act(2);

        this.getTransactionManager().rollback();

        log.info(TestConnectionStatusManager.toDebugString());


        TestStatusStack statusStack1 = TestConnectionStatusManager.getStatusStack(conId1);
        TestCase.assertTrue(statusStack1 != null);
        TestCase.assertTrue(statusStack1.isRolledback());
        TestCase.assertTrue(statusStack1.isReleased());

        TestStatusStack statusStack2 = TestConnectionStatusManager.getStatusStack(conId2);
        TestCase.assertTrue(statusStack2 != null);
        TestCase.assertTrue(statusStack2.isCommitted());
        // one-phase commit -> no prepare
        TestCase.assertTrue(!statusStack2.isPrepared());
        TestCase.assertTrue(statusStack2.isReleased());


    }
    /**
     *
     * Suspending one XAConnection opens a second transactional branch and therefore a second physical connection.
     *
     * remember: the connection are associated to a TX by the call of xaConn.getConnection
     *
     * @throws Exception
     */
    public void testMixedLocalGlobalTransactionAndSuspend() throws Exception {

        IPhynixxXAConnection<ITestConnection> xaCon = factory1.getXAConnection();

        this.getTransactionManager().begin();

        ITestConnection con0 = xaCon.getConnection();
        con0.act(3);

        Transaction transaction = this.getTransactionManager().getTransaction();
        this.getTransactionManager().suspend();

        // con1 on local transaction
        ITestConnection con1 = xaCon.getConnection();
        con1.act(1);
        con1.act(2);
        con1.commit();
        con1.close();


        // con2 on global transaction
        this.getTransactionManager().begin();
        ITestConnection con2 = xaCon.getConnection();
        con2.act(1);

        this.getTransactionManager().rollback();

        this.getTransactionManager().resume(transaction);

        con0.act(2);

        this.getTransactionManager().rollback();

        Assert.assertTrue(con0.getConnectionId()!=con2.getConnectionId());
        Assert.assertTrue(con1.getConnectionId()==con2.getConnectionId());

        log.info(TestConnectionStatusManager.toDebugString());

        TestStatusStack statusStack1 = TestConnectionStatusManager.getStatusStack(con1.getConnectionId());
        TestCase.assertTrue(statusStack1 != null);
        TestCase.assertTrue(statusStack1.isCommitted());
        TestCase.assertTrue(statusStack1.isReleased());

        TestStatusStack statusStack2 = TestConnectionStatusManager.getStatusStack(con2.getConnectionId());
        TestCase.assertTrue(statusStack2 != null);
        TestCase.assertTrue(statusStack2.isRolledback());
        // one-phase commit -> no prepare
        TestCase.assertTrue(!statusStack2.isPrepared());
        TestCase.assertTrue(statusStack2.isReleased());
    }


    /**
     * A XAConnection is bound to different transactions having different XIDs.
     * @throws Exception
     */
    public void testDifferentTransactionBranches() throws Exception {

        IPhynixxXAResource<ITestConnection> xares = factory1.getXAResource();

        XidWrapper xid1= new XidWrapper(10, new byte[] {0x10}, new byte[] {0x10});
        XidWrapper xid2= new XidWrapper(10, new byte[] {0x20}, new byte[] {0x20});

        xares.start(xid1,XAResource.TMNOFLAGS );
        ITestConnection con1= xares.getXAConnection().getConnection();
        con1.act(1);

        xares.end(xid1, XAResource.TMSUCCESS);
        xares.commit(xid1, true);


        xares.start(xid2,XAResource.TMNOFLAGS );
        ITestConnection con2= xares.getXAConnection().getConnection();
        con2.act(1);

        xares.end(xid2, XAResource.TMSUCCESS);
        xares.rollback(xid2);

        log.info(TestConnectionStatusManager.toDebugString());

        TestStatusStack statusStack1 = TestConnectionStatusManager.getStatusStack(con1.getConnectionId());
        TestCase.assertTrue(statusStack1 != null);
        TestCase.assertTrue(statusStack1.isCommitted());
        // one-phase commit -> no prepare
        TestCase.assertTrue(!statusStack1.isPrepared());
        TestCase.assertTrue(statusStack1.isReleased());

        TestStatusStack statusStack2 = TestConnectionStatusManager.getStatusStack(con2.getConnectionId());
        TestCase.assertTrue(statusStack2.isRolledback());
        TestCase.assertTrue(statusStack2 != null);
        TestCase.assertTrue(statusStack2.isReleased());
    }

    /**
     * A XAConnection is bound to different transactions having different XIDs.
     * A second TX is started bust the first TX is already bound to a global transaction.
     *
     * This is prohibited by the JTA spec.
     *
     * @throws Exception
     */
    public void testDifferentTransactionBranches2() throws Exception {

        IPhynixxXAResource<ITestConnection> xares = factory1.getXAResource();

        XidWrapper xid1= new XidWrapper(10, new byte[] {0x10}, new byte[] {0x10});
        XidWrapper xid2= new XidWrapper(10, new byte[] {0x20}, new byte[] {0x20});

        xares.start(xid1,XAResource.TMNOFLAGS );
        ITestConnection con1= xares.getXAConnection().getConnection();
        con1.act(1);

        try {
            xares.start(xid2,XAResource.TMNOFLAGS );
            new AssertionFailedError("A XAResource may no be associated to 2 active Transactions");
        } catch(Exception e) {}

    }


    /**
     * two different XAResourceProgressState Factories ( == resourceManagers) instanciate two connections
     * <p/>
     * If one of connection is closed, the 'commit' has to fail and
     * the XAResources have to be rollbacked
     *
     * @throws Exception
     */
    /**
     public void testHeuristicRollback() throws Exception {

     int freeConnection1 = factory1.freeConnectionSize();
     int freeConnection2 = factory2.freeConnectionSize();

     IPhynixxXAConnection<ITestConnection> xaCon1 = factory1.getXAConnection();
     ITestConnection con1 = (ITestConnection) xaCon1.getConnection();
     Object conId1 = con1.getConnectionId();

     IPhynixxXAConnection<ITestConnection> xaCon2 = factory2.getXAConnection();
     ITestConnection con2 = (ITestConnection) xaCon2.getConnection();
     Object conId2 = con2.getConnectionId();

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
     //TestCase.assertTrue(statusStack.isReleased());


     statusStack = TestConnectionStatusManager.getStatusStack(conId2);
     TestCase.assertTrue(statusStack != null);
     TestCase.assertTrue(!statusStack.isCommitted());
     TestCase.assertTrue(!statusStack.isPrepared());

     // con1 is not any longer available, threrefore no rollback
     TestCase.assertTrue(statusStack.isRollbacked());
     //TestCase.assertTrue(statusStack.isReleased());
     }
     **/

    /**
     * scenario:
     * transaction timeout is set to 2 secs.
     * The commit has to fail,when the XAResource expired
     *
     * @throws Exception
     */
    public void testTimeoutNotSupported() throws Exception {
        IPhynixxXAConnection<ITestConnection> xaCon = factory1.getXAConnection();
        ITestConnection con = xaCon.getConnection();
        XAResource xaresource = xaCon.getXAResource();

        try {
            xaresource.setTransactionTimeout(2);
            throw new AssertionFailedError("Timeout not supported");
        } catch (Exception e) {
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }

    /**
     * scenario:
     * transaction timeout is set to 2 secs.
     * The commit has to fail,when the XAResource expired
     *
     * @throws Exception
     */
    public void testTimeout1() throws Exception {
        IPhynixxXAConnection<ITestConnection> xaCon = factory1.getXAConnection();
        ITestConnection con = xaCon.getConnection();
        XAResource xaresource = xaCon.getXAResource();
        try {
             xaresource.setTransactionTimeout(10);
            throw new AssertionFailedError("Timeout not supported");
        } catch (Exception e) {}

        try {
            this.getTransactionManager().begin();

            // act transactional and enlist the current resource
            con.act(1);

            //  sleeping 7 secs to provoke timeout
            TestUtils.sleep(10 * 1000);

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
        IPhynixxXAConnection<ITestConnection> xaCon = factory1.getXAConnection();
        ITestConnection con = (ITestConnection) xaCon.getConnection();
        XAResource xaresource = xaCon.getXAResource();
        try {
        xaresource.setTransactionTimeout(2);
        throw new AssertionFailedError("Timeout not supported");
    } catch (Exception e) {}

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
