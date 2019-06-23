/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.wp.data.ITrackedBot;


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
public class BestGoalShotRaterCalc extends ACalculator
{
	private final IColorPicker colorPicker = ColorPickerFactory.greenRedGradient();
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		final AngleRangeRater rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
		rater.setObstacles(baseAiFrame.getWorldFrame().getBots().values());
		rater.setStraightBallConsultant(baseAiFrame.getWorldFrame().getBall().getStraightConsultant());
		
		List<IDrawableShape> directShotShapes = newTacticalField.getDrawableShapes()
				.get(EAiShapesLayer.AI_BEST_DIRECT_SHOT);
		
		Map<BotID, Optional<IRatedTarget>> bestDirectShotTargetsForTigerBots = new HashMap<>();
		for (ITrackedBot bot : getWFrame().getTigerBotsAvailable().values())
		{
			rater.setExcludedBots(Collections.singleton(bot.getBotId()));
			final Optional<IRatedTarget> ratedTarget = rater.rate(bot.getBotKickerPos());
			bestDirectShotTargetsForTigerBots.put(bot.getBotId(), ratedTarget);
			
			if (ratedTarget.isPresent() && ratedTarget.get().getScore() > 0.01)
			{
				ILineSegment line = Lines.segmentFromPoints(bot.getBotKickerPos(), ratedTarget.get().getTarget());
				DrawableLine drawableLine = new DrawableLine(line, colorPicker.getColor(ratedTarget.get().getScore()));
				directShotShapes.add(drawableLine);
			}
		}
		newTacticalField.setBestGoalKickTargetForBot(bestDirectShotTargetsForTigerBots);
		
		Optional<IRatedTarget> ballTarget = rater.rate(getBall().getPos());
		if (ballTarget.isPresent())
		{
			ILineSegment line = Lines.segmentFromPoints(getBall().getPos(), ballTarget.get().getTarget());
			DrawableLine drawableLine = new DrawableLine(line, colorPicker.getColor(ballTarget.get().getScore()));
			directShotShapes.add(drawableLine);
		}
		ballTarget.ifPresent(newTacticalField::setBestGoalKickTarget);
	}
}
