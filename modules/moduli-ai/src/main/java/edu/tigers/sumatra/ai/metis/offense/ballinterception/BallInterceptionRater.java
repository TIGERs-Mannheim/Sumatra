/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.ballinterception;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableTrajectoryPath;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2DAsync;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


public class BallInterceptionRater
{
	@Configurable(defValue = "200.0")
	private static double acceptedBotToTravelLineDist = 200.0;

	@Configurable(defValue = "200.0")
	private static double acceptedDistForInterception = 200.0;

	static
	{
		ConfigRegistration.registerClass("metis", BallInterceptionRater.class);
	}

	private boolean debug = false;

	private ITrackedBot bot;
	private BallInterception prevBallInterception;
	private WorldFrame wFrame;


	/**
	 * Assumption: target is on the rolling ball travel line
	 *
	 * @param wFrame
	 * @param botID
	 * @param target
	 * @return
	 */
	public BallInterception rate(final WorldFrame wFrame, final BotID botID, final IVector2 target,
			final BallInterception prevBallInterception)
	{
		this.wFrame = wFrame;
		this.prevBallInterception = prevBallInterception;
		bot = wFrame.getBot(botID);

		final ILineSegment travelLine = wFrame.getBall().getTrajectory().getTravelLineRolling();

		BangBangTrajectory2DAsync botTrajToTarget = asyncTrajectoryToTarget(bot, wFrame.getBall(), target);

		double timeBot2ApproachingPos = botTrajToTarget.getTotalTimeToPrimaryDirection();
		double timeBot2Target = botTrajToTarget.getTotalTime();
		IVector2 botApproachingPos = botTrajToTarget.getPositionMM(timeBot2ApproachingPos);
		double timeBall2Target = wFrame.getBall().getTrajectory().getTimeByPos(target);

		boolean botReachesTargetBeforeBall = timeBot2Target < timeBall2Target;
		boolean approachingPosInterceptable = travelLine.isPointOnLine(botApproachingPos);

		IVector2 botTarget;
		double time2Target;
		if (botReachesTargetBeforeBall || !approachingPosInterceptable)
		{
			botTarget = target;
			time2Target = timeBot2Target;
		} else
		{
			botTarget = botApproachingPos;
			time2Target = timeBot2ApproachingPos;
		}

		IVector2 ballPosWhenBotReachedTarget = wFrame.getBall().getTrajectory().getPosByTime(time2Target).getXYVector();

		double distance2BallWhenOnBallLine = getDistance2BallWhenOnBallLine(botTarget, ballPosWhenBotReachedTarget);
		boolean ballIsInterceptable = ballIsInterceptable(distance2BallWhenOnBallLine, botTarget);

		double ballContactTime;
		if (ballIsInterceptable)
		{
			ballContactTime = wFrame.getBall().getTrajectory().getTimeByPos(botTarget)
					+ timeBot2ApproachingPos;
		} else
		{
			ballContactTime = asyncTrajectoryToTarget(
					bot,
					wFrame.getBall(),
					ballPosWhenBotReachedTarget)
							.getTotalTime()
					+ timeBot2ApproachingPos;
		}

		final List<IDrawableShape> shapes;
		if (debug)
		{
			shapes = new ArrayList<>();
			shapes.add(new DrawableTrajectoryPath(botTrajToTarget, Color.gray));

			shapes.add(new DrawableLine(Line.fromPoints(bot.getPos(), botTarget), Color.red));
			shapes.add(new DrawableLine(Line.fromPoints(bot.getPos(), ballPosWhenBotReachedTarget), Color.green));
		} else
		{
			shapes = Collections.emptyList();
		}

		return new BallInterception(botID, ballIsInterceptable, ballContactTime, distance2BallWhenOnBallLine, shapes,
				botTarget);
	}


	private double getDistance2BallWhenOnBallLine(final IVector2 botTarget, final IVector2 ballPosWhenBotReachedTarget)
	{
		IVector2 ball2TargetWhenBotReachedTarget = botTarget.subtractNew(ballPosWhenBotReachedTarget);
		boolean ballReachesTargetBeforeBot = ball2TargetWhenBotReachedTarget.angleToAbs(wFrame.getBall().getVel())
				.orElse(AngleMath.PI) < AngleMath.PI_HALF;
		double distance2BallWhenOnBallLine = ball2TargetWhenBotReachedTarget.getLength2();
		if (ballReachesTargetBeforeBot)
		{
			distance2BallWhenOnBallLine *= -1;
		}
		return distance2BallWhenOnBallLine;
	}


	private boolean ballIsInterceptable(final double distance2BallWhenOnBallLine,
			final IVector2 approachingPos)
	{
		ITrackedBall ball = wFrame.getBall();
		if (ball.getVel().getLength2() > 1.5 && ballMovesAwayFromBot(ball, bot))
		{
			// this is primary required for kicks to quickly switch the attacker
			return false;
		}

		if (ball.getTrajectory().getTravelLineRolling().distanceTo(bot.getPos()) < acceptedBotToTravelLineDist)
		{
			return true;
		}

		double timeToApproachingPos = ball.getTrajectory().getTimeByPos(approachingPos);
		double ballVelAtApproachingPos = ball.getTrajectory().getVelByTime(timeToApproachingPos).getLength2();
		if (ballVelAtApproachingPos < 0.5)
		{
			return false;
		}

		if (!Geometry.getFieldWBorders().isPointInShape(approachingPos)
				|| Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius() * 2)
						.isPointInShapeOrBehind(approachingPos)
				|| Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius())
						.isPointInShapeOrBehind(approachingPos))
		{
			// approaching pos is out of range for the robot
			return false;
		}

		final boolean botNearApproachingPos = isBotNearApproachingPos(approachingPos, ball);
		final boolean futureBallMovesTowardsApproachingPos = distance2BallWhenOnBallLine < acceptedDistForInterception;

		return botNearApproachingPos || futureBallMovesTowardsApproachingPos;
	}


	private boolean isBotNearApproachingPos(final IVector2 approachingPos, final ITrackedBall ball)
	{
		if (prevBallInterception == null || !prevBallInterception.isInterceptable())
		{
			return false;
		}

		IVector2 closestPointToApproachingPos = ball.getTrajectory().getTravelLineRolling()
				.closestPointOnLine(approachingPos);
		return closestPointToApproachingPos.distanceTo(bot.getPos()) < 500
				|| bot.getPos().distanceTo(ball.getPos()) < 500;
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


	public void setDebug(final boolean debug)
	{
		this.debug = debug;
	}
}
