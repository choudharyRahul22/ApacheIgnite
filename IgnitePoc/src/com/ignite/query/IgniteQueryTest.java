package com.ignite.query;

import java.util.ArrayList;
import java.util.List;

import javax.cache.Cache;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.query.ContinuousQuery;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;

public class IgniteQueryTest {

	private static final long ASSET_MV = 500;

	private static final long NUM_PORTFOLIOS = 10000;

	private static final long POS_PER_PORT = 100;

	public static void main(String[] args) {
		IgniteQueryTest test = new IgniteQueryTest();
		//test.loadData();
		//test.testSqlQuery();
		test.continuousQueryTest();
	}

	private void testSqlQuery() {
		Ignition.setClientMode(true);

		try (Ignite ignite = Ignition.start()) {
			IgniteCache<Long, Portfolio> cache = ignite.getOrCreateCache("portCache");

			SqlFieldsQuery countSql = new SqlFieldsQuery("select count(*) from portfolio");
			try (QueryCursor<List<?>> cursor = cache.query(countSql)) {
				System.out.println("Cache entries: " + cursor.getAll());
			}

			SqlFieldsQuery aumSql = new SqlFieldsQuery("select sum(nav) from portfolio");
			try (QueryCursor<List<?>> cursor = cache.query(aumSql)) {
				System.out.println("Total value of assets under management: " + cursor.getAll());
			}
		
		}
		
	}
	
	public void continuousQueryTest() {
        try (Ignite ignite = Ignition.start()) {
            ignite.destroyCache("myCache");
            IgniteCache<Long, Portfolio> cache = ignite.getOrCreateCache("myCache");

            ContinuousQuery<Long, Portfolio> qry = new ContinuousQuery<>();

            // Optional initial query to select all entries with NAV>1000
            qry.setInitialQuery(new ScanQuery<Long, Portfolio>((k, v) -> v.getNav() > 1000));

            // Remotely filter portfolios with small NAVs
            qry.setRemoteFilterFactory(() -> e -> e.getValue().getNav() > 1000);

            // Callback for update notifications
            qry.setLocalListener((evts) -> evts
                    .forEach(e -> System.out.println("Listener Event: " + e.getEventType()
                            + " key=" + e.getKey() + ", val=" + e.getValue())));

            // Seed the cache with two entries
            cache.put(1L, new Portfolio(1L, "P1", 1000L, null));
            cache.put(2L, new Portfolio(2L, "P2", 2000L, null));

            // Execute the query
            try (QueryCursor<Cache.Entry<Long, Portfolio>> cur = cache.query(qry)) {
            	System.out.println("Initial query result: " + cur.getAll());

	            // Adding items causes notifications
	            cache.put(2L, new Portfolio(2L, "P2", 3000L, null));
	            cache.put(3L, new Portfolio(3L, "P3", 4000L, null));
            }
        }
    }

	private void loadData() {
		// We have 2 mode client and server mode
		// server mode : we will participant in jobs : data will be copied to our node
		// client mode will load some data and quit
		Ignition.setClientMode(true);

		try (Ignite ignite = Ignition.start()) {

			CacheConfiguration<Long, Portfolio> config = new CacheConfiguration<>("portCache");
			// truns on indexing
			config.setIndexedTypes(Long.class, Portfolio.class);
			// backing up , so we will get 2 portfoilio one origional and one backup
			config.setBackups(1);

			ignite.getOrCreateCache(config);

			System.out.println("Loading Data......");

			try (IgniteDataStreamer<Long, Portfolio> dataStreamer = ignite.dataStreamer("portCache")) {

				for (long portIdx = 0; portIdx < NUM_PORTFOLIOS; portIdx++) {
					List<Position> positions = new ArrayList<>();
					for (long posIdx = 0; posIdx < POS_PER_PORT; posIdx++) {
						Position pos = new Position(posIdx, portIdx, "CUSIP" + posIdx, ASSET_MV);
						positions.add(pos);
					}

					Portfolio portfolio = new Portfolio(portIdx, portIdx + "PORT", ASSET_MV * POS_PER_PORT, positions);
					dataStreamer.addData(portIdx, portfolio);
				}

			}
			
			System.out.println("Cache data load complete");
		}

	}

}
