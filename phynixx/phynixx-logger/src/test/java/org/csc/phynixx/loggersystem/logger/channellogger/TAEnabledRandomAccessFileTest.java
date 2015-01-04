package org.csc.phynixx.loggersystem.logger.channellogger;

/*
 * #%L
 * phynixx-logger
 * %%
 * Copyright (C) 2014 - 2015 Christoph Schmidt-Casdorff
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


import org.csc.phynixx.common.TestUtils;
import org.csc.phynixx.common.TmpDirectory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class TAEnabledRandomAccessFileTest {

    public static final String RANDOM_ACCESS_FILE_NAME = "randomAccessFile1.txt";
    private TmpDirectory tmpDir = null;

    private TAEnabledRandomAccessFile taEnabledRandomAccessFile;
    private RandomAccessFile randomAccessFile;

    @Before
    public void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();
        // delete all tmp files ...
        this.tmpDir = new TmpDirectory("channel");
        this.tmpDir.clear();

        randomAccessFile = new RandomAccessFile(this.tmpDir.assertExitsFile(RANDOM_ACCESS_FILE_NAME), "rw");
        this.taEnabledRandomAccessFile= new TAEnabledRandomAccessFile(randomAccessFile);
    }

    @After
    public void tearDown() throws Exception {
        if( taEnabledRandomAccessFile!=null) {
            taEnabledRandomAccessFile.close();
        }
        if( randomAccessFile!=null) {
            randomAccessFile.close();
        }
        // delete all tmp files ...
        this.tmpDir.clear();
    }


    @Test
    public void testGetHeaderLength() throws Exception {

        Assert.assertEquals(TAEnabledRandomAccessFile.HEADER_LENGTH, this.taEnabledRandomAccessFile.getHeaderLength());

    }

    @Test
    public void testGetRandomAccessFile() throws Exception {

    }

    @Test
    public void testAvailable() throws Exception {

    }

    @Test
    public void testPosition() throws Exception {
        Assert.assertEquals(0, this.taEnabledRandomAccessFile.position());
        this.taEnabledRandomAccessFile.writeLong(2l);

        // Position is beyond the committed area. This is permitted
        Assert.assertEquals(8, this.taEnabledRandomAccessFile.position());


    }

    @Test
    public void testClose() throws Exception {

        this.taEnabledRandomAccessFile.close();


    }

    @Test
    public void testGetCommittedSize() throws Exception {

        Assert.assertEquals(0, this.taEnabledRandomAccessFile.getCommittedSize());
        byte[] data1= "1234".getBytes("UTF-8");
        this.taEnabledRandomAccessFile.write(data1);
        this.taEnabledRandomAccessFile.commit();

        Assert.assertEquals(data1.length, this.taEnabledRandomAccessFile.getCommittedSize());

        this.taEnabledRandomAccessFile.writeLong(2l);
        this.taEnabledRandomAccessFile.commit();
        Assert.assertEquals(data1.length + 8, this.taEnabledRandomAccessFile.getCommittedSize());
    }


    @Test
    public void testWriteAndReadBytes() throws Exception {
        byte[] data1= "1234".getBytes("UTF-8");

        this.taEnabledRandomAccessFile.write(data1);

        this.taEnabledRandomAccessFile.commit();

        this.taEnabledRandomAccessFile.rewind();

        final byte[] read1 = this.taEnabledRandomAccessFile.read(data1.length);
        System.out.println(new String(read1, "UTF-8"));
        Assert.assertTrue(Arrays.equals(data1, read1));
    }


    @Test
    public void testMultipleWriteBytes() throws Exception {
        byte[] data1= "1234".getBytes("UTF-8");
        byte[] data2= "5678".getBytes("UTF-8");

        this.taEnabledRandomAccessFile.write(data1);
        this.taEnabledRandomAccessFile.write(data2);

        this.taEnabledRandomAccessFile.commit();

        this.taEnabledRandomAccessFile.rewind();

        final byte[] read1 = this.taEnabledRandomAccessFile.read(data1.length);
        System.out.println(new String(read1, "UTF-8"));
        Assert.assertTrue(Arrays.equals(data1, read1));

        final byte[] read2 = this.taEnabledRandomAccessFile.read(data2.length);
        System.out.println(new String(read2, "UTF-8"));
        Assert.assertTrue(Arrays.equals(data2, read2));
    }


    @Test
    public void testWriteAndReadShort() throws Exception {
        this.taEnabledRandomAccessFile.writeShort((short)1);
        this.taEnabledRandomAccessFile.commit();
        this.taEnabledRandomAccessFile.rewind();
        Assert.assertEquals((short)1, this.taEnabledRandomAccessFile.readShort());

    }

    @Test
    public void testWriteAndReadInt() throws Exception {

        this.taEnabledRandomAccessFile.writeInt(1);
        this.taEnabledRandomAccessFile.commit();
        this.taEnabledRandomAccessFile.rewind();
        Assert.assertEquals(1, this.taEnabledRandomAccessFile.readInt());

    }

    @Test
    public void testWriteAndReadLong() throws Exception {
        this.taEnabledRandomAccessFile.writeLong(1l);
        this.taEnabledRandomAccessFile.commit();
        this.taEnabledRandomAccessFile.rewind();
        Assert.assertEquals(1l, this.taEnabledRandomAccessFile.readLong());

    }

    @Test public void testRewind() throws IOException {
        byte[] data1= "1234".getBytes("UTF-8");
        this.taEnabledRandomAccessFile.write(data1);
        this.taEnabledRandomAccessFile.commit();
        Assert.assertEquals(0,this.taEnabledRandomAccessFile.available());

        this.taEnabledRandomAccessFile.rewind();
        Assert.assertEquals(data1.length,this.taEnabledRandomAccessFile.available());


    }


    @Test public void testForwardWind() throws IOException {
        byte[] data1= "1234".getBytes("UTF-8");
        this.taEnabledRandomAccessFile.write(data1);
        this.taEnabledRandomAccessFile.commit();

        this.taEnabledRandomAccessFile.rewind();
        Assert.assertEquals(data1.length,this.taEnabledRandomAccessFile.available());

        this.taEnabledRandomAccessFile.forwardWind();

        Assert.assertEquals(0,this.taEnabledRandomAccessFile.available());
        this.taEnabledRandomAccessFile.writeLong(1l);
        this.taEnabledRandomAccessFile.commit();
        Assert.assertEquals(data1.length+8,this.taEnabledRandomAccessFile.getCommittedSize());




    }

    @Test
    public void testReset() throws Exception {



    }

    @Test
    public void testCommit() throws Exception {

    }


    @Test
    public void testMultipleWrite() throws Exception {
        byte[] data1= "1234".getBytes("UTF-8");
        byte[] data2= "5678".getBytes("UTF-8");

        this.taEnabledRandomAccessFile.write(data1);
        this.taEnabledRandomAccessFile.write(data2);

        this.taEnabledRandomAccessFile.commit();

        this.taEnabledRandomAccessFile.rewind();

        final byte[] read1 = this.taEnabledRandomAccessFile.read(data1.length);
        System.out.println(new String(read1, "UTF-8"));
        Assert.assertTrue(Arrays.equals(data1, read1));

        final byte[] read2 = this.taEnabledRandomAccessFile.read(data2.length);
        System.out.println(new String(read2, "UTF-8"));
        Assert.assertTrue(Arrays.equals(data2, read2));
    }

}