import java.io.Serializable;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

public class TestIgnite implements Serializable {

	public static void main(String[] args) throws InterruptedException {

		// Ignite start will start a new node which will join the cluster nodes
		try (Ignite ignite = Ignition.start()) {
			
			// will broadcast this on all nodes
			ignite.compute().broadcast(() -> System.out.println("Hi Rahul"));
			
			// once done will leave the cluster
		}

	}

}
