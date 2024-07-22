/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors.repulsive;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.IHalfLine;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


@RequiredArgsConstructor
public class RepulsivePassReceiverForceGenerator
{
	// Sigmas
	@Configurable(comment = "[mm]", defValue = "2500.0")
	private static double sigmaBallAttraction = 2500;
	@Configurable(comment = "[mm]", defValue = "400.0")
	private static double sigmaBehindOpponentLine = 400;
	@Configurable(comment = "[mm]", defValue = "400.0")
	private static double sigmaFreeLine = 400;
	// Magnitudes
	@Configurable(comment = "[mm]", defValue = "3500.0")
	private static double magnitudeBallAttraction = 3500;
	@Configurable(comment = "[mm]", defValue = "-1000.0")
	private static double magnitudeBehindOpponentLine = -1000;
	@Configurable(comment = "[mm]", defValue = "2500.0")
	private static double magnitudeFreeLine = 2500;
	@Configurable(comment = "Optimal pass distance", defValue = "2000.0")
	private static double radiusMeanBallDistance = 2000.0;
	@Configurable(comment = "degree", defValue = "30.0")
	private static double minAngleForFreeLine = 30;

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

	static
	{
		ConfigRegistration.registerClass("metis", RepulsivePassReceiverForceGenerator.class);
	}

	private final List<IArc> offensiveShadows;
	private final Map<BotID, RatedOffensiveAction> offensiveActions;
	private final Map<EPlay, Set<BotID>> desiredBots;
	private final WorldFrame worldFrame;


	public List<Force> getForceRepelFromOpponentBot(Collection<ITrackedBot> opponents, BotState affectedBot)
	{
		return opponents.stream()
				.filter(b -> b.getPos().distanceTo(affectedBot.getPos()) < sigmaOpponentBot * 3)
				.map(b -> new Force(b.getPos(), sigmaOpponentBot, magnitudeOpponentBot))
				.toList();
	}


	public List<Force> getForceRepelFromTeamBot(Collection<ITrackedBot> supporter, BotState affectedBot)
	{
		return getForceRepelFromTeamBot(supporter, affectedBot, sigmaTeamBot, magnitudeTeamBot);
	}


	private List<Force> getForceRepelFromTeamBot(
			Collection<ITrackedBot> supporter,
			BotState affectedBot,
			double sigma,
			double magnitude)
	{
		return supporter.stream()
				.filter(b -> !b.getBotId().equals(affectedBot.getBotId()))
				.filter(b -> b.getPos().distanceTo(affectedBot.getPos()) < sigmaTeamBot * 3)
				.map(b -> new Force(b.getPos(), sigma, magnitude))
				.toList();
	}


	public List<Force> getForceRepelFromPassLine(BotState affectedBot)
	{
		return offensiveActions.values().stream()
				.map(e -> e.getAction().getPass())
				.filter(Objects::nonNull)
				.map(pass -> Lines.segmentFromPoints(pass.getKick().getSource(), pass.getKick().getTarget()))
				.map(line -> line.closestPointOnPath(affectedBot.getPos()))
				.map(referencePoint -> new Force(referencePoint, sigmaPassLine, magnitudePassLine))
				.toList();
	}


	public Force getForceStayInsideField(BotState affectedBot)
	{
		IVector2 referencePoint = Geometry.getField().withMargin(Geometry.getBotRadius())
				.nearestPointOutside(affectedBot.getPos());
		return new Force(referencePoint, sigmaFieldBorderRepel, magnitudeFieldBorderRepel);
	}


	public Force getForceRepelFromBall()
	{
		return new Force(worldFrame.getBall().getPos(), sigmaBallRepel, magnitudeBallRepel);
	}


	public List<Force> getForceRepelFromOffensiveGoalSight(BotState affectedBot)
	{
		return desiredBots.get(EPlay.OFFENSIVE).stream()
				.map(worldFrame::getBot)
				.map(ITrackedBot::getPos)
				.map(offensivePos -> getForceForOffensiveBot(affectedBot, offensivePos))
				.toList();
	}


