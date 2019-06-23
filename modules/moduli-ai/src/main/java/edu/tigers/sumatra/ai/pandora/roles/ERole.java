/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s): ChristianK
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;

import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.KeeperRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.PenaltyKeeperRole;
import edu.tigers.sumatra.ai.pandora.roles.motorlearner.MotorLearnerRole;
import edu.tigers.sumatra.ai.pandora.roles.motorlearner.MotorLearnerRole.EMotorOptimizer;
import edu.tigers.sumatra.ai.pandora.roles.motorlearner.MotorTrajSamplerRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveBallToRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.tigers.sumatra.ai.pandora.roles.offense.EpicPenaltyShooterRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.KickoffShooterRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.ai.pandora.roles.test.CalibCtrlRole;
import edu.tigers.sumatra.ai.pandora.roles.test.ChipKickTrainerRole;
import edu.tigers.sumatra.ai.pandora.roles.test.ChipKickTrainerV2Role;
import edu.tigers.sumatra.ai.pandora.roles.test.DestChangedTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.InterceptionRole;
import edu.tigers.sumatra.ai.pandora.roles.test.KickSkillTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.MoveStressTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.MoveTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.ReceiveTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.RedirectRole;
import edu.tigers.sumatra.ai.pandora.roles.test.RedirectTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.SimpleShooterRole;
import edu.tigers.sumatra.ai.pandora.roles.test.StraightKickTrainerRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.PrimaryPlacementRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.SecondaryPlacementRole;
import edu.tigers.sumatra.control.motor.EMotorModel;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.sampler.velocity.EVelocitySampler;
import edu.tigers.sumatra.skillsystem.skills.ReceiverSkill.EReceiverMode;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Enumeration that represents the different {@link ARole} When added a new role do not forget to adjust role factory in
 * {@link edu.tigers.sumatra.ai.lachesis.Lachesis}.
 * 
 * @author Gero, ChristianK
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum ERole implements IInstanceableEnum
{
	// main
	/**  */
	OFFENSIVE(new InstanceableClass(OffensiveRole.class)),
	/**  */
	KICKOFF_SHOOTER(new InstanceableClass(KickoffShooterRole.class)),
	/**  */
	SUPPORT(new InstanceableClass(SupportRole.class)),
	/** */
	INTERCEPTION(new InstanceableClass(InterceptionRole.class)),
	/** */
	DEFENDER(new InstanceableClass(DefenderRole.class)),
	/** */
	KEEPER(new InstanceableClass(KeeperRole.class)),
	/**  */
	EPIC_PENALTY_SHOOTER(new InstanceableClass(EpicPenaltyShooterRole.class)),
	
	
	// movement
	/** */
	MOVE(new InstanceableClass(MoveRole.class,
			new InstanceableParameter(EMoveBehavior.class, "moveBehavior", "NORMAL"))),
	/**  */
	MOVE_BALL_TO(new InstanceableClass(MoveBallToRole.class, new InstanceableParameter(IVector2.class, "ballTarget",
			"0,0"))),
	
	
	// Standards
	/** */
	PENALTY_KEEPER(new InstanceableClass(PenaltyKeeperRole.class)),
	
	/** */
	PRIMARY_AUTOMATED_THROW_IN(new InstanceableClass(PrimaryPlacementRole.class)),
	/** */
	SECONDARY_AUTOMATED_THROW_IN(new InstanceableClass(SecondaryPlacementRole.class)),
	
	// test
	/**  */
	MOTOR_LEARNER(new InstanceableClass(MotorLearnerRole.class,
			new InstanceableParameter(IVector3.class, "init pos/orient", "-2800,-1700,pi/2+pi/4"),
			new InstanceableParameter(IVector3.class, "target vel", "1,0,0"),
			new InstanceableParameter(Double.TYPE, "sampleTime", "0.5"),
			new InstanceableParameter(Double.TYPE, "delayTime", "0.5"),
			new InstanceableParameter(Double.TYPE, "acc", "2"),
			new InstanceableParameter(EMotorOptimizer.class, "optimizer", "LM"))),
	/**  */
	MOTOR_SAMPLER(new InstanceableClass(MotorLearnerRole.class,
			new InstanceableParameter(IVector3.class, "init pos/orient", "-2800,-1700,pi/2+pi/4"),
			new InstanceableParameter(Double.TYPE, "sampleTime", "0.5"),
			new InstanceableParameter(Double.TYPE, "delayTime", "0.5"),
			new InstanceableParameter(Double.TYPE, "acc", "2"),
			new InstanceableParameter(Double.TYPE, "motorNoise", "0"),
			new InstanceableParameter(EMotorModel.class, "motorModel", "MATRIX"),
			new InstanceableParameter(EVelocitySampler.class, "velSampler", "CONTINOUS"))),
	/**  */
	MOTOR_FULL_OPTIMIZER(new InstanceableClass(MotorLearnerRole.class,
			new InstanceableParameter(IVector3.class, "init pos/orient", "-2800,-1700,pi/2"),
			new InstanceableParameter(Double.TYPE, "sampleTime", "0.5"),
			new InstanceableParameter(Double.TYPE, "delayTime", "0.5"),
			new InstanceableParameter(Double.TYPE, "acc", "2"),
			new InstanceableParameter(EMotorModel.class, "motorModel", "MATRIX"),
			new InstanceableParameter(EVelocitySampler.class, "velSampler", "CONTINOUS"),
			new InstanceableParameter(EMotorOptimizer.class, "optimizer", "CLEVER_ONE"))),
	/**  */
	MOTOR_GP_SAMPLER(new InstanceableClass(MotorLearnerRole.class,
			new InstanceableParameter(IVector3.class, "init pos/orient", "-2800,-1700,pi/2+pi/4"),
			new InstanceableParameter(Double.TYPE, "sampleTime", "0.5"),
			new InstanceableParameter(Double.TYPE, "delayTime", "0.5"),
			new InstanceableParameter(Double.TYPE, "acc", "2"),
			new InstanceableParameter(Integer.TYPE, "numSamples", "50"))),
	/**  */
	MOTOR_EVALUATOR(new InstanceableClass(MotorLearnerRole.class,
			new InstanceableParameter(IVector3.class, "init pos/orient", "-2800,-1700,pi/2+pi/4"),
			new InstanceableParameter(IVector3.class, "target vel", "1,0,0"),
			new InstanceableParameter(Double.TYPE, "sampleTime", "0.5"),
			new InstanceableParameter(Double.TYPE, "delayTime", "0.5"),
			new InstanceableParameter(Double.TYPE, "acc", "2"),
			new InstanceableParameter(EMotorModel.class, "motorModel", "MATRIX"))),
	/**  */
	MOTOR_TRAJ_SAMPLER(new InstanceableClass(MotorTrajSamplerRole.class,
			new InstanceableParameter(IVector3.class, "initPos", "-2800,-1700,0"),
			new InstanceableParameter(IVector3.class, "finalPos", "-500,-500,0"),
			new InstanceableParameter(EMotorModel.class, "motorModel", "MATRIX"))),
	
	
	/**  */
	CALIB_CTRL(new InstanceableClass(CalibCtrlRole.class,
			new InstanceableParameter(IVector2.class, "initPos", "1500,1000"),
			new InstanceableParameter(Double.TYPE, "initOrientation", "0"),
			new InstanceableParameter(Double.TYPE, "dist", "2"),
			new InstanceableParameter(Double.TYPE, "acc", "1"),
			new InstanceableParameter(Double.TYPE, "startAngleDeg", "0"),
			new InstanceableParameter(Double.TYPE, "stopAngleDeg", "180"),
			new InstanceableParameter(Double.TYPE, "stepDeg", "10"),
			new InstanceableParameter(Double.TYPE, "iterations", "1"))),
	
	/**  */
	MOVE_TEST(new InstanceableClass(MoveTestRole.class,
			new InstanceableParameter(MoveTestRole.EMoveMode.class, "mode", "TRAJ_VEL"),
			new InstanceableParameter(IVector2.class, "initPos", "1500,1000"),
			new InstanceableParameter(Double.TYPE, "orientation", "0"),
			new InstanceableParameter(Double.TYPE, "scale", "1000"),
			new InstanceableParameter(Double.TYPE, "startAngleDeg", "0"),
			new InstanceableParameter(Double.TYPE, "stopAngleDeg", "180"),
			new InstanceableParameter(Double.TYPE, "stepAngleDeg", "10"),
			new InstanceableParameter(Double.TYPE, "angleTurn", "0"),
			new InstanceableParameter(Integer.TYPE, "iterations", "1"),
			new InstanceableParameter(String.class, "logFileName", ""))),
	/** */
	MOVE_TO_TEST(new InstanceableClass(MoveRole.class,
			new InstanceableParameter(IVector2.class, "destination", "1000,0"),
			new InstanceableParameter(Double.TYPE, "orientation", "0.0"))),
	/**  */
	MOVE_STRESS_TEST(new InstanceableClass(MoveStressTestRole.class)),
	/**  */
	DEST_CHANGED(new InstanceableClass(DestChangedTestRole.class, new InstanceableParameter(IVector2.class, "diffDest",
			"0,1000"), new InstanceableParameter(IVector2.class, "diffAngle", "1000,0"), new InstanceableParameter(
					Integer.TYPE, "freq", "30"))),
	/**  */
	SIMPLE_SHOOTER(new InstanceableClass(SimpleShooterRole.class,
			new InstanceableParameter(DynamicPosition.class, "passTarget", "4500,0"))),
	/**  */
	KICK_SKILL_TEST(new InstanceableClass(KickSkillTestRole.class,
			new InstanceableParameter(DynamicPosition.class, "passTarget", "4500,0"))),
	
	
	/**  */
	CHIP_KICK_TRAINER(new InstanceableClass(ChipKickTrainerRole.class, new InstanceableParameter(Integer.TYPE, "durLow",
			"2"), new InstanceableParameter(Integer.TYPE, "durHigh", "8"), new InstanceableParameter(Integer.TYPE,
					"dribbleLow", "5000"),
			new InstanceableParameter(Integer.TYPE, "dribbleHigh", "15000"))),
	/**  */
	CHIP_KICK_TRAINER_V2(new InstanceableClass(ChipKickTrainerV2Role.class, new InstanceableParameter(Integer.TYPE,
			"durLow",
			"2"), new InstanceableParameter(Integer.TYPE, "durHigh", "8"), new InstanceableParameter(Integer.TYPE,
					"dribbleLow", "5000"),
			new InstanceableParameter(Integer.TYPE, "dribbleHigh", "15000"))),
	
	/**  */
	STRAIGHT_KICK_TRAINER(new InstanceableClass(StraightKickTrainerRole.class,
			new InstanceableParameter(DynamicPosition.class, "target", "0,0"),
			new InstanceableParameter(Integer.TYPE, "durLow", "1000"),
			new InstanceableParameter(Integer.TYPE, "durHigh", "6000"))),
	/**  */
	REDIRECT(
			new InstanceableClass(RedirectRole.class, new InstanceableParameter(DynamicPosition.class, "target", "0,0"))),
	/**  */
	REDIRECT_TEST(
			new InstanceableClass(RedirectTestRole.class,
					new InstanceableParameter(DynamicPosition.class, "target", "0,0"),
					new InstanceableParameter(Boolean.class, "receive?", "false"))),
	/**  */
	RECEIVE_TEST(new InstanceableClass(ReceiveTestRole.class,
			new InstanceableParameter(IVector2.class, "dest", "1500,1500"),
			new InstanceableParameter(DynamicPosition.class, "target", "4500,0"),
			new InstanceableParameter(EReceiverMode.class, "mode", "STOP_DRIBBLER"))),;
	
	private final InstanceableClass clazz;
	
	
	/**
	 */
	private ERole(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}
	
	
	/**
	 * @return the paramImpls
	 */
	@Override
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
}
