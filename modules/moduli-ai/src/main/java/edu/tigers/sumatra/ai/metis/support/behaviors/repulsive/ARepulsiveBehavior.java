/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors.repulsive;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.behaviors.ASupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.SupportBehaviorPosition;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

	// Sigmas
	@Configurable(comment = "[mm]", defValue = "1250.0")
	private static double sigmaOpponentBot = 1250;
	@Configurable(comment = "[mm]", defValue = "1800.0")
	private static double sigmaTeamBot = 1800.0;
	@Configurable(comment = "[mm]", defValue = "300.0")
	private static double sigmaFieldBorderRepel = 300;
	@Configurable(comment = "[mm]", defValue = "300.0")
	private static double sigmaBallRepel = 300;
	@Configurable(comment = "[mm]", defValue = "400.0")
	private static double sigmaGoalSight = 400;
	@Configurable(comment = "[mm]", defValue = "400.0")
	private static double sigmaPassLine = 400;

	// Magnitudes
	@Configurable(comment = "[mm]", defValue = "-1500.0")
	private static double magnitudeOpponentBot = -1500.0;
	@Configurable(comment = "[mm]", defValue = "-1750.0")
	private static double magnitudeTeamBot = -1750.0;
	@Configurable(comment = "[mm]", defValue = "-3000.0")
	private static double magnitudeFieldBorderRepel = -3000;
	@Configurable(comment = "[mm]", defValue = "-2000.0")
	private static double magnitudeBallRepel = -2000;
	@Configurable(comment = "[mm]", defValue = "-2500.0")
	private static double magnitudeGoalSight = -2500;
	@Configurable(comment = "[mm]", defValue = "-2500.0")
	private static double magnitudePassLine = -2500;

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
	private final Supplier<Map<BotID, OffensiveAction>> offensiveActions;


	@Override
	public SupportBehaviorPosition calculatePositionForRobot(BotID botID)
	{
		List<ITrackedBot> opponents = new ArrayList<>(getWFrame().getOpponentBots().values());
		List<ITrackedBot> supporter = desiredBots.get().get(EPlay.SUPPORT).stream()
				.map(id -> getWFrame().getBot(id))
				.filter(s -> s.getBotId() != botID)
				.collect(Collectors.toList());

		ITrackedBot tBot = getWFrame().getBot(botID);
		var botState = BotState.of(tBot.getBotId(), tBot.getBotState());

		List<IDrawableShape> shapes = getAiFrame().getShapes(EAiShapesLayer.SUPPORT_FORCE_FIELD_FLOW);
		IVector2 resultingForce = Vector2.zero();
		for (int i = 0; i < numberOfLookAHeads; i++)
		{
			List<Force> forces = collectForces(botState, supporter, opponents);
			IVector2 force = calcResultingDirection(forces, botState).multiplyNew(1. / (i + 1));

			resultingForce = resultingForce.addNew(force);
			IVector2 destination = setPositionInsideAllowedArea(botState.getPos(), force);
			shapes.add(DrawableArrow.fromPositions(botState.getPos(), destination).setColor(color));
			botState = BotState.of(tBot.getBotId(), State.of(Pose.from(destination, tBot.getOrientation())));
		}
		IVector2 destination = setPositionInsideAllowedArea(getWFrame().getBot(botID).getPos(), resultingForce);
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


	protected List<Force> getForceRepelFromOpponentBot(Collection<ITrackedBot> opponents, BotState affectedBot)
	{
		return opponents.stream()
				.filter(b -> b.getPos().distanceTo(affectedBot.getPos()) < sigmaOpponentBot * 3)
				.map(b -> new Force(b.getPos(), sigmaOpponentBot, magnitudeOpponentBot))
				.collect(Collectors.toList());
	}


	protected List<Force> getForceRepelFromTeamBot(Collection<ITrackedBot> supporter, BotState affectedBot)
	{
		return getForceRepelFromTeamBot(supporter, affectedBot, sigmaTeamBot, magnitudeTeamBot);
	}


	protected List<Force> getForceRepelFromTeamBot(
			Collection<ITrackedBot> supporter,
			BotState affectedBot,
			double sigma,
			double magnitude
	)
	{
		return supporter.stream()
				.filter(b -> b.getBotId() != affectedBot.getBotId())
				.filter(b -> b.getPos().distanceTo(affectedBot.getPos()) < sigmaTeamBot * 3)
				.map(b -> new Force(b.getPos(), sigma, magnitude))
				.collect(Collectors.toList());
	}


	protected List<Force> getForceRepelFromPassLine(BotState affectedBot)
	{
		return offensiveActions.get().values().stream()
				.map(OffensiveAction::getPass)
				.filter(Objects::nonNull)
				.map(pass -> Lines.segmentFromPoints(pass.getKick().getSource(), pass.getKick().getTarget()))
				.map(line -> line.closestPointOnLine(affectedBot.getPos()))
				.map(referencePoint -> new Force(referencePoint, sigmaPassLine, magnitudePassLine))
				.collect(Collectors.toUnmodifiableList());
	}


	protected Force getForceStayInsideField(BotState affectedBot)
	{
		IVector2 referencePoint = Geometry.getField().withMargin(Geometry.getBotRadius())
				.nearestPointOutside(affectedBot.getPos());
		return new Force(referencePoint, sigmaFieldBorderRepel, magnitudeFieldBorderRepel);
	}


	protected Force getForceRepelFromBall()
	{
		return new Force(getWFrame().getBall().getPos(), sigmaBallRepel, magnitudeBallRepel);
	}


	protected List<Force> getForceRepelFromOffensiveGoalSight(BotState affectedBot)
	{
		return desiredBots.get().get(EPlay.OFFENSIVE).stream()
				.map(id -> getWFrame().getBot(id))
				.map(ITrackedBot::getPos)
				.map(offensivePos -> getForceForOffensiveBot(affectedBot, offensivePos))
				.collect(Collectors.toList());
	}


	private Force getForceForOffensiveBot(BotState affectedBot, IVector2 offensivePos)
	{
		ILineSegment goalLine = Lines.segmentFromPoints(offensivePos, Geometry.getGoalTheir().getCenter());
		IVector2 referencePoint = goalLine.closestPointOnLine(affectedBot.getPos());
		return new Force(referencePoint, sigmaGoalSight, magnitudeGoalSight);
	}


	private IVector2 calcResultingDirection(List<Force> forces, BotState affectedBot)
	{
		IVector2 resultingForce = Vector2.zero();

		for (Force f : forces)
		{
			double dist = f.getMean() - f.getPosition().distanceTo(affectedBot.getPos());
			double resultingLength;
			switch (f.getFunc())
			{
				case CONSTANT:
					resultingLength = 1;
					break;
				case LINEAR:
					resultingLength = 1 / dist;
					break;
				case EXPONENTIAL:
					resultingLength = calcExponentialFactor(f, dist);
					break;
				default:
					resultingLength = 0;
			}
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


	protected IVector2 setPositionInsideAllowedArea(IVector2 currentPos, IVector2 force)
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
		destination = Geometry.getField().nearestPointInside(destination, -(fieldMargin + Geometry.getBotRadius()));

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
		if (placementLine.intersectSegment(pos2Dest).isPresent())
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
				.collect(Collectors.toList());

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
