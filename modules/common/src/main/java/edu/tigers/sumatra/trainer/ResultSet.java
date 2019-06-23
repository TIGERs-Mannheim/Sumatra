/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trainer;

import java.util.ArrayList;
import java.util.List;


public class ResultSet
{
	private TrainingSet trainingSet;
	private long clientID = -1;
	private List<Result> results = new ArrayList<>();
	
	
	public List<Result> getResults()
	{
		return results;
	}
	
	
	public void setResults(final List<Result> results)
	{
		this.results = results;
	}
	
	
	public TrainingSet getTrainingSet()
	{
		return trainingSet;
	}
	
	
	public void setTrainingSet(final TrainingSet trainingSet)
	{
		this.trainingSet = trainingSet;
	}
	
	
	public long getClientID()
	{
		return clientID;
	}
	
	
	public void setClientID(final long clientID)
	{
		this.clientID = clientID;
	}
}
