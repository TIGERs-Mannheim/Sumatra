/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.behaviors.BreakthroughDefensive;
import edu.tigers.sumatra.ai.pandora.roles.support.behaviors.FakePassReceiver;
import edu.tigers.sumatra.ai.pandora.roles.support.behaviors.MoveOnVoronoi;
import edu.tigers.sumatra.ai.pandora.roles.support.behaviors.PassReceiver;
import edu.tigers.sumatra.ai.pandora.roles.support.behaviors.TestBehavior;
import edu.tigers.sumatra.ai.pandora.roles.support.behaviors.repulsive.RepulsiveAttacker;
import edu.tigers.sumatra.ai.pandora.roles.support.behaviors.repulsive.RepulsivePassReceiver;
import edu.tigers.sumatra.statemachine.IEvent;


public enum ESupportBehavior implements IInstanceableEnum, IEvent
{
	FAKE_PASS_RECEIVER(new InstanceableClass(FakePassReceiver.class,
			new InstanceableParameter(ARole.class, "", ""))),
	PASS_RECEIVER(new InstanceableClass(PassReceiver.class, new InstanceableParameter(ARole.class, "", ""))),
	BREAKTHROUGH_DEFENSIVE(
			new InstanceableClass(BreakthroughDefensive.class, new InstanceableParameter(ARole.class, "", ""))),
	REPULSIVE_PASS_RECEIVER(
			new InstanceableClass(RepulsivePassReceiver.class, new InstanceableParameter(ARole.class, "", ""))),
	REPULSIVE_ATTACKER(
			new InstanceableClass(RepulsiveAttacker.class, new InstanceableParameter(ARole.class, "", ""))),
	MOVE_VORONOI(new InstanceableClass(MoveOnVoronoi.class, new InstanceableParameter(ARole.class, "", ""))),
	TEST(new InstanceableClass(TestBehavior.class, new InstanceableParameter(ARole.class, "", "")));
	
	private final InstanceableClass clazz;
	
	
	ESupportBehavior(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}
	
	
	@Override
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
}
