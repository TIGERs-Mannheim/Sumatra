/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPassFactory;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ITrackedObject;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This behaviour intends to bring our supporter between the opponents defense and their penalty area.
 * It is ONLY for the breakthrough. This is necessary, because it is hard to model and may be deactivated according to
 * the opponent.
 * When the supporter is behind the defense, this behaviour should not be active.
 * The positions close to the penalty area are good for a bounced of ball or a straight pass through the penalty area.
 * The number of allowed supporter near the penalty area is restricted.
 */
public class BreakThroughDefenseRepulsiveBehavior extends ASupportBehavior
{
	@Configurable(comment = "Defines whether this behavior is active of not", defValue = "true")
	private static boolean enabled = true;

	@Configurable(comment = "Defender width", defValue = "500")
	private static int defensiveCircleWidth = 500;

	@Configurable(comment = "Min gap size for attak", defValue = "500")
	private static int minGapSize = 800;

	@Configurable(comment = "Max number of team members at penalty area", defValue = "1")
	private static int maxNumberAtPenaltyArea = 1;

	@Configurable(comment = "Always try to keep the specified margin from the PenArea [bot radius]", defValue = "2.5")
	private static double penAreaSafetyMargin = 2.5;

	@Configurable(comment = "Only consider a position if passScore > Threshold", defValue = "0.15")
	private static double passScoreThreshold = 0.15;

	@Configurable(comment = "Area to limit the number of tiger bots to [mm]", defValue = "1000.0")
	private static double penAreaSearchRadius = 1000.0;

	@Configurable(comment = "How many defenders are allowed near a breakthrough point", defValue = "3")
	private static int opponentsLimit = 3;

	@Configurable(comment = "Radius around possible position to check for nearby defenders", defValue = "1500.0")
	private static double searchCircleRadius = 1500.0;

	@Configurable(comment = "Score of the viability that should be computed by using the loneliness rating [0-1]", defValue = "0.75")
	private static double maximumLonelinessScore = 0.75;

	private final PassFactory passFactory = new PassFactory();
	private final RatedPassFactory ratedPassFactory = new RatedPassFactory();


	@Override
	public SupportBehaviorPosition calculatePositionForRobot(BotID botID)
	{
		passFactory.update(getWFrame());
		ratedPassFactory.update(getWFrame().getOpponentBots().values());

		if (!getAiFrame().getGameState().isRunning())
		{
			return SupportBehaviorPosition.notAvailable();
		}

		var bot = getWFrame().getBot(botID);
		var botState = BotState.of(bot.getBotId(), bot.getBotState());
		return getBreakthroughDestination(botState, getWFrame().getOpponentBots().values())
				.map(dest -> getSupportBehaviorPosition(bot, dest))
				.orElseGet(SupportBehaviorPosition::notAvailable);
	}


	private SupportBehaviorPosition getSupportBehaviorPosition(ITrackedBot bot, IVector2 destination)
	{
		if (!isBreakthroughReasonable(bot.getPos(), destination))
		{
			drawBreakThroughDefensePath(destination, bot.getPos(), Color.RED);
			return SupportBehaviorPosition.notAvailable();
		}


		var pass = passFactory.straight(getWFrame().getBall().getPos(), destination, BotID.noBot(), bot.getBotId());
		var rating = ratedPassFactory.rateMaxCombined(pass, EPassRating.PASSABILITY, EPassRating.INTERCEPTION);

		if (rating > passScoreThreshold)
		{
			drawBreakThroughDefensePath(destination, bot.getPos(), Color.CYAN);
			double score = getLonelinessRating(destination);
			if (score > 0)
			{
				return SupportBehaviorPosition.fromDestination(destination, Math.min(1.0, score + rating));
			}
			return SupportBehaviorPosition.fromDestination(destination, score);
		}

		drawBreakThroughDefensePath(destination, bot.getPos(), Color.ORANGE);
		return SupportBehaviorPosition.notAvailable();
	}


	private void drawBreakThroughDefensePath(IVector2 dest, IVector2 start, Color c)
	{
		var shapes = getAiFrame().getShapes(EAiShapesLayer.SUPPORT_BREAK_THROUGH_DEFENSE);
		shapes.add(new DrawableCircle(Circle.createCircle(dest, Geometry.getBotRadius()), c));
		shapes.add(new DrawableLine(Lines.segmentFromPoints(dest, start), c));
	}


