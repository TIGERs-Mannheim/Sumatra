/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.others.cheerings;

import java.util.List;

import edu.tigers.sumatra.ai.pandora.plays.others.CheeringPlay;
import edu.tigers.sumatra.math.vector.IVector2;


public interface ICheeringPlay
{
	void initialize(CheeringPlay play);
	
	
	boolean isDone();
	
	
	List<IVector2> calcPositions();
	
	
	void doUpdate();
	
	
	ECheeringPlays getType();
}
