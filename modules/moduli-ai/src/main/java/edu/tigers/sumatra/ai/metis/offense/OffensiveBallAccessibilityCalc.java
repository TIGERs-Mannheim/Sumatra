/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Calculates the possible approaching angles to interact with the ball
 */
public class OffensiveBallAccessibilityCalc extends ACalculator
{

	@Configurable(defValue = "1 Y")
	private static BotID butToShowDebugShapesFor = BotID.createBotId(0, ETeamColor.YELLOW);

	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		IVector2 ballPos = getWFrame().getBall().getPos();
		
		List<AngleRange> unaccessibleAngles = new ArrayList<>();
		Map<BotID, List<AngleRange>> map = new HashMap<>();

		unaccessibleAngles.addAll(addOpponentBotAngles(ballPos));
		unaccessibleAngles.addAll(addPenaltyAreaAngles(ballPos, Geometry.getPenaltyAreaTheir()));
		unaccessibleAngles.addAll(addPenaltyAreaAngles(ballPos, Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius())));
		
		for (ITrackedBot tigerBot : getWFrame().getTigerBotsVisible().values())
		{
			// add generally forbidden angles
			map.put(tigerBot.getBotId(), new ArrayList<>(unaccessibleAngles));
			
			// now add bot specific forbidden angles
			for (ITrackedBot bot : getWFrame().getTigerBotsVisible().values())
			{
				if (bot.getBotId() != tigerBot.getBotId())
				{
					map.get(tigerBot.getBotId()).addAll(addUnaccesibleRangeByBot(ballPos, bot));
				}
			}
			if (tigerBot.getBotId().equals(butToShowDebugShapesFor))
			{
				visualizeApproachAngles(map.get(tigerBot.getBotId()));
			}
		}
		newTacticalField.setUnaccessibleBallAngles(map);
	}
	
	
	private List<AngleRange> addPenaltyAreaAngles(final IVector2 ballPos, final IPenaltyArea area)
	{
		final List<AngleRange> unaccessibleAngles = new ArrayList<>();
		if (area.distanceTo(ballPos) < Geometry.getBotRadius())
		{
			List<ILine> edges = area.getRectangle().getEdges();
			for (ILine line : edges)
			{
				List<IVector2> list = new ArrayList<>();
				if (area.isPointInShape(ballPos))
				{
					list.add(line.getEnd());
					list.add(line.getStart());
				} else
				{
					list.add(line.getStart());
					list.add(line.getEnd());
				}
				unaccessibleAngles.addAll(addForbiddenRegionByPositions(ballPos, list));
			}
		}
		return unaccessibleAngles;
	}
	
	
	private List<AngleRange> addOpponentBotAngles(final IVector2 ballPos)
	{
		final List<AngleRange> unaccessibleAngles = new ArrayList<>();
		for (ITrackedBot bot : getWFrame().getFoeBots().values())
		{
			unaccessibleAngles.addAll(addUnaccesibleRangeByBot(ballPos, bot));
		}
		return unaccessibleAngles;
	}
	
	
	private List<AngleRange> addUnaccesibleRangeByBot(final IVector2 ballPos, final ITrackedBot bot)
	{
		final List<AngleRange> unaccessibleAngles = new ArrayList<>();
		if (bot.getPos().distanceTo(ballPos) < Geometry.getBotRadius() * 4.0 && bot.getPos().distanceTo(ballPos) > 5)
		{
			double dist = Math.min(Geometry.getBotRadius() * 2.0, Math.max(1, ballPos.distanceTo(bot.getPos()) - 5));
			ICircle botCircle = Circle.createCircle(bot.getPos(), dist);
			List<IVector2> intersections = botCircle.tangentialIntersections(ballPos);
			unaccessibleAngles.addAll(addForbiddenRegionByPositions(ballPos, intersections));
		}
		return unaccessibleAngles;
	}


	private List<AngleRange> addForbiddenRegionByPositions(final IVector2 ballPos, final List<IVector2> intersections)
	{
		List<AngleRange> unaccessibleAngles = new ArrayList<>();
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ACCESSIBILITY)
				.add(new DrawableCircle(Circle.createCircle(intersections.get(0), 50), Color.BLACK));
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ACCESSIBILITY)
				.add(new DrawableCircle(Circle.createCircle(intersections.get(1), 50), Color.BLACK));
		IVector2 left = intersections.get(0).subtractNew(ballPos);
		IVector2 right = intersections.get(1).subtractNew(ballPos);
		
		double rightAngle;
		double leftAngle;
		if (left.angleTo(right).orElse(0.0) > 0)
		{
			rightAngle = left.getAngle();
			leftAngle = right.getAngle();
		} else
		{
			leftAngle = left.getAngle();
			rightAngle = right.getAngle();
		}
		
		if (rightAngle > leftAngle)
		{
			rightAngle -= AngleMath.PI_TWO;
		}
		unaccessibleAngles.add(new AngleRange(rightAngle, leftAngle));
		return unaccessibleAngles;
	}
	
	
	private void visualizeApproachAngles(final List<AngleRange> unaccessibleAngles)
	{
		IVector2 ballPos = getWFrame().getBall().getPos();
		DrawableCircle dc = new DrawableCircle(Circle.createCircle(ballPos, 250), new Color(42, 255, 0, 138));
		dc.setFill(true);
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ACCESSIBILITY).add(dc);

		for (AngleRange range : unaccessibleAngles)
		{
			DrawableArc da = new DrawableArc(DrawableArc.createArc(ballPos, 250, range.getRightAngle(),
					range.getAngleWidth()), new Color(255, 0, 0, 100));
			da.setFill(true);
			getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ACCESSIBILITY).add(da);
		}
	}
}
