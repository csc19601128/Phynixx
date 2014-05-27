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


import javax.transaction.xa.Xid;
import java.io.*;

public class XidWrapper implements Xid {

    private int formatId = 0;

    private byte[] globalTransactionId = null;

    private byte[] branchQualifier = null;

    public byte[] getBranchQualifier() {
        return branchQualifier;
    }

    public int getFormatId() {
        return formatId;
    }

    public byte[] getGlobalTransactionId() {
        return globalTransactionId;
    }


    public XidWrapper(Xid otherXid) {
        this.formatId = otherXid.getFormatId();
        this.branchQualifier = otherXid.getBranchQualifier();
        this.globalTransactionId = otherXid.getGlobalTransactionId();
    }

    public XidWrapper(int formatId, byte[] globalTransactionId, byte[] branchQualifer) throws IOException {

        this.formatId = formatId;
        this.branchQualifier =branchQualifer;
        this.globalTransactionId = globalTransactionId;
    }


    public XidWrapper(byte[] xid) throws IOException {
        this.internalize(xid);
    }


    private void internalize(byte[] xid) throws IOException {
        DataInputStream inputIO = null;

        try {
            ByteArrayInputStream byteIO = new ByteArrayInputStream(xid);
            inputIO = new DataInputStream(byteIO);
            this.formatId = inputIO.readInt();

            int gloabalTransactionIdLength = inputIO.readInt();
            this.globalTransactionId = new byte[gloabalTransactionIdLength];
            inputIO.read(this.globalTransactionId);

            int branchLength = inputIO.readInt();
            this.branchQualifier = new byte[branchLength];
            inputIO.read(this.branchQualifier);
        } finally {
            if (inputIO != null) inputIO.close();
        }


    }

    public byte[] export() throws IOException {

        DataOutputStream out = null;

        try {
            ByteArrayOutputStream byteIO = new ByteArrayOutputStream();
            out = new DataOutputStream(byteIO);
            out.writeInt(this.getFormatId());
            out.writeInt(this.globalTransactionId.length);
            out.write(globalTransactionId);
            out.writeInt(branchQualifier.length);
            out.write(branchQualifier);

            out.flush();

            return byteIO.toByteArray();
        } finally {
            if (out != null) out.close();
        }


    }

}
