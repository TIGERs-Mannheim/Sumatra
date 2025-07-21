/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveActionType;
import edu.tigers.sumatra.ai.metis.offense.situation.zone.OffensiveZones;
import edu.tigers.sumatra.data.TimeLimitedBuffer;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.KickedBall;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * Track the ongoing pass by matching passes with the ball kick fit state.
 */
@Log4j2
public class OngoingPassCalc extends ACalculator
{
	@Configurable(defValue = "0.5", comment = "Time horizon [s] of the pass buffer")
	private static double passBufferHorizon = 0.5;

	@Configurable(defValue = "0.1", comment = "[s]")
	private static double timePassReceiverHasToArriveBeforePassToReceiveSuccessful = 0.1;

	@Configurable(defValue = "0.3", comment = "[m/s]")
	private static double maxVelToReceiveBallSuccessfully = 0.3;

	@Configurable(defValue = "10", comment = "Max passes to keep in buffer")
	private static int maxPassesToRemember = 10;

	@Configurable(defValue = "45.0", comment = "min Angle to accept new KickEvent direction [deg]")
	private static double maxAcceptableKickEventAngleChange = 45.0;

	@Configurable(defValue = "0.5", comment = "Min score to accept pass")
	private static double minBallKickWithPassFitScore = 0.5;

	@Configurable(defValue = "0.7", comment = "Min velocity [m/s] of the ball to consider")
	private static double minKickVelocity = 0.7;

	@Configurable(defValue = "0.2", comment = "Min kick fit age [s] after which passes can be rejected")
	private static double minKickFitAge = 0.2;

	@Configurable(defValue = "0.2", comment = "hyt bonus score [0,1] will be applied twice very early after the kick")
	private static double keepCurrentPassHyst = 0.2;

	private final TimeLimitedBuffer<Pass> passBuffer = new TimeLimitedBuffer<>();

	private OngoingPass ongoingPass;

	private OffensiveZones zones;

	private OffensiveZones.OffensiveZoneGeometry oldGeometry;

	private KickedBall oldKickFitState = null;


	@Override
	public void doCalc()
	{
		generateZoneGeometry();
		handlePassBufferContent();

		visualizePassBuffer(passBuffer.getElements());

		Optional<KickedBall> kickFitStateOpt = getWFrame().getKickedBall()
				.filter(b -> b.getAbsoluteKickSpeed() > minKickVelocity);
		if (noKickEventForUs() || kickFitStateOpt.isEmpty())
		{
			getShapes(EAiShapesLayer.OFFENSE_ONGOING_PASS).add(
					new DrawableAnnotation(getBall().getPos(), "Invalid KickFit", Vector2.fromY(20))
							.setColor(Color.BLACK));

			ongoingPass = null;
			oldKickFitState = null;
			return;
		}

		if (oldKickFitState != null &&
				oldKickFitState.getKickTimestamp() != kickFitStateOpt.get().getKickTimestamp())
		{
			getShapes(EAiShapesLayer.OFFENSE_ONGOING_PASS).add(
					new DrawableAnnotation(getBall().getPos(), "New KickFitEvent", Vector2.fromY(40))
							.setColor(Color.BLACK));
			ongoingPass = null;
		}

		var kickFitState = kickFitStateOpt.get();
		oldKickFitState = kickFitState;
		visualizeKickFitState(kickFitState);

		List<Pass> consideredPasses = new ArrayList<>(passBuffer.getElements());
		getOngoingPass().map(OngoingPass::getPass).ifPresent(consideredPasses::add);

		IVector2 averagePassTarget = getAveragePassTarget(consideredPasses, kickFitState);
		getShapes(EAiShapesLayer.OFFENSE_ONGOING_PASS).add(
				new DrawableCircle(Circle.createCircle(averagePassTarget, 40), Color.GREEN).setFill(true)
		);
		getShapes(EAiShapesLayer.OFFENSE_ONGOING_PASS).add(
				new DrawableCircle(Circle.createCircle(averagePassTarget, 30), Color.RED).setFill(true)
		);

		if (isKickFitStateTrustworthy(kickFitState))
		{
			// after some time we trust the kick fit state and choose the pass close to the kick fit state
			ongoingPass = consideredPasses.stream()
					.filter(pass -> getWFrame().getTigerBotsAvailable().containsKey(pass.getReceiver()))
					.max(Comparator.comparing(p -> ballKickWithPassFitScore(kickFitState, p)))
					.filter(pass -> acceptPass(kickFitState, pass))
					.map(this::updatePass)
					.orElse(null);
		} else
		{
			// initially we do not trust the kick fit state and only consider passes that are close to the average pass target
			ongoingPass = consideredPasses.stream()
					.filter(pass -> getWFrame().getTigerBotsAvailable().containsKey(pass.getReceiver()))
					.min(Comparator.comparing(e -> e.getKick().getTarget().distanceTo(averagePassTarget)))
					.filter(pass -> isOngoingPassTrustworthy(pass, kickFitState))
					.map(this::updatePass)
					.orElse(null);
		}

		getOngoingPass().ifPresent(this::drawOngoingPass);
	}


