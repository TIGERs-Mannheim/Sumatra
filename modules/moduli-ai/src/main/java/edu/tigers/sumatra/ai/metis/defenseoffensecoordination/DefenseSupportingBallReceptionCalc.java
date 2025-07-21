/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defenseoffensecoordination;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseBallThreatSourceType;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.ERotationDirection;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class DefenseSupportingBallReceptionCalc extends ACalculator
{
	private static final Set<EDefenseBallThreatSourceType> ALLOWED_SOURCE_TYPES = Set.of(
			EDefenseBallThreatSourceType.GOAL_SHOT,
			EDefenseBallThreatSourceType.PASS_RECEIVE,
			EDefenseBallThreatSourceType.BALL
	);
	@Configurable(comment = "[s] minimum SlackTime we require for the attacker", defValue = "-0.5")
	private static double maximumSlackTime = -0.5;
	@Configurable(comment = "[mm] minimal margin that will always be held between defense and offense", defValue = "150")
	private static double marginBetween = 150;

	private final Supplier<List<DefenseThreatAssignment>> outerThreatAssignments;
	private final Supplier<Set<BotID>> crucialDefenders;
	private final Supplier<ITrackedBot> opponentPassReceiver;
	private final Supplier<OffensiveStrategy> offensiveStrategy;
	private final Supplier<Map<BotID, RatedOffensiveAction>> offensiveActions;
	private final Supplier<Map<BotID, RatedBallInterception>> ballInterceptions;

	@Getter
	private IVector2 supportiveDefensePosition = null;


	@Override
	protected boolean isCalculationNecessary()
	{
		return offensiveStrategy.get().getAttackerBot()
				.map(botID -> offensiveActions.get().containsKey(botID))
				.orElse(false);
	}


	@Override
	protected void reset()
	{
		supportiveDefensePosition = null;
	}


	@Override
	public void doCalc()
	{
		var attacker = offensiveStrategy.get().getAttackerBot().orElseThrow();
		var ballThreatOpt = getBallThreat();

		if (ballThreatOpt.isPresent() && isDefenseInTheWayOfTheOffense(ballThreatOpt.get())
				&& isOffenseInterceptingEarlier(attacker)
				&& doesAttackerHaveEnoughTimeForReceive(attacker))
		{
			supportiveDefensePosition = createSupportiveDefensePosition();
		} else
		{
			supportiveDefensePosition = null;
		}


	}


	private Optional<IVector2> getOffenseBallContactPos()
	{
		return offensiveStrategy.get().getAttackerBot()
				.map(botID -> offensiveActions.get().get(botID))
				.map(ratedAction -> ratedAction.getAction().getBallContactPos());
	}


	private Optional<IVector2> getOpponentBallContactPos()
	{
		return Optional.ofNullable(opponentPassReceiver.get())
				.map(ITrackedBot::getPos)
				.map(pos -> getBall().getTrajectory().getTravelLine().closestPointOnPath(pos));
	}


	private boolean isDefenseInTheWayOfTheOffense(DefenseBallThreat ballThreat)
	{
		return getOffenseBallContactPos()
				.map(pos -> ballThreat.getProtectionLine().orElseThrow().distanceTo(pos)
						< marginBetween + 3 * Geometry.getBotRadius())
				.orElse(false);
	}


	private boolean doesAttackerHaveEnoughTimeForReceive(BotID attacker)
	{
		var interception = ballInterceptions.get().get(attacker);
		return interception != null && interception.getMinCorridorSlackTime() <= maximumSlackTime;
	}


	private boolean isOffenseInterceptingEarlier(BotID attacker)
	{
		var offenseOpt = getOffenseInterceptionPosition(attacker);
		var opponentOpt = getOpponentBallContactPos();
		if (offenseOpt.isEmpty())
		{
			return false;
		}
		getShapes(EAiShapesLayer.DO_COORD_DEFENSE_SUPPORTIVE_BALL_RECEPTION)
				.add(new DrawableCircle(offenseOpt.get(), Geometry.getBotRadius() + 30, new Color(0, 255, 0, 100)).setFill(
						true));
		if (opponentOpt.isEmpty())
		{
			return true;
		}

		var ballPos = getBall().getPos();
		return (offenseOpt.get().distanceTo(ballPos) + 2 * Geometry.getBotRadius())
				< opponentOpt.get().distanceTo(ballPos);
	}


	private Optional<IVector2> getOffenseInterceptionPosition(BotID attacker)
	{
		var ratedAction = offensiveActions.get().get(attacker);
		if (ratedAction == null)
		{
			return Optional.empty();
		}

		var ballInterception = ballInterceptions.get().get(attacker);
		if (ballInterception == null || ballInterception.getMinCorridorSlackTime() > maximumSlackTime)
		{
			return Optional.empty();
		}

		return Optional.ofNullable(ratedAction.getAction().getBallContactPos());
	}


	private Optional<DefenseBallThreat> getBallThreat()
	{
		return outerThreatAssignments.get().stream()
				.filter(dta -> dta.getBotIds().stream().noneMatch(crucialDefenders.get()::contains))
				.map(DefenseThreatAssignment::getThreat)
				.filter(DefenseBallThreat.class::isInstance)
				.map(DefenseBallThreat.class::cast)
				.filter(threat -> ALLOWED_SOURCE_TYPES.contains(threat.getSourceType()))
				.findAny();
	}


	private IVector2 createSupportiveDefensePosition()
	{
		var offensePos = getOffenseBallContactPos().orElseThrow();
		var goalOur = Geometry.getGoalOur().withMargin(-Geometry.getBotRadius());

		var angleLeft = Vector2.fromPoints(offensePos, goalOur.getLeftPost()).getAngle();
		var angleRight = Vector2.fromPoints(offensePos, goalOur.getRightPost()).getAngle();
		var angleCenter = Vector2.fromPoints(offensePos, Geometry.getGoalOur().bisection(offensePos)).getAngle();
		var angleBall = getBall().getVel().getAngle();

		ERotationDirection rotationDir;
		if (supportiveDefensePosition != null)
		{
			var angleLastRotation = Vector2.fromPoints(offensePos, supportiveDefensePosition).getAngle();
			rotationDir = AngleMath.rotationDirection(angleBall, angleLastRotation);
		} else
		{
			rotationDir = AngleMath.rotationDirection(angleBall, angleCenter);
		}
		var angleWanted = AngleMath.rotateAngle(angleBall, AngleMath.PI_QUART, rotationDir);
		var cappedAngle = AngleMath.capAngle(angleWanted, angleLeft, angleRight);

		var distToOffense = marginBetween + 3 * Geometry.getBotRadius();

		getShapes(EAiShapesLayer.DO_COORD_DEFENSE_SUPPORTIVE_BALL_RECEPTION).add(
				new DrawableCircle(offensePos, distToOffense));
		drawAngle(offensePos, angleLeft, Color.GRAY);
		drawAngle(offensePos, angleRight, Color.GRAY);
		drawAngle(offensePos, angleCenter, Color.RED);
		drawAngle(offensePos, angleBall, Color.ORANGE);
		drawAngle(offensePos, angleWanted, Color.YELLOW);
		drawAngle(offensePos, cappedAngle, Color.GREEN);

		// Whole Attacker diameter + radius of defender + 2 radii buffer
		return offensePos.addNew(Vector2.fromAngleLength(cappedAngle, distToOffense));
	}


	private void drawAngle(IVector2 pos, double angle, Color color)
	{
		getShapes(EAiShapesLayer.DO_COORD_DEFENSE_SUPPORTIVE_BALL_RECEPTION).add(
				new DrawableLine(pos,
						pos.addNew(Vector2.fromAngleLength(angle, marginBetween + 3 * Geometry.getBotRadius())), color)
		);
	}
}
