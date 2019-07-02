/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.botmanager.sim;

import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.botskills.EBotSkill;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillCircleBallSim;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillFastGlobalPositionSim;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillGlobalPositionSim;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillGlobalVelXyPosWSim;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillGlobalVelocitySim;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillInput;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillLocalForceSim;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillLocalVelocitySim;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillMotorsOffSim;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillOutput;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillPenaltyShooterSim;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillSineSim;
import edu.tigers.sumatra.botmanager.sim.skills.BotSkillWheelVelocitySim;
import edu.tigers.sumatra.botmanager.sim.skills.IBotSkillSim;


/**
 * Execute bot skills for simulation. Converts bot skill input to movement commands
 */
public class BotSkillSimulator
{
	private static final Logger log = Logger.getLogger(BotSkillSimulator.class.getName());
	
	private final Map<EBotSkill, IBotSkillSim> botSkills = new EnumMap<>(EBotSkill.class);
	private EBotSkill lastBotSkill = EBotSkill.MOTORS_OFF;
	private BotSkillOutput lastBotSkillOutput = BotSkillOutput.Builder.create().empty().build();
	
	
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
		botSkills.put(EBotSkill.LOCAL_FORCE, new BotSkillLocalForceSim());
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
}
