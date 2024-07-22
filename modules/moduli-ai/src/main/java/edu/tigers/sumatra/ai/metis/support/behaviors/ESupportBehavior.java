/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import edu.tigers.sumatra.ai.metis.support.behaviors.repulsive.AttackerRepulsiveBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.repulsive.MidfieldRepulsiveBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.repulsive.PassReceiverRepulsiveBehavior;
import edu.tigers.sumatra.statemachine.IEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ESupportBehavior implements IInstanceableEnum, IEvent
{
	DIRECT_REDIRECTOR(new InstanceableClass<>(DirectRedirectorSupportBehavior.class)),
	FAKE_PASS_RECEIVER(new InstanceableClass<>(FakePassReceiverSupportBehavior.class)),
	PENALTY_AREA_ATTACKER(new InstanceableClass<>(PenaltyAreaAttackerBehavior.class)),
	BREAKTHROUGH_DEFENSIVE(new InstanceableClass<>(BreakThroughDefenseBehavior.class)),
	MIDFIELD(new InstanceableClass<>(MidfieldRepulsiveBehavior.class)),
	KICKOFF(new InstanceableClass<>(KickoffSupportBehavior.class)),
	MAN_2_MAN_MARKING(new InstanceableClass<>(AggressiveMan2ManMarkerBehavior.class)),
	REPULSIVE_PASS_RECEIVER(new InstanceableClass<>(PassReceiverRepulsiveBehavior.class)),
	REPULSIVE_ATTACKER(new InstanceableClass<>(AttackerRepulsiveBehavior.class)),
	;

	private final InstanceableClass<?> instanceableClass;


	@Override
	public IInstanceableEnum parse(String value)
	{
		return valueOf(value);
	}
}