	private Force getForceForOffensiveBot(BotState affectedBot, IVector2 offensivePos)
	{
		ILineSegment goalLine = Lines.segmentFromPoints(offensivePos, Geometry.getGoalTheir().getCenter());
		IVector2 referencePoint = goalLine.closestPointOnPath(affectedBot.getPos());
		return new Force(referencePoint, sigmaGoalSight, magnitudeGoalSight);
	}


	private Force getForceDesiredPassDistance(int idx)
	{
		IVector2 ballPos = worldFrame.getBall().getPos();
		double mean = radiusMeanBallDistance * idx;
		return new Force(ballPos, sigmaBallAttraction, magnitudeBallAttraction, mean, true);
	}


	private List<Force> getForceRepelLinesBehindOpponentBot(Collection<ITrackedBot> opponents, BotState affectedBot)
	{
		return opponents.stream()
				.map(bot -> getForceForOpponent(affectedBot, bot))
				.toList();
	}


	private Force getForceForOpponent(BotState affectedBot, ITrackedBot opponent)
	{
		IVector2 lineDirVector = Vector2.fromPoints(worldFrame.getBall().getPos(), opponent.getPos());
		IHalfLine passSegment = Lines.halfLineFromDirection(opponent.getPos(), lineDirVector);
		IVector2 referencePoint = passSegment.closestPointOnPath(affectedBot.getPos());
		return new Force(referencePoint, sigmaBehindOpponentLine, magnitudeBehindOpponentLine);
	}


	private List<Force> getForceForFreeLinesFromAttacker(final BotState affectedBot)
	{
		return offensiveShadows.stream()
				.filter(arc -> arc.getRotation() > AngleMath.deg2rad(minAngleForFreeLine))
				.map(arc -> getForceForArc(affectedBot, arc))
				.toList();
	}


	private Force getForceForArc(BotState affectedBot, IArc arc)
	{
		double angleBisector = arc.getStartAngle() + (arc.getRotation() * 0.5d);
		IVector2 lineDirection = Vector2.fromAngle(angleBisector);
		IHalfLine freeLine = Lines.halfLineFromDirection(arc.center(), lineDirection);
		IVector2 referencePoint = freeLine.closestPointOnPath(affectedBot.getPos());
		return new Force(referencePoint, sigmaFreeLine, magnitudeFreeLine);
	}


	public List<Force> getRepulsivePassReceiverForces(WorldFrame worldFrame, BotState affectedBot,
			Collection<ITrackedBot> supporter,
			Collection<ITrackedBot> opponents)
	{
		double distToBallSqr = affectedBot.getPos().distanceToSqr(worldFrame.getBall().getPos());
		int idx = (int) desiredBots.get(EPlay.SUPPORT).stream()
				.map(worldFrame::getBot)
				.filter(bot -> worldFrame.getBall().getPos().distanceToSqr(bot.getPos()) < distToBallSqr)
				.count() / 2 + 1;

		List<Force> forces = new ArrayList<>();
		forces.addAll(getForceRepelFromOpponentBot(opponents, affectedBot));
		forces.addAll(getForceRepelLinesBehindOpponentBot(opponents, affectedBot));
		forces.addAll(getForceRepelFromTeamBot(supporter, affectedBot, sigmaTeamBot, magnitudeTeamBot));
		forces.addAll(getForceRepelFromOffensiveGoalSight(affectedBot));
		forces.addAll(getForceRepelFromPassLine(affectedBot));

		forces.add(getForceStayInsideField(affectedBot));
		forces.add(getForceRepelFromBall());
		forces.add(getForceDesiredPassDistance(idx));

		forces.addAll(getForceForFreeLinesFromAttacker(affectedBot));
		return forces;
	}
}
