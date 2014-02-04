package org.csc.phynixx.tutorial;

import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IXADataRecorderAware;
import org.csc.phynixx.connection.RequiresTransaction;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by zf4iks2 on 04.02.14.
 */
public interface TAEnabledUTFWriter extends IPhynixxConnection, IXADataRecorderAware {

    @RequiresTransaction
    void resetContent() throws IOException;

    @RequiresTransaction
    void open(File file);

    @RequiresTransaction
    TAEnabledUTFWriter write(String value) throws IOException;

    List<String> getContent();
}
