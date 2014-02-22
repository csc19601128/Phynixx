package org.csc.phynixx.phynixx.testconnection;

/*
 * #%L
 * phynixx-connection
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


import java.util.ArrayList;
import java.util.List;

public class TestStatusStack {

    private Object id = null;

    private List<TestConnectionStatus> statusStack = new ArrayList<TestConnectionStatus>();

    TestStatusStack(Object id, List<TestConnectionStatus> statusStack) {
        super();
        this.id = id;
        this.statusStack = statusStack;
    }

    public Object getConnectionId() {
        return id;
    }

    public boolean isEmpty() {
        return this.statusStack == null || this.statusStack.isEmpty();
    }

    public int countStatus(TestConnectionStatus status) {

        int counter = 0;
        for (TestConnectionStatus stat : this.statusStack) {
            if (stat == status) {
                counter++;
            }
        }

        return counter;
    }


    public boolean isStatusEntered(TestConnectionStatus status) {
        return this.statusStack.contains(status);
    }


    public boolean isCommitted() {
        return this.statusStack.contains(TestConnectionStatus.COMMITTED);
    }

    public boolean isClosed() {
        return this.statusStack.contains(TestConnectionStatus.CLOSED);
    }

    public boolean isRolledback() {
        return this.statusStack.contains(TestConnectionStatus.ROLLEDBACK);
    }

    public boolean isRequiresTransaction() {
        return this.statusStack.contains(TestConnectionStatus.REQUIRES_TRANSACTION);
    }

    public boolean isPrepared() {
        return this.statusStack.contains(TestConnectionStatus.PREPARED);
    }

    public boolean isRecoverd() {
        return this.statusStack.contains(TestConnectionStatus.RECOVERED);
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TestStatusStack)) {
            return false;
        }

        return ((TestStatusStack) obj).id.equals(this.id);
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return "TestStatusStack{" +
                "id=" + id +
                ", statusStack=" + statusStack +
                '}';
    }
}