	private double getLonelinessRating(final IVector2 dest)
	{
		Vector2 direction = dest.subtractNew(Geometry.getGoalTheir().getCenter());
		IVector2 shiftedCenter = dest.addNew(direction.scaleTo(0.5 * searchCircleRadius));

		ICircle surrounding = Circle.createCircle(shiftedCenter, searchCircleRadius);
		long nOpponents = getWFrame().getBots().values().stream()
				.filter(bot -> bot.getBotId() != getAiFrame().getKeeperOpponentId())
				.filter(bot -> surrounding.isPointInShape(bot.getPos()))
				.count();

		DrawableCircle drawableSurrounding = new DrawableCircle(surrounding, new Color(0.0f, 0.0f, 0.0f, 0.1f));
		drawableSurrounding.setFill(true);
		getAiFrame().getShapes(EAiShapesLayer.SUPPORT_BREAK_THROUGH_DEFENSE).add(drawableSurrounding);

		return nOpponents <= opponentsLimit ? maximumLonelinessScore / (1 + nOpponents) : 0;
	}


	private boolean isBreakthroughReasonable(IVector2 start, IVector2 dest)
	{
		IPenaltyArea penaltyAreaTheir = Geometry.getPenaltyAreaTheir();
		long numberTeamRobotsAtPenaltyArea = getWFrame().getTigerBotsVisible().values().stream()
				.filter(p -> penaltyAreaTheir.withMargin(penAreaSearchRadius).isPointInShape(p.getPos()))
				.count();
		boolean isMemberAtPenaltyAreaMissing = maxNumberAtPenaltyArea > numberTeamRobotsAtPenaltyArea;
		boolean isBallOnOpponentSide = getWFrame().getBall().getPos().x() > 0;
		boolean isBotOnOpponentSide = start.x() > 0;
		boolean isDestinationInsideOpponent = getWFrame().getOpponentBots().values().stream()
				.anyMatch(b -> dest.distanceTo(b.getPos()) < Geometry.getBotRadius());
		return isMemberAtPenaltyAreaMissing
				&& isBallOnOpponentSide
				&& isBotOnOpponentSide
				&& !isDestinationInsideOpponent;
	}


	private Optional<IVector2> getBreakthroughDestination(BotState affectedBot, Collection<ITrackedBot> opponents)
	{
		Vector2f goalCenter = Geometry.getGoalTheir().getCenter();
		List<IVector2> hedgehogDefenders = calcHedgehogDefenders(opponents).stream()
				.sorted(Comparator.comparingDouble(b -> goalCenter.subtractNew(b).getAngle()))
				.collect(Collectors.toList());

		if (hedgehogDefenders.isEmpty())
		{
			return Optional.empty();
		}

		Optional<IVector2> midOfGap = findClosestGap(hedgehogDefenders, affectedBot);

		return midOfGap.flatMap(pos -> findPointOnPenaltyAreaAccordingToGap(pos, affectedBot));
	}


	private Set<IVector2> calcHedgehogDefenders(Collection<ITrackedBot> opponents)
	{
		List<Double> defenderDistances = opponents.stream()
				.filter(b -> b.getBotId() != getAiFrame().getKeeperOpponentId())
				.mapToDouble(b -> b.getPos().distanceTo(Geometry.getGoalTheir().getCenter()))
				.sorted().boxed().collect(Collectors.toList());

		if (defenderDistances.isEmpty())
		{
			return Collections.emptySet();
		}

		double hedgehogDistance = defenderDistances.get(defenderDistances.size() > 1 ? 1 : 0);

		var shapes = getAiFrame().getShapes(EAiShapesLayer.SUPPORT_BREAK_THROUGH_DEFENSE);

		// Draw search Area
		shapes.add(new DrawableArc(Arc.createArc(Geometry.getGoalTheir().getCenter(),
				hedgehogDistance + defensiveCircleWidth, 0.5 * Math.PI, Math.PI), Color.CYAN));

		Set<IVector2> hedgehogDefenders = opponents.stream()
				.filter(b -> b.getBotId() != getAiFrame().getKeeperOpponentId())
				.map(ITrackedObject::getPos)
				.filter(pos -> isHedgehogDefender(pos, hedgehogDistance))
				.collect(Collectors.toSet());

		hedgehogDefenders.add(Vector2.fromY(hedgehogDistance).add(Geometry.getGoalTheir().getCenter()));
		hedgehogDefenders.add(Vector2.fromY(-hedgehogDistance).add(Geometry.getGoalTheir().getCenter()));

		// Mark Hedgehog Defenders
		for (IVector2 def : hedgehogDefenders)
		{
			shapes.add(new DrawableCircle(Circle.createCircle(def, Geometry.getBotRadius() * 1.2), Color.CYAN));
		}

		return hedgehogDefenders;
	}


