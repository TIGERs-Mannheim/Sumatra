/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.ballinterception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * estimate how each robot can intercept the ball and store this information in the tactical field
 */
public class BallInterceptionCalc extends ACalculator
{
	@Configurable(defValue = "false")
	private static boolean debug = false;
	
	private final ApproachBallRater approachBallRater = new ApproachBallRater();
	private final List<IDrawableShape> shapes = new ArrayList<>();
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		shapes.clear();
		approachBallRater.setDebug(debug);
		
		Map<BotID, BallInterception> offensiveTimeEstimationMap = new HashMap<>();
		for (ITrackedBot tBot : OffensiveMath.getPotentialOffensiveBotMap(newTacticalField, baseAiFrame).values())
		{
			BotID botID = tBot.getBotId();
			
			IVector2 target = receiveTarget(tBot.getBotKickerPos());
			
			BallInterception ballInterception;
			double step = 0;
			do
			{
				IVector2 steppedTarget = receiveTarget(target.addNew(getBall().getVel().scaleToNew(step)));
				ballInterception = approachBallRater.rate(getWFrame(), botID, steppedTarget);
				if (step > 0)
				{
					step *= -1;
				} else
				{
					step = Math.abs(step) + 300;
				}
			} while (!ballInterception.isInterceptable() && step > -3000);
			
			shapes.addAll(approachBallRater.getShapes());
			offensiveTimeEstimationMap.put(botID, ballInterception);
			annotate(tBot, ballInterception);
		}
		newTacticalField.setBallInterceptions(offensiveTimeEstimationMap);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_BALL_INTERCEPTION).addAll(shapes);
	}
	
	
	private IVector2 receiveTarget(final IVector2 target)
	{
		return getWFrame().getBall().getTrajectory().getTravelLineRolling()
				.closestPointOnLine(target);
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
