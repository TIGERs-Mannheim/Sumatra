/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.penaltyarea.IPenaltyArea;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Calculates the possible approaching angles to interact with the ball
 */

@RequiredArgsConstructor
public class OffensiveBallAccessibilityCalc extends ACalculator
{
	@Configurable(defValue = "1 Y")
	private static BotID botToShowDebugShapesFor = BotID.createBotId(0, ETeamColor.YELLOW);

	@Getter
	private Map<BotID, List<AngleRange>> inaccessibleBallAngles;


	@Override
	public void doCalc()
	{
		List<AngleRange> inaccessibleAngles = new ArrayList<>();
		inaccessibleAngles.addAll(createInaccessibleRangeForOpponentBots(getBall().getPos()));
		inaccessibleAngles.addAll(
				createInaccessibleRangeForPenaltyArea(getBall().getPos(), Geometry.getPenaltyAreaTheir()));

		IPenaltyArea penaltyAreaOur = Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius());
		inaccessibleAngles.addAll(createInaccessibleRangeForPenaltyArea(getBall().getPos(), penaltyAreaOur));

		inaccessibleBallAngles = new HashMap<>();
		for (ITrackedBot tigerBot : getWFrame().getTigerBotsVisible().values())
		{
			// add generally forbidden angles
			ArrayList<AngleRange> inaccessibleAnglesForBot = new ArrayList<>(inaccessibleAngles);
			if (tigerBot.getBotKickerPos().distanceTo(getBall().getPos()) > Geometry.getBotRadius())
			{
				inaccessibleAnglesForBot.addAll(createInaccessibleRangeForOpponentBots(getBall().getPos()));
			}
			// now add bot specific forbidden angles
			for (ITrackedBot bot : getWFrame().getTigerBotsVisible().values())
			{
				if (!bot.getBotId().equals(tigerBot.getBotId()))
				{
					createInaccessibleRangeForBot(getBall().getPos(), bot)
							.ifPresent(inaccessibleAnglesForBot::add);
				}
			}
			if (tigerBot.getBotId().equals(botToShowDebugShapesFor))
			{
				visualizeApproachAngles(inaccessibleAnglesForBot);
			}

			inaccessibleBallAngles.put(tigerBot.getBotId(), inaccessibleAnglesForBot);
		}
	}


	private List<AngleRange> createInaccessibleRangeForPenaltyArea(final IVector2 ballPos, final IPenaltyArea area)
	{
		final List<AngleRange> inaccessibleAngles = new ArrayList<>();
		if (area.distanceTo(ballPos) < Geometry.getBotRadius())
		{
			var edges = area.getRectangle().getEdges();
			for (var line : edges)
			{
				List<IVector2> list = new ArrayList<>();
				if (area.isPointInShape(ballPos))
				{
					list.add(line.getPathEnd());
					list.add(line.getPathStart());
				} else
				{
					list.add(line.getPathStart());
					list.add(line.getPathEnd());
				}
				inaccessibleAngles.add(createForbiddenRangeByPositions(ballPos, list));
			}
		}
		return inaccessibleAngles;
	}


	private List<AngleRange> createInaccessibleRangeForOpponentBots(final IVector2 ballPos)
	{
		return getWFrame().getOpponentBots().values().stream()
				.map(bot -> createInaccessibleRangeForBot(ballPos, bot))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();
	}


	private Optional<AngleRange> createInaccessibleRangeForBot(final IVector2 ballPos, final ITrackedBot bot)
	{
		// minDistBotToBall should be smaller than the minimum possible distance between opponent robot and ball,
		// when our robot is in between.
		// Otherwise, an inaccessible angle may be generated although the robot is already within this angle range.
		double minDistBotToBall = Geometry.getBotRadius() * 2 + Geometry.getOpponentCenter2DribblerDist() - 20;
		double maxCircleRadius = Geometry.getBotRadius() + 50;
		// The offset is required for the tangential intersection below, otherwise ballPos would be on the circle
		double tangentialIntersectionOffset = 5;
		double distBotToBall = bot.getPos().distanceTo(ballPos);
		double radius = Math.min(maxCircleRadius, distBotToBall - tangentialIntersectionOffset);
		if (distBotToBall < minDistBotToBall && radius > 0)
		{
			var botCircle = Circle.createCircle(bot.getPos(), radius);
			var intersections = botCircle.tangentialIntersections(ballPos);
			return Optional.of(createForbiddenRangeByPositions(ballPos, intersections));
		}
		return Optional.empty();
	}


	private AngleRange createForbiddenRangeByPositions(final IVector2 ballPos, final List<IVector2> intersections)
	{
		getShapes(EAiShapesLayer.OFFENSIVE_ACCESSIBILITY)
				.add(new DrawableCircle(Circle.createCircle(intersections.get(0), 50), Color.BLACK));
		getShapes(EAiShapesLayer.OFFENSIVE_ACCESSIBILITY)
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
		return AngleRange.fromAngles(rightAngle, leftAngle);
	}


	private void visualizeApproachAngles(final List<AngleRange> inaccessibleAngles)
	{
		IVector2 ballPos = getWFrame().getBall().getPos();
		var dc = new DrawableCircle(Circle.createCircle(ballPos, 250), new Color(42, 255, 0, 138));
		dc.setFill(true);
		getShapes(EAiShapesLayer.OFFENSIVE_ACCESSIBILITY).add(dc);

		for (AngleRange range : inaccessibleAngles)
		{
			var da = new DrawableArc(Arc.createArc(ballPos, 250, range.getRight(),
					range.getWidth()), new Color(255, 0, 0, 100));
			da.setFill(true);
			getShapes(EAiShapesLayer.OFFENSIVE_ACCESSIBILITY).add(da);
		}
	}
}
