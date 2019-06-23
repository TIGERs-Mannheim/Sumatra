/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.math.kick.BestDirectShotBallPossessingBot;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.ValuePoint;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Calculates the best shot target on the goal for every of our bots.
 * The result is a target in the opponents goal and a probability a shot to this target will score a goal for every of
 * our bots,
 * dependent on their position.
 * In addition the best shot target for the ball is calculated.
 * The result will be visualized as cyan annotation to the bots.
 * The score probability for the ball is visualized in addition by showing the uncovered angles on the opponents goal.
 * Grey circles show the obstacle rendered by the opponent bots.
 * The visualizations can be activated and deactivated in the visualizer menu at: AI -> Best shot target
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author Felix Bayer <bayer.fel@googlemail.com>
 */
public class ShooterCalc extends ACalculator
{
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = getWFrame();
		
		List<IDrawableShape> directShotShapes = newTacticalField.getDrawableShapes().get(EAiShapesLayer.BEST_DIRECT_SHOT);
		List<IDrawableShape> tmpShapes = new ArrayList<>();

		List<ITrackedBot> foeBots = new ArrayList<>(wFrame.getFoeBots().values());
		
		// Evaluate indirect goal shot targets
		Map<BotID, ValuePoint> bestDirectShotTargetsForTigerBots = new HashMap<>();
		for (Map.Entry<BotID, ITrackedBot> bot : wFrame.getTigerBotsAvailable())
		{
			double tDeflect = DefenseMath.calculateTDeflect(bot.getValue().getPos(), getBall().getPos(),
					DefenseMath.getBisectionGoal(getBall().getPos()));
			
			ValuePoint bestTargetFromBot = BestDirectShotBallPossessingBot
					.getBestShot(Geometry.getGoalTheir(), bot.getValue().getBotKickerPos(), foeBots, tDeflect)
					.orElse(new ValuePoint(Geometry.getGoalTheir().getCenter(), 0));
			
			tmpShapes
					.add(new DrawableAnnotation(bot.getValue().getPos(), String.format("%.3f", bestTargetFromBot.getValue()),
							Color.CYAN).setOffset(Vector2.fromY(-3 * Geometry.getBallRadius() - Geometry.getBotRadius())));
			
			bestDirectShotTargetsForTigerBots.put(bot.getKey(), bestTargetFromBot);
		}
		newTacticalField.setBestDirectShotTargetsForTigerBots(bestDirectShotTargetsForTigerBots);
		
		ValuePoint bestTargetFromBall = BestDirectShotBallPossessingBot
				.getBestShot(Geometry.getGoalTheir(), getBall().getPos(), foeBots, directShotShapes)
				.orElse(new ValuePoint(Geometry.getGoalTheir().getCenter(), 0));
		
		directShotShapes
				.add(new DrawableAnnotation(getBall().getPos(), String.format("%.3f", bestTargetFromBall.getValue()),
						Color.CYAN).setOffset(Vector2.fromY(-3 * Geometry.getBallRadius())));
		directShotShapes.addAll(tmpShapes);

		newTacticalField.setBestDirectShotTarget(bestTargetFromBall);
	}
}