	private void generateZoneGeometry()
	{
		OffensiveZones.OffensiveZoneGeometry geometry = new OffensiveZones.OffensiveZoneGeometry(Geometry.getFieldWidth(),
				Geometry.getFieldLength(), Geometry.getPenaltyAreaTheir().getPosCorner(),
				Geometry.getPenaltyAreaTheir().getNegCorner());
		if (oldGeometry == null || !oldGeometry.equals(geometry))
		{
			zones = OffensiveZones.generateDefaultOffensiveZones(geometry);
			oldGeometry = geometry;
		}
	}


	private void handlePassBufferContent()
	{
		passBuffer.setMaxElements(maxPassesToRemember);
		passBuffer.setMaxDuration(passBufferHorizon);

		boolean isPassAction = getAiFrame().getPrevFrame().getTacticalField().getOffensiveStrategy()
				.getAttackerBot()
				.stream()
				.map(botId -> getAiFrame().getPrevFrame().getTacticalField().getOffensiveActions().get(botId))
				.filter(Objects::nonNull)
				.anyMatch(ratedAction -> ratedAction.getAction().getType() == EOffensiveActionType.PASS);

		var activePass = getActivePass();

		if (isPassAction)
		{
			activePass
					.filter(p -> getWFrame().getBots().containsKey(p.getShooter()))
					.ifPresent(this::addPassIfOrientationMatches);
		}
		passBuffer.reduceByAbsoluteDuration(getWFrame().getTimestamp());
	}


	private IVector2 getAveragePassTarget(List<Pass> consideredPasses, KickedBall kickFitState)
	{
		IVector2 sumOfPassTargets = Vector2.zero();
		int numOfUsedPasses = 0;
		for (Pass pass : consideredPasses)
		{
			IVector2 passTarget = pass.getKick().getTarget();

			double angleDiff = kickFitState.getKickVel().getXYVector()
					.angleToAbs(pass.getKick().getKickVel().getXYVector())
					.orElse(AngleMath.PI_TWO);
			if (angleDiff < AngleMath.deg2rad(maxAcceptableKickEventAngleChange))
			{
				sumOfPassTargets = sumOfPassTargets.addNew(passTarget);
				numOfUsedPasses++;
			}
		}
		return numOfUsedPasses > 0 ? sumOfPassTargets.multiplyNew(1.0 / numOfUsedPasses) : Vector2.zero();
	}


	/**
	 * Only add passes that could actually be played at this moment
	 *
	 * @param pass
	 */
	private void addPassIfOrientationMatches(Pass pass)
	{
		var shooter = getWFrame().getBot(pass.getShooter());
		if (shooter == null)
		{
			return;
		}
		double shooterOrientation = shooter.getOrientation();
		double angleDiff = AngleMath.diffAbs(pass.getKick().getKickVel().getXYVector().getAngle(), shooterOrientation);
		if (angleDiff < AngleMath.deg2rad(30))
		{
			passBuffer.add(getWFrame().getTimestamp(), pass);
		}
	}


	private void visualizePassBuffer(List<Pass> consideredPasses)
	{
		consideredPasses.forEach(e ->
				getShapes(EAiShapesLayer.OFFENSE_ONGOING_PASS)
						.add(new DrawableCircle(Circle.createCircle(e.getKick().getTarget(), 30), Color.ORANGE).setFill(true)
						));
	}


	private void visualizeKickFitState(KickedBall kickFitState)
	{
		getShapes(EAiShapesLayer.OFFENSE_ONGOING_PASS).add(
				new DrawableArrow(kickFitState.getKickPos(), kickFitState.getKickVel().multiplyNew(1000).getXYVector())
						.setColor(Color.DARK_GRAY)
		);
	}


	private boolean acceptPass(KickedBall kickFitState, Pass pass)
	{
		if (!isKickFitStateTrustworthy(kickFitState))
		{
			return true;
		}
		return ballKickWithPassFitScore(kickFitState, pass) > minBallKickWithPassFitScore;
	}


	private boolean isKickFitStateTrustworthy(KickedBall kickFitState)
	{
		return (getWFrame().getTimestamp() - kickFitState.getKickTimestamp()) / 1e9 > minKickFitAge;
	}


	public Optional<OngoingPass> getOngoingPass()
	{
		return Optional.ofNullable(ongoingPass);
	}


