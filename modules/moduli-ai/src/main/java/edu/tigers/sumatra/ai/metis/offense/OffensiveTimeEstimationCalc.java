/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.OffensiveTimeEstimation;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveMoveAndTargetInformation;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableTrajectoryPath;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class OffensiveTimeEstimationCalc extends ACalculator
{
	private final Map<BotID, OffensiveTimeEstimator> estimators = new HashMap<>();
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_TIME_ESTIMATION);
		
		IVector2 secondaryDest = newTacticalField.getSupportiveAttackerMovePos();
		
		Map<BotID, OffensiveTimeEstimation> offensiveTimeEstimationMap = new HashMap<>();
		for (ITrackedBot tBot : baseAiFrame.getWorldFrame().getTigerBotsVisible().values())
		{
			BotID botID = tBot.getBotId();
			OffensiveAction action = baseAiFrame.getPrevFrame().getTacticalField().getOffensiveActions().get(botID);
			OffensiveTimeEstimation offensiveTimeEstimation;
			OffensiveTimeEstimator estimator = estimators.computeIfAbsent(botID, OffensiveTimeEstimator::new);
			if (action == null)
			{
				offensiveTimeEstimation = OffensiveTimeEstimation.newBuilder()
						.withBallContactTime(0)
						.withSecondaryTime(estimateSecondaryOffensiveTime(tBot, secondaryDest, shapes))
						.build();
				estimator.reset();
			} else
			{
				OffensiveMoveAndTargetInformation info = action.getMoveAndTargetInformation();
				offensiveTimeEstimation = OffensiveTimeEstimation.newBuilder()
						.withBallContactTime(
								estimator.estimateGetBallContactTime(baseAiFrame.getWorldFrame(), shapes, info))
						.withSecondaryTime(estimateSecondaryOffensiveTime(tBot, secondaryDest, shapes))
						.build();
			}
			offensiveTimeEstimationMap.put(botID, offensiveTimeEstimation);
		}
		newTacticalField.setOffensiveTimeEstimations(offensiveTimeEstimationMap);
	}
	
	
	private double estimateSecondaryOffensiveTime(ITrackedBot tBot, IVector2 movePos, final List<IDrawableShape> shapes)
	{
		ITrajectory<IVector2> traj = TrajectoryGenerator.generatePositionTrajectory(tBot, movePos);
		double pathTime = traj.getTotalTime();
		shapes.add(
				new DrawableAnnotation(tBot.getPos(), String.format("secondary: %.2f", pathTime),
						Vector2.fromXY(0, -170))
								.setCenterHorizontally(true));
		shapes.add(new DrawableTrajectoryPath(traj, Color.darkGray));
		return pathTime;
	}
}
