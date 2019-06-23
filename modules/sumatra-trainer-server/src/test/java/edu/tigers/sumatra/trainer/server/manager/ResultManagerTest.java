/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trainer.server.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.tigers.sumatra.trainer.Result;
import edu.tigers.sumatra.trainer.ResultSet;
import edu.tigers.sumatra.trainer.TrainingSet;


public class ResultManagerTest
{
	
	ResultManager manager = ManagerRegistry.getResultManager();
	
	
	@Test
	public void acceptResultTest() throws Exception
	{
		List<Result> results = new ArrayList<>();
		results.add(new Result("test", "test value"));
		TrainingSet trainingSet = new TrainingSet(0, 1);
		ResultSet resultSet = new ResultSet();
		resultSet.setResults(results);
		resultSet.setTrainingSet(trainingSet);
		assertThat(manager.acceptResult(resultSet)).isTrue();
		assertThat(manager.acceptResult(resultSet)).isFalse();
		
		assertThat(Files.exists(Paths.get("data/test"))).isTrue();
		Files.delete(Paths.get("data/test"));
	}
	
}