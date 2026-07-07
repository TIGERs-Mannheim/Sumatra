package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballinterception.BallInterceptionInformation;
import edu.tigers.sumatra.ai.metis.ballinterception.BallInterceptor;
import edu.tigers.sumatra.ai.metis.ballinterception.InterceptionFinderParameters;
import edu.tigers.sumatra.ai.metis.ballinterception.PreviousBallInterception;
import edu.tigers.sumatra.ai.metis.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallControl;
import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * estimate how each robot can intercept the ball and store this information in the tactical field
 */
@RequiredArgsConstructor
public class OffensiveBallInterceptionCalc extends ACalculator
{
	@Configurable(defValue = "0.1", comment = "Maximum expected time the vision might deviate from the real world", unit = EConfigUnit.TIME_S)
	private static double maxExpectedVisionTimeDeviation = 0.1;

	@Configurable(defValue = "0.25", unit = EConfigUnit.TIME_S)
	private static double oldTargetInterceptionBaseBonus = 0.25;

	@Configurable(defValue = "0.12", unit = EConfigUnit.TIME_S)
	private static double timeToTrustPlannedPassInsteadOfKickFitState = 0.12;

	@Configurable(defValue = "500.0", comment = "Consider all bots to catch if deviation from planned pass to high", unit = EConfigUnit.DISTANCE_MM)
	private static double maxDeviationFromPlannedPass = 500;
	private final Supplier<Set<BotID>> potentialOffensiveBots;
	private final Supplier<BallPossession> ballPossession;
	private Supplier<Optional<OngoingPass>> ongoingPass;
	@Getter
	private Map<BotID, RatedBallInterception> ballInterceptions;
	private Map<BotID, PreviousBallInterception> previousInterceptionPositions = new HashMap<>();

	@Getter
	private Map<BotID, BallInterceptionInformation> ballInterceptionInformationMap;
	private boolean tigerDribblingBall;
	@Getter(AccessLevel.PRIVATE)
	private IBallTrajectory ballTrajectory;
	private boolean canOngoingPassBeTrusted;
	private BallInterceptor ballInterceptor;


	public OffensiveBallInterceptionCalc(
			Supplier<Set<BotID>> potentialOffensiveBots,
			Supplier<BallPossession> ballPossession,
			Supplier<Optional<OngoingPass>> ongoingPass
	)
	{
		ballInterceptor = new BallInterceptor(
				this::getOngoingPassInterceptionTarget,
				this::getPreviousInterceptionTarget,
				this::getBallTrajectory,
				Color.MAGENTA
		);
		this.potentialOffensiveBots = potentialOffensiveBots;
		this.ongoingPass = ongoingPass;
		this.ballPossession = ballPossession;
	}


	@Override
	protected boolean isCalculationNecessary()
	{
		var closestTiger = potentialOffensiveBots.get().stream()
				.map(e -> getWFrame().getBot(e))
				.min(Comparator.comparingDouble(
						e -> e.getBotKickerPos().distanceTo(getBall().getPos())));
		boolean tigerHasBall = closestTiger.map(e -> e.getBallContact().hadContact(0.1)).orElse(false)
				&& getBall().getVel().getLength() < 2.0;

		return ballInterceptor.ballIsNotUnderOurControl(getWFrame()) &&
				Geometry.getField().isPointInShape(getBall().getPos()) &&
				ballPossession.get().getOpponentBallControl() != EBallControl.STRONG &&
				!tigerHasBall;
	}


	public boolean isTigerDribblingBall()
	{
		return tigerDribblingBall;
	}


	@Override
	protected void reset()
	{
		tigerDribblingBall = true;
		ballTrajectory = null;
		previousInterceptionPositions = Collections.emptyMap();
		ballInterceptionInformationMap = Collections.emptyMap();
		ballInterceptions = Collections.emptyMap();
		canOngoingPassBeTrusted = true;
	}


