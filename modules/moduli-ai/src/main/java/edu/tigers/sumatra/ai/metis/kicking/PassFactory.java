/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.kicking;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


public class PassFactory
{
	@Getter
	@Configurable(defValue = "3.2", comment = "Default maximum ball speed when receiving the ball")
	private static double defaultMaxReceivingBallSpeedStraight = 3.2;

	@Getter
	@Configurable(defValue = "2.5", comment = "Default maximum ball speed when receiving the ball")
	private static double defaultMaxReceivingBallSpeedChip = 2.5;

	@Configurable(defValue = "0.1", comment = "Minimum receiving speed")
	private static double minReceivingSpeed = 0.1;

	static
	{
		ConfigRegistration.registerClass("metis", PassFactory.class);
	}

	private final KickSpeedFactory kickSpeedFactory = new KickSpeedFactory();
	private final KickFactory kickFactory = new KickFactory();
	private WorldFrame worldFrame;

	@Setter
	private Double maxReceivingBallSpeed;


	public void update(WorldFrame worldFrame)
	{
		this.worldFrame = worldFrame;
		kickFactory.update(worldFrame);
	}


	private double getMaxReceivingBallSpeedStraight()
	{
		return maxReceivingBallSpeed == null ? defaultMaxReceivingBallSpeedStraight : maxReceivingBallSpeed;
	}


	private double getMaxReceivingBallSpeedChip()
	{
		return maxReceivingBallSpeed == null ? defaultMaxReceivingBallSpeedChip : maxReceivingBallSpeed;
	}


	public void setAimingTolerance(double aimingTolerance)
	{
		kickFactory.setAimingTolerance(aimingTolerance);
	}


	/**
	 * Get chip and straight passes.
	 *
	 * @param source          the pass origin, e.g. the ball position
	 * @param target          the location where the ball is passed to
	 * @param shooter         the robot that performs the pass
	 * @param receiver        the robot that receives the pass
	 * @param minPassDuration the minimum duration that the ball must travel till it reaches the target
	 * @param preparationTime the time that the shooter will need to prepare the pass
	 * @return
	 */
	public List<Pass> passes(
			IVector2 source,
			IVector2 target,
			BotID shooter,
			BotID receiver,
			double minPassDuration,
			double preparationTime,
			EBallReceiveMode receiveMode
	)
	{
		var straightPass = straight(source, target, shooter, receiver, minPassDuration, preparationTime, receiveMode);
		var chipPass = chip(source, target, shooter, receiver, minPassDuration, preparationTime, receiveMode);
		return Stream.of(straightPass, chipPass)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();
	}


	/**
	 * Create a pass with a chip kick.
	 *
	 * @param source   the pass origin, e.g. the ball position
	 * @param target   the location where the ball is passed to
	 * @param shooter  the robot that performs the pass
	 * @param receiver the robot that receives the pass
	 * @return
	 */
	public Optional<Pass> chip(IVector2 source, IVector2 target, BotID shooter, BotID receiver,
			EBallReceiveMode receiveMode)
	{
		return chip(source, target, shooter, receiver, 0.0, 0.0, receiveMode);
	}


	/**
	 * Create a pass with a chip kick.
	 *
	 * @param source          the pass origin, e.g. the ball position
	 * @param target          the location where the ball is passed to
	 * @param shooter         the robot that performs the pass
	 * @param receiver        the robot that receives the pass
	 * @param minPassDuration the minimum duration that the ball must travel till it reaches the target
	 * @param preparationTime the time that the shooter will need to prepare the pass
	 * @return
	 */
	public Optional<Pass> chip(
			IVector2 source,
			IVector2 target,
			BotID shooter,
			BotID receiver,
			double minPassDuration,
			double preparationTime,
			EBallReceiveMode receiveMode
	)
	{
		var distance = source.distanceTo(target);
		var shooterBot = worldFrame.getBot(shooter);
		var maxSpeed = kickSpeedFactory.maxChip(shooterBot);
		var speed = kickSpeedFactory.chip(distance, getMaxReceivingBallSpeedChip(), maxSpeed, minPassDuration);
		var consultant = worldFrame.getBall().getChipConsultant();
		var duration = consultant.getTimeForKick(distance, speed);
		var receivingSpeed = consultant.getVelForKickByTime(speed, duration);

		if (duration < minPassDuration
				|| receivingSpeed < minReceivingSpeed)
		{
			return Optional.empty();
		}

		var kick = kickFactory.chip(source, target, speed);

		return Optional.of(new Pass(
				kick,
				receiver,
				shooter,
				receivingSpeed,
				duration,
				preparationTime,
				receiveMode
		));
	}


	/**
	 * Create a pass with a straight kick.
	 *
	 * @param source   the pass origin, e.g. the ball position
	 * @param target   the location where the ball is passed to
	 * @param shooter  the robot that performs the pass
	 * @param receiver the robot that receives the pass
	 * @return
	 */
	public Optional<Pass> straight(IVector2 source, IVector2 target, BotID shooter, BotID receiver,
			EBallReceiveMode receiveMode)
	{
		return straight(source, target, shooter, receiver, 0.0, 0.0, receiveMode);
	}


	/**
	 * Create a pass with a straight kick.
	 *
	 * @param source          the pass origin, e.g. the ball position
	 * @param target          the location where the ball is passed to
	 * @param shooter         the robot that performs the pass
	 * @param receiver        the robot that receives the pass
	 * @param minPassDuration the minimum duration that the ball must travel till it reaches the target
	 * @param preparationTime the time that the shooter will need to prepare the pass
	 * @return
	 */
	public Optional<Pass> straight(
			IVector2 source,
			IVector2 target,
			BotID shooter,
			BotID receiver,
			double minPassDuration,
			double preparationTime,
			EBallReceiveMode receiveMode
	)
	{
		var distance = source.distanceTo(target);
		var shooterBot = worldFrame.getBot(shooter);
		var maxSpeed = kickSpeedFactory.maxStraight(shooterBot);
		var speed = kickSpeedFactory.straight(distance, getMaxReceivingBallSpeedStraight(), maxSpeed, minPassDuration);
		var duration = worldFrame.getBall().getStraightConsultant().getTimeForKick(distance, speed);
		var receivingSpeed = worldFrame.getBall().getStraightConsultant().getVelForKickByTime(speed, duration);

		if (duration + 0.01 < minPassDuration
				|| receivingSpeed < minReceivingSpeed)
		{
			return Optional.empty();
		}

		var kick = kickFactory.straight(source, target, speed);

		return Optional.of(new Pass(
				kick,
				receiver,
				shooter,
				receivingSpeed,
				duration,
				preparationTime,
				receiveMode
		));
	}
}
