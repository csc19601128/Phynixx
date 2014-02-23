package org.csc.phynixx.xa.recovery;

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


import junit.framework.TestCase;
import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.phynixx.testconnection.ITestConnection;
import org.csc.phynixx.phynixx.testconnection.TestConnectionStatusManager;
import org.csc.phynixx.xa.IPhynixxXAConnection;
import org.csc.phynixx.xa.TestXAResourceFactory;
import org.objectweb.howl.log.Configuration;
import org.objectweb.howl.log.xa.XALogger;
import org.objectweb.jotm.Jotm;
import org.objectweb.jotm.TransactionRecovery;
import org.objectweb.jotm.TransactionResourceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class XARecoveryTest extends TestCase {

    private void loadJotmProps() throws Exception {
        //File f= new File("./src/test/test-resources/conf");
        InputStream io = Thread.currentThread().getContextClassLoader().getResourceAsStream("conf/jotm.properties");
        Properties props = new Properties();
        props.load(io);

        // save the props in a tmp file ...
        File d = new File(this.tmpDirectory.getDirectory(), "/conf");
        if (!d.exists()) {
            d.mkdir();
        }
        File f = new File(d, "jotm.properties");
        props.store(new FileOutputStream(f), null);
        System.getProperties().setProperty("jotm.base", tmpDirectory.getDirectory().getCanonicalPath());

    }

    private IPhynixxLogger log = PhynixxLogManager.getLogger(this.getClass());

    private TmpDirectory tmpDirectory = null;
    private Jotm jotm = null;
    private TestXAResourceFactory factory1 = null;
    private TestXAResourceFactory factory2 = null;

    protected void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();

        this.tmpDirectory = new TmpDirectory();

        loadJotmProps();


        // read the howl-config an copies it to an temp. file required by the JOTM

        TestConnectionStatusManager.clear();
        this.jotm = new Jotm(true, false);

        this.factory1 = new TestXAResourceFactory(
                "RF1", this.tmpDirectory.getDirectory(),
                this.jotm.getTransactionManager());

        this.factory2 = new TestXAResourceFactory(
                "RF2", this.tmpDirectory.getDirectory(),
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


        // delete all tmp files ...
        new TmpDirectory().clear();
    }

    private Jotm getJotm() {
        return this.jotm;
    }

    private void provokeRecoverySituation() throws Exception {
        Runnable runnable = new Runnable() {
            public void run() {
                IPhynixxXAConnection<ITestConnection> xaCon1 = XARecoveryTest.this.factory1.getXAConnection();
                ITestConnection con1 = xaCon1.getConnection();

                IPhynixxXAConnection<ITestConnection> xaCon2 = XARecoveryTest.this.factory2.getXAConnection();
                ITestConnection con2 = xaCon2.getConnection();

                try {
                    XARecoveryTest.this.getJotm().getTransactionManager().begin();
                    // act transactional and enlist the current resource
                    con1.act(1);
                    con2.act(1);
                    // no act on the second  resource ...
                    // .... con2.act();

                    // Thread is aborted during commit ....
                    XARecoveryTest.this.getJotm().getTransactionManager().commit();
                } catch (Exception e) {
                    log.info("Transaction aborted programmatically" + e.getMessage());
                    e.printStackTrace();
                } finally {
                    if (con1 != null) {
                        con1.close();
                    }
                    if (con2 != null) {
                        con2.close();
                    }
                }
            }
        };


        Thread runner = new Thread(runnable);
        runner.start();

        runner.join();


        // unlock the files ....
        //this.unlockLogfiles();
        //String property="org.objectweb.howl." + name.getAbsolutePath() + ".locked";
        this.jotm.stop();
        this.jotm = new Jotm(true, false);
        // debug recovery log content ...
        Configuration cfg = this.loadHowlConfig();
        XALogger xaLog = new XALogger(cfg);
        //xaLog.reopen(new TestReplayListener());

        // xaLog.replay(new TestReplayListener());
    }

    public void testRecovery() throws Exception {
        this.provokeRecoverySituation();


        IPhynixxXAConnection<ITestConnection> xaCon1 = XARecoveryTest.this.factory1.getXAConnection();
        ITestConnection con1 = xaCon1.getConnection();

        IPhynixxXAConnection<ITestConnection> xaCon2 = XARecoveryTest.this.factory2.getXAConnection();
        ITestConnection con2 = xaCon2.getConnection();


        try {
            this.getJotm().getUserTransaction().begin();

            TransactionRecovery recovery = this.getJotm().getTransactionRecovery();

            TransactionResourceManager resourceMgr = new SampleTransactionResourceManager();
            recovery.registerResourceManager(factory1.getId(), xaCon1.getXAResource(), "test Recovery", resourceMgr);
            recovery.registerResourceManager(factory2.getId(), xaCon2.getXAResource(), "test Recovery", resourceMgr);

            recovery.startResourceManagerRecovery();

        } finally {
            if (con1 != null) {
                con1.close();
            }
            if (con2 != null) {
                con2.close();
            }
        }

    }

    private Configuration loadHowlConfig() throws Exception {

        Properties systEnv = new Properties();
        systEnv.putAll(System.getProperties());
        String jotmBase = systEnv.getProperty("jotm.base");

        jotmBase = jotmBase.trim();

        // JOTM_BASE/conf/fileName
        String fileFullPathname = jotmBase + File.separator + "conf" + File.separator + "jotm.properties";

        if (log.isInfoEnabled()) {
            log.info("JOTM properties file= " + fileFullPathname);
        }

        Properties howlprop = new Properties();
        FileInputStream inStr = new FileInputStream(fileFullPathname);

        systEnv.load(inStr);

        String myhowlprop = null;
        myhowlprop = systEnv.getProperty("howl.log.ListConfiguration", "false");
        howlprop.put("listConfig", myhowlprop);
        myhowlprop = systEnv.getProperty("howl.log.BufferSize", "4");
        howlprop.put("bufferSize", myhowlprop);
        myhowlprop = systEnv.getProperty("howl.log.MinimumBuffers", "16");
        howlprop.put("minBuffers", myhowlprop);
        myhowlprop = systEnv.getProperty("howl.log.MaximumBuffers", "16");
        howlprop.put("maxBuffers", myhowlprop);
        myhowlprop = systEnv.getProperty("howl.log.MaximumBlocksPerFile", "200");
        howlprop.put("maxBlocksPerFile", myhowlprop);
        myhowlprop = systEnv.getProperty("howl.log.FileDirectory", systEnv.getProperty("basedir", "."));
        howlprop.put("logFileDir", myhowlprop);
        myhowlprop = systEnv.getProperty("howl.log.FileName", "howl");
        howlprop.put("logFileName", myhowlprop);
        myhowlprop = systEnv.getProperty("howl.log.MaximumFiles", "2");
        howlprop.put("maxLogFiles", myhowlprop);

        return new Configuration(howlprop);
    }


    private void unlockLogfiles() throws Exception {
        Properties props = new Properties();
        props.putAll(System.getProperties());
        for (Iterator iterator = props.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry prop = (Map.Entry) iterator.next();

            String key = (String) prop.getKey();
            if (key.startsWith("org.objectweb.howl.") && key.endsWith(".locked")) {
                System.getProperties().setProperty(key, "false");
                log.info(key + "=" + System.getProperties().getProperty(key));
            }
        }
    }
}
