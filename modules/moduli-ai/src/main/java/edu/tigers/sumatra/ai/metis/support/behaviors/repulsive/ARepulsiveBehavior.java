/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors.repulsive;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.support.behaviors.ASupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.SupportBehaviorPosition;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.penaltyarea.IPenaltyArea;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static edu.tigers.sumatra.geometry.RuleConstraints.getPenAreaMarginStandard;
import static java.lang.Math.exp;


/**
 * This is the base for all repulsive behaviours, which uses forces to model good reaction to the opponents defensive.
 * It also provides several standard forces e.g. repel from opponent bot.
 * All repulsive behaviours are derived from this class.
 */
@RequiredArgsConstructor
public abstract class ARepulsiveBehavior extends ASupportBehavior
{
	@Configurable(comment = "Margin around the field which robots should not move into", defValue = "200.0")
	private static double fieldMargin = 200.0;
	@Configurable(comment = "Margin around our penalty area which robots should not move into", defValue = "1000.0")
	private static double penAreaOurMargin = 1000.0;
	@Configurable(comment = "Margin around their penalty area which robots should not move into", defValue = "270.0")
	private static double penAreaTheirMargin = 270.0;

	@Configurable(defValue = "50.0", comment = "Extra margin [mm] to add to forbidden areas")
	private static double extraMargin = 50.0;

	@Configurable(defValue = "100", comment = "Number of arrows to draw for debugging (horizontal)")
	private static int debugArrowsHorizontal = 100;

	@Configurable(defValue = "50", comment = "Number of arrows to draw for debugging (vertical)")
	private static int debugArrowsVertical = 50;

	@Configurable(defValue = "10", comment = "Number of steps to follow the forces")
	private static int numberOfLookAHeads = 10;

	private final Color color;
	private final Supplier<Map<EPlay, Set<BotID>>> desiredBots;

	@Override
	public SupportBehaviorPosition calculatePositionForRobot(BotID botID)
	{
		List<ITrackedBot> opponents = new ArrayList<>(getWFrame().getOpponentBots().values());
		List<ITrackedBot> supporter = desiredBots.get().get(EPlay.SUPPORT).stream()
				.map(id -> getWFrame().getBot(id))
				.filter(s -> !s.getBotId().equals(botID))
				.toList();

		ITrackedBot tBot = getWFrame().getBot(botID);
		var botState = BotState.of(tBot.getBotId(), tBot.getBotState());

		List<IDrawableShape> shapes = getAiFrame().getShapes(EAiShapesLayer.SUPPORT_FORCE_FIELD_FLOW);
		IVector2 resultingForce = Vector2.zero();
		for (int i = 0; i < numberOfLookAHeads; i++)
		{
			List<Force> forces = collectForces(botState, supporter, opponents);
			IVector2 force = calcResultingDirection(forces, botState).multiplyNew(1. / (i + 1));

			resultingForce = resultingForce.addNew(force);
			IVector2 destination = getPositionOutsideForbiddenArea(botState.getPos(), force);
			shapes.add(DrawableArrow.fromPositions(botState.getPos(), destination).setColor(color));
			botState = BotState.of(tBot.getBotId(), State.of(Pose.from(destination, tBot.getOrientation())));
		}
		IVector2 destination = getPositionOutsideForbiddenArea(getWFrame().getBot(botID).getPos(), resultingForce);
		shapes.add(DrawableArrow.fromPositions(botState.getPos(), destination).setColor(color));

		if (isDrawing())
		{
			drawGrid(botID);
		}

		return SupportBehaviorPosition
				.fromDestinationAndRotationTarget(destination, getWFrame().getBall().getPos(), getViability(botID));
	}


	abstract boolean isDrawing();

	abstract double getViability(BotID botID);

	abstract List<Force> collectForces(
			BotState affectedBot,
			Collection<ITrackedBot> supporter,
			Collection<ITrackedBot> opponents
	);


	private IVector2 calcResultingDirection(List<Force> forces, BotState affectedBot)
	{
		IVector2 resultingForce = Vector2.zero();

		for (Force f : forces)
		{
			double dist = f.getMean() - f.getPosition().distanceTo(affectedBot.getPos());
			double resultingLength = switch (f.getFunc())
			{
				case CONSTANT -> 1;
				case LINEAR -> 1 / dist;
				case EXPONENTIAL -> calcExponentialFactor(f, dist);
			};
			if (f.isInvert())
			{
				resultingLength = 1 - resultingLength;
			}
			resultingLength *= f.getMagnitude();

			IVector2 force = f.getPosition().subtractNew(affectedBot.getPos()).scaleTo(resultingLength);
			if (dist > 0)
			{
				force = force.multiplyNew(-1);
			}
			resultingForce = resultingForce.addNew(force);
		}
		return resultingForce;
	}


