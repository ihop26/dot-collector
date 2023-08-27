package neuralnet2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

//you'd write a different agent class for something other than this particular approach to minesweeping (or something other than minesweeping)
public class AgentMS {
	public NeuralNetwork brain; // each agent has a brain (neural net)
	private Point2D position; // where the agent is on the map
	private Point2D facing; // which way they're facing (used as inputs) as an (x, y) pair
	private double rotation; // the angle from which facing is calculated
	private double speed; // the speed of the agent
	private double lTrack, rTrack; // the influence rating toward turning left and turning right, used as outputs
	private double fitness; // how well the agent is doing, quantified (for the genetic algorithm)
	private double scale; // the size of the agent
	private int closestMine; // the index in the mines list of the mine closest to the agent (used to
								// determine inputs for the neural net)
	private int secondClosestMine;
	private int closestBadMine;

	public AgentMS() { // initialization
		Random rnd = new Random();
		brain = new NeuralNetwork(Params.INPUTS, Params.OUTPUTS, Params.HIDDEN, Params.NEURONS_PER_HIDDEN);
		rotation = rnd.nextDouble() * Math.PI * 2;
		lTrack = 0.16;
		rTrack = 0.16;
		fitness = 0;
		scale = ControllerMS.SCALE;
		closestMine = 0;
		position = new Point2D.Double(rnd.nextDouble() * Params.WIN_WIDTH, rnd.nextDouble() * Params.WIN_HEIGHT);
		facing = new Point2D.Double(-Math.sin(rotation), Math.cos(rotation));
	}

	public boolean update(ArrayList<Point2D> mines, ArrayList<Point2D> badMines) { // updates all the parameters of the
																					// sweeper
		ArrayList<Double> inputs = new ArrayList<Double>();
		// find the closest mine, figure out the direction the mine is from the
		// sweeper's perspective by creating a unit vector

		getClosestMines(badMines, true);
		double bx = (badMines.get(closestBadMine).getX() - position.getX())//find x distance to closest bad mine
				/ position.distance(badMines.get(closestBadMine));
		double by = (badMines.get(closestBadMine).getY() - position.getY())//find y distance to closest bad mine
				/ position.distance(badMines.get(closestBadMine));
		inputs.add(bx);
		inputs.add(by);//add the bad mines to inputs
		//do the same for good mines
		getClosestMines(mines, false);
		double x = (mines.get(closestMine).getX() - position.getX()) / position.distance(mines.get(closestMine));
		double y = (mines.get(closestMine).getY() - position.getY()) / position.distance(mines.get(closestMine));
		inputs.add(x);
		inputs.add(y);
		
		x = (mines.get(secondClosestMine).getX() - position.getX()) / position.distance(mines.get(secondClosestMine));
		y = (mines.get(secondClosestMine).getY() - position.getY()) / position.distance(mines.get(secondClosestMine));
		inputs.add(x);
		inputs.add(y);
		
		double distance = 0;
		if(position.distance(mines.get(closestMine))<=position.distance(badMines.get(closestBadMine))) {
			distance = .5;
		}else {
			distance = -.5;
		}
		inputs.add(distance);

		inputs.add(facing.getX());
		inputs.add(facing.getY());
		//inputs.add(reldistance);

		// get outputs from the sweeper's brain
		ArrayList<Double> output = brain.Update(inputs);
		if (output.size() < Params.OUTPUTS) {
			System.out.println("Incorrect number of outputs.");
			return false; // something went really wrong if this happens
		}

		// turn left or turn right?
		lTrack = output.get(0);
		rTrack = output.get(1);
		double rotationForce = lTrack - rTrack;
		rotationForce = Math.min(ControllerMS.MAX_TURN_RATE, Math.max(rotationForce, -ControllerMS.MAX_TURN_RATE)); // clamp
																													// between
																													// lower
																													// and
																													// upper
																													// bounds
		rotation += rotationForce;

		// update the speed and direction of the sweeper
		speed = Math.min(ControllerMS.MAX_SPEED, lTrack + rTrack);
		facing.setLocation(-Math.sin(rotation), Math.cos(rotation));

		// then update the position, torus style
		double xPos = (Params.WIN_WIDTH + position.getX() + facing.getX() * speed) % Params.WIN_WIDTH;
		double yPos = (Params.WIN_HEIGHT + position.getY() + facing.getY() * speed) % Params.WIN_HEIGHT;
		position.setLocation(xPos, yPos);
		return true;
	}

	public Point2D getClosestMines(ArrayList<Point2D> mines, boolean bad) { // finds the mine closest to the sweeper
		double closestSoFar = 99999999;
		Point2D closestObject = new Point2D.Double(0, 0);
		double length;
		for (int i = 0; i < mines.size(); i++) {
			length = Point2D.distanceSq(mines.get(i).getX(), mines.get(i).getY(), position.getX(), position.getY());
			if (length < closestSoFar) {
				closestSoFar = length;
				closestObject = new Point2D.Double(position.getX() - mines.get(i).getX(),
						position.getY() - mines.get(i).getY());
				if (bad) {
					closestBadMine = i;
				} else {
					secondClosestMine = closestMine;
					closestMine = i;
				}
			}
		}
		return closestObject;
	}

	public int checkForMine(ArrayList<Point2D> mines, double size, boolean bad) { // has the sweeper actually swept up																			// the closest mine to it this tick?
		if (bad) {
			if (Point2D.distance(position.getX(), position.getY(), mines.get(closestBadMine).getX(),mines.get(closestBadMine).getY()) < (size + scale / 2)) {
				return closestBadMine;
			}
			return -1;
		} else {
			if (Point2D.distance(position.getX(), position.getY(), mines.get(closestMine).getX(),mines.get(closestMine).getY()) < (size + scale / 2)) {
				return closestMine;
			}
			return -1;
		}
	}

	public void reset() { // reinitialize this sweeper's position/direction values
		Random rnd = new Random();
		rotation = rnd.nextDouble() * Math.PI * 2;
		position = new Point2D.Double(rnd.nextDouble() * Params.WIN_WIDTH, rnd.nextDouble() * Params.WIN_HEIGHT);
		facing = new Point2D.Double(-Math.sin(rotation), Math.cos(rotation));
		fitness = 0;
	}

	public void draw(Graphics2D g) { // draw the sweeper in its correct place
		AffineTransform at = g.getTransform(); // affine transforms are a neat application of matrix algebra
		g.rotate(rotation, position.getX(), position.getY()); // they allow you to rotate a g.draw kind of function's
																// output
		// draw the sweeper using a fancy color scheme
		g.setColor(new Color(255, 255, 0));
		g.drawRect((int) (position.getX() - scale / 2), (int) (position.getY() - scale / 2), (int) scale, (int) scale);
		if (fitness >= 0) {
			g.setColor(new Color(0, Math.min(255, 15 + (int) fitness * 12), Math.min(255, 15 + (int) fitness * 12)));
		} else {
			g.setColor(new Color(Math.min(255, 15 - (int) fitness * 12), 0, 0));
		}
		g.fillRect((int) (position.getX() - scale / 2) + 1, (int) (position.getY() - scale / 2) + 1, (int) scale - 2,
				(int) scale - 2);

		// draw the direction it's facing
		g.setTransform(at); // set the transform back to the normal transform
		g.setColor(new Color(255, 0, 255));
		g.drawLine((int) (position.getX()), (int) (position.getY()), (int) (position.getX() + facing.getX() * scale),
				(int) (position.getY() + facing.getY() * scale));

		// draw its fitness
		g.setColor(new Color(0, 255, 255));
		g.drawString("" + fitness, (int) position.getX() - (int) (scale / 2), (int) position.getY() + 2 * (int) scale);

		// you're welcome to alter the drawing, I just wanted something simple and
		// quasi-functional
	}

	// simple functions
	public Point2D getPos() {
		return position;
	}

	public void incrementFitness() {
		fitness++;
	} // this may need to get more elaborate pending what you would want sweepers to
		// learn...

	public void decreaseFitness(int decFit) {
		fitness-=decFit;
	}

	public double getFitness() {
		return fitness;
	}

	public void putWeights(ArrayList<Double> w) {
		brain.replaceWeights(w);
	}

	public int getNumberOfWeights() {
		return brain.getNumberOfWeights();
	}
}
