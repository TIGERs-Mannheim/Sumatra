/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.botskills.sim;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.EDataAcquisitionMode;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillInput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillOutput;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqBotModel;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqDelays;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqMotorModel;
import edu.tigers.sumatra.control.motor.MatrixMotorModel;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BotSkillSimulator
{
	private static final Logger				log						= Logger.getLogger(BotSkillSimulator.class.getName());
	
	private Map<EBotSkill, IBotSkillSim>	botSkills				= new EnumMap<>(EBotSkill.class);
	private EBotSkill								lastBotSkill			= EBotSkill.MOTORS_OFF;
	private BotSkillOutput						lastBotSkillOutput	= BotSkillOutput.Builder.create().empty()
			.build();
	
	
	/**
	 * Create simulator.
	 */
	public BotSkillSimulator()
	{
		botSkills.put(EBotSkill.CIRCLE_BALL, new BotSkillCircleBallSim());
		botSkills.put(EBotSkill.FAST_GLOBAL_POSITION, new BotSkillFastGlobalPositionSim());
		botSkills.put(EBotSkill.GLOBAL_POSITION, new BotSkillGlobalPositionSim());
		botSkills.put(EBotSkill.GLOBAL_VELOCITY, new BotSkillGlobalVelocitySim());
		botSkills.put(EBotSkill.GLOBAL_VEL_XY_POS_W, new BotSkillGlobalVelXyPosWSim());
		botSkills.put(EBotSkill.LOCAL_VELOCITY, new BotSkillLocalVelocitySim());
		botSkills.put(EBotSkill.MOTORS_OFF, new BotSkillMotorsOffSim());
		botSkills.put(EBotSkill.BOT_SKILL_SINE, new BotSkillSineSim());
		botSkills.put(EBotSkill.WHEEL_VELOCITY, new BotSkillWheelVelocitySim());
		botSkills.put(EBotSkill.PENALTY_SHOOTER_SKILL, new BotSkillPenaltyShooterSim());
	}
	
	
	/**
	 * Execute a bot skill.
	 * 
	 * @param input bot skill input with pos/vel/acc and timestamp.
	 * @return bot skill output with drive/kicker/dribbler infos
	 */
	public BotSkillOutput execute(final BotSkillInput input)
	{
		ABotSkill botSkill = input.getSkill();
		if (!botSkills.containsKey(botSkill.getType()))
		{
			log.error("Unsupported bot skill in simulator: " + botSkill.getType());
			return lastBotSkillOutput;
		}
		
		IBotSkillSim sim = botSkills.get(botSkill.getType());
		
		if (botSkill.getType() != lastBotSkill)
		{
			sim.init(input);
			lastBotSkill = botSkill.getType();
		}
		
		lastBotSkillOutput = botSkills.get(botSkill.getType()).execute(input);
		return lastBotSkillOutput;
	}
	
	
	public BotSkillOutput getLastBotSkillOutput()
	{
		return lastBotSkillOutput;
	}
	
	
	/**
	 * Parse data acquisition mode and generate according commands.
	 * 
	 * @param input
	 * @param mode
	 * @return
	 */
	@SuppressWarnings("squid:S1151")
	public static List<ACommand> parseDataAcquisitionMode(final BotSkillInput input, final EDataAcquisitionMode mode)
	{
		List<ACommand> cmds = new ArrayList<>();
		
		switch (mode)
		{
			case BOT_MODEL:
				TigerDataAcqBotModel bm = new TigerDataAcqBotModel();
				bm.setTimestamp(input.gettNow() / 1000);
				bm.setVisionTime(input.gettNow() / 1000);
				bm.setOutVelocity(input.getCurVelLocal().toArray());
				bm.setVisionPosition(
						Vector3.from2d(input.getCurPos().getXYVector().multiplyNew(0.001), input.getCurPos().z()).toArray());
				cmds.add(bm);
				break;
			case DELAYS:
				TigerDataAcqDelays del = new TigerDataAcqDelays();
				del.setTimestamp(input.gettNow() / 1000);
				del.setVisionTime(input.gettNow() / 1000);
				del.setOutVelocityW(input.getCurVelLocal().z());
				del.setVisionPositionW(input.getCurPos().z());
				del.setGyroVelocityW(input.getCurVelLocal().z());
				cmds.add(del);
				break;
			case MOTOR_MODEL:
				TigerDataAcqMotorModel amm = new TigerDataAcqMotorModel();
				amm.setTimestamp(input.gettNow() / 1000);
				
				MatrixMotorModel mm = new MatrixMotorModel();
				IVectorN wheelSpeed = mm.getWheelSpeed(input.getCurVelLocal());
				amm.setMotorVelocity(wheelSpeed.multiplyNew(50.0 / 15.0).toArray());
				amm.setMotorVoltage(wheelSpeed.multiplyNew(((50.0 / 15.0) * 60.0) / (380.0 * 2.0 * Math.PI)).toArray());
				cmds.add(amm);
				break;
			default:
				break;
		}
		
		return cmds;
	}
}
