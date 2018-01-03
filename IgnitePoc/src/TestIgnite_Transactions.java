import java.io.Serializable;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.transactions.Transaction;

public class TestIgnite_Transactions implements Serializable {

	public static void main(String[] args) throws InterruptedException {

		try (Ignite ignite = Ignition.start()) {

			// Creating CacheConfiguration
			CacheConfiguration<String, String> config = new CacheConfiguration<>();
			config.setName("Node-1");
			config.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
			
			// public interface IgniteCache<K,V> extends javax.cache.Cache<K,V>
			IgniteCache<String, String> cache = ignite.getOrCreateCache(config);

			// Putting Inside Cache
			String key = "1";
			String value = "Rahul";
			cache.put(key, value);

			// Modify Cache
			try (Transaction tx = ignite.transactions().txStart()) {
				cache.put(key, cache.get(key) + "******");
				//tx.commit();
			}
			
			
			// Modify Cache 
			/*try (Transaction tx = Ignition.ignite().transactions().txStart()) {
				cache.put(key, cache.get(key) + "******");
				tx.commit();
			}*/

			// Read Cache Entry
			System.out.println(cache.get(key));

		}

	}

}
