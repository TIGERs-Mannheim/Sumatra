/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.states.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.engine.states.IAutoRefStateContext;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * This rule handles the ball placement states. It waits for the ball to be placed by the responsible team or sends a
 * new ball placement command if the ball is not placed after a certain amount of time. If no other team is capable of
 * placing the ball or the ball has been placed the rule will issue a {@link Command#STOP} command.
 * 
 * @author "Lukas Magel"
 */
public class PlaceBallState extends AbstractAutoRefState
{
	private Long entryTime;
	private boolean stopSend = false;
	
	
	@Override
	public void doUpdate(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		updateEntryTime(frame);
		
		if ((ctx.getFollowUpAction() == null) || !ctx.getFollowUpAction().getNewBallPosition().isPresent())
		{
			// No idea where the ball is supposed to be placed, this situation needs to be fixed by the human ref
			return;
		}
		
		IVector2 targetPos = ctx.getFollowUpAction().getNewBallPosition().get();
		if (criteriaAreMet(frame.getWorldFrame(), targetPos))
		{
			// The ball has been placed at the kick position. Return to the stopped state to perform the action
			sendCommandIfReady(ctx, new RefboxRemoteCommand(Command.STOP), !stopSend);
			stopSend = true;
			return;
		}
		
		// Wait until the team has had enough time to place the ball
		if ((frame.getTimestamp() - entryTime) < TimeUnit.MILLISECONDS.toNanos(AutoRefConfig.getBallPlacementWindow()))
		{
			return;
		}
		
		RefboxRemoteCommand cmd = determineNextAction(frame, targetPos);
		sendCommandIfReady(ctx, cmd, !stopSend);
		stopSend = cmd.getCommand() == Command.STOP;
	}
	
	
	/**
	 * @param frame
	 * @return
	 */
	private RefboxRemoteCommand determineNextAction(final IAutoRefFrame frame, final IVector2 targetPos)
	{
		List<ETeamColor> completedAttempts = determinePlacementAttempts(frame);
		List<ETeamColor> capableTeams = AutoRefConfig.getBallPlacementTeams();
		
		capableTeams.removeAll(completedAttempts);
		
		RefboxRemoteCommand cmd = new RefboxRemoteCommand(Command.STOP, null);
		
		if (!capableTeams.isEmpty())
		{
			cmd = new RefboxRemoteCommand(capableTeams.get(0) == ETeamColor.BLUE ? Command.BALL_PLACEMENT_BLUE
					: Command.BALL_PLACEMENT_YELLOW, targetPos);
		}
		
		
		return cmd;
	}
	
	
	private List<ETeamColor> determinePlacementAttempts(final IAutoRefFrame frame)
	{
		List<ETeamColor> placements = new ArrayList<>();
		
		// Add the team which is currently attempting to place the ball
		placements.add(frame.getGameState().getForTeam());
		
		// Add the last state if it was also a placement attempt
		List<GameState> stateHistory = frame.getStateHistory();
		if ((stateHistory.size() > 1) && (stateHistory.get(1).getState() == EGameState.BALL_PLACEMENT))
		{
			placements.add(stateHistory.get(1).getForTeam());
		}
		
		return placements;
	}
	
	
	private void updateEntryTime(final IAutoRefFrame frame)
	{
		if ((entryTime == null) || !(frame.getGameState().equals(frame.getPreviousFrame().getGameState())))
		{
			entryTime = frame.getTimestamp();
		}
	}
	
	
	private boolean criteriaAreMet(final SimpleWorldFrame frame, final IVector2 targetPos)
	{
		ITrackedBall ball = frame.getBall();
		boolean botDistanceCorrect = AutoRefMath.botStopDistanceIsCorrect(frame);
		boolean ballPlaced = AutoRefMath.ballIsPlaced(ball, targetPos, AutoRefConfig.getRobotBallPlacementAccuracy());
		boolean ballStationary = AutoRefMath.ballIsStationary(ball);
		
		
		return botDistanceCorrect && ballPlaced && ballStationary;
	}
	
	
	@Override
	public void doReset()
	{
		entryTime = null;
		stopSend = false;
	}
	
}
