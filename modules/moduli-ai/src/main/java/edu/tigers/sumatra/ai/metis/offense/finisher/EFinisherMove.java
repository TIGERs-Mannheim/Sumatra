/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.finisher;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;

import edu.tigers.sumatra.ai.metis.offense.finisher.AFinisherMoveCharlieOne.FinisherMoveCharlieOneLeft;
import edu.tigers.sumatra.ai.metis.offense.finisher.AFinisherMoveCharlieOne.FinisherMoveCharlieOneRight;
import edu.tigers.sumatra.ai.metis.offense.finisher.AFinisherMoveSierraOne.FinisherMoveSierraOneLeft;
import edu.tigers.sumatra.ai.metis.offense.finisher.AFinisherMoveSierraOne.FinisherMoveSierraOneRight;


public enum EFinisherMove implements IInstanceableEnum
{
	SIERRA_ONE_LEFT(new InstanceableClass(FinisherMoveSierraOneLeft.class)),
	
	SIERRA_ONE_RIGHT(new InstanceableClass(FinisherMoveSierraOneRight.class)),
	
	CHARLIE_ONE_LEFT(new InstanceableClass(FinisherMoveCharlieOneLeft.class)),
	
	CHARLIE_ONE_RIGHT(new InstanceableClass(FinisherMoveCharlieOneRight.class)),
	;
	
	
	private final InstanceableClass clazz;
	
	
	EFinisherMove(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}
	
	
	@Override
	public InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
}