	@Override
	public void doCalc()
	{
		final List<IDrawableShape> shapes = getShapes(EAiShapesLayer.OFFENSE_BALL_INTERCEPTION);
		tigerDribblingBall = false;
		ballTrajectory = findBallTrajectory();
		ballInterceptor.setOldTargetInterceptionBonus(calculateOldTargetInterceptionBaseBonus());
		ballInterceptor.setAreaOfInterest(Geometry.getField());
		ballInterceptor.setConsideredBots(consideredBots());
		ballInterceptor.setExclusionAreas(List.of(
				Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius() + Geometry.getBallRadius()),
				Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius() + Geometry.getBallRadius())
		));
		if (OffensiveConstants.isAllowTrajectoryTricksWhileReceiving())
		{
			ballInterceptor.setFinderParams(List.of(
					new InterceptionFinderParameters(-0.35, false, 0),
					new InterceptionFinderParameters(0, true, OffensiveConstants.getMaxPlannedBotReceiveVelocity())
			));
		} else
		{
			ballInterceptor.setFinderParams(List.of(
					new InterceptionFinderParameters(-0.35, false, 0),
					new InterceptionFinderParameters(0.2, false, 0)
			));
		}
		previousInterceptionPositions = invalidatePreviousInterceptionPositions();

		var result = ballInterceptor.processFrame(getWFrame());

		ballInterceptions = result.interceptionMap();
		ballInterceptionInformationMap = result.informationMap();
		shapes.addAll(ballInterceptor.getShapes());

		for (var interception : ballInterceptions.values())
		{
			var botPos = getWFrame().getBot(interception.getBallInterception().getBotID()).getPos();
			var direction = Vector2.fromPoints(botPos, interception.getBallInterception().getPos());
			shapes.add(new DrawableArrow(botPos, direction, Color.CYAN).setStrokeWidth(20));
		}

		previousInterceptionPositions = ballInterceptions
				.entrySet()
				.stream()
				.collect(Collectors.toUnmodifiableMap(
						Map.Entry::getKey,
						e -> new PreviousBallInterception(
								e.getValue().getBallInterception().getPos(), e.getValue().isOvershoot())
				));

		for (var pair : previousInterceptionPositions.entrySet())
		{
			var botPos = getWFrame().getBot(pair.getKey()).getPos();
			var direction = Vector2.fromPoints(botPos, pair.getValue());
			shapes.add(new DrawableArrow(botPos, direction, Color.GREEN).setStrokeWidth(20));
		}
	}


	private double calculateOldTargetInterceptionBaseBonus()
	{
		if (canOngoingPassBeTrusted)
		{
			return oldTargetInterceptionBaseBonus;
		}
		var kickedBall = getWFrame().getKickedBall();
		if (kickedBall.isEmpty())
		{
			return oldTargetInterceptionBaseBonus;
		}

		var timeSinceKick = (getWFrame().getTimestamp() - kickedBall.get().getKickTimestamp()) / 1e9;
		return SumatraMath.relative(timeSinceKick, 0, timeToTrustPlannedPassInsteadOfKickFitState)
				* oldTargetInterceptionBaseBonus;
	}


	private Set<BotID> consideredBots()
	{
		return ongoingPass.get()
				.filter(e -> isOngoingPassDeviationFromBallTrajWithinErrorMargin(e, getBall().getTrajectory()))
				.map(OngoingPass::getPass)
				.map(RatedPass::getPass)
				.map(Pass::getReceiver)
				.filter(id -> getWFrame().getBots().containsKey(id))
				.map(Set::of)
				.orElse(potentialOffensiveBots.get());
	}


	private Map<BotID, PreviousBallInterception> invalidatePreviousInterceptionPositions()
	{
		var previousAttacker = getAiFrame().getPrevFrame().getTacticalField()
				.getOffensiveStrategy().getAttackerBot();

		return previousInterceptionPositions.entrySet().stream()
				.filter(e -> previousAttacker.isPresent() && previousAttacker.get() == e.getKey())
				.filter(e -> ballMovesTowardsPreviousInterceptionPoint(e.getValue()))
				.collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
	}


	private Optional<PreviousBallInterception> getPreviousInterceptionTarget(BotID botID)
	{
		return Optional.ofNullable(previousInterceptionPositions.get(botID));
	}


	private Optional<PreviousBallInterception> getOngoingPassInterceptionTarget(BotID botID)
	{
		return ongoingPass.get()
				.map(OngoingPass::getPass)
				.filter(pass -> pass.getPass().getReceiver().equals(botID))
				.map(RatedPass::getPass)
				.map(Pass::getKick)
				// We assume here that all planned passes are non overshoot, this information should better be
				// stored in the pass object and then put in here. For now its fine to assume passes are planned as non overshoot
				.map(e -> new PreviousBallInterception(e.getTarget(), false));
	}


	private boolean ballMovesTowardsPreviousInterceptionPoint(IVector2 previousInterceptionPoint)
	{
		var maxExpectedBallMovingDistance = ballTrajectory.getDistByTime(maxExpectedVisionTimeDeviation);
		if (ballTrajectory.getInitialPos().getXYVector().distanceTo(previousInterceptionPoint)
				< maxExpectedBallMovingDistance)
		{
			return true;
		}
		var ballDir = ballTrajectory.getTravelLine().directionVector();
		var ballToPreviousInterceptionPoint = previousInterceptionPoint.subtractNew(getBall().getPos());
		return (ballDir.angleToAbs(ballToPreviousInterceptionPoint).orElse(0.0) < AngleMath.deg2rad(15));
	}


	private boolean isOngoingPassDeviationFromBallTrajWithinErrorMargin(
			OngoingPass ongoingPass,
			IBallTrajectory trajectory
	)
	{
		boolean canBallTrajBeTrusted = (getWFrame().getTimestamp() - ongoingPass.getKickStartTime()) / 1e9
				>= timeToTrustPlannedPassInsteadOfKickFitState;
		if (!canBallTrajBeTrusted)
		{
			return true;
		}

		IVector2 plannedTarget = ongoingPass.getPass().getPass().getKick().getTarget();
		return trajectory.getTravelLine().closestPointOnPath(plannedTarget).distanceTo(plannedTarget)
				< maxDeviationFromPlannedPass;
	}


	private IBallTrajectory findBallTrajectory()
	{
		var ongoingPassTrajectory = ongoingPass.get()
				.filter(og -> (getWFrame().getTimestamp() - og.getKickStartTime()) / 1e9
						< timeToTrustPlannedPassInsteadOfKickFitState)
				.map(this::createBallTrajectoryFromOngoingPass);
		canOngoingPassBeTrusted = ongoingPassTrajectory.isPresent();
		return ongoingPassTrajectory.orElseGet(() -> getBall().getTrajectory());
	}


	private IBallTrajectory createBallTrajectoryFromOngoingPass(OngoingPass ongoingPass)
	{
		var timePassedSinceKick = (getWFrame().getTimestamp() - ongoingPass.getKickStartTime()) / 1e9;
		var pass = ongoingPass.getPass();
		var traj = Geometry.getBallFactory().createTrajectoryFromKickedBallWithoutSpin(
				pass.getPass().getKick().getSource(),
				pass.getPass().getKick().getKickVel().multiplyNew(1000)
		);
		return Geometry.getBallFactory().createTrajectoryFromState(
				traj.getMilliStateAtTime(timePassedSinceKick)
		);
	}


	public Optional<Boolean> canOngoingPassBeTrusted()
	{
		return Optional.of(canOngoingPassBeTrusted);
	}
}