	private OngoingPass updatePass(Pass pass)
	{
		if (ongoingPass != null && ongoingPass.getPass().equals(pass))
		{
			// ongoingPass is still valid
			var receivingBot = getWFrame().getTiger(pass.getReceiver());
			var receiverKickerPos = receivingBot.getBotKickerPos();
			var ballPos = getBall().getPos();
			if (!ongoingPass.isPassLineBeenReachedByReceiver() &&
					getBall().getTrajectory().getTravelLine().distanceTo(receiverKickerPos) < Geometry.getBotRadius() &&
					receivingBot.getVel().getLength2() < maxVelToReceiveBallSuccessfully &&
					getWFrame().getBall().getTrajectory().getTimeByDist(ballPos.distanceTo(pass.getKick().getTarget()))
							> timePassReceiverHasToArriveBeforePassToReceiveSuccessful)
			{
				// update ongoing pass with ball reached Information
				return ongoingPass
						.toBuilder()
						.passLineBeenReachedByReceiver(true)
						.build();
			}
			return ongoingPass;
		}

		long startTime = getWFrame().getTimestamp();
		var originatingZone = zones.getZoneByPoint(pass.getKick().getSource());
		var targetZone = zones.getZoneByPoint(pass.getKick().getTarget());

		if (originatingZone.isEmpty() || targetZone.isEmpty())
		{
			return ongoingPass;
		}

		// create a new ongoing pass
		var eOriginatingZone = originatingZone.get();
		var eZoneName = targetZone.get().getZoneName();
		return OngoingPass.builder()
				.pass(pass)
				.kickStartTime(startTime)
				.originatingZone(eOriginatingZone.getZoneName())
				.targetZone(eZoneName)
				.build();
	}


	private double ballKickWithPassFitScore(KickedBall ballKickFitState, Pass pass)
	{
		double angleDiff = ballKickFitState.getKickVel().getXYVector()
				.angleToAbs(pass.getKick().getKickVel().getXYVector())
				.orElse(AngleMath.PI_TWO);

		double score = SumatraMath.relative(angleDiff, AngleMath.deg2rad(maxAcceptableKickEventAngleChange), 0);
		var currentOngoingPass = getOngoingPass();
		if (currentOngoingPass.isPresent() && currentOngoingPass.get().getPass().equals(pass))
		{
			score += keepCurrentPassHyst;
		}
		return Math.min(1, score);
	}


	private boolean noKickEventForUs()
	{
		return getWFrame().getKickedBall()
				.filter(e -> e.getKickingBot().getTeamColor() == getWFrame().getTeamColor())
				.isEmpty();
	}


	private boolean isOngoingPassTrustworthy(Pass pass, KickedBall kickFitState)
	{
		var timeSinceKick = (getWFrame().getTimestamp() - kickFitState.getKickTimestamp()) / 1e9;
		if (timeSinceKick < 0.3 * minKickFitAge)
		{
			return true;
		} else
		{
			var adaptionOverTime =
					keepCurrentPassHyst * SumatraMath.relative(timeSinceKick, 0.3 * minKickFitAge, minKickFitAge);
			return ballKickWithPassFitScore(kickFitState, pass) > minBallKickWithPassFitScore + adaptionOverTime;
		}
	}


	private Optional<Pass> getActivePass()
	{
		var keeperPass = getAiFrame().getPrevFrame().getTacticalField().getKeeperPass();
		if (keeperPass != null)
		{
			return Optional.of(keeperPass);
		}
		return getAiFrame().getPrevFrame().getTacticalField().getOffensiveStrategy()
				.getAttackerBot()
				.map(e -> getAiFrame().getPrevFrame().getTacticalField().getOffensiveActions().get(e))
				.map(e -> e.getAction().getPass());
	}


	private void drawOngoingPass(OngoingPass pass)
	{
		IVector2 kickOrigin = pass.getPass().getKick().getSource();
		IVector2 kickTarget = pass.getPass().getKick().getTarget();
		IVector2 kickLine = kickTarget.subtractNew(kickOrigin);
		getShapes(EAiShapesLayer.OFFENSE_ONGOING_PASS).add(
				new DrawableAnnotation(kickOrigin, pass.getOriginatingZone().name(), Color.BLUE));
		getShapes(EAiShapesLayer.OFFENSE_ONGOING_PASS).add(
				new DrawableAnnotation(kickTarget, pass.getTargetZone().name(), Color.BLUE));
		getShapes(EAiShapesLayer.OFFENSE_ONGOING_PASS).add(
				new DrawableArrow(kickOrigin, kickLine)
						.setColor(new Color(0, 255, 191, 100))
		);
		getShapes(EAiShapesLayer.OFFENSE_ONGOING_PASS).addAll(
				pass.getPass().createDrawables().stream()
						.map(s -> s.setColor(new Color(0, 255, 191, 100)))
						.toList()
		);
	}

}
