/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trainer.server.manager;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import edu.tigers.sumatra.trainer.TrainingSet;


public class TrainingSetManager
{
	private ConcurrentLinkedQueue<TrainingSet> trainingSets = new ConcurrentLinkedQueue<>();
	private DelayQueue<AssignedTrainingSet> assignedTrainingSets = new DelayQueue<>();
	
	private static final long SEC_TO_NANO = 1_000_000;
	private static final int TIME_MULTIPLIER_TO_FINISH_TRAINING = 3;
	
	private static long trainingSetIDCounter = 0;
	
	
	TrainingSetManager()
	{
		for (int i = 1; i < 100; i++)
		{
			trainingSets.add(new TrainingSet(trainingSetIDCounter++, i));
		}
	}
	
	
	public synchronized void addTrainingSet(TrainingSet trainingSet)
	{
		trainingSet.setTrainingSetID(trainingSetIDCounter++);
		trainingSets.add(trainingSet);
	}
	
	
	public void addTrainingSets(Collection<TrainingSet> trainingSets)
	{
		trainingSets.forEach(this::addTrainingSet);
	}
	
	
	public synchronized TrainingSet acquireTrainingSet(long id)
	{
		if (assignedTrainingSets.stream().noneMatch(o -> o.getClientID() == id))
		{
			TrainingSet trainingSet = trainingSets.poll();
			if (trainingSet == null)
			{
				trainingSet = getExpiredAssignedTrainingSet();
			}
			if (trainingSet != null)
			{
				assignedTrainingSets.add(new AssignedTrainingSet(id, trainingSet,
						(long) trainingSet.getDuration() * TIME_MULTIPLIER_TO_FINISH_TRAINING * SEC_TO_NANO));
			}
		}
		AssignedTrainingSet assignedTrainingSet = getAssignedTrainingSet(id);
		return assignedTrainingSet != null ? assignedTrainingSet.getTrainingSet() : null;
	}
	
	
	public boolean releaseTrainingSet(long id, boolean finished)
	{
		AssignedTrainingSet assignedTrainingSet = getAssignedTrainingSet(id);
		if (assignedTrainingSets.stream().anyMatch(o -> o.getClientID() == id) && !finished
				&& assignedTrainingSet != null)
		{
			trainingSets.add(assignedTrainingSet.getTrainingSet());
		}
		return assignedTrainingSets.remove(assignedTrainingSet);
	}
	
	
	private TrainingSet getExpiredAssignedTrainingSet()
	{
		AssignedTrainingSet ast = assignedTrainingSets.poll();
		if (ast != null)
		{
			return ast.getTrainingSet();
			
		}
		return null;
	}
	
	
	private AssignedTrainingSet getAssignedTrainingSet(long id)
	{
		return assignedTrainingSets.stream().filter(ats -> ats.clientID == id)
				.findFirst().orElse(null);
	}
	
	
	public void reset()
	{
		trainingSets = new ConcurrentLinkedQueue<>();
		assignedTrainingSets = new DelayQueue<>();
	}
	
	private class AssignedTrainingSet implements Delayed
	{
		
		private long clientID;
		private TrainingSet trainingSet;
		private long startTime;
		
		
		public AssignedTrainingSet(long clientID, TrainingSet trainingSet, long delay)
		{
			this.clientID = clientID;
			this.trainingSet = trainingSet;
			this.startTime = System.currentTimeMillis() + delay;
		}
		
		
		@Override
		public long getDelay(TimeUnit unit)
		{
			long diff = startTime - System.currentTimeMillis();
			return unit.convert(diff, TimeUnit.MILLISECONDS);
		}
		
		
		@Override
		public int compareTo(Delayed o)
		{
			return Long.compare(this.startTime, ((AssignedTrainingSet) o).startTime);
		}
		
		
		public long getClientID()
		{
			return clientID;
		}
		
		
		public TrainingSet getTrainingSet()
		{
			return trainingSet;
		}
	}
}