	private boolean isHedgehogDefender(IVector2 defender, double hedgehogDistance)
	{
		Vector2f goalCenter = Geometry.getGoalTheir().getCenter();
		boolean isInCircle = defender.distanceTo(goalCenter) < hedgehogDistance + defensiveCircleWidth;

		IPenaltyArea penaltyAreaTheir = Geometry.getPenaltyAreaTheir();
		boolean isNearPenaltyArea = penaltyAreaTheir.withMargin(defensiveCircleWidth).isPointInShape(defender);
		return isInCircle || isNearPenaltyArea;
	}


	private Optional<IVector2> findClosestGap(List<IVector2> sortedHedgehogDefenders, BotState affectedBot)
	{
		int index = calcIndexOfLastConsideredDefender(sortedHedgehogDefenders, affectedBot);

		// botA <-- gap1 --> botB <-- gap2 --> botC
		IVector2 botA = sortedHedgehogDefenders.get(Math.min(index, sortedHedgehogDefenders.size() - 1));
		IVector2 botB = sortedHedgehogDefenders.get(index - 1);
		IVector2 botC = sortedHedgehogDefenders.get(Math.max(index - 2, 0));

		Optional<IVector2> midOfGap = Optional.empty();
		if (botA != botB && botA.distanceTo(botB) > minGapSize)
		{
			midOfGap = Optional.of(botA.subtractNew(botB).multiply(0.5).add(botB));
		}
		if (botB != botC && botB.distanceTo(botC) > minGapSize)
		{
			IVector2 secondMidGap = botC.subtractNew(botB).multiply(0.5).add(botB);
			if (midOfGap.isPresent())
			{
				double avgDistance1 = getAverageDistanceToOurBots(affectedBot.getBotId(), midOfGap.get());
				double avgDistance2 = getAverageDistanceToOurBots(affectedBot.getBotId(), secondMidGap);
				if (avgDistance1 < avgDistance2)
				{
					return Optional.of(secondMidGap);
				}
			} else
			{
				return Optional.of(secondMidGap);
			}
		}

		return midOfGap;
	}


	private double getAverageDistanceToOurBots(final BotID myself, final IVector2 pos)
	{
		return getWFrame().getTigerBotsVisible().values().stream()
				.filter(bot -> bot.getPos().x() > 0)
				.filter(bot -> bot.getBotId() != myself)
				.filter(bot -> bot.getBotId() != getAiFrame().getKeeperId())
				.mapToDouble(bot -> bot.getPos().distanceToSqr(pos))
				.average().orElse(0);
	}


	/**
	 * has to be simplified
	 */
	private int calcIndexOfLastConsideredDefender(List<IVector2> sortedHedgehogDefenders, BotState affectedBot)
	{
		ILineSegment goalLine = Lines.segmentFromPoints(Geometry.getGoalTheir().getCenter(), affectedBot.getPos());
		double distToGoalLine = Double.MAX_VALUE;
		int index;

		for (index = 0; index < sortedHedgehogDefenders.size(); index++)
		{
			double dist = goalLine.distanceTo(sortedHedgehogDefenders.get(index));
			if (dist > distToGoalLine)
			{
				break;
			}
			distToGoalLine = dist;
		}
		return index;
	}


	private Optional<IVector2> findPointOnPenaltyAreaAccordingToGap(IVector2 midOfGap, BotState affectedBot)
	{
		List<IVector2> intersections = Geometry.getPenaltyAreaTheir()
				.withMargin(Geometry.getBotRadius() + Geometry.getBotRadius() * penAreaSafetyMargin)
				.lineIntersections(Lines.lineFromPoints(midOfGap, Geometry.getGoalTheir().getCenter()));

		Optional<IVector2> newDestination = intersections.stream()
				.min(Comparator.comparingDouble(b -> b.distanceTo(affectedBot.getPos())));

		return newDestination.map(pos -> Geometry.getPenaltyAreaTheir()
				.withMargin(Geometry.getBotRadius() + Geometry.getBotRadius() * penAreaSafetyMargin)
				.lineIntersections(Lines.lineFromPoints(pos, affectedBot.getPos()))
				.stream().min(Comparator.comparingDouble(b -> b.distanceTo(affectedBot.getPos()))).orElse(pos));

	}


	public static int getMaxNumberAtPenaltyArea()
	{
		return maxNumberAtPenaltyArea;
	}


	@Override
	public boolean isEnabled()
	{
		return enabled;
	}
}
