/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import edu.tigers.sumatra.ai.data.OffensiveStrategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.AOffensiveRoleState;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleDelayState;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleFreeSkirmishState;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleInterceptionState;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleKickState;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleSpecialMovementState;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleStopState;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleSupportiveAttackerState;
import edu.tigers.sumatra.math.vector.IVector2;
import org.apache.commons.lang.NotImplementedException;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRole extends AOffensiveRole
{
	private final OffensiveRoleKickState kicker;
	private final OffensiveRoleStopState stop;
	private final OffensiveRoleDelayState delay;
	private final OffensiveRoleSpecialMovementState specialMove;
	private final OffensiveRoleInterceptionState intercept;
	private final OffensiveRoleSupportiveAttackerState supporter;
	private final OffensiveRoleFreeSkirmishState skirmish;
	private EOffensiveStrategy eStateID = null;
	
	
	/**
	 * Default
	 */
	public OffensiveRole()
	{
		super();
		kicker = new OffensiveRoleKickState(this);
		stop = new OffensiveRoleStopState(this);
		delay = new OffensiveRoleDelayState(this);
		specialMove = new OffensiveRoleSpecialMovementState(this);
		intercept = new OffensiveRoleInterceptionState(this);
		supporter = new OffensiveRoleSupportiveAttackerState(this);
		skirmish = new OffensiveRoleFreeSkirmishState(this);
		setInitialState(stop);
		initTransitions();
	}
	
	
	/**
	 * With parameters
	 * 
	 * @param offensiveState
	 * @param allowStateSwitch
	 */
	public OffensiveRole(final EOffensiveStrategy offensiveState, boolean allowStateSwitch)
	{
		super();
		activeSwitching = false;
		kicker = new OffensiveRoleKickState(this);
		stop = new OffensiveRoleStopState(this);
		delay = new OffensiveRoleDelayState(this);
		specialMove = new OffensiveRoleSpecialMovementState(this);
		intercept = new OffensiveRoleInterceptionState(this);
		supporter = new OffensiveRoleSupportiveAttackerState(this);
		skirmish = new OffensiveRoleFreeSkirmishState(this);
		eStateID = offensiveState;
		setInitialState(stop);
		
		if (allowStateSwitch)
		{
			initTransitions();
		}
	}
	
	
	private void initTransitions()
	{
		addTransition(EOffensiveStrategy.KICK, kicker);
		addTransition(EOffensiveStrategy.DELAY, delay);
		addTransition(EOffensiveStrategy.STOP, stop);
		addTransition(EOffensiveStrategy.SPECIAL_MOVE, specialMove);
		addTransition(EOffensiveStrategy.INTERCEPT, intercept);
		addTransition(EOffensiveStrategy.SUPPORTIVE_ATTACKER, supporter);
		addTransition(EOffensiveStrategy.FREE_SKIRMISH, skirmish);
	}
	
	
	@Override
	public void beforeFirstUpdate()
	{
		EOffensiveStrategy initialState;
		if (eStateID != null)
		{
			initialState = eStateID;
		} else
		{
			initialState = getAiFrame().getTacticalField().getOffensiveStrategy()
					.getCurrentOffensivePlayConfiguration().get(getBotID());
		}
		
		initialState = determineInitialState(initialState);
		
		switch (initialState)
		{
			case SPECIAL_MOVE:
				setInitialState(specialMove);
				break;
			case DELAY:
				setInitialState(delay);
				break;
			case INTERCEPT:
				setInitialState(intercept);
				break;
			case KICK:
				setInitialState(kicker);
				break;
			case STOP:
				setInitialState(stop);
				break;
			case SUPPORTIVE_ATTACKER:
				setInitialState(supporter);
				break;
			case FREE_SKIRMISH:
				setInitialState(skirmish);
				break;
			default:
				throw new NotImplementedException();
		}
	}
	
	
	private EOffensiveStrategy determineInitialState(EOffensiveStrategy initialState)
	{
		if (initialState == null)
		{
			int idx = getAiFrame().getPrevFrame().getAICom().getUnassignedStateCounter();
			if (idx >= getAiFrame().getTacticalField().getOffensiveStrategy().getUnassignedStrategies().size())
			{
				return EOffensiveStrategy.SPECIAL_MOVE;
			} else
			{
				getAiFrame().getPrevFrame().getAICom().setUnassignedStateCounter(idx + 1);
				return getAiFrame().getTacticalField().getOffensiveStrategy().getUnassignedStrategies()
						.get(idx);
			}
		}
		return initialState;
	}
	
	
	/**
	 * @return shootTarget of offensive role, is null when no target is set yet
	 */
	public IVector2 getTarget()
	{
		EOffensiveStrategy state = EOffensiveStrategy.valueOf(getCurrentState().getIdentifier());
		if (state == EOffensiveStrategy.KICK)
		{
			return kicker.getTarget();
		}
		return null;
	}
	
	
	/**
	 * @return The current move Position of the offensiveRole
	 */
	public IVector2 getDestination()
	{
		return ((AOffensiveRoleState) getCurrentState()).getMoveDest();
	}
	
}
