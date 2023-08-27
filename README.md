# dot-collector

This repository details a project completed in Travis Ortogero's Artificial Intelligence Class at George School.

The goal of the project is to train "agents" (or sweepers, or dot-collectors, or whatever you wish to call them) to collect "good mines" and avoid "bad mines" in a 2D space. The agents are controlled by a neural network and they compete on the same 2D space to collect as many mines as possible in a fixed amount of time.


![image](https://github.com/ihop26/dot-collector/assets/128655862/23830daf-a8b3-45c2-a578-123345ab241a)


Watch the videos of them working here:

https://clipchamp.com/watch/zom98xap3rh: Gen 100 clip

https://clipchamp.com/watch/LOsXtk85yCS: Gen 0 clip

# Details of Classes:

## Params:
Stores important parameters that are used in other classes, alter at your own risk

## Agent MS
Contains a neural network and valuable data on its score as well as position and direction it is facing. Also calculates its own inputs to the neural network and responds to its outputs.

## NeuralNetwork
Generic neural network framework. Contains layers, neurons, connections, and biases. Has methods for changing weights as well as calculating its output. 

## Genetic Alg
The most interesting part of the program. Details how the next generation will be formed from the previous generation. Methods for choosing top performers as well as genetic crossover and mutation of weights/biases. Also computes statistics on each generation. Lots of randomness and interesting things to alter/try here.

## Controller MS
The brain of the operation, runs the simulation, loads/saves from files, connects all the previous classes together.

## Wrapper
Run this file to start the simulation. Errors may occur while trying to read from a file/save from a file, you can change file path in "ControllerMS" to something that works on your computer.

