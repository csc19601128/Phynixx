package org.csc.phynixx.loggersystem.logrecord;

/*
 * #%L
 * phynixx-logger
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
