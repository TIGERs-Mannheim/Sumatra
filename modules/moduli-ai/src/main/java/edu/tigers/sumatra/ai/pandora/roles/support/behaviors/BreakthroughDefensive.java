/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.behaviors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPassFactory;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.ASupportBehavior;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ITrackedObject;

import java.awt.Color;
import java.util.ArrayList;
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
public class BreakthroughDefensive extends ASupportBehavior
{

	@Configurable(comment = "Defines whether this behavior is active of not", defValue = "true")
	private static boolean isActive = true;

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


	static
	{
		ConfigRegistration.registerClass("roles", BreakthroughDefensive.class);
	}

	private final PassFactory passFactory = new PassFactory();
	private final RatedPassFactory ratedPassFactory = new RatedPassFactory();
	private IVector2 destination;
	private final List<IDrawableShape> shapes = new ArrayList<>();
	private MoveToSkill skill;


	public BreakthroughDefensive(final ARole role)
	{
		super(role);
	}


	@Override
	public void doEntryActions()
	{
		skill = MoveToSkill.createMoveToSkill();
		getRole().setNewSkill(skill);
	}


	@Override
	public double calculateViability()
	{
		shapes.clear();
		if (!isActive || !getRole().getAiFrame().getGameState().isRunning())
		{
			return 0;
		}
		Optional<IVector2> dest = getBreakthroughDestination(getRole().getBot(),
				getRole().getWFrame().getOpponentBots().values());
		if (dest.isPresent() && isBreakthroughReasonable(dest.get()))
		{
			destination = dest.get();
			shapes.add(new DrawableCircle(Circle.createCircle(destination, Geometry.getBotRadius())));
			shapes.add(new DrawableLine(Line.fromPoints(destination, getRole().getPos()), Color.CYAN));
			getRole().getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_BREAK_THROUGH_DEFENSE)
					.addAll(shapes);

			passFactory.update(getRole().getWFrame());
			ratedPassFactory.update(getRole().getWFrame().getOpponentBots().values());

			var pass = passFactory
					.straight(getRole().getBall().getPos(), destination, BotID.noBot(), getRole().getBotID());
			var rating = ratedPassFactory.rateMaxCombined(pass, EPassRating.PASSABILITY, EPassRating.INTERCEPTION);

			if (rating > passScoreThreshold)
			{
				shapes.add(new DrawableCircle(Circle.createCircle(destination, Geometry.getBotRadius()), Color.CYAN));
				shapes.add(new DrawableLine(Line.fromPoints(destination, getRole().getPos()), Color.CYAN));
				double score = getLonelinessRating(dest.get());
				getRole().getAiFrame().getShapeMap()
						.get(EAiShapesLayer.SUPPORT_BREAK_THROUGH_DEFENSE)
						.addAll(shapes);

				if (score > 0)
				{
					score = Math.min(1.0, score + rating);
				}
				return score;
			} else
			{
				shapes.add(new DrawableCircle(Circle.createCircle(destination, Geometry.getBotRadius()), Color.ORANGE));
				shapes.add(new DrawableLine(Line.fromPoints(destination, getRole().getPos()), Color.ORANGE));
				getRole().getAiFrame().getShapeMap()
						.get(EAiShapesLayer.SUPPORT_BREAK_THROUGH_DEFENSE)
						.addAll(shapes);
				return 0;
			}


		} else if (dest.isPresent())
		{
			shapes.add(new DrawableCircle(Circle.createCircle(dest.get(), Geometry.getBotRadius()), Color.RED));
			shapes.add(new DrawableLine(Lines.segmentFromPoints(dest.get(), getRole().getPos()), Color.RED));
			getRole().getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_BREAK_THROUGH_DEFENSE)
					.addAll(shapes);
		}
		return 0;
	}


	private double getLonelinessRating(final IVector2 dest)
	{
		IVector2 shiftedCenter = dest.addNew(
				dest.subtractNew(Geometry.getGoalTheir().getCenter()).scaleTo(0.5 * searchCircleRadius));

		ICircle surrounding = Circle.createCircle(shiftedCenter, searchCircleRadius);
		long nOpponents = getRole().getWFrame().getBots().values().stream()
				.filter(bot -> bot.getBotId() != getRole().getAiFrame().getKeeperOpponentId())
				.filter(bot -> surrounding.isPointInShape(bot.getPos()))
				.count();

		DrawableCircle drawableSurrounding = new DrawableCircle(surrounding, new Color(0.0f, 0.0f, 0.0f, 0.1f));
		drawableSurrounding.setFill(true);
		shapes.add(drawableSurrounding);

		return nOpponents <= opponentsLimit ? maximumLonelinessScore / (1 + nOpponents) : 0;
	}


	private boolean isBreakthroughReasonable(IVector2 dest)
	{
		long numberTeamRobotsAtPenaltyArea = getRole().getWFrame().getTigerBotsVisible().values().stream()
				.filter(p -> Geometry.getPenaltyAreaTheir().withMargin(penAreaSearchRadius)
						.isPointInShape(p.getPos()))
				.count();
		boolean isMemberAtPenaltyAreaMissing = maxNumberAtPenaltyArea > numberTeamRobotsAtPenaltyArea;
		boolean isBallOnOpponentSide = getRole().getBall().getPos().x() > 0;
		boolean isBotOnOpponentSide = getRole().getPos().x() > 0;
		boolean isDestinationInsideOpponent = getRole().getWFrame().getOpponentBots().values().stream()
				.anyMatch(b -> dest.distanceTo(b.getPos()) < Geometry.getBotRadius());
		return isMemberAtPenaltyAreaMissing && isBallOnOpponentSide && isBotOnOpponentSide
				&& !isDestinationInsideOpponent;
	}


	@Override
	public void doUpdate()
	{
		if (destination != null)
		{
			skill.updateDestination(destination);

			getRole().getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_ACTIVE_ROLES)
					.add(new DrawableCircle(destination, Geometry.getBotRadius(), Color.RED));
		}
	}


	@Override
	public boolean getIsActive()
	{
		return BreakthroughDefensive.isActive;
	}


	private Optional<IVector2> getBreakthroughDestination(ITrackedBot affectedBot, Collection<ITrackedBot> opponents)
	{
		List<IVector2> hedgehogDefenders = calcHedgehogDefenders(opponents).stream()
				.sorted(Comparator
						.comparingDouble(b -> Geometry.getGoalTheir().getCenter().subtractNew(b).getAngle()))
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
				.filter(b -> b.getBotId() != getRole().getAiFrame().getKeeperOpponentId())
				.mapToDouble(b -> b.getPos().distanceTo(Geometry.getGoalTheir().getCenter()))
				.sorted().boxed().collect(Collectors.toList());

		if (defenderDistances.isEmpty())
		{
			return Collections.emptySet();
		}

		double hedgehogDistance = defenderDistances.get(defenderDistances.size() > 1 ? 1 : 0);

		// Draw search Area
		shapes.add(new DrawableArc(Arc.createArc(Geometry.getGoalTheir().getCenter(),
				hedgehogDistance + defensiveCircleWidth, 0.5 * Math.PI, Math.PI), Color.CYAN));

		Set<IVector2> hedgehogDefenders = opponents.stream()
				.filter(b -> b.getBotId() != getRole().getAiFrame().getKeeperOpponentId())
				.filter(b -> isHedgehogDefender(b.getPos(), hedgehogDistance))
				.map(ITrackedObject::getPos)
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
		boolean isInCircle = defender.distanceTo(Geometry.getGoalTheir().getCenter()) < (hedgehogDistance
				+ defensiveCircleWidth);

		boolean isNearPenaltyArea = Geometry.getPenaltyAreaTheir().withMargin(defensiveCircleWidth)
				.isPointInShape(defender);
		return isInCircle || isNearPenaltyArea;
	}


	private Optional<IVector2> findClosestGap(List<IVector2> sortedHedgehogDefenders, ITrackedBot affectedBot)
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
				double avgDistance1 = getAverageDistanceToOurBots(midOfGap.get());
				double avgDistance2 = getAverageDistanceToOurBots(secondMidGap);
				if (avgDistance1 < avgDistance2)
				{
					midOfGap = Optional.of(secondMidGap);
				}
			} else
			{
				midOfGap = Optional.of(secondMidGap);
			}
		}

		return midOfGap;
	}


	private double getAverageDistanceToOurBots(final IVector2 pos)
	{
		return getRole().getWFrame().getTigerBotsVisible().values().stream()
				.filter(bot -> bot.getPos().x() > 0)
				.filter(bot -> bot.getBotId() != getRole().getBotID())
				.filter(bot -> bot.getBotId() != getRole().getAiFrame().getKeeperId())
				.mapToDouble(bot -> bot.getPos().distanceToSqr(pos))
				.average().orElse(0);
	}


	/**
	 * has to be simplified
	 */
	private int calcIndexOfLastConsideredDefender(List<IVector2> sortedHedgehogDefenders, ITrackedBot affectedBot)
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


	private Optional<IVector2> findPointOnPenaltyAreaAccordingToGap(IVector2 midOfGap, ITrackedBot affectedBot)
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
}
