/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.sim.skills;

import edu.tigers.sumatra.botmanager.botskills.BotSkillPenaltyShooter;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.VectorN;
import edu.tigers.sumatra.sim.dynamics.bot.EDriveMode;


/**
 * @author arne
 */
public class BotSkillPenaltyShooterSim implements IBotSkillSim
{
	
	private double currentAngleDifference = 0;
	private long startTime = 0;
	private BotSkillPenaltyShooter penaltyShooter;
	private IVector2 startPos = null;
	
	private EBotSkillPenaltyShooterSimState state;
	
	private enum EBotSkillPenaltyShooterSimState
	{
		SLOW_MOVE,
		THREATENING_REST,
		TURN,
		KICK,
		FINISHED
	}
	
	
	@Override
	public BotSkillOutput execute(final BotSkillInput input)
	{
		BotSkillOutput.Builder outputBuilder = BotSkillOutput.Builder.create();
		outputBuilder.kickDevice(EKickerDevice.STRAIGHT);
		outputBuilder.kickMode(EKickerMode.NONE);
		outputBuilder.driveLimits(penaltyShooter.getMoveConstraints());
		if (startPos == null)
		{
			startPos = input.getState().getPose().getPos();
		}
		
		switch (state)
		{
			case SLOW_MOVE:
				performSlowMoveState(input, outputBuilder);
				break;
			case THREATENING_REST:
				performThreateningRestState(input, outputBuilder);
				break;
			case TURN:
				performTurnState(input, outputBuilder);
				break;
			case KICK:
				performKick(outputBuilder);
				break;
			case FINISHED:
				startPos = null;
				break;
		}
		return outputBuilder.build();
	}
	
	
	private void performTurnState(final BotSkillInput input, final BotSkillOutput.Builder outputBuilder)
	{
		outputBuilder.modeXY(EDriveMode.LOCAL_VEL);
		outputBuilder.modeW(EDriveMode.LOCAL_VEL);
		outputBuilder.targetVelLocal(Vector3.from2d(penaltyShooter.getSpeedInTurn().multiplyNew(1e3),
				penaltyShooter.getRotationSpeed()));
		double nextAngleDifference = AngleMath
				.normalizeAngle(penaltyShooter.getTargetAngle() - input.getState().getPose().getOrientation());
		if (nextAngleDifference * currentAngleDifference < .0)
		{
			state = EBotSkillPenaltyShooterSimState.KICK;
		}
		currentAngleDifference = nextAngleDifference;
	}
	
	
	private void performThreateningRestState(final BotSkillInput input, final BotSkillOutput.Builder outputBuilder)
	{
		outputBuilder.modeXY(EDriveMode.WHEEL_VEL);
		outputBuilder.modeW(EDriveMode.WHEEL_VEL);
		outputBuilder.targetWheelVel(VectorN.from(0, 0, 0, 0));
		if (input.gettNow() - startTime > penaltyShooter.getTimeToShoot())
		{
			state = EBotSkillPenaltyShooterSimState.TURN;
		}
	}
	
	
	private void performSlowMoveState(final BotSkillInput input, final BotSkillOutput.Builder outputBuilder)
	{
		outputBuilder.modeXY(EDriveMode.LOCAL_VEL);
		outputBuilder.modeW(EDriveMode.LOCAL_VEL);
		outputBuilder.targetVelLocal(Vector3.fromXYZ(0, penaltyShooter.getApproachSpeed() * 1000, 0));
		if (input.getState().isBarrierInterrupted())
		{
			state = EBotSkillPenaltyShooterSimState.THREATENING_REST;
			startTime = input.gettNow();
		}
	}
	
	
	private void performKick(BotSkillOutput.Builder builder)
	{
		builder.modeXY(EDriveMode.OFF);
		builder.modeW(EDriveMode.OFF);
		builder.kickDevice(EKickerDevice.STRAIGHT);
		builder.kickMode(EKickerMode.FORCE);
		builder.kickSpeed(penaltyShooter.getPenaltyKickSpeed());
	}
	
	
	@Override
	public void init(final BotSkillInput input)
	{
		penaltyShooter = (BotSkillPenaltyShooter) input.getSkill();
		currentAngleDifference = AngleMath
				.normalizeAngle(penaltyShooter.getTargetAngle() - input.getState().getPose().getOrientation());
		state = EBotSkillPenaltyShooterSimState.SLOW_MOVE;
	}
}
