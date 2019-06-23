/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 3, 2015
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.learning.lcase;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public enum ELearningCase implements IInstanceableEnum
{
	
	/**  */
	BALL_ANALYZER(new InstanceableClass(BallAnalyzerLearningCase.class)),
	
	/**  */
	ROBOT_MOVEMENT(new InstanceableClass(RobotMovementLearningCase.class));
	
	
	private final InstanceableClass	clazz;
	
	
	@Override
	public InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
	
	
	/**
	 */
	private ELearningCase(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}
}
