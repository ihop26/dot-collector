package neuralnet2;

public class Params { 
	//general parameters
	public static final int WIN_WIDTH = Wrapper.FRAMESIZE*3;			//width of world map
	public static final int WIN_HEIGHT = Wrapper.FRAMESIZE*3/2;			//height of world map
	public static final int FPS = 60;								//frames per second for drawing...not used

	//for the neural network
	public static final int INPUTS = 9;					//number of inputs
	public static final int HIDDEN = 2;					//number of hidden layers
	public static final int NEURONS_PER_HIDDEN = 10; 	//number of neurons in each hidden layer
	public static final int OUTPUTS = 2;				//number of outputs
	public static final double BIAS =-1;				//the threshold (bias) value
	public static final double ACT_RESPONSE = 1;		//adjusts the sigmoid function
	
	//for the genetic algorithm 
	public static final double CROSSOVER_RATE = 0.3;	//the chance of crossover happening
	public static final double MUTATION_RATE = 0.3;		//the chance of a particular value in a genome changing
	public static final double MAX_PERTURBATION = 0.3;	//maximum magnitude of the delta from mutation
	public static final int NUM_ELITE = 8;				//how many of the top performers advance to the next generation
	public static final int NUM_COPIES_ELITE = 1;		//and how many copies of those performers we'll use

}
