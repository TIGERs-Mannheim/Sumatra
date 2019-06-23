/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.EBallHandlingEvent;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.statemachine.IStateMachine;


public class AttackerRoleTest
{
	@SuppressWarnings("unchecked")
	@Test
	public void allEventsUsed()
	{
		AttackerRole role = new AttackerRole();
		IStateMachine<IState> stateMachine = (IStateMachine<IState>) Whitebox.getInternalState(role, "stateMachine");
		Map<IEvent, Map<IState, IState>> transitions = (Map<IEvent, Map<IState, IState>>) Whitebox
				.getInternalState(stateMachine, "transitions");
		
		for (EBallHandlingEvent event : EBallHandlingEvent.values())
		{
			assertThat(transitions.keySet()).contains(event);
		}
	}
}