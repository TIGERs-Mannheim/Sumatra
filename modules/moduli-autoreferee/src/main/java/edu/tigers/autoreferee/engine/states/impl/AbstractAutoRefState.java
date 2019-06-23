/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.states.impl;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.states.IAutoRefState;
import edu.tigers.autoreferee.engine.states.IAutoRefStateContext;
import edu.tigers.autoreferee.remote.ICommandResult;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author "Lukas Magel"
 */
public abstract class AbstractAutoRefState implements IAutoRefState
{
	private boolean			firstRun		= true;
	/** in ns */
	private long				entryTime	= 0;
	/** in ns */
	private long				currentTime	= 0;
	
	private ICommandResult	lastCommand	= null;
	
	private boolean			canProceed;
	
	
	protected static void registerClass(final Class<?> clazz)
	{
		ConfigRegistration.registerClass("autoreferee", clazz);
	}
	
	
	@Override
	public final void update(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		if (firstRun)
		{
			prepare(frame);
			entryTime = frame.getTimestamp();
			firstRun = false;
		}
		currentTime = frame.getTimestamp();
		doUpdate(frame, ctx);
	}
	
	
	@Override
	public final void reset()
	{
		firstRun = true;
		canProceed = false;
		lastCommand = null;
		doReset();
	}
	
	
	protected void prepare(final IAutoRefFrame frame)
	{
	}
	
	
	protected void doReset()
	{
		
	}
	
	
	protected long getEntryTime()
	{
		return entryTime;
	}
	
	
	@Override
	public boolean canProceed()
	{
		return canProceed;
	}
	
	
	protected void setCanProceed(final boolean canProceed)
	{
		this.canProceed = canProceed;
	}
	
	
	protected void sendCommand(final IAutoRefStateContext ctx, final RefboxRemoteCommand cmd)
	{
		lastCommand = ctx.sendCommand(cmd);
	}
	
	
	protected boolean sendCommandIfReady(final IAutoRefStateContext ctx, final RefboxRemoteCommand cmd)
	{
		if ((lastCommand == null) || lastCommand.hasFailed())
		{
			lastCommand = ctx.sendCommand(cmd);
			return true;
		}
		return false;
	}
	
	
	protected boolean sendCommandIfReady(final IAutoRefStateContext ctx, final RefboxRemoteCommand cmd, final boolean doProceed)
	{
		if (doProceed)
		{
			sendCommand(ctx, cmd);
			return true;
		}
		return sendCommandIfReady(ctx, cmd);
	}
	
	
	protected boolean timeElapsedSinceEntry(final long timeMs)
	{
		return (currentTime - entryTime) >= TimeUnit.MILLISECONDS.toNanos(timeMs);
	}
	
	
	protected abstract void doUpdate(final IAutoRefFrame frame, final IAutoRefStateContext ctx);
	
	
	@Override
	public boolean handleGameEvent(final IGameEvent gameEvent, final IAutoRefStateContext ctx)
	{
		switch (gameEvent.getType())
		{
			case BOT_STOP_SPEED:
			case BOT_COUNT:
				return false;
			default:
				break;
		}
		
		ctx.sendCommand(new RefboxRemoteCommand(Command.STOP));
		gameEvent.getCardPenalty().ifPresent(cardPenalty -> ctx.sendCommand(cardPenalty.toRefCommand()));
		
		FollowUpAction followUp = gameEvent.getFollowUpAction();
		if (followUp != null)
		{
			ctx.setFollowUpAction(followUp);
		}
		return true;
	}
	
	
	protected boolean checkBotStopDistance(final IAutoRefFrame frame, final List<IDrawableShape> shapes)
	{
		Collection<ITrackedBot> bots = frame.getWorldFrame().getBots().values();
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		return checkBotStopDistance(bots, ballPos, shapes);
		
	}
	
	
	protected boolean checkBotStopDistance(final Collection<ITrackedBot> bots, final IVector2 ballPos,
			final List<IDrawableShape> shapes)
	{
		List<ITrackedBot> violators = bots.stream()
				.filter(bot -> VectorMath.distancePP(ballPos, bot.getPos()) < Geometry.getBotToBallDistanceStop())
				.collect(Collectors.toList());
		
		violators.forEach(bot -> shapes.add(new DrawableCircle(bot.getPos(), Geometry.getBotRadius() * 2, Color.RED)));
		
		return violators.isEmpty();
	}
	
	
	protected boolean checkBotsStationary(final IAutoRefFrame frame, final List<IDrawableShape> shapes)
	{
		Collection<ITrackedBot> bots = frame.getWorldFrame().getBots().values();
		
		List<ITrackedBot> violators = bots.stream()
				.filter(bot -> bot.getVel().getLength() > AutoRefConfig.getBotStationarySpeedThreshold())
				.collect(Collectors.toList());
		
		violators.forEach(bot -> shapes.add(new DrawableCircle(bot.getPos(), Geometry.getBotRadius() * 2, Color.RED)));
		
		return violators.isEmpty();
	}
	
	
	protected boolean checkBallPlaced(final ITrackedBall ball, final IVector2 targetPos,
			final List<IDrawableShape> shapes)
	{
		boolean ballPlaced = AutoRefMath.ballIsPlaced(ball, targetPos);
		if (!ballPlaced)
		{
			shapes.add(new DrawableCircle(ball.getPos(), Geometry.getBallRadius() * 2, Color.RED));
		}
		return ballPlaced;
	}
	
	
	protected boolean checkBotsOnCorrectSide(final IAutoRefFrame frame, final List<IDrawableShape> shapes)
	{
		IRectangle field = NGeometry.getField();
		Collection<ITrackedBot> bots = frame.getWorldFrame().getBots().values();
		List<ITrackedBot> violators = bots.stream()
				.filter(bot -> field.isPointInShape(bot.getPos()))
				.filter(bot -> {
					IRectangle side = NGeometry.getFieldSide(bot.getBotId().getTeamColor());
					return !side.isPointInShape(bot.getPos());
				}).collect(Collectors.toList());
		
		violators.forEach(bot -> shapes.add(new DrawableCircle(bot.getPos(), Geometry.getBotRadius() * 2, Color.RED)));
		
		return violators.isEmpty();
	}
	
	
	protected void drawReadyCircle(final int percentage, final IVector2 ballPos, final List<IDrawableShape> shapes)
	{
		for (int i = 1; i <= 3; i++)
		{
			if (percentage >= (i * 30))
			{
				shapes.add(new DrawableCircle(ballPos, Geometry.getBotRadius() * (1.5d * i), Color.GREEN));
			}
		}
	}
	
}
