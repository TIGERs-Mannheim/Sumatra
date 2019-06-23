/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.scenario;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum ESimulationScenario implements IInstanceableEnum
{
	/**  */
	DEFAULT(new InstanceableClass(DefaultSimScenario.class)),
	/**  */
	TEST(new InstanceableClass(TestSimScenario.class)),
	/**  */
	REFEREE_STOP(new InstanceableClass(RefereeStopSimScenario.class)),
	/**  */
	REFEREE_DIRECT_KICK(new InstanceableClass(RefereeDirectKickSimScenario.class)),
	/**  */
	REFEREE_KICKOFF(new InstanceableClass(RefereeKickoffSimScenario.class));
	
	private final InstanceableClass	clazz;
	
	
	/**
	 */
	private ESimulationScenario(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}
	
	
	@Override
	public InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
}
