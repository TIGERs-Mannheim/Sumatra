/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.kicking;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Setter;

import java.util.List;


public class PassFactory
{
	@Configurable(defValue = "2.5", comment = "Default maximum ball speed when receiving the ball")
	private static double defaultMaxReceivingBallSpeed = 2.5;

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


	private double getMaxReceivingBallSpeed()
	{
		return maxReceivingBallSpeed == null ? defaultMaxReceivingBallSpeed : maxReceivingBallSpeed;
	}


	public void setAimingTolerance(double aimingTolerance)
	{
		kickFactory.setAimingTolerance(aimingTolerance);
	}


	/**
	 * Get chip and straight passes.
	 *
	 * @param source   the pass origin, e.g. the ball position
	 * @param target   the location where the ball is passed to
	 * @param shooter  the robot that performs the pass (to determine max kick speed, can be {@link BotID#noBot()})
	 * @param receiver the robot that receives the pass (stored in pass target, can be {@link BotID#noBot()})
	 * @return
	 */
	public List<Pass> passes(IVector2 source, IVector2 target, BotID shooter, BotID receiver,
			double minPassDuration)
	{
		Pass straightPass = straight(source, target, shooter, receiver, minPassDuration);
		Pass chipPass = chip(source, target, shooter, receiver, minPassDuration);
		return List.of(straightPass, chipPass);
	}


	/**
	 * Create a pass with a chip kick.
	 *
	 * @param source   the pass origin, e.g. the ball position
	 * @param target   the location where the ball is passed to
	 * @param shooter  the robot that performs the pass (to determine max kick speed, can be {@link BotID#noBot()})
	 * @param receiver the robot that receives the pass (stored in pass target, can be {@link BotID#noBot()})
	 * @return
	 */
	public Pass chip(IVector2 source, IVector2 target, BotID shooter, BotID receiver)
	{
		return chip(source, target, shooter, receiver, 0.0);
	}


	/**
	 * Create a pass with a chip kick.
	 *
	 * @param source          the pass origin, e.g. the ball position
	 * @param target          the location where the ball is passed to
	 * @param shooter         the robot that performs the pass (to determine max kick speed, can be {@link BotID#noBot()})
	 * @param receiver        the robot that receives the pass (stored in pass target, can be {@link BotID#noBot()})
	 * @param minPassDuration the minimum duration that the ball must travel till it reaches the target
	 * @return
	 */
	public Pass chip(IVector2 source, IVector2 target, BotID shooter, BotID receiver, double minPassDuration)
	{
		var distance = source.distanceTo(target);
		var shooterBot = worldFrame.getBot(shooter);
		var maxSpeed = kickSpeedFactory.maxChip(shooterBot);
		var speed = kickSpeedFactory.chip(distance, getMaxReceivingBallSpeed(), maxSpeed, minPassDuration);
		var consultant = worldFrame.getBall().getChipConsultant();
		var duration = consultant.getTimeForKick(distance, speed);
		var receivingSpeed = consultant.getVelForKickByTime(speed, duration);

		var kick = kickFactory.chip(source, target, speed);

		return Pass.builder()
				.kick(kick)
				.receiver(receiver)
				.receivingSpeed(receivingSpeed)
				.duration(duration)
				.shooter(shooter)
				.build();
	}


	/**
	 * Create a pass with a straight kick.
	 *
	 * @param source   the pass origin, e.g. the ball position
	 * @param target   the location where the ball is passed to
	 * @param shooter  the robot that performs the pass (to determine max kick speed, can be {@link BotID#noBot()})
	 * @param receiver the robot that receives the pass (stored in pass target, can be {@link BotID#noBot()})
	 * @return
	 */
	public Pass straight(IVector2 source, IVector2 target, BotID shooter, BotID receiver)
	{
		return straight(source, target, shooter, receiver, 0.0);
	}


	/**
	 * Create a pass with a straight kick.
	 *
	 * @param source          the pass origin, e.g. the ball position
	 * @param target          the location where the ball is passed to
	 * @param shooter         the robot that performs the pass (to determine max kick speed, can be {@link BotID#noBot()})
	 * @param receiver        the robot that receives the pass (stored in pass target, can be {@link BotID#noBot()})
	 * @param minPassDuration the minimum duration that the ball must travel till it reaches the target
	 * @return
	 */
	public Pass straight(IVector2 source, IVector2 target, BotID shooter, BotID receiver, double minPassDuration)
	{
		var distance = source.distanceTo(target);
		var shooterBot = worldFrame.getBot(shooter);
		var maxSpeed = kickSpeedFactory.maxStraight(shooterBot);
		var speed = kickSpeedFactory.straight(distance, getMaxReceivingBallSpeed(), maxSpeed, minPassDuration);
		var duration = worldFrame.getBall().getStraightConsultant().getTimeForKick(distance, speed);
		var receivingSpeed = worldFrame.getBall().getStraightConsultant().getVelForKickByTime(speed, duration);

		var kick = kickFactory.straight(source, target, speed);

		return Pass.builder()
				.kick(kick)
				.receiver(receiver)
				.receivingSpeed(receivingSpeed)
				.duration(duration)
				.shooter(shooter)
				.build();
	}
}
