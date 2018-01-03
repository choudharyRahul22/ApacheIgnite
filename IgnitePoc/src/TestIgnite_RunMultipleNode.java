import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;

public class TestIgnite_RunMultipleNode {

	public static void main(String[] args) throws InterruptedException{
		
		/*  This class defines grid runtime configuration. 
			This configuration is passed to Ignition.start(IgniteConfiguration) method
		*/
		IgniteConfiguration config1 = new IgniteConfiguration();
		config1.setIgniteInstanceName("Node-1");
		Ignite ignite1 = Ignition.start(config1);

		IgniteConfiguration config2 = new IgniteConfiguration();
		config2.setIgniteInstanceName("Node-2");
		Ignite ignite2 = Ignition.start(config2);
		
		ignite1.compute().broadcast(() -> System.out.println("Hello From ignite1"));
		
		ignite2.compute().broadcast(() -> System.out.println("Hello From ignite2"));

	}

}
