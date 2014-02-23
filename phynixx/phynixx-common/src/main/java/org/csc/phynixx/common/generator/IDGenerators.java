package org.csc.phynixx.common.generator;

/**
 * Created by christoph on 23.02.14.
 */
public class IDGenerators {


    public static <T> IDGenerator<T> synchronizeGenerator(IDGenerator<T> generator) {
        return new SynchronizedGenerator<T>(generator);
    }



    public static IDGenerator<Long> createLongGenerator(long seed) {

        return new IDLongGenerator(seed);
    }

    public static IDGenerator<Long> createLongGenerator(long seed, boolean synchronize) {

        IDGenerator<Long> generator=new IDLongGenerator(seed);
        if(synchronize) {
            generator= synchronizeGenerator(generator);
        }

        return generator;
    }

}
