/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 5, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;


/**
 * Simple automatic referee
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AutoReferee implements IAIObserver
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log						= Logger.getLogger(AutoReferee.class.getName());
	private boolean					active					= false;
	private boolean					replaceBallOutside	= false;
	private boolean					replaceBallGoal		= false;
	private final AReferee			refereeHandler;
	
	private Command					nextRefereeCommand	= null;
	
	private boolean					replaceBallSchedules	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param refereeHandler
	 */
	public AutoReferee(final AReferee refereeHandler)
	{
		this.refereeHandler = refereeHandler;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void onNewAIInfoFrame(final AIInfoFrame frame)
	{
		if (!active)
		{
			return;
		}
		
		EGameState curGameState = frame.getTacticalField().getGameState();
		
		switch (curGameState)
		{
			case PREPARE_KICKOFF_THEY:
			case PREPARE_KICKOFF_WE:
			case PREPARE_PENALTY_THEY:
			case PREPARE_PENALTY_WE:
			case STOPPED:
				boolean fulfilled = true;
				for (TrackedTigerBot bot : frame.getWorldFrame().getBots().values())
				{
					if (bot.getVel().getLength2() > 0.3f)
					{
						fulfilled = false;
						break;
					}
				}
				
				if (fulfilled && (nextRefereeCommand != null))
				{
					refereeHandler.sendOwnRefereeMsg(nextRefereeCommand, 0, 0, (short) 899);
					nextRefereeCommand = null;
				}
				return;
			case UNKNOWN:
				final IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
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
						if (frame.getWorldFrame().isInverted())
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
					refereeHandler.sendOwnRefereeMsg(Command.PREPARE_KICKOFF_BLUE, 0, 0, (short) 899);
					break;
				case PREPARE_PENALTY_WE:
					refereeHandler.sendOwnRefereeMsg(Command.PREPARE_KICKOFF_YELLOW, 0, 0, (short) 899);
					break;
				default:
					break;
			}
			IVector2 reqBallPos = nextGameState.getRequiredBallPos(frame);
			if (reqBallPos != null)
			{
				if (frame.getWorldFrame().isInverted())
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
		refereeHandler.sendOwnRefereeMsg(Command.STOP, 0, 0, (short) 899);
	}
	
	
	private EGameState predictNextGameState(final IRecordFrame frame)
	{
		EGameState curGameState = frame.getTacticalField().getGameState();
		
		switch (frame.getTacticalField().getPossibleGoal())
		{
			case NO_ONE:
				break;
			case THEY:
				log.info("Blue scored a goal!");
				nextRefereeCommand = Command.NORMAL_START;
				return EGameState.PREPARE_KICKOFF_WE;
			case WE:
				log.info("Yellow scored a goal!");
				nextRefereeCommand = Command.NORMAL_START;
				return EGameState.PREPARE_KICKOFF_THEY;
			default:
				break;
		}
		
		if (!AIConfig.getGeometry().getField().isPointInShape(frame.getWorldFrame().getBall().getPos()))
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
	
	
	@Override
	public void onAIException(final Exception ex, final IRecordFrame frame, final IRecordFrame prevFrame)
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param active the active to set
	 */
	public final void setActive(final boolean active)
	{
		this.active = active;
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
