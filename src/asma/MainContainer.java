package asma;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;


public class MainContainer {

	Runtime rt;
	ContainerController container;

	public ContainerController initContainerInPlatform(String host, String port, String containerName) {
		// Get the JADE runtime interface (singleton)
		this.rt = Runtime.instance();

		// Create a Profile, where the launch arguments are stored
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, containerName);
		profile.setParameter(Profile.MAIN_HOST, host);
		profile.setParameter(Profile.MAIN_PORT, port);
		// create a non-main agent container
		ContainerController container = rt.createAgentContainer(profile);
		return container;
	}

	public void initMainContainerInPlatform(String host, String port, String containerName) {

		// Get the JADE runtime interface (singleton)
		this.rt = Runtime.instance();

		// Create a Profile, where the launch arguments are stored
		Profile prof = new ProfileImpl();
		prof.setParameter(Profile.CONTAINER_NAME, containerName);
		prof.setParameter(Profile.MAIN_HOST, host);
		prof.setParameter(Profile.MAIN_PORT, port);
		prof.setParameter(Profile.MAIN, "true");
		prof.setParameter(Profile.GUI, "true");

		// create a main agent container
		this.container = rt.createMainContainer(prof);
		rt.setCloseVM(true);

	}

	public void startAgentInPlatform(String name, String classpath, Object[] args) {
		try {
			AgentController ac = container.createNewAgent(name, classpath, args);
			ac.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		MainContainer a = new MainContainer();

		try {

			a.initMainContainerInPlatform("localhost", "8888", "MainContainer");

			// First Open Central Agent - Manager
			a.startAgentInPlatform("manager", "agents.manager", new String[0]);

			// Provide some time for Agent to register in services

			Thread.sleep(500);


			a.startAgentInPlatform("gameManager1", "agents.gameManager", new String[0]);
			a.startAgentInPlatform("gameManager2", "agents.gameManager", new String[0]);
			
			Thread.sleep(1000);
			
			for (int n = 0; n < 10; n++) {
				a.startAgentInPlatform("piece" + n, "agents.piece", new String[0]);
			}
			
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
