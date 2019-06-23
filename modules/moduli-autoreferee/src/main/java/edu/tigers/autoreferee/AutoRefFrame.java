/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
 * Implementation of {@link IAutoRefFrame}
 */
public class AutoRefFrame implements IAutoRefFrame
{
	private final ShapeMap shapes = new ShapeMap();
	private final WorldFrameWrapper worldFrameWrapper;
	private AutoRefFrame previousFrame;
	
	private List<BotPosition> botsLastTouchedBall = Collections.emptyList();
	private List<BotPosition> botsTouchingBall = Collections.emptyList();
	
	private boolean isBallInsideField = true;
	private TimedPosition ballLeftFieldPos = new TimedPosition();
	private IVector2 lastStopBallPos;
	private List<GameState> stateHistory;
	private PossibleGoal possibleGoal;
	
	
	public AutoRefFrame(final AutoRefFrame previousFrame, final WorldFrameWrapper worldFrameWrapper)
	{
		this.previousFrame = previousFrame;
		this.worldFrameWrapper = worldFrameWrapper;
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
	public List<BotPosition> getBotsLastTouchedBall()
	{
		return botsLastTouchedBall;
	}
	
	
	public void setBotsLastTouchedBall(final List<BotPosition> botLastTouchedBall)
	{
		this.botsLastTouchedBall = botLastTouchedBall;
	}
	
	
	@Override
	public List<BotPosition> getBotsTouchingBall()
	{
		return botsTouchingBall;
	}
	
	
	public void setBotsTouchingBall(final List<BotPosition> botsTouchingBall)
	{
		this.botsTouchingBall = botsTouchingBall;
	}
	
	
	@Override
	public Optional<TimedPosition> getBallLeftFieldPos()
	{
		return Optional.ofNullable(ballLeftFieldPos);
	}
	
	
	public void setBallLeftFieldPos(final TimedPosition getBallLeftFieldPos)
	{
		ballLeftFieldPos = getBallLeftFieldPos;
	}
	
	
	@Override
	public boolean isBallInsideField()
	{
		return isBallInsideField;
	}
	
	
	public void setBallInsideField(final boolean value)
	{
		isBallInsideField = value;
	}
	
	
	@Override
	public IVector2 getLastStopBallPosition()
	{
		return lastStopBallPos;
	}
	
	
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
	
	
	@Override
	public Optional<PossibleGoal> getPossibleGoal()
	{
		return Optional.ofNullable(possibleGoal);
	}
	
	
	public void setPossibleGoal(final PossibleGoal possibleGoal)
	{
		this.possibleGoal = possibleGoal;
	}
}
