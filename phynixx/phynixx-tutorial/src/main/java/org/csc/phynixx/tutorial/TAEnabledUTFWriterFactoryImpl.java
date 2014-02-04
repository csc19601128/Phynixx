package org.csc.phynixx.tutorial;

/**
 * Created by zf4iks2 on 04.02.14.
 */
public class TAEnabledUTFWriterFactoryImpl implements TAEnabledUTFWriterFactory {


    @Override
    public TAEnabledUTFWriter getConnection() {
        return new TAEnabledUTFWriterImpl();
    }

    @Override
    public Class<TAEnabledUTFWriter> getConnectionInterface() {
        return TAEnabledUTFWriter.class;
    }

    @Override
    public void close() {

    }
}
