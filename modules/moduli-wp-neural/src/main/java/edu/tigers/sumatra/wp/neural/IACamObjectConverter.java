/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.12.2014
 * Author(s): KaiE
 * *********************************************************
 */
package edu.tigers.sumatra.wp.neural;

import java.util.Collection;

import edu.tigers.sumatra.cam.data.ACamObject;


/**
 * This interface is for the .train() Method of the {@link NeuralNetworkImpl}.
 * 
 * @author KaiE
 */
public interface IACamObjectConverter
{
	
	
	/**
	 * It is used to generate an array of double values from the last n camDetectionFrames.
	 * For frames.size() should be NeuralWPConfigs.LastNFrames;
	 * 
	 * @param data the ACamObject's that should be used for the input-layer
	 * @param lookahead the time in ms that should be used as the lookahead for the prediction
	 * @return double[] for the train method
	 */
	double[] convertInput(Collection<ACamObject> data, double lookahead);
	
	
	/**
	 * This Method is used to convert the data as a post-processing step from the neural Network to usable data
	 * returns an array containing
	 * position (x,y);
	 * velocity (x,y);
	 * orientation (degree);[Robots only]
	 * orientVel (degree); [Robots only]
	 * 
	 * @param dataToConvert
	 * @param newest
	 * @return
	 */
	double[] convertOutput(double[] dataToConvert, ACamObject newest);
	
	
	/**
	 * generates the ideal data output for the training
	 * 
	 * @param frames
	 * @param referenceItem
	 * @param timestep in ms
	 * @return
	 * @throws RuntimeException
	 */
	double[] createIdealOutput(Collection<ACamObject> frames, final ACamObject referenceItem, final double timestep);
}
