/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trainer.server.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.tigers.sumatra.trainer.TrainingSet;


public class TrainingSetManagerTest
{

	
	@Test
	public void addTrainingSet()
	{
		TrainingSetManager manager = new TrainingSetManager();
		manager.reset();
		TrainingSet trainingSet = manager.acquireTrainingSet(0);
		assertThat(trainingSet).isNull();
		
		fillTrainingSetManager(manager, 10);
		
		trainingSet = manager.acquireTrainingSet(0);
		assertThat(trainingSet).isNotNull();
		
		TrainingSet trainingSet2 = manager.acquireTrainingSet(1);
		assertThat(trainingSet2).isNotNull();
		assertThat(trainingSet).isNotEqualTo(trainingSet2);
	}
	
	
	@Test
	public void acquireTrainingSet()
	{
		TrainingSetManager manager = new TrainingSetManager();
		fillTrainingSetManager(manager, 10);
		TrainingSet set1 = manager.acquireTrainingSet(0);
		TrainingSet set2 = manager.acquireTrainingSet(0);
		assertThat(set1).isEqualTo(set2);
		
		set2 = manager.acquireTrainingSet(1);
		assertThat(set1).isNotEqualTo(set2);
	}
	
	
	@Test
	public void releaseTrainingSet()
	{
		TrainingSetManager manager = ManagerRegistry.getTrainingSetManager();
		manager.reset();
		fillTrainingSetManager(manager, 2);
		TrainingSet trainingSet = manager.acquireTrainingSet(0);
		assertThat(trainingSet).isNotNull();
		
		manager.releaseTrainingSet(0, true);
		TrainingSet trainingSet2 = manager.acquireTrainingSet(0);
		assertThat(trainingSet).isNotEqualTo(trainingSet2);
		
		assertThat(manager.releaseTrainingSet(0, false)).isTrue();
		TrainingSet trainingSet3 = manager.acquireTrainingSet(0);
		assertThat(trainingSet2).isEqualTo(trainingSet3);
	}
	
	
	private void fillTrainingSetManager(TrainingSetManager manager, int numberOfTrainingSets)
	{
		List<TrainingSet> trainingSets = new ArrayList<>();
		for (int i = 0; i < numberOfTrainingSets; i++)
		{
			trainingSets.add(new TrainingSet(i, i));
		}
		manager.addTrainingSets(trainingSets);
	}
	
}