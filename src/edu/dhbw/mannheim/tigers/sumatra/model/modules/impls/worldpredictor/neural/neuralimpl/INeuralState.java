/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.04.2015
 * Author(s): KaiE
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;


/**
 * This class contains the state of the neural network.
 * 
 * @author KaiE
 */
public interface INeuralState
{
	
	/**
	 * this method creates.
	 * If the file exists it is loaded
	 * 
	 * @param filenameStub
	 */
	void loadNetwork(final String filenameStub);
	
	
	/**
	 * Saves the neural network to a file
	 * 
	 * @param filenameStub
	 */
	void saveNetwork(final String filenameStub);
	
	
	/**
	 * Method to update the state when a new cam detection frame is processed
	 * 
	 * @param newframe
	 */
	void updateState(final MergedCamDetectionFrame newframe);
	
	
	/**
	 * Trains on the stored data
	 */
	void trainNetworks();
	
	
	/**
	 * creates a prediction
	 */
	void performPrediction();
	
	
	/**
	 * Get the predicted Objects
	 * 
	 * @return
	 */
	Iterable<INeuralPredicitonData> getPredictedObjects();
	
	
	/**
	 * Resets the Status and discards the network
	 */
	void reset();
	
	
	/**
	 * Used to get the team color
	 * 
	 * @return
	 */
	ETeamColor getTeamColor();
	
	
}
