import java.io.Serializable;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.transactions.Transaction;

public class TestIgnite_Key_Value implements Serializable {

	public static void main(String[] args) throws InterruptedException {

		try (Ignite ignite = Ignition.start()) {

			// Creating Cache
			IgniteCache<String, String> cache = ignite.getOrCreateCache("cache1");

			// Putting Inside Cache
			String key = "1";
			String value = "Rahul";
			cache.put(key, value);

			// Modify Cache
			cache.put(key, cache.get(key) + " Choudhary");

			// Read Cache Entry
			System.out.println(cache.get(key));

		}

	}

}
