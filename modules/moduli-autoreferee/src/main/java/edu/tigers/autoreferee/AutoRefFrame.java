/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee;

import java.util.List;
import java.util.Optional;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.autoreferee.engine.calc.PossibleGoalCalc.PossibleGoal;
import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.autoreferee.generic.TimedPosition;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author "Lukas Magel"
 */
@Persistent
public class AutoRefFrame implements IAutoRefFrame
{
	
	private final ShapeMap shapes;
	private WorldFrameWrapper worldFrameWrapper;
	private AutoRefFrame previousFrame;
	
	private BotPosition lastBotCloseToBall;
	private BotPosition botLastTouchedBall;
	private BotPosition botTouchedBall;
	
	private boolean isBallInsideField;
	private TimedPosition ballLeftFieldPos;
	private IVector2 lastStopBallPos;
	
	private List<GameState> stateHistory;
	
	private PossibleGoal possibleGoal;
	
	
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private AutoRefFrame()
	{
		shapes = new ShapeMap();
	}
	
	
	/**
	 * @param previous
	 * @param worldFrameWrapper
	 */
	public AutoRefFrame(final AutoRefFrame previous,
			final WorldFrameWrapper worldFrameWrapper)
	{
		botLastTouchedBall = new BotPosition();
		botTouchedBall = null;
		previousFrame = previous;
		this.worldFrameWrapper = worldFrameWrapper;
		shapes = new ShapeMap();
		
		ballLeftFieldPos = new TimedPosition();
		isBallInsideField = true;
	}
	
	
	@Override
	public AutoRefFrame getPreviousFrame()
	{
		return previousFrame;
	}
	
	
	@Override
	public SimpleWorldFrame getWorldFrame()
	{
		return worldFrameWrapper.getSimpleWorldFrame();
	}
	
	
	@Override
	public BotPosition getBotLastTouchedBall()
	{
		return botLastTouchedBall;
	}
	
	
	/**
	 * @param botLastTouchedBall
	 */
	public void setBotLastTouchedBall(final BotPosition botLastTouchedBall)
	{
		this.botLastTouchedBall = botLastTouchedBall;
	}
	
	
	/**
	 * @return the botTouchedBall
	 */
	@Override
	public Optional<BotPosition> getBotTouchedBall()
	{
		return Optional.ofNullable(botTouchedBall);
	}
	
	
	/**
	 * @param botTouchedBall the botTouchedBall to set
	 */
	public void setBotTouchedBall(final BotPosition botTouchedBall)
	{
		this.botTouchedBall = botTouchedBall;
	}
	
	
	@Override
	public BotPosition getLastBotCloseToBall()
	{
		return lastBotCloseToBall;
	}
	
	
	/**
	 * @param lastBotCloseToBall the lastBotCloseToBall to set
	 */
	public void setLastBotCloseToBall(final BotPosition lastBotCloseToBall)
	{
		this.lastBotCloseToBall = lastBotCloseToBall;
	}
	
	
	@Override
	public TimedPosition getBallLeftFieldPos()
	{
		return ballLeftFieldPos;
	}
	
	
	/**
	 * @param getBallLeftFieldPos
	 */
	public void setBallLeftFieldPos(final TimedPosition getBallLeftFieldPos)
	{
		ballLeftFieldPos = getBallLeftFieldPos;
	}
	
	
	@Override
	public boolean isBallInsideField()
	{
		return isBallInsideField;
	}
	
	
	/**
	 * @param value
	 */
	public void setBallInsideField(final boolean value)
	{
		isBallInsideField = value;
	}
	
	
	@Override
	public IVector2 getLastStopBallPosition()
	{
		return lastStopBallPos;
	}
	
	
	/**
	 * @param pos
	 */
	public void setLastStopBallPosition(final IVector2 pos)
	{
		lastStopBallPos = pos;
	}
	
	
	@Override
	public GameState getGameState()
	{
		return worldFrameWrapper.getGameState();
	}
	
	
	@Override
	public void cleanUp()
	{
		previousFrame = null;
	}
	
	
	@Override
	public List<GameState> getStateHistory()
	{
		return stateHistory;
	}
	
	
	/**
	 * @param stateHistory the stateHistory to set
	 */
	public void setStateHistory(final List<GameState> stateHistory)
	{
		this.stateHistory = stateHistory;
	}
	
	
	@Override
	public long getTimestamp()
	{
		return worldFrameWrapper.getSimpleWorldFrame().getTimestamp();
	}
	
	
	@Override
	public RefereeMsg getRefereeMsg()
	{
		return worldFrameWrapper.getRefereeMsg();
	}
	
	
	@Override
	public ShapeMap getShapes()
	{
		return shapes;
	}
	
	
	/**
	 * @return the possibleGoal
	 */
	@Override
	public Optional<PossibleGoal> getPossibleGoal()
	{
		return Optional.ofNullable(possibleGoal);
	}
	
	
	/**
	 * @param possibleGoal the possibleGoal to set
	 */
	public void setPossibleGoal(final PossibleGoal possibleGoal)
	{
		this.possibleGoal = possibleGoal;
	}
}
