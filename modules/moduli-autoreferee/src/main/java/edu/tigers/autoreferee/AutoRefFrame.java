/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee;

import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.wp.data.BallLeftFieldPosition;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


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
	private BallLeftFieldPosition ballLeftFieldPos = null;
	private List<GameState> stateHistory = Collections.emptyList();


	public AutoRefFrame(final AutoRefFrame previousFrame, final WorldFrameWrapper worldFrameWrapper)
	{
		this.previousFrame = previousFrame;
		this.worldFrameWrapper = worldFrameWrapper;
		shapes.get(EAutoRefShapesLayer.MODE).add(
				new DrawableBorderText(
						Vector2.fromXY(1, 6),
						"AutoRef: " + SumatraModel.getInstance().getModule(AutoRefModule.class).getMode().toString())
						.setColor(Color.WHITE));
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
	public Optional<BallLeftFieldPosition> getBallLeftFieldPos()
	{
		return Optional.ofNullable(ballLeftFieldPos);
	}


	public void setBallLeftFieldPos(final BallLeftFieldPosition getBallLeftFieldPos)
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
}
