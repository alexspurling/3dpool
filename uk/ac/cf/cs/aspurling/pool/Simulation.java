package uk.ac.cf.cs.aspurling.pool;

public abstract class Simulation {
	
	public GLWindow window;
	
	public Simulation(GLWindow window) {
		this.window = window;
	}
	
	//Updates the simulation state
	//dt is the time step
	public abstract void update(float dt);
	
	//Renders the simulation
	public abstract void render();
	
	//Process keyboard and mouse input
	public abstract void processInput();
	
	//Shutdown sim
	public abstract void shutdown();

}
