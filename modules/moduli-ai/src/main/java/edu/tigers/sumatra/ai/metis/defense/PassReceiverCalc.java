/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.ai.metis.defense;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.animated.AnimatedCrosshair;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.ball.prediction.IBallTrajectory;


/**
 * Determines the opponent's pass receiver. <br>
 * Calculations are based on planar curves. For the ball a planar curve is directly calculated from the trajectory.
 * A chipped ball's planar curve starts at the first touchdown location.<br>
 * The robot uses a planar curve segment that assumes it wants to stop as quickly as possible. Hence, its velocity is
 * used for a small lookahead.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class PassReceiverCalc extends ACalculator
{
	
	@Configurable(comment = "Bots with a minimum curve to (ball) curve distance below this are consired to be pass receivers. [mm]", defValue = "200.0")
	private static double validReceiveDistance = 200.0;
	
	@Configurable(comment = "Minimum ball speed to start looking for a pass receiver. [m/s]", defValue = "1.0")
	private static double minBallSpeed = 1.0;
	
	@Configurable(comment = "Bots further away than this from the ball travel line are not considered as pass receivers. [mm]", defValue = "2000.0")
	private static double maxBotToBallTravelLineDistance = 2000.0;
	
	
	@Override
	public boolean isCalculationNecessary(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		return getBall().getVel().getLength2() >= minBallSpeed;
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		IBallTrajectory ballTrajectory = getBall().getTrajectory();
		
		List<DefenseMath.ReceiveData> ratings = DefenseMath.calcReceiveRatingsFor(ballTrajectory,
				getWFrame().getFoeBots().values(), maxBotToBallTravelLineDistance);
		Optional<DefenseMath.ReceiveData> bestReceiver = ratings.stream()
				.filter(r -> r.getDistToBallCurve() < PassReceiverCalc.getValidReceiveDistance())
				.sorted(Comparator.comparingDouble(DefenseMath.ReceiveData::getDistToBall))
				.findFirst();
		
		if (bestReceiver.isPresent())
		{
			newTacticalField.setOpponentPassReceiver(bestReceiver.get().getBot());
		} else
		{
			newTacticalField.setOpponentPassReceiver(null);
		}
		
		// drawing
		List<IDrawableShape> shapes = newTacticalField
				.getDrawableShapes()
				.get(EAiShapesLayer.PASS_RECEIVER);
		
		for (DefenseMath.ReceiveData data : ratings)
		{
			DrawableAnnotation distAnno = new DrawableAnnotation(data.getBot().getPos(),
					String.format("%.1f", data.getDistToBallCurve()),
					Vector2.fromXY(0, -150));
			distAnno.setCenterHorizontally(true);
			shapes.add(distAnno);
		}
		
		if (bestReceiver.isPresent())
		{
			AnimatedCrosshair cross = AnimatedCrosshair
					.aCrosshairWithContinuousRotation(Circle.createCircle(bestReceiver.get().getBot().getPos(), 120), 2.0f,
							Color.CYAN);
			shapes.add(cross);
		}
	}
	
	
	public static double getValidReceiveDistance()
	{
		return validReceiveDistance;
	}
	
	
	public static double getMaxBotToBallTravelLineDistance()
	{
		return maxBotToBallTravelLineDistance;
	}
}
