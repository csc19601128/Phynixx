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


import javax.transaction.Status;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.util.HashMap;
import java.util.Map;

public abstract class ConstantsPrinter {

    private static Map stati = new HashMap();
    private static Map xaresourceFlags = new HashMap();
    private static Map errorcodes = new HashMap();

    static {
        stati.put(new Integer(Status.STATUS_ACTIVE), "STATUS_ACTIVE");
        stati.put(new Integer(Status.STATUS_NO_TRANSACTION), "STATUS_NO_TRANSACTION");
        stati.put(new Integer(Status.STATUS_MARKED_ROLLBACK), "STATUS_MARKED_ROLLBACK");
        stati.put(new Integer(Status.STATUS_UNKNOWN), "STATUS_UNKNOWN");
        stati.put(new Integer(Status.STATUS_COMMITTED), "STATUS_COMMITTED");
        stati.put(new Integer(Status.STATUS_COMMITTING), "STATUS_COMMITTING");
        stati.put(new Integer(Status.STATUS_PREPARED), "STATUS_PREPARED");
        stati.put(new Integer(Status.STATUS_PREPARING), "STATUS_PREPARING");
        stati.put(new Integer(Status.STATUS_ROLLEDBACK), "STATUS_ROLLEDBACK");
        stati.put(new Integer(Status.STATUS_ROLLING_BACK), "STATUS_ROLLING_BACK");

        xaresourceFlags.put(new Integer(XAResource.TMFAIL), "TMFAIL");
        xaresourceFlags.put(new Integer(XAResource.TMSTARTRSCAN), "TMSTARTRSCAN");
        xaresourceFlags.put(new Integer(XAResource.TMENDRSCAN), "TMENDRSCAN");
        xaresourceFlags.put(new Integer(XAResource.TMJOIN), "TMJOIN");
        xaresourceFlags.put(new Integer(XAResource.TMNOFLAGS), "TMNOFLAGS");
        xaresourceFlags.put(new Integer(XAResource.TMONEPHASE), "TMONEPHASE");
        xaresourceFlags.put(new Integer(XAResource.TMRESUME), "TMRESUME");
        xaresourceFlags.put(new Integer(XAResource.TMSUCCESS), "TMSUCCESS");
        xaresourceFlags.put(new Integer(XAResource.TMSUSPEND), "TMSUSPEND");
        xaresourceFlags.put(new Integer(XAResource.XA_OK), "XA_OK");
        xaresourceFlags.put(new Integer(XAResource.XA_RDONLY), "XA_RDONLY");


        errorcodes.put(new Integer(XAException.XA_HEURCOM), "XA_HEURCOM");
        errorcodes.put(new Integer(XAException.XA_HEURHAZ), "XA_HEURHAZ");
        errorcodes.put(new Integer(XAException.XA_HEURMIX), "XA_HEURMIX");
        errorcodes.put(new Integer(XAException.XA_HEURRB), "XA_HEURRB");
        errorcodes.put(new Integer(XAException.XA_NOMIGRATE), "XA_NOMIGRATE");
        errorcodes.put(new Integer(XAException.XA_RBBASE), "XA_RBBASE");
        errorcodes.put(new Integer(XAException.XA_RBCOMMFAIL), "XA_RBCOMMFAIL");
        errorcodes.put(new Integer(XAException.XA_RBDEADLOCK), "XA_RBDEADLOCK");
        errorcodes.put(new Integer(XAException.XA_RBEND), "XA_RBEND");
        errorcodes.put(new Integer(XAException.XA_RBINTEGRITY), "XA_RBINTEGRITY");
        errorcodes.put(new Integer(XAException.XA_RBOTHER), "XA_RBOTHER");
        errorcodes.put(new Integer(XAException.XA_RBPROTO), "XA_RBPROTO");
        errorcodes.put(new Integer(XAException.XA_RBROLLBACK), "XA_RBROLLBACK");
        errorcodes.put(new Integer(XAException.XA_RBTIMEOUT), "XA_RBTIMEOUT");
        errorcodes.put(new Integer(XAException.XA_RBTRANSIENT), "XA_RBTRANSIENT");
        errorcodes.put(new Integer(XAException.XA_RDONLY), "XA_RDONLY");
        errorcodes.put(new Integer(XAException.XA_RETRY), "XA_RETRY");
        errorcodes.put(new Integer(XAException.XAER_ASYNC), "XAER_ASYNC");
        errorcodes.put(new Integer(XAException.XAER_DUPID), "XAER_DUPID");
        errorcodes.put(new Integer(XAException.XAER_INVAL), "XAER_INVAL");
        errorcodes.put(new Integer(XAException.XAER_NOTA), "XAER_NOTA");
        errorcodes.put(new Integer(XAException.XAER_OUTSIDE), "XAER_OUTSIDE");
        errorcodes.put(new Integer(XAException.XAER_PROTO), "XAER_PROTO");
        errorcodes.put(new Integer(XAException.XAER_RMERR), "XAER_RMERR");
        errorcodes.put(new Integer(XAException.XAER_RMFAIL), "XAER_RMFAIL");

    }

    public static String getStatusMessage(XAResourceProgressState status) {
        return status.toString();

    }

    public static String getStatusMessage(XAResourceActivationState status) {
        return status.toString();
    }

    public static String getXAResourceMessage(int flags) {
        return (String) xaresourceFlags.get(new Integer(flags));
    }


    public static String getXAErrorCode(int errorCode) {
        return (String) errorcodes.get(new Integer(errorCode));
    }

}
