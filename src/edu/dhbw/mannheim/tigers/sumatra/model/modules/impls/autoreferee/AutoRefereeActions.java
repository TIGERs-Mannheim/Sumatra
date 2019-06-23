/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 5, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EPossibleGoal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


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
	
	private int							goalsYellow				= 0, goalsBlue = 0;
	
	private static final int		MATCH_HALF_LENGTH		= 60_0_000_000;
	private long						matchStarttime			= SumatraClock.nanoTime();
	
	private long						lastPossibleGoal		= 0;
	
	
	/**
	 * @param refereeHandler
	 */
	public AutoRefereeActions(final AReferee refereeHandler)
	{
		this.refereeHandler = refereeHandler;
	}
	
	
	private void sendRefereeCommand(final Command cmd)
	{
		int timeLeft = (int) (MATCH_HALF_LENGTH - TimeUnit.NANOSECONDS.toMicros(SumatraClock.nanoTime()
				- matchStarttime));
		refereeHandler.sendOwnRefereeMsg(cmd, goalsBlue, goalsYellow, timeLeft);
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
		
		/*
		 * if (frame.getLatestRefereeMsg() != null)
		 * {
		 * sendRefereeCommand(frame.getLatestRefereeMsg().getCommand());
		 * }
		 */
		
		EGameState curGameState = frame.getTacticalField().getGameState();
		WorldFrame wFrame = frame.getWorldFrame();
		
		switch (curGameState)
		{
			case PREPARE_KICKOFF_THEY:
			case PREPARE_KICKOFF_WE:
			case PREPARE_PENALTY_THEY:
			case PREPARE_PENALTY_WE:
			case STOPPED:
				boolean fulfilled = true;
				for (TrackedTigerBot bot : wFrame.getBots().values())
				{
					if (bot.getPos().equals(wFrame.getBall().getPos(), 800) && (bot.getVel().getLength2() > 0.3f))
					{
						fulfilled = false;
						break;
					}
				}
				
				if (fulfilled && (nextRefereeCommand != null))
				{
					sendRefereeCommand(nextRefereeCommand);
					nextRefereeCommand = null;
				}
				return;
			case UNKNOWN:
				final IVector2 ballPos = wFrame.getBall().getPos();
				if (!AIConfig.getGeometry().getField().isPointInShape(ballPos))
				{
					if (Math.abs(ballPos.y()) < (AIConfig.getGeometry().getGoalSize() / 2))
					{
						if (replaceBallGoal)
						{
							scheduleReplaceBall(AVector2.ZERO_VECTOR);
						}
					} else if (replaceBallOutside)
					{
						IVector2 p1 = new Vector2((AIConfig.getGeometry().getFieldLength() / 2) - 200, (AIConfig
								.getGeometry().getFieldWidth() / 2) - 200);
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
		
		EGameState nextGameState = predictNextGameState(frame);
		
		if (curGameState != nextGameState)
		{
			sentStop();
			switch (nextGameState)
			{
				case PREPARE_KICKOFF_THEY:
					sendRefereeCommand(Command.PREPARE_KICKOFF_BLUE);
					break;
				case PREPARE_PENALTY_WE:
					sendRefereeCommand(Command.PREPARE_KICKOFF_YELLOW);
					break;
				default:
					break;
			}
			IVector2 reqBallPos = nextGameState.getRequiredBallPos(frame);
			if (reqBallPos != null)
			{
				if (wFrame.isInverted())
				{
					reqBallPos = reqBallPos.multiplyNew(-1);
				}
				scheduleReplaceBall(reqBallPos);
			}
		}
	}
	
	
	private void scheduleReplaceBall(final IVector2 pos)
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
					refereeHandler.replaceBall(pos);
					replaceBallSchedules = false;
				}
			}, 500);
		}
	}
	
	
	private void sentStop()
	{
		sendRefereeCommand(Command.STOP);
	}
	
	
	private EGameState predictNextGameState(final MetisAiFrame frame)
	{
		EGameState curGameState = frame.getTacticalField().getGameState();
		WorldFrame wFrame = frame.getWorldFrame();
		
		if ((frame.getTacticalField().getPossibleGoal() != EPossibleGoal.NO_ONE)
				&& ((SumatraClock.nanoTime() - lastPossibleGoal) > 5e9))
		{
			switch (frame.getTacticalField().getPossibleGoal())
			{
				case NO_ONE:
					break;
				case THEY:
					log.info("Blue scored a goal!");
					nextRefereeCommand = Command.NORMAL_START;
					goalsBlue++;
					lastPossibleGoal = SumatraClock.nanoTime();
					sendRefereeCommand(Command.GOAL_BLUE);
					return EGameState.PREPARE_KICKOFF_WE;
				case WE:
					log.info("Yellow scored a goal!");
					goalsYellow++;
					lastPossibleGoal = SumatraClock.nanoTime();
					nextRefereeCommand = Command.NORMAL_START;
					sendRefereeCommand(Command.GOAL_YELLOW);
					return EGameState.PREPARE_KICKOFF_THEY;
				default:
					break;
			}
		}
		
		
		if (!AIConfig.getGeometry().getField().isPointInShape(wFrame.getBall().getPos()))
		{
			IVector2 ballLeftFieldPos = frame.getTacticalField().getBallLeftFieldPos();
			ETeamColor teamLastTouchedBall = frame.getTacticalField().getBotLastTouchedBall().getTeamColor();
			if (Math.abs(ballLeftFieldPos.x()) > ((AIConfig.getGeometry().getFieldLength() / 2) - 50))
			{
				// goal line -> freekick
				
				if (ballLeftFieldPos.x() < 0)
				{
					// yellows half of field
					if (teamLastTouchedBall == ETeamColor.YELLOW)
					{
						nextRefereeCommand = Command.DIRECT_FREE_BLUE;
						return EGameState.CORNER_KICK_THEY;
					}
					nextRefereeCommand = Command.DIRECT_FREE_YELLOW;
					return EGameState.GOAL_KICK_WE;
				}
				// blues half of field
				if (teamLastTouchedBall == ETeamColor.YELLOW)
				{
					nextRefereeCommand = Command.DIRECT_FREE_BLUE;
					return EGameState.GOAL_KICK_THEY;
				}
				nextRefereeCommand = Command.DIRECT_FREE_YELLOW;
				return EGameState.CORNER_KICK_WE;
			}
			if (teamLastTouchedBall == ETeamColor.YELLOW)
			{
				nextRefereeCommand = Command.INDIRECT_FREE_BLUE;
				return EGameState.THROW_IN_THEY;
			}
			nextRefereeCommand = Command.INDIRECT_FREE_YELLOW;
			return EGameState.THROW_IN_WE;
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
			matchStarttime = SumatraClock.nanoTime();
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
