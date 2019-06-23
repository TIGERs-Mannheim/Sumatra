/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.06.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillEncTrain;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillGlobalPosVel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillGlobalPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillGlobalVelocity;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillPenaltyShoot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillPositionPid;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillTrajCtrl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillTunePid;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableEnum;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableParameter;


/**
 * @author AndreR
 */
public enum EBotSkill implements IInstanceableEnum
{
	/** */
	MOTORS_OFF(0, new InstanceableClass(BotSkillMotorsOff.class)),
	/** */
	LOCAL_VELOCITY(1, new InstanceableClass(BotSkillLocalVelocity.class,
			new InstanceableParameter(IVector2.class, "xy", "0,0"),
			new InstanceableParameter(Float.TYPE, "w", "0"))),
	/** */
	GLOBAL_VELOCITY(2, new InstanceableClass(BotSkillGlobalVelocity.class,
			new InstanceableParameter(IVector2.class, "xy", "0,0"),
			new InstanceableParameter(Float.TYPE, "w", "0"))),
	/** */
	GLOBAL_POSITION(3, new InstanceableClass(BotSkillGlobalPosition.class,
			new InstanceableParameter(IVector2.class, "dest", "0,0"),
			new InstanceableParameter(Float.TYPE, "orient", "0"),
			new InstanceableParameter(Float.TYPE, "t", "-1"))),
	// /** */
	// BOT_SKILL_SINE(4, new InstanceableClass(BotSkillGlobalPosition.class)),
	/** */
	ENC_TRAIN(5, new InstanceableClass(BotSkillEncTrain.class,
			new InstanceableParameter(IVector2.class, "startPos", "-2500,-1000"),
			new InstanceableParameter(IVector2.class, "endPos", "-500,-1000"),
			new InstanceableParameter(Integer.TYPE, "firstAngleSteps", "20"),
			new InstanceableParameter(Integer.TYPE, "secondAngleSteps", "60"),
			new InstanceableParameter(Integer.TYPE, "xyRepeat", "3"))),
	/** */
	TUNE_PID(6, new InstanceableClass(BotSkillTunePid.class)),
	/**  */
	GLOBAL_POS_VEL(7, new InstanceableClass(BotSkillGlobalPosVel.class,
			new InstanceableParameter(IVector2.class, "pos xy", "0,0"),
			new InstanceableParameter(Float.TYPE, "orient", "0"),
			new InstanceableParameter(IVector3.class, "vel xyw", "0,0,0"))),
	/**  */
	PENALTY_SHOOT(8, new InstanceableClass(BotSkillPenaltyShoot.class,
			new InstanceableParameter(IVector2.class, "penalty pos", "0,0"),
			new InstanceableParameter(IVector2.class, "ball position", "0,0"),
			new InstanceableParameter(Integer.class, "time to shoot", "0"),
			new InstanceableParameter(BotSkillPenaltyShoot.EPenaltyShootFlags.class, "flag1", "NO_OP"),
			new InstanceableParameter(BotSkillPenaltyShoot.EPenaltyShootFlags.class, "flag2", "NO_OP"),
			new InstanceableParameter(BotSkillPenaltyShoot.EPenaltyShootFlags.class, "flag3", "NO_OP")
			// ,new InstanceableParameter(BotSkillPenaltyShoot.EPenaltyShootFlags.class, "flag4", "NO_OP")
			)),
	/**  */
	POSITION_PID(9, new InstanceableClass(BotSkillPositionPid.class)),
	/**  */
	TRAJ_CTRL(10, new InstanceableClass(BotSkillTrajCtrl.class));
	
	private final InstanceableClass	clazz;
	private final int						id;
	
	
	/**
	 */
	private EBotSkill(final int id, final InstanceableClass clazz)
	{
		this.clazz = clazz;
		this.id = id;
	}
	
	
	/**
	 * @return the paramImpls
	 */
	@Override
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
	
	
	/**
	 * @return
	 */
	public Class<?> getClazz()
	{
		return clazz.getImpl();
	}
	
	
	/**
	 * @return
	 */
	public int getId()
	{
		return id;
	}
}
