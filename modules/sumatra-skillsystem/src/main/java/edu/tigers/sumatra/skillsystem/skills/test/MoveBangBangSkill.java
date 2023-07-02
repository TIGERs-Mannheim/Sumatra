/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills.test;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.botskills.BotSkillWheelVelocity;
import edu.tigers.sumatra.botmanager.botskills.EBotSkill;
import edu.tigers.sumatra.control.motor.MatrixMotorModel;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.BotMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.trajectory.ITrajectory;
import lombok.Setter;

import java.util.Optional;


/**
 * Move along simple default trajectories from current position towards a destination with a certain type
 * of bot skill.
 */
public class MoveBangBangSkill extends AMoveSkill
{
	private final IVector2 dest;
	private final double orient;
	private final EBotSkill botSkill;

	@Setter
	private boolean rollOut;
	@Setter
	private Double velMax;
	@Setter
	private Double accMax;

	private ITrajectory<IVector2> trajXY;
	private ITrajectory<Double> trajW;
	private long tStart;
	private boolean motorsOff;
	private MoveConstraints moveConstraints;


	/**
	 * @param dest     [mm]
	 * @param orient   [rad]
	 * @param botSkill
	 */
	public MoveBangBangSkill(final IVector2 dest, final double orient, final EBotSkill botSkill)
	{
		this(dest, orient, botSkill, false);
	}


	/**
	 * @param dest     [mm]
	 * @param orient   [rad]
	 * @param botSkill
	 * @param rollOut
	 */
	public MoveBangBangSkill(final IVector2 dest, final double orient, final EBotSkill botSkill, final boolean rollOut)
	{
		this.dest = dest;
		this.orient = orient;
		this.botSkill = botSkill;
		this.rollOut = rollOut;
	}


	@Override
	public void doEntryActions()
	{
		moveConstraints = defaultMoveConstraints();
		Optional.ofNullable(accMax).ifPresent(moveConstraints::setAccMax);
		Optional.ofNullable(velMax).ifPresent(moveConstraints::setVelMax);

		trajXY = TrajectoryGenerator.generatePositionTrajectory(getTBot(), dest, moveConstraints);
		trajW = TrajectoryGenerator.generateRotationTrajectory(getTBot(), orient, moveConstraints);
		tStart = getWorldFrame().getTimestamp();
		motorsOff = false;
	}


	@Override
	public void doUpdate()
	{
		double t = (getWorldFrame().getTimestamp() - tStart) / 1e9;
		IVector2 vel = trajXY.getVelocity(t);

		if (rollOut)
		{
			IVector2 acc = trajXY.getAcceleration(t);
			if (  vel.getLength() > velMax / 2.0 &&
					acc.getLength() > accMax / 2.0 &&
					vel.angleToAbs(acc).orElse(0.0) > AngleMath.PI_HALF + AngleMath.PI_QUART)
			{
				motorsOff = true;
			}
		}

		if (motorsOff)
		{
			setLocalForce(Vector2f.ZERO_VECTOR, 0.0);
			return;
		}

		double rot = trajW.getVelocity(t);
		switch (botSkill)
		{
			case GLOBAL_POSITION -> setTargetPose(dest, orient, moveConstraints);
			case GLOBAL_VELOCITY -> setGlobalVelocity(vel, rot, moveConstraints);
			case LOCAL_VELOCITY ->
					setLocalVelocity(BotMath.convertGlobalBotVector2Local(vel, trajW.getPosition(t)), rot, moveConstraints);
			case WHEEL_VELOCITY ->
			{
				MatrixMotorModel motorModel = new MatrixMotorModel();
				IVectorN motors = motorModel.getWheelSpeed(Vector3.from2d(vel, rot));
				BotSkillWheelVelocity skill = new BotSkillWheelVelocity(motors.toArray());
				getMatchCtrl().setSkill(skill);
			}
			default -> throw new IllegalArgumentException("Unsupported bot skill: " + botSkill);
		}
	}
}
