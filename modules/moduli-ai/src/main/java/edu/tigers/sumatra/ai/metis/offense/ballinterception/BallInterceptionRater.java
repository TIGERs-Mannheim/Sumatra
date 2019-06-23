/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.ballinterception;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableTrajectoryPath;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2DAsync;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


public class BallInterceptionRater
{
	private final List<IDrawableShape> shapes = new ArrayList<>();
	
	private boolean debug = false;
	
	
	public BallInterception rate(final WorldFrame worldFrame, final BotID botID, final IVector2 target)
	{
		ITrackedBot bot = worldFrame.getBot(botID);
		shapes.clear();
		
		BangBangTrajectory2DAsync trajectory = asyncTrajectoryToTarget(bot, worldFrame.getBall(), target);
		
		double time2ApproachingLine = trajectory.getTotalTimeToPrimaryDirection();
		IVector2 approachingPos = trajectory.getPositionMM(time2ApproachingLine);
		// use some time tolerance, because the bot will actually never reach the approaching position (kicker vs. bot
		// target)
		double ballTime = Math.max(0, time2ApproachingLine - 0.2);
		IVector2 ballPos = worldFrame.getBall().getTrajectory().getPosByTime(ballTime).getXYVector();
		IVector2 futureBall2ApproachingPos = approachingPos.subtractNew(ballPos);
		
		final Boolean ballIsInterceptable = ballIsInterceptable(futureBall2ApproachingPos, worldFrame.getBall(),
				approachingPos, bot);
		double distance2BallWhenOnBallLine = futureBall2ApproachingPos.getLength2();
		double ballContactTime;
		if (ballIsInterceptable)
		{
			ballContactTime = worldFrame.getBall().getTrajectory().getTimeByPos(approachingPos);
		} else
		{
			// time to reach future ball
			BangBangTrajectory2DAsync trajectory2Ball = asyncTrajectoryToTarget(bot, worldFrame.getBall(), ballPos);
			ballContactTime = trajectory2Ball.getTotalTime();
		}
		
		if (debug)
		{
			shapes.add(new DrawableTrajectoryPath(trajectory, Color.gray));
			
			shapes.add(new DrawableLine(Line.fromPoints(bot.getPos(), approachingPos), Color.red));
			shapes.add(new DrawableLine(Line.fromPoints(bot.getPos(), ballPos), Color.green));
		}
		
		return new BallInterception(botID, ballIsInterceptable, ballContactTime, distance2BallWhenOnBallLine);
	}
	
	
	private Boolean ballIsInterceptable(final IVector2 futureBall2ApproachingPos, final ITrackedBall ball,
			final IVector2 approachingPos, final ITrackedBot tBot)
	{
		if (ball.getVel().getLength2() > 1.5 && ballMovesAwayFromBot(ball, tBot))
		{
			// this is primary required for kicks to quickly switch the attacker
			return false;
		}
		
		IVector2 closestPointToApproachingPos = ball.getTrajectory().getTravelLineRolling()
				.closestPointOnLine(approachingPos);
		final boolean botNearApproachingPos = closestPointToApproachingPos.distanceTo(tBot.getPos()) < 500
				&& futureBall2ApproachingPos.getLength2() < 1000;
		final boolean ballNearBot = tBot.getPos().distanceTo(ball.getPos()) < 300;
		final boolean futureBallMovesTowardsApproachingPos = futureBall2ApproachingPos.angleToAbs(ball.getVel())
				.orElse(AngleMath.PI) < AngleMath.PI_HALF;
		
		return botNearApproachingPos || ballNearBot || futureBallMovesTowardsApproachingPos;
	}
	
	
	private boolean ballMovesAwayFromBot(ITrackedBall ball, ITrackedBot bot)
	{
		if (ball.getPos().distanceTo(bot.getBotKickerPos()) < Geometry.getBallRadius() + 10)
		{
			return false;
		}
		IVector2 bot2Ball = ball.getPos().subtractNew(bot.getPos());
		return bot2Ball.angleToAbs(ball.getVel()).orElse(0.0) < 0.3;
	}
	
	
	private BangBangTrajectory2DAsync asyncTrajectoryToTarget(final ITrackedBot bot, final ITrackedBall ball,
			final IVector2 target)
	{
		return new BangBangTrajectory2DAsync(bot.getPos().multiplyNew(1e-3),
				target.multiplyNew(1e-3),
				bot.getVel(),
				bot.getMoveConstraints().getVelMax(),
				bot.getMoveConstraints().getAccMax(),
				ball.getVel());
	}
	
	
	public List<IDrawableShape> getShapes()
	{
		return shapes;
	}
	
	
	public void setDebug(final boolean debug)
	{
		this.debug = debug;
	}
}
