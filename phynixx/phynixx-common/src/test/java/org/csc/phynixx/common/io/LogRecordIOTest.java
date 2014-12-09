package org.csc.phynixx.common.io;

/*
 * #%L
 * phynixx-common
 * %%
 * Copyright (C) 2014 Christoph Schmidt-Casdorff
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


import org.csc.phynixx.common.logger.PhynixxLogManager;
import  org.junit.Assert;
import org.junit.Test;

/**
 * Created by christoph on 15.03.14.
 */
public class LogRecordIOTest {


    @Test
    public void testInt() throws Exception {
        PhynixxLogManager.getLogger(this.getClass()).info("Logger found");
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
     * @throws Exception
     */
    @Test
    public void testFloat() throws Exception {
        LogRecordWriter writer= new LogRecordWriter();
        final byte[] bytes = writer.writeFloat(1.2f).toByteArray();

        LogRecordReader reader= new LogRecordReader(bytes);
        Assert.assertEquals(Float.valueOf(1.2f),Float.valueOf(reader.readFloat()));

    }

    /**
     * @throws Exception
     */
    @Test
    public void testByte() throws Exception {
        LogRecordWriter writer= new LogRecordWriter();
        final byte[] bytes = writer.writeByte(Byte.valueOf("1")).toByteArray();

        LogRecordReader reader= new LogRecordReader(bytes);
        Assert.assertEquals(Byte.valueOf("1"),Byte.valueOf(reader.readByte()));

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
