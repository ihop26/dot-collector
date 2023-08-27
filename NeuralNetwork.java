package neuralnet2;

import java.util.ArrayList;
import java.util.Random;

//as general a description of a neural network as possible so that it can be used in any NN scenario
public class NeuralNetwork {								

	//a subclass defining a neuron
	class Neuron {
		private int numInputs;								//each neuron takes in inputs
		ArrayList<Double> weights;							//whose significance is modified by a weight
		
		public Neuron(int inputs) {
			numInputs = inputs + 1;	
			//System.out.print(numInputs);//one extra for the threshold value
			weights = new ArrayList<Double>(numInputs);
			Random rnd = new Random();
			for (int i = 0; i < numInputs; i++) {			//randomized weight initialization from -1 to 1
				weights.add(rnd.nextDouble()*2.0 - 1);
			}
		}
	}
	
	//a subclass defining a layer of neurons in a network
	class NeuronLayer {
		private int numNeurons;								//a layer consists of at least one neuron
		ArrayList<Neuron> neurons;							//the neurons of the layer
		
		public NeuronLayer(int neuronCount, int inputsPerNeuron) {
			numNeurons = neuronCount;
			neurons = new ArrayList<Neuron>(numNeurons);
			for (int i = 0; i < neuronCount; i++) {			//randomized neuron initialization
				neurons.add(new Neuron(inputsPerNeuron));
			}
		}
	}
	
	private int numInputs;									//a neural net takes in a set of inputs
	private int numOutputs;									//and delivers a set of outputs
	private int numHiddenLayers;							//between these inputs and outputs are 'hidden' layers of neurons
	private int numNeuronsPerHiddenLayer;					//which may have many neurons to create the many synaptic connections
	private ArrayList<NeuronLayer> layers;
	
	//initialization/creation of a network given the parameters defining the size of the network
	public NeuralNetwork(int numIn, int numOut, int numHidden, int numNeuronPerHidden) {
		numInputs = numIn;
		numOutputs = numOut;
		numHiddenLayers = numHidden;
		numNeuronsPerHiddenLayer = numNeuronPerHidden;
		layers = new ArrayList<NeuronLayer>();
		createNet();
	}
	
	public void createNet() {
		//create layers of the network
		if (numHiddenLayers > 0) {
			//add a new layer to Layers connecting the inputs to the first hidden network if one exists
			layers.add(new NeuronLayer(numNeuronsPerHiddenLayer, numInputs));
			for (int i = 0; i < numHiddenLayers - 1; i++) {						//for the hidden middle layers, one hidden layer to the next
				layers.add(new NeuronLayer(numNeuronsPerHiddenLayer,layers.get(i).numNeurons));
			}
			layers.add(new NeuronLayer(numOutputs,layers.get(layers.size()-1).numNeurons));
			//one last layer to connect the last hidden layer to the outputs
		} else {
			layers.add(new NeuronLayer(numOutputs, numInputs));					//if there's no hidden layers, just one layer with inputs and outputs
		}
	}

	//idea for these methods: read through the neural net layer by layer and append all of them into one long weights ArrayList
	public ArrayList<Double> getWeights() { //gets the weights from the network and turns it into a simple list
		ArrayList<Double> weights = new ArrayList<Double>();
		//for each weight in each neuron in each layer
		for (NeuronLayer l : layers) {
			for (int j = 0; j < l.numNeurons; j++) {
				for (int k = 0; k < l.neurons.get(j).numInputs; k++) {
					weights.add(l.neurons.get(j).weights.get(k));
				}
			}
		}
		return weights;
	}
	
	public int getNumberOfWeights() { //returns total number of weights in the whole network, if you need it
		return getWeights().size();
	}
	
	public void replaceWeights(ArrayList<Double> newWeights) { //...replaces weights given an input ArrayList
		int cWeight = 0; //index to walk through newWeights
		for(NeuronLayer l : layers) {
			for(int j = 0; j<l.numNeurons; j++) {
				for(int k = 0; k<l.neurons.get(j).numInputs; k++) {
					l.neurons.get(j).weights.set(k,newWeights.get(cWeight));
					cWeight++;
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Double> Update(ArrayList<Double> inputs) { //takes the inputs and computes the outputs having run through the neural net layer
		ArrayList<Double> outputs = new ArrayList<Double>(numOutputs);
		double netInput = 0;
		if (inputs.size() != numInputs) {
			System.out.println("uh oh");
			return outputs;		//empty outputs if incorrect number of inputs
		}
		for (int i = 0; i < numHiddenLayers + 1; i++) { //for each layer
			if (i > 0) {
				inputs = (ArrayList<Double>) outputs.clone(); //make the new inputs be the outputs from the previous iteration of the loop
			}
			outputs.clear();
			//an indexing variable
			//for each neuron in that layer
			for(int x = 0; x<layers.get(i).neurons.size(); x++) {
				 //for each input-weight combo in that neuron
				netInput = 0;
				int neuronInputs = layers.get(i).neurons.get(x).weights.size()-1;
				for(int y = 0; y<neuronInputs; y++) {
					netInput += inputs.get(y)*layers.get(i).neurons.get(x).weights.get(y);
				}
				netInput += layers.get(i).neurons.get(x).weights.get(neuronInputs)*Params.BIAS;
				outputs.add(sigmoid(netInput, Params.ACT_RESPONSE));
			}

			  //do the summation of input*weight (called the activation value)

				//the output of the neuron is then dependent upon the activation exceeding a threshold value stored as the bias
				//the bias is stored as the last, extra 'weight' at the end of the weights ArrayList
				//netInput += layers.get(i).neurons.get(j).weights.get(neuronInputs - 1)*Params.BIAS; 
				//outputs.add(sigmoid(netInput, Params.ACT_RESPONSE)); //scale the activation using a sigmoid function 
		}
		return outputs;
	}
	
	public double sigmoid(double activation, double response) { //the sigmoid function returns a value between 0 and 1, <0.5 for negative inputs, >0.5 for positive inputs
		return 1.0 / (1.0 + Math.exp(-activation / response));
	}
}
