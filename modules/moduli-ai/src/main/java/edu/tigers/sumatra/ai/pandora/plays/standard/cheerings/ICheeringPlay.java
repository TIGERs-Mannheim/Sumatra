package edu.tigers.sumatra.ai.pandora.plays.standard.cheerings;

import edu.tigers.sumatra.ai.pandora.plays.standard.CheeringPlay;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.List;


public interface ICheeringPlay
{
	void initialize(CheeringPlay play);


	boolean isDone();


	List<IVector2> calcPositions();


	void doUpdate();


	ECheeringPlays getType();

	default int requiredNumRobots()
	{
		return 3;
	}
}
