package org.csc.phynixx.common.io;

import  org.junit.Assert;
import org.junit.Test;

/**
 * Created by christoph on 15.03.14.
 */
public class LogRecordIOTest {


    @Test
    public void testInt() throws Exception {

    }

    @Test
    public void testSerializable() throws Exception {

        LogRecordWriter writer= new LogRecordWriter();

        byte[] byteArray= writer.writeObject("abcd").writeObject(null).toByteArray();

        LogRecordReader reader= new LogRecordReader(byteArray);
        Assert.assertEquals("abcd",reader.readObject());
        Assert.assertTrue(reader.readObject() == null);

    }

    /**
     * TODO complete me
     * @throws Exception
     */
    @Test
    public void testFloat() throws Exception {

    }

    /**
     * TODO complete me
     * @throws Exception
     */
    @Test
    public void testShort() throws Exception {

    }

    /**
     * TODO complete me
     * @throws Exception
     */
    @Test
    public void testDouble() throws Exception {

    }

    /**
     * TODO complete me
     * @throws Exception
     */
    @Test
    public void testUTF() throws Exception {

    }

    /**
     * TODO complete me
     * @throws Exception
     */
    @Test
    public void testLong() throws Exception {

    }

    /**
     * TODO complete me
     * @throws Exception
     */
    @Test
    public void testBoolean() throws Exception {

    }


    /**
     * TODO complete me
     * @throws Exception
     */
    @Test
    public void testIntegrationScenario() throws Exception {

        LogRecordWriter writer= new LogRecordWriter();

        writer.writeObject("abcd").writeObject(null);


    }
}
