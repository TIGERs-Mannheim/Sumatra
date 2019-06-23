/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.states;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.kickstate.AOffensiveRoleKickStateState;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.kickstate.EKickStateEvent;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.kickstate.NormalKickStateState;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.kickstate.ProtectionKickStateState;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.kickstate.RedirectKickStateState;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.statemachine.IStateMachine;
import edu.tigers.sumatra.statemachine.StateMachine;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRoleKickState extends AOffensiveRoleState
{
	private IStateMachine<IState> stateMachine;
	
	
	/**
	 * @param role
	 */
	public OffensiveRoleKickState(final OffensiveRole role)
	{
		super(role);
	}
	
	
	@Override
	public IVector2 getMoveDest()
	{
		AOffensiveRoleKickStateState kickState = (AOffensiveRoleKickStateState) stateMachine.getCurrentState();
		return kickState.getDestination();
	}
	
	
	@Override
	public String getIdentifier()
	{
		return OffensiveStrategy.EOffensiveStrategy.KICK.name();
	}
	
	
	@Override
	public void doEntryActions()
	{
		stateMachine = new StateMachine<>();
		IState normal = new NormalKickStateState(this);
		IState protection = new ProtectionKickStateState(this);
		IState redirect = new RedirectKickStateState(this);

		OffensiveAction action = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
		if (action.getMoveAndTargetInformation().isReceiveActive())
		{
			stateMachine.setInitialState(redirect);
		} else
		{
			stateMachine.setInitialState(normal);
		}
		
		stateMachine.addTransition(normal, EKickStateEvent.PROTECT_BALL, protection);
		stateMachine.addTransition(normal, EKickStateEvent.CATCH_BALL, redirect);
		
		stateMachine.addTransition(protection, EKickStateEvent.FOUND_GOOD_STRATEGY, normal);
		stateMachine.addTransition(protection, EKickStateEvent.TIMED_OUT, normal);
		
		stateMachine.addTransition(redirect, EKickStateEvent.CATCH_NOT_POSSIBLE, normal);
		
		stateMachine.setExtendedLogging(!SumatraModel.getInstance().isProductive());
	}
	
	
	@Override
	public void doUpdate()
	{
		stateMachine.update();
		String currentState = "\n\n" + stateMachine.getCurrentState().getIdentifier();
		DrawableAnnotation dt = new DrawableAnnotation(getPos(), currentState, getBotID().getTeamColor().getColor())
				.setOffset(Vector2.fromXY(130, 10))
				.setFontHeight(50);
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ROLE_STATUS).add(dt);
	}
	
	
	/**
	 * Triggers event of inner kickState statemachine
	 * 
	 * @param event
	 */
	public void triggerInnerEvent(final EKickStateEvent event)
	{
		stateMachine.triggerEvent(event);
	}

}
