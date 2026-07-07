package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.kicking.EBallReceiveMode;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.targetrater.RotationTimeHelper;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Setter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


public class PassCreator
{
	@Configurable(defValue = "0.3", comment = "The time that the pass receiver should get after it moved to the target pos", unit = EConfigUnit.TIME_S)
	private static double minPassReceiverPrepareTime = 0.3;

	@Configurable(defValue = "0.1", comment = "The time that the pass receiver should get after it moved to the target pos", unit = EConfigUnit.TIME_S)
	private static double minPassReceiverPrepareTimeThrough = 0.1;

	@Configurable(defValue = "1.0", comment = "min pass duration", unit = EConfigUnit.TIME_S)
	private static double defaultMinPassDuration = 1.0;

	static
	{
		ConfigRegistration.registerClass("metis", PassCreator.class);
	}

	private final PassFactory passFactory = new PassFactory();
	private WorldFrame wFrame;
	@Setter
	private double maxPlannedBotVelAtReceive = 0;


	public void update(WorldFrame wFrame)
	{
		this.wFrame = wFrame;
		passFactory.update(wFrame);
	}


	public List<Pass> createPasses(KickOrigin passOrigin, IVector2 pos, BotID receiver)
	{
		var shooter = passOrigin.getShooter();
		var origin = passOrigin.getPos();
		var prepTime = rotationTime(wFrame.getBot(shooter), pos.subtractNew(origin));
		var timeTillPassStarts = Math.max(prepTime, passOrigin.impactTimeOrZero());

		var receiverTimeStopping = timeUntilReceiverReachedTargetStopping(origin, pos, receiver);
		var minPassDuration = Math.max(
				receiverTimeStopping - timeTillPassStarts + minPassReceiverPrepareTime,
				defaultMinPassDuration
		);

		var ballPos = this.wFrame.getBall().getPos();

		boolean isRedirectable = OffensiveMath.getRedirectAngle(ballPos, passOrigin.getPos(), pos)
				< OffensiveConstants.getMaximumReasonableRedirectAngle();
		EBallReceiveMode receiveMode = isRedirectable ? EBallReceiveMode.REDIRECT : EBallReceiveMode.RECEIVE;

		var stoppingPasses = passFactory.passes(origin, pos, shooter, receiver, minPassDuration, prepTime, receiveMode);

		if (SumatraMath.isZero(maxPlannedBotVelAtReceive) || wFrame.getBot(receiver) == null)
		{
			return stoppingPasses;
		}

		var minPassDurationThrough = Math.max(
				timeUntilReceiverReachedTargetMoving(origin, pos, receiver)
						- timeTillPassStarts
						+ minPassReceiverPrepareTimeThrough,
				defaultMinPassDuration
		);

		return Stream.concat(
				stoppingPasses.stream(),
				passFactory.through(origin, pos, shooter, receiver, minPassDurationThrough, prepTime, receiveMode)
						.filter(pass -> pass.getDuration() < receiverTimeStopping)
						.stream()
		).toList();
	}


	private double rotationTime(ITrackedBot shooter, IVector2 dir)
	{
		return RotationTimeHelper.calcRotationTime(
				shooter.getAngularVel(),
				shooter.getAngleByTime(0),
				dir.getAngle(),
				shooter.getMoveConstraints().getVelMaxW(),
				shooter.getMoveConstraints().getAccMaxW()
		);
	}


	private double timeUntilReceiverReachedTargetStopping(IVector2 origin, IVector2 pos, BotID botID)
	{
		return createTrajectoryToTarget(origin, pos, botID)
				.map(ITrajectory::getTotalTime)
				.orElse(0.0);
	}


	private double timeUntilReceiverReachedTargetMoving(IVector2 origin, IVector2 pos, BotID botID)
	{
		return createTrajectoryToTarget(origin, pos, botID)
				.map(this::estimateOvershootTimeSavings)
				.orElse(0.0);
	}


	private double estimateOvershootTimeSavings(ITrajectory<IVector2> traj)
	{
		var timeSections = traj.getTimeSections();
		if (timeSections.size() < 2)
		{
			return traj.getTotalTime();
		}

		var tBrakeStart = timeSections.get(timeSections.size() - 2);
		var tBrakeEnd = timeSections.getLast();
		var tBrakeDuration = tBrakeEnd - tBrakeStart;

		var aBrake = traj.getAcceleration((tBrakeStart + tBrakeEnd) / 2).getLength2();

		if (tBrakeDuration <= 0 || aBrake <= 0)
		{
			return 0.0;
		}

		// Subtract time difference between:
		// - breaking at the end of the trajectory
		// - continue driving with maxPlannedBotVelAtReceive
		return traj.getTotalTime() - Math.min(tBrakeDuration, maxPlannedBotVelAtReceive / aBrake) * 0.5;
	}


	private Optional<ITrajectory<IVector2>> createTrajectoryToTarget(IVector2 origin, IVector2 pos, BotID botID)
	{
		ITrackedBot tBot = wFrame.getBot(botID);
		if (tBot == null)
		{
			return Optional.empty();
		}
		IVector2 dest = LineMath.stepAlongLine(pos, origin, -(tBot.getCenter2DribblerDist() + Geometry.getBallRadius()));
		return Optional.of(TrajectoryGenerator.generatePositionTrajectory(tBot, dest));
	}

	public double getDefaultMinPassDuration()
	{
		return defaultMinPassDuration;
	}
}
