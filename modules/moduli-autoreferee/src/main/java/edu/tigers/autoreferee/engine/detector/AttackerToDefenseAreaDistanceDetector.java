/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import com.github.g3force.configurable.Configurable;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.NGeometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.AttackerTooCloseToDefenseArea;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Monitors the distance between attackers and the defense area of the defending team when a free kick is performed.
 */
public class AttackerToDefenseAreaDistanceDetector extends AGameEventDetector
{
	private static final double INACCURACY_TOLERANCE = 15;

	@Configurable(comment = "[s] The grace period for the bots to position them after a the activation of the detector", defValue = "2.0")
	private static double gracePeriod = 2.0;

	private final double requiredMargin = RuleConstraints.getBotToPenaltyAreaMarginStandard()
			+ Geometry.getBotRadius()
			- INACCURACY_TOLERANCE;

	private final Map<BotID, Long> eventsSentForBotMap = new HashMap<>();


	/**
	 * Default constructor
	 */
	public AttackerToDefenseAreaDistanceDetector()
	{
		super(EGameEventDetectorType.ATTACKER_TO_DEFENSE_AREA_DISTANCE,
				EnumSet.of(EGameState.STOP, EGameState.INDIRECT_FREE, EGameState.DIRECT_FREE, EGameState.KICKOFF));
	}


	@Override
	protected void doReset()
	{
		eventsSentForBotMap.clear();
	}


	@Override
	public Optional<IGameEvent> doUpdate()
	{
		if (!isActiveForAtLeast(gracePeriod))
		{
			return Optional.empty();
		}
		return evaluate();
	}


	private Optional<IGameEvent> evaluate()
	{
		Optional<ITrackedBot> optOffender = frame.getWorldFrame().getBots().values().stream()
				.filter(this::isInOpponentPenaltyArea)
				.peek(bot -> drawBot(bot, Color.orange, 100))
				.filter(this::notBeingPushed)
				.peek(bot -> drawBot(bot, Color.red, 120))
				.filter(this::notSentRecently)
				.findFirst();

		optOffender.ifPresent(g -> eventsSentForBotMap.put(g.getBotId(), g.getTimestamp()));

		return optOffender.map(this::buildViolation);
	}


	private boolean isInOpponentPenaltyArea(ITrackedBot bot)
	{
		return NGeometry.getPenaltyArea(bot.getTeamColor().opposite())
				.withMargin(requiredMargin)
				.withRoundedCorners(requiredMargin)
				.isPointInShape(bot.getPos());
	}


	private void drawBot(ITrackedBot bot, Color color, double radius)
	{
		frame.getShapes().get(EAutoRefShapesLayer.VIOLATED_DISTANCES).add(
				new DrawableCircle(Circle.createCircle(bot.getPos(), radius)).setColor(color)
		);
	}


	private boolean notSentRecently(ITrackedBot bot)
	{
		Long lastTimeSent = eventsSentForBotMap.get(bot.getBotId());
		return lastTimeSent == null || (bot.getTimestamp() - lastTimeSent) / 1e9 > gracePeriod;
	}


	private boolean notBeingPushed(ITrackedBot attacker)
	{
		var defenderPenaltyArea = NGeometry.getPenaltyArea(attacker.getTeamColor().opposite());
		return frame.getWorldFrame().getBots().values().stream()
				// bots from defending team
				.filter(b -> b.getTeamColor() != attacker.getTeamColor())
				// that touch the attacker
				.filter(b -> attacker.getPos().distanceTo(b.getPos()) <= Geometry.getBotRadius() * 2)
				// push in direction of penalty area
				.map(b -> Lines.halfLineFromPoints(b.getPos(), attacker.getPos()))
				// find intersection that show that defenders pushes towards penArea
				.map(defenderPenaltyArea::intersectPerimeterPath)
				.flatMap(List::stream)
				.findAny()
				// if any intersection is present, some defender pushes the attacker
				.isEmpty();
	}


	private IGameEvent buildViolation(final ITrackedBot offender)
	{
		double distance = NGeometry.getPenaltyArea(offender.getTeamColor().opposite())
				.distanceTo(offender.getPos()) - Geometry.getBotRadius();
		distance = Math.max(distance, 0);

		return new AttackerTooCloseToDefenseArea(offender.getBotId(), offender.getPos(), distance,
				frame.getWorldFrame().getBall().getPos());
	}
}
