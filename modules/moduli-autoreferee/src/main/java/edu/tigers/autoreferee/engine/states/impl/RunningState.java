/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.states.impl;

import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.engine.calc.PossibleGoalCalc.PossibleGoal;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.states.IAutoRefStateContext;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * The goal rule detects regular and indirect goals
 * 
 * @author "Lukas Magel"
 */
public class RunningState extends AbstractAutoRefState
{
	private static final Logger log = Logger.getLogger(RunningState.class);
	
	private boolean goalDetected = false;
	private boolean indirectDetected = false;
	
	
	@Override
	public boolean handleGameEvent(final IGameEvent gameEvent, final IAutoRefStateContext ctx)
	{
		switch (gameEvent.getType())
		{
			case GOAL:
				goalDetected = true;
				
				ETeamColor teamInFavor = gameEvent.getResponsibleTeam();
				Command goalCmd = teamInFavor == ETeamColor.BLUE ? Command.GOAL_BLUE : Command.GOAL_YELLOW;
				
				ctx.sendCommand(new RefboxRemoteCommand(Command.STOP));
				ctx.sendCommand(new RefboxRemoteCommand(goalCmd));
				ctx.setFollowUpAction(gameEvent.getFollowUpAction());
				return true;
			case INDIRECT_GOAL:
				indirectDetected = true;
				break;
			case BALL_LEFT_FIELD:
				boolean followUpSet = ctx.getFollowUpAction() != null;
				if (goalDetected || indirectDetected || followUpSet)
				{
					log.trace("Dropping " + EGameEvent.BALL_LEFT_FIELD + " violation.");
					return false;
				}
				break;
			default:
				break;
		}
		return super.handleGameEvent(gameEvent, ctx);
	}
	
	
	@Override
	public void doUpdate(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		Optional<PossibleGoal> optDetectedGoal = frame.getPossibleGoal();
		if (!optDetectedGoal.isPresent())
		{
			doReset();
		}
	}
	
	
	@Override
	protected void doReset()
	{
		goalDetected = false;
		indirectDetected = false;
	}
	
}
