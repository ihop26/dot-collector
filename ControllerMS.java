package neuralnet2;

import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import neuralnet2.GeneticAlg.Genome;

//specific to the minesweeping scenario, hence the MS at the end of the class name
//this runs the simulation
@SuppressWarnings("serial")
public class ControllerMS extends JPanel implements ActionListener {

	private Timer timer; // timer runs the simulation
	private int ticks; // tick counter for a run of a generation's agents
	private int numMines; // the number of mines to use in the simulator
	private int generations; // the counter for which generation the sim's on
	private int numAgents; // how many agents
	private int numWeights; // how many weights an agent has
	private int badcnt = 0;
	private int goodcnt = 0;
	private int decFit = 2;
//	private ArrayList<Double> avgFitness; 	//useful if you were plotting the progression of fitness
//	private ArrayList<Double> bestFitness;
	private GeneticAlg GA; // the genetic algorithm that manages the genome weights
	private ArrayList<Genome> pop; // the weights of the neural nets for each of the agents
	private ArrayList<AgentMS> agents; // the agents themselves (the sweepers)
	private ArrayList<Point2D> mines;
	private ArrayList<Point2D> badMines; // the mines
	private BufferedImage pic; // the image in which things are drawn
	private JLabel picLabel; // the label that holds the image
	private JLabel dataLabel; // the label that holds the fitness information

	// these are specific to the mine sweeping scenario
	// for the controller to run the whole simulation
	public static final int MINES = 500;
	public static final int SWEEPERS = 100;
	public static final int TICKS = 3000; // how long sweepers have a chance to gain fitness
	public static final double MINE_SIZE = 4;

	// for the mine sweepers
	public static final double MAX_TURN_RATE = 0.4; // how quickly they may turn
	public static final double MAX_SPEED = 1; // how fast they can go
	public static final int SCALE = 15; // the size of the sweepers

	public ControllerMS(int xDim, int yDim) {
		setBackground(Color.LIGHT_GRAY);
		setFocusable(true);
		setDoubleBuffered(true);
		// create the things to display, then add them
		pic = new BufferedImage(xDim, yDim, BufferedImage.TYPE_INT_RGB);
		picLabel = new JLabel(new ImageIcon(pic));
		dataLabel = new JLabel("Info");
		dataLabel.setFont(new Font("SansSerif", Font.BOLD, 9));
		add(picLabel);
		add(dataLabel);
		// initialize all of the variables!
		numMines = MINES;
		numAgents = SWEEPERS;
		ticks = 0;
		generations = 0;

		// make up agents and give weights
		makeAgents();


		// set up the mines
		mines = new ArrayList<Point2D>();
		Random rnd = new Random();
		for (int i = 0; i < numMines; i++) {
			mines.add(new Point2D.Double(rnd.nextDouble() * xDim, rnd.nextDouble() * yDim));
		}

		badMines = new ArrayList<Point2D>();
		for (int i = 0; i < numMines/4; i++) {
			badMines.add(new Point2D.Double(rnd.nextDouble() * xDim, rnd.nextDouble() * yDim));
		}
		// start it up!
		timer = new Timer(1, this);
		timer.start();
	}

