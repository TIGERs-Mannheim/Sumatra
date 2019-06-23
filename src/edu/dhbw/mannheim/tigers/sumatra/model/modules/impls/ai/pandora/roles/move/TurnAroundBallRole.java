/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s): DanielAl
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.TurnAroundBallSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * Turn around the ball.
 */
public class TurnAroundBallRole extends ARole
{
	private int	rounds;
	
	private enum EStateId
	{
		TURN,
	}
	
	private enum EEvent
	{
		LOOKING_AT_TARGET,
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  */
	public TurnAroundBallRole()
	{
		this(1);
	}
	
	
	/**
	 * @param rounds number of rounds the bot has to move
	 */
	public TurnAroundBallRole(int rounds)
	{
		super(ERole.BALL_GETTER);
		
		final IRoleState turnState = new TurnState();
		this.rounds = rounds;
		
		setInitialState(turnState);
		addEndTransition(EStateId.TURN, EEvent.LOOKING_AT_TARGET);
	}
	
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private class TurnState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new TurnAroundBallSkill(AngleMath.PI_TWO * rounds));
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
		@Override
		public EStateId getIdentifier()
		{
			return EStateId.TURN;
		}
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			switch (getMoveCon().getAngleCon().checkCondition(getAiFrame().worldFrame, botID))
			{
				case FULFILLED:
					nextState(EEvent.LOOKING_AT_TARGET);
					break;
				default:
					doEntryActions();
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
	}
}