	private double calcExponentialFactor(Force f, double dist)
	{
		return exp(-(dist * dist) / (2 * f.getSigma() * f.getSigma()));
	}


	private IVector2 getPositionOutsideForbiddenArea(IVector2 currentPos, IVector2 force)
	{
		IVector2 destination = currentPos.addNew(force);

		// not too close to their penalty area
		final IPenaltyArea theirPenAreaWithMargin = Geometry.getPenaltyAreaTheir().withMargin(penAreaTheirMargin);
		if (theirPenAreaWithMargin.isPointInShapeOrBehind(destination))
		{
			destination = theirPenAreaWithMargin.projectPointOnToPenaltyAreaBorder(destination);
		}

		// not too close to our penalty area
		final IPenaltyArea ourPenAreaWithMargin = Geometry.getPenaltyAreaOur().withMargin(penAreaOurMargin);
		if (ourPenAreaWithMargin.isPointInShapeOrBehind(destination))
		{
			destination = ourPenAreaWithMargin.projectPointOnToPenaltyAreaBorder(destination);
		}

		// not too close to field borders
		destination = Geometry.getField().withMargin(-(fieldMargin + Geometry.getBotRadius()))
				.nearestPointInside(destination);

		if (getAiFrame().getGameState().isRunning())
		{
			return destination;
		}

		// not in forbidden area around their penalty area during stop and standards
		double penAreaStandardSitMargin = getPenAreaMarginStandard() + Geometry.getBotRadius() + extraMargin;
		final IPenaltyArea theirPenAreaForStop = Geometry.getPenaltyAreaTheir().withMargin(penAreaStandardSitMargin);
		if (theirPenAreaForStop.isPointInShapeOrBehind(destination))
		{
			destination = theirPenAreaForStop.projectPointOnToPenaltyAreaBorder(destination);
		}

		// not inside ball placement tube
		IVector2 ballPos = getWFrame().getBall().getPos();
		IVector2 ballPlacementPositionForUs = getAiFrame().getGameState().getBallPlacementPositionForUs();
		IVector2 targetBallPos = Optional.ofNullable(ballPlacementPositionForUs).orElse(ballPos);
		double penAreaStopMargin = RuleConstraints.getStopRadius() + Geometry.getBotRadius() + extraMargin;
		Tube tube = Tube.create(ballPos, targetBallPos, penAreaStopMargin);
		ILineSegment placementLine = Lines.segmentFromPoints(ballPos, targetBallPos);
		ILineSegment pos2Dest = Lines.segmentFromPoints(currentPos, destination);
		if (!placementLine.intersect(pos2Dest).isEmpty())
		{
			// avoid crossing the placement tube
			return tube.nearestPointOutside(currentPos);
		}
		return tube.nearestPointOutside(destination);
	}


	private void drawGrid(BotID botID)
	{
		List<IDrawableShape> shapes = getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_FORCE_FIELD);
		if (!shapes.isEmpty())
		{
			return;
		}
		Collection<ITrackedBot> opponents = getWFrame().getOpponentBots().values();
		List<ITrackedBot> supportBots = desiredBots.get().get(EPlay.SUPPORT).stream()
				.map(id -> getWFrame().getBot(id))
				.toList();

		for (int x = -debugArrowsHorizontal / 2; x < debugArrowsHorizontal / 2; x++)
		{
			for (int y = -debugArrowsVertical / 2; y < debugArrowsVertical / 2; y++)
			{
				IVector2 fakeBotPos = Vector2.fromXY(x * Geometry.getFieldLength() / debugArrowsHorizontal,
						y * Geometry.getFieldWidth() / debugArrowsVertical);
				var botState = BotState.of(botID, State.of(Pose.from(fakeBotPos, 0)));

				List<Force> forces = collectForces(botState, supportBots, opponents);
				IVector2 direction = calcResultingDirection(forces, botState);

				Color teamColor = getWFrame().getTeamColor().getColor();
				Color c = new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(),
						(int) (20 + 235 * SumatraMath.relative(direction.getLength(), 0, 2000)));
				shapes.add(new DrawableArrow(fakeBotPos, direction.normalizeNew().multiply(100), c, 30));
			}
		}
	}
}
