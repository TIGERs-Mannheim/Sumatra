/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 5, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.autoreferee;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.EPossibleGoal;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AVector3;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Simple automatic referee
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AutoRefereeActions
{
	private static final Logger	log						= Logger.getLogger(AutoRefereeActions.class.getName());
	private boolean					active					= false;
	private boolean					replaceBallOutside	= false;
	private boolean					replaceBallGoal		= false;
	private final AReferee			refereeHandler;
											
	private Command					nextRefereeCommand	= null;
																		
	private boolean					replaceBallSchedules	= false;
																		
	private int							goalsYellow				= 0,
													goalsBlue = 0;
													
	private static final int		MATCH_HALF_LENGTH		= 60_0_000_000;
	private long						matchStarttime			= 0;
																		
	private long						lastPossibleGoal		= 0;
																		
																		
	/**
	 * @param refereeHandler
	 */
	public AutoRefereeActions(final AReferee refereeHandler)
	{
		this.refereeHandler = refereeHandler;
	}
	
	
	private void sendRefereeCommand(final Command cmd, final long timestamp)
	{
		int timeLeft = (int) (MATCH_HALF_LENGTH - TimeUnit.NANOSECONDS.toMicros(timestamp
				- matchStarttime));
		refereeHandler.sendOwnRefereeMsg(cmd, goalsBlue, goalsYellow, timeLeft, timestamp, null);
	}
	
	
	/**
	 * @param frame
	 */
	public void process(final MetisAiFrame frame)
	{
		if (!active)
		{
			return;
		}
		
		if (matchStarttime == 0)
		{
			matchStarttime = frame.getSimpleWorldFrame().getTimestamp();
		}
		/*
		 * if (frame.getLatestRefereeMsg() != null)
		 * {
		 * sendRefereeCommand(frame.getLatestRefereeMsg().getCommand());
		 * }
		 */
		
		EGameStateTeam curGameState = frame.getTacticalField().getGameState();
		WorldFrame wFrame = frame.getWorldFrame();
		
		switch (curGameState)
		{
			case PREPARE_KICKOFF_THEY:
			case PREPARE_KICKOFF_WE:
			case PREPARE_PENALTY_THEY:
			case PREPARE_PENALTY_WE:
			case STOPPED:
				boolean fulfilled = true;
				for (ITrackedBot bot : wFrame.getBots().values())
				{
					if (bot.getPos().equals(wFrame.getBall().getPos(), 800) && (bot.getVel().getLength2() > 0.3))
					{
						fulfilled = false;
						break;
					}
				}
				
				if (fulfilled && (nextRefereeCommand != null))
				{
					sendRefereeCommand(nextRefereeCommand, wFrame.getTimestamp());
					nextRefereeCommand = null;
				}
				return;
			case UNKNOWN:
				final IVector2 ballPos = wFrame.getBall().getPos();
				if (!Geometry.getField().isPointInShape(ballPos))
				{
					if (Math.abs(ballPos.y()) < (Geometry.getGoalSize() / 2.0))
					{
						if (replaceBallGoal)
						{
							scheduleReplaceBall(AVector2.ZERO_VECTOR);
						}
					} else if (replaceBallOutside)
					{
						IVector2 p1 = new Vector2((Geometry.getFieldLength() / 2.0) - 200,
								(Geometry.getFieldWidth() / 2.0) - 200);
						IVector2 p2 = p1.multiplyNew(-1);
						Rectangle rect = new Rectangle(p1, p2);
						IVector2 dest = rect.nearestPointInside(ballPos);
						if (wFrame.isInverted())
						{
							dest = dest.multiplyNew(-1);
						}
						scheduleReplaceBall(dest);
					}
				}
				return;
			default:
				break;
		}
		
		EGameStateTeam nextGameState = predictNextGameState(frame);
		
		if (curGameState != nextGameState)
		{
			sendRefereeCommand(Command.STOP, wFrame.getTimestamp());
			switch (nextGameState)
			{
				case PREPARE_KICKOFF_THEY:
					sendRefereeCommand(Command.PREPARE_KICKOFF_BLUE, wFrame.getTimestamp());
					break;
				case PREPARE_PENALTY_WE:
					sendRefereeCommand(Command.PREPARE_KICKOFF_YELLOW, wFrame.getTimestamp());
					break;
				default:
					break;
			}
			IVector2 reqBallPos = nextGameState.getRequiredBallPos(frame.getWorldFrame(), frame.getTacticalField());
			if (reqBallPos != null)
			{
				if (wFrame.isInverted())
				{
					reqBallPos = reqBallPos.multiplyNew(-1);
				}
				scheduleReplaceBall(new Vector3(reqBallPos, 0), AVector3.ZERO_VECTOR);
			}
		}
	}
	
	
	private void scheduleReplaceBall(final IVector2 pos)
	{
		scheduleReplaceBall(new Vector3(pos, 0), AVector3.ZERO_VECTOR);
	}
	
	
	private void scheduleReplaceBall(final IVector3 pos, final IVector3 vel)
	{
		if (!replaceBallSchedules)
		{
			replaceBallSchedules = true;
			Timer timer = new Timer();
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					refereeHandler.replaceBall(pos, vel);
					replaceBallSchedules = false;
				}
			}, 500);
		}
	}
	
	
	private EGameStateTeam predictNextGameState(final MetisAiFrame frame)
	{
		EGameStateTeam curGameState = frame.getTacticalField().getGameState();
		WorldFrame wFrame = frame.getWorldFrame();
		
		if ((frame.getTacticalField().getPossibleGoal() != EPossibleGoal.NO_ONE)
				&& ((wFrame.getTimestamp() - lastPossibleGoal) > 5e9))
		{
			switch (frame.getTacticalField().getPossibleGoal())
			{
				case NO_ONE:
					break;
				case THEY:
					log.info("Blue scored a goal!");
					nextRefereeCommand = Command.NORMAL_START;
					goalsBlue++;
					lastPossibleGoal = wFrame.getTimestamp();
					sendRefereeCommand(Command.GOAL_BLUE, wFrame.getTimestamp());
					return EGameStateTeam.PREPARE_KICKOFF_WE;
				case WE:
					log.info("Yellow scored a goal!");
					goalsYellow++;
					lastPossibleGoal = wFrame.getTimestamp();
					nextRefereeCommand = Command.NORMAL_START;
					sendRefereeCommand(Command.GOAL_YELLOW, wFrame.getTimestamp());
					return EGameStateTeam.PREPARE_KICKOFF_THEY;
				default:
					break;
			}
		}
		
		
		if (!Geometry.getField().isPointInShape(wFrame.getBall().getPos()))
		{
			IVector2 ballLeftFieldPos = frame.getTacticalField().getBallLeftFieldPos();
			ETeamColor teamLastTouchedBall = frame.getTacticalField().getBotLastTouchedBall().getTeamColor();
			if (Math.abs(ballLeftFieldPos.x()) > ((Geometry.getFieldLength() / 2.0) - 50))
			{
				// goal line -> freekick
				
				if (ballLeftFieldPos.x() < 0)
				{
					// yellows half of field
					if (teamLastTouchedBall == ETeamColor.YELLOW)
					{
						nextRefereeCommand = Command.DIRECT_FREE_BLUE;
						return EGameStateTeam.CORNER_KICK_THEY;
					}
					nextRefereeCommand = Command.DIRECT_FREE_YELLOW;
					return EGameStateTeam.GOAL_KICK_WE;
				}
				// blues half of field
				if (teamLastTouchedBall == ETeamColor.YELLOW)
				{
					nextRefereeCommand = Command.DIRECT_FREE_BLUE;
					return EGameStateTeam.GOAL_KICK_THEY;
				}
				nextRefereeCommand = Command.DIRECT_FREE_YELLOW;
				return EGameStateTeam.CORNER_KICK_WE;
			}
			if (teamLastTouchedBall == ETeamColor.YELLOW)
			{
				nextRefereeCommand = Command.INDIRECT_FREE_BLUE;
				return EGameStateTeam.THROW_IN_THEY;
			}
			nextRefereeCommand = Command.INDIRECT_FREE_YELLOW;
			return EGameStateTeam.THROW_IN_WE;
		}
		
		return curGameState;
	}
	
	
	/**
	 * @param active the active to set
	 */
	public final void setActive(final boolean active)
	{
		this.active = active;
		if (active)
		{
			matchStarttime = 0;
		}
	}
	
	
	/**
	 * @param replaceBallOutside the replaceBallOutside to set
	 */
	public final void setReplaceBallOutside(final boolean replaceBallOutside)
	{
		this.replaceBallOutside = replaceBallOutside;
	}
	
	
	/**
	 * @param replaceBallGoal the replaceBallGoal to set
	 */
	public final void setReplaceBallGoal(final boolean replaceBallGoal)
	{
		this.replaceBallGoal = replaceBallGoal;
	}
}
