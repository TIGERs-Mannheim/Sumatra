/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.ballinterception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.util.InterceptorUtil;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * estimate how each robot can intercept the ball and store this information in the tactical field
 */
public class BallInterceptionCalc extends ACalculator
{
	@Configurable(defValue = "false")
	private static boolean debug = false;

	@Configurable(defValue = "false")
	private static boolean calculateForAllBots = false;

	private final ApproachBallRater approachBallRater = new ApproachBallRater();
	private final List<IDrawableShape> shapes = new ArrayList<>();


	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		shapes.clear();
		approachBallRater.setDebug(debug);

		Map<BotID, BallInterception> offensiveTimeEstimationMap = new HashMap<>();
		for (ITrackedBot tBot : consideredBots())
		{
			BotID botID = tBot.getBotId();

			IVector2 target = receiveTarget(tBot);

			BallInterception prevBallInterception = baseAiFrame.getPrevFrame().getTacticalField().getBallInterceptions()
					.get(botID);
			BallInterception ballInterception = approachBallRater.rate(getWFrame(), botID, target, prevBallInterception);
			if (ballInterception.isInterceptable())
			{
				ballInterception = optimizeForward(botID, target, ballInterception, prevBallInterception);
			} else
			{
				ballInterception = optimizeBackwards(botID, target, ballInterception, prevBallInterception);
			}

			shapes.addAll(ballInterception.getShapes());
			offensiveTimeEstimationMap.put(botID, ballInterception);
			annotate(tBot, ballInterception);
		}
		newTacticalField.setBallInterceptions(offensiveTimeEstimationMap);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_BALL_INTERCEPTION).addAll(shapes);
	}


	private Collection<ITrackedBot> consideredBots()
	{
		if (calculateForAllBots)
		{
			return getWFrame().getTigerBotsAvailable().values();
		}
		return getNewTacticalField().getPotentialOffensiveBots().stream()
				.map(id -> getWFrame().getBot(id))
				.collect(Collectors.toSet());
	}


	private BallInterception optimizeForward(
			final BotID botID,
			final IVector2 target,
			final BallInterception ballInterception,
			final BallInterception prevBallInterception)
	{
		BallInterception optimizedBallInterception = ballInterception;
		BallInterception beforeOptBallInterception = ballInterception;
		double stepSize = 500;
		for (double step = stepSize; step < 1800; step += stepSize)
		{
			IVector2 steppedTarget = onBallLine(target.addNew(getBall().getVel().scaleToNew(-step)));
			BallInterception newBallInterception = approachBallRater.rate(getWFrame(), botID, steppedTarget,
					prevBallInterception);
			if (!newBallInterception.isInterceptable()
					|| newBallInterception.getBallContactTime() > optimizedBallInterception.getBallContactTime())
			{
				break;
			}
			beforeOptBallInterception = optimizedBallInterception;
			optimizedBallInterception = newBallInterception;
		}
		return beforeOptBallInterception;
	}


	private BallInterception optimizeBackwards(
			final BotID botID,
			final IVector2 target,
			final BallInterception ballInterception,
			final BallInterception prevBallInterception)
	{
		BallInterception optimizedBallInterception = ballInterception;
		double stepSize = 300;
		int nInterceptable = 0;
		for (double step = stepSize; step < 3000; step += stepSize)
		{
			IVector2 steppedTarget = onBallLine(target.addNew(getBall().getVel().scaleToNew(step)));
			optimizedBallInterception = approachBallRater.rate(getWFrame(), botID, steppedTarget, prevBallInterception);
			if (optimizedBallInterception.isInterceptable())
			{
				nInterceptable++;
			}
			if (nInterceptable > 1
					|| (nInterceptable >= 1 && !optimizedBallInterception.isInterceptable()))
			{
				break;
			}
		}
		return optimizedBallInterception;
	}


	private IVector2 receiveTarget(final ITrackedBot bot)
	{
		return InterceptorUtil.closestInterceptionPos(getBall().getTrajectory().getTravelLineSegment(), bot);
	}


	private IVector2 onBallLine(final IVector2 target)
	{
		return getWFrame().getBall().getTrajectory().getTravelLineRolling().closestPointOnLine(target);
	}


	private void annotate(final ITrackedBot bot, final BallInterception ballInterception)
	{
		shapes.add(
				new DrawableAnnotation(bot.getPos(),
						String.format("interceptable: %s\ndist: %.2f\ncontact:%.2f",
								ballInterception.isInterceptable(),
								ballInterception.getDistanceToBallWhenOnBallLine(),
								ballInterception.getBallContactTime()),
						Vector2.fromXY(0, -180))
								.withCenterHorizontally(true));
	}
}
