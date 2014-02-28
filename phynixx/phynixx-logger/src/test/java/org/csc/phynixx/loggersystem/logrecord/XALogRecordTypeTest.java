package org.csc.phynixx.loggersystem.logrecord;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by christoph on 28.02.14.
 */

@RunWith(Parameterized.class)
public class XALogRecordTypeTest {

    private static class FixtureData {
        final short type;
        final XALogRecordType recorderType;

        private FixtureData(short type, XALogRecordType recorderType) {
            this.type = type;
            this.recorderType = recorderType;
        }

        @Override
        public String toString() {
            return "FixtureData{" +
                    "type=" + type +
                    ", recorderType=" + recorderType +
                    '}';
        }
    }


    private FixtureData fixtureData;

    public XALogRecordTypeTest(FixtureData fixtureData) {
        this.fixtureData = fixtureData;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] data= { {new FixtureData(XALogRecordType.UNKNOWN_TYPE, XALogRecordType.UNKNOWN)},
                {new FixtureData(XALogRecordType.ROLLBACK_DATA_TYPE, XALogRecordType.ROLLBACK_DATA)},
                {new FixtureData(XALogRecordType.ROLLFORWARD_DATA_TYPE, XALogRecordType.ROLLFORWARD_DATA)},
                {new FixtureData(XALogRecordType.USER_TYPE, XALogRecordType.USER)}
        };
        return Arrays.asList(data);
    }

    @Test
    public void testResolve() throws Exception {
        Assert.assertSame(this.fixtureData.recorderType, XALogRecordType.resolve(this.fixtureData.recorderType.getType()));
    }

    @Test
    public void testType() throws Exception {
        Assert.assertSame(this.fixtureData.type,this.fixtureData.recorderType.getType());

    }

    @Test
    public void testGetType() throws Exception {

    }
}
