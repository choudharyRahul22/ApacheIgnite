package com.ignite;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.cache.configuration.FactoryBuilder;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.transactions.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.ignite.transactions.TransactionConcurrency.PESSIMISTIC;
import static org.apache.ignite.transactions.TransactionIsolation.REPEATABLE_READ;

/**
 * Created by isatimur on 8/9/16.
 */
public class CacheStoreSample {

    /*
      Cache name to store posts.
      */
    private static final String POST_CACHE_NAME = CacheStoreSample.class.getSimpleName() + "-post";
    private static Logger LOGGER = LoggerFactory.getLogger(CacheStoreSample.class);
    private static final String MYSQL = "mysql";


    /**
     * This is an entry point of CacheStoreSample, the ignite configuration lies upon resources directory as
     * example-ignite.xml.
     *
     * @param args Command line arguments, none required.
     */
    public static void main(String[] args) throws Exception
    {
        /*if(args.length <= 0 ){
            LOGGER.error("Usages! java -jar .\\target\\cache-store-runnable.jar postgresql|mongodb");
            System.exit(0);
        }
        if(args[0].equalsIgnoreCase(MYSQL)){*/
            jdbcStoreExample();
       /* }*/

    }

    private static void jdbcStoreExample() throws Exception{
        //let's make a dynamic cache on the fly which is distributed across all running nodes.
        //the same configuration you would probably set in configuration xml format
        IgniteConfiguration cfg = new IgniteConfiguration();

        CacheConfiguration configuration = new CacheConfiguration();
        configuration.setName("dynamicCache");
        configuration.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);

        configuration.setCacheStoreFactory(FactoryBuilder.factoryOf(MySQLDBStore.class));
        configuration.setReadThrough(true);
        configuration.setWriteThrough(true);

        configuration.setWriteBehindEnabled(true);
        
        // Second Node
        CacheConfiguration configuration1 = new CacheConfiguration();
        configuration.setName("staticCache");
        configuration.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);

        configuration.setCacheStoreFactory(FactoryBuilder.factoryOf(MySQLDBStore.class));
        configuration.setReadThrough(true);
        configuration.setWriteThrough(true);

        configuration.setWriteBehindEnabled(true);

        System.out.println("Start. PersistenceStore example.");
        cfg.setCacheConfiguration(configuration);
        cfg.setCacheConfiguration(configuration1);

        try (Ignite ignite = Ignition.start(cfg)) {
            //create cache if it doesn't exist
            int count = 10;
            try (IgniteCache<String, Post> igniteCache = ignite.getOrCreateCache(configuration)) {
                try (Transaction tx = ignite.transactions().txStart(PESSIMISTIC, REPEATABLE_READ)) {
                    //let us clear

                    for (int i = 1; i <= count; i++)
                        igniteCache.put("_" + i, new Post("_" + i, "title-" + i, "description-" + i, LocalDate.now().plus(i, ChronoUnit.DAYS), "author-" + i));

                    tx.commit();

                    for (int i = 1; i < count; i += 2) {
                        igniteCache.clear("_" + i);
                        System.out.println("Clear every odd key: " + i);
                    }

                    for (long i = 1; i <= count; i++)
                    	System.out.println("Local peek at [key=_" + i + ", val=" + igniteCache.localPeek("_" + i) + ']');

                    for (long i = 1; i <= count; i++)
                    	System.out.println("Got [key=_" + i + ", val=" + igniteCache.get("_" + i) + ']');

                    tx.commit();
                }
            }

            log("PersistenceStore example finished.");
            //ignite.destroyCache("dynamicCache");
            Thread.sleep(Integer.MAX_VALUE);
        }
    }

    

    /**
     * Prints message to logger.
     *
     * @param msg String.
     */
    private static void log(String msg) {
        LOGGER.info("\t" + msg);
    }

}