/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trainer;

public class TrainingSet
{
	private long trainingSetID;
	
	private double duration;
	
	
	public TrainingSet()
	{
		
	}
	
	public TrainingSet(long trainingSetID, double duration)
	{
		this.trainingSetID = trainingSetID;
		this.duration = duration;
	}
	
	
	public long getTrainingSetID()
	{
		return trainingSetID;
	}
	
	
	public void setTrainingSetID(final long trainingSetID)
	{
		this.trainingSetID = trainingSetID;
	}
	
	
	public double getDuration()
	{
		return duration;
	}
	
	
	public void setDuration(final double duration)
	{
		this.duration = duration;
	}
}