	public void drawThings(Graphics2D g) {//courtesy of Travis Ortogero (George School instructor)
		// cover everything with a blank screen
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, pic.getWidth(), pic.getHeight());
		// draw agents
		for (AgentMS a : agents) {
			a.draw(g);
		}
		// draw mines
		g.setColor(Color.GREEN);
		for (Point2D m : mines) {
			g.fillRect((int) (m.getX() - MINE_SIZE / 2), (int) (m.getY() - MINE_SIZE / 2), (int) MINE_SIZE,
					(int) MINE_SIZE);
		}
		g.setColor(Color.RED);
		for (Point2D m : badMines) {
			g.fillRect((int) (m.getX() - MINE_SIZE / 2), (int) (m.getY() - MINE_SIZE / 2), (int) MINE_SIZE,
					(int) MINE_SIZE);
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	public void updateAgents() {
		ticks = (ticks + 1); // count ticks to set the length of the generation run
		if (ticks < TICKS) { // do another tick toward finishing a generation
			Random rnd = new Random();
			// update each agent by calling their update function and checking to see if
			// they got a mine
			for (int i = 0; i < numAgents; i++) {
				if (!agents.get(i).update(mines, badMines)) {
					System.out.println("Error: Wrong amount of neural net inputs.");
					break;
				}
				// did it find a mine
				// if it found a mine, add to that agent's fitness and make a new mine
				int foundBadMine = agents.get(i).checkForMine(badMines, MINE_SIZE, true);
				if (foundBadMine >= 0) {
					agents.get(i).decreaseFitness(decFit);

					badcnt++;
					badMines.set(foundBadMine,
							new Point2D.Double(rnd.nextDouble() * pic.getWidth(), rnd.nextDouble() * pic.getHeight()));
				}
				int foundMine = agents.get(i).checkForMine(mines, MINE_SIZE, false);
				if (foundMine >= 0) {
					agents.get(i).incrementFitness();
					goodcnt++;
					mines.set(foundMine,
							new Point2D.Double(rnd.nextDouble() * pic.getWidth(), rnd.nextDouble() * pic.getHeight()));
				}
				// keep track of that agent's fitness in the GA as well as the NN
				pop.get(i).setFitness(agents.get(i).getFitness());
			}
		} else { // a generation has completed, run the genetic algorithm and update the agents
			GA.calculateBestWorstAvgTot();
			mines.clear();
			badMines.clear();
			Random rnd = new Random();
			for (int i = 0; i < numMines; i++) {
				mines.add(new Point2D.Double(rnd.nextDouble() * pic.getWidth(), rnd.nextDouble() * pic.getHeight()));
			}
			for (int i = 0; i < numMines/4; i++) {
				badMines.add(new Point2D.Double(rnd.nextDouble() * pic.getWidth(), rnd.nextDouble() * pic.getHeight()));
			}
			generations++;
			dataLabel.setText("Previous generation " + generations + ":  Avg. fitness of " + GA.avgFitness()
					+ ".  Best fitness of " + GA.bestFitness() + ".");
			System.out.println("Previous generation " + generations + ": Avg. fitness of " + GA.avgFitness()
					+ ". Best fitness of " + GA.bestFitness() + "." + " badmines collected: " + badcnt + " good mines collected:  "+goodcnt);
			ticks = 0;
			badcnt = 0;
			goodcnt = 0;
			pop = GA.epoch(pop); // the big genetic algorithm process line
			for (int i = 0; i < numAgents; i++) { // give the agents all the new weights information
				agents.get(i).putWeights(pop.get(i).getWeights());
				agents.get(i).reset();
			}
		}
	}

	public void save() {
		try {
			File myObj = new File("C:\\Users\\Ian\\Desktop\\saves\\save.txt");
			if (myObj.createNewFile()) {
				System.out.println("File created: " + myObj.getName());
			} else {
				System.out.println("File already exists.");
			}
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

		try {
			FileWriter myWriter = new FileWriter("C:\\Users\\Ian\\Desktop\\saves\\save.txt");

			myWriter.write(generations + " " + numWeights + "\n");
			for (int x = 0; x < agents.size(); x++) {
				for (int i = 0; i < agents.get(x).getNumberOfWeights(); i++) {
					myWriter.append("" + agents.get(x).brain.getWeights().get(i) + "\n");
				}
			}

			myWriter.close();
			System.out.println("Successfully wrote to the file.");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	// @Override
	public void actionPerformed(ActionEvent e) {
		updateAgents();
		drawThings((Graphics2D) pic.getGraphics());
		repaint();
	}

	public void makeAgents() {
		try {
			File myObj = new File("C:\\Users\\Ian\\Desktop\\saves\\save.txt");//see if there is a save file
			Scanner myReader = new Scanner(myObj);
			String data = myReader.nextLine();
			String gencount = "";
			String numweights = "";
			for (int x = 0; x < data.length(); x++) {//read in metadata
				if (data.charAt(x) != ' ') {
					gencount += data.charAt(x);
				} else {
					numweights = data.substring(x + 1);
					break;
				}
			}
			generations = Integer.parseInt(gencount);
			numWeights = Integer.parseInt(numweights);

			System.out.println("Save found; Generation:" + generations + " Number of Weights per Agent:" + numWeights);
			//read in all the weights of the neural nets
			double[][] weights = new double[numAgents][numWeights];
			for (int x = 0; x < numAgents; x++) {
				for (int y = 0; y < numWeights; y++) {
					data = myReader.nextLine();
					weights[x][y] = Double.parseDouble(data);
				}
			}

			agents = new ArrayList<AgentMS>(numAgents);
			for (int i = 0; i < numAgents; i++) {// make agents based on read in data
				agents.add(new AgentMS());
			}
			GA = new GeneticAlg(numAgents, Params.MUTATION_RATE, Params.CROSSOVER_RATE, numWeights, weights);// set weights
			pop = GA.getChromosomes();
			for (int i = 0; i < numAgents; i++) {
				agents.get(i).putWeights(pop.get(i).getWeights());// put weights in agents
			}

			// System.out.println(data);

			myReader.close();
		} catch (FileNotFoundException e) { // create new default / from scratch
			System.out.println("Save not found, creating new agents");
			agents = new ArrayList<AgentMS>(numAgents);
			for (int i = 0; i < numAgents; i++) {
				agents.add(new AgentMS());
			}
			numWeights = agents.get(0).getNumberOfWeights();
			GA = new GeneticAlg(numAgents, Params.MUTATION_RATE, Params.CROSSOVER_RATE, numWeights);
			pop = GA.getChromosomes();
			for (int i = 0; i < numAgents; i++) {
				agents.get(i).putWeights(pop.get(i).getWeights());
			}
		}

	}
	
	public void windowClosing(WindowEvent e) {
        //save data before exiting
		System.out.println("closing");
       save();
       //end program
       System.exit(0);
    }

}
