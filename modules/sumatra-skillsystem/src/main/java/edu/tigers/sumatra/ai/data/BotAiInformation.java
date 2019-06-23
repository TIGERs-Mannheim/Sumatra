/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.IState;


/**
 * Dataholder for bot information from AI
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 3)
public class BotAiInformation
{
	private final Map<EBotInformation, String> map = new EnumMap<>(EBotInformation.class);
	
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");
	
	
	/**
	 * @return the map containing all info
	 */
	public Map<EBotInformation, String> getMap()
	{
		return Collections.unmodifiableMap(map);
	}
	
	
	/**
	 * @return the play
	 */
	public final String getPlay()
	{
		return StringUtils.defaultIfBlank(map.get(EBotInformation.PLAY), "");
	}
	
	
	public String getRole()
	{
		return StringUtils.defaultIfBlank(map.get(EBotInformation.ROLE), "");
	}
	
	
	public String getRoleState()
	{
		return StringUtils.defaultIfBlank(map.get(EBotInformation.ROLE_STATE), "");
	}
	
	
	public void setBattery(final double percent)
	{
		map.put(EBotInformation.BATTERY, Integer.toString((int) (percent * 100)));
	}
	
	
	public void setVersion(final String version)
	{
		map.put(EBotInformation.VERSION, version);
	}
	
	
	public void setKickerCharge(final double voltage)
	{
		map.put(EBotInformation.KICKER_CHARGE, DECIMAL_FORMAT.format(voltage));
	}
	
	
	public void setBrokenFeatures(final Set<EFeature> features)
	{
		map.put(EBotInformation.BROKEN_FEATURES, StringUtils.join(features, ","));
	}
	
	
	public void setBallContact(final String contact)
	{
		map.put(EBotInformation.BALL_CONTACT, contact);
	}
	
	
	public void setKickerSpeed(final double speed)
	{
		map.put(EBotInformation.KICKER_SPEED, vel2Str(speed));
	}
	
	
	public void setKickerDevice(final EKickerDevice device)
	{
		map.put(EBotInformation.KICKER_DEVICE, device.name());
	}
	
	
	public void setDribblerSpeed(final double speed)
	{
		map.put(EBotInformation.DRIBBLER_SPEED, Integer.toString((int) speed));
	}
	
	
	public void setVelocityCurrent(final double velocity)
	{
		map.put(EBotInformation.VELOCITY_CURRENT, vel2Str(velocity));
	}
	
	
	public void setVelocityMax(final double velocityMax)
	{
		map.put(EBotInformation.VELOCITY_MAX, vel2Str(velocityMax));
	}
	
	
	public void setVelocityLimit(final double velocityLimit)
	{
		map.put(EBotInformation.VELOCITY_LIMIT, vel2Str(velocityLimit));
	}
	
	
	public void setAccelerationLimit(final double accelerationLimit)
	{
		map.put(EBotInformation.ACCELERATION_LIMIT, vel2Str(accelerationLimit));
	}
	
	
	public void setPlay(final String play)
	{
		map.put(EBotInformation.PLAY, play);
	}
	
	
	public void setRole(final String role)
	
	{
		map.put(EBotInformation.ROLE, role);
	}
	
	
	public void setSkill(final ESkill skill)
	{
		map.put(EBotInformation.SKILL, skill.name());
	}
	
	
	private String vel2Str(final double vel)
	{
		return DECIMAL_FORMAT.format(vel);
	}
	
	
	public void setSkillState(final IState skillState)
	{
		map.put(EBotInformation.SKILL_STATE, skillState == null ? "" : skillState.getIdentifier());
	}
	
	
	public void setRoleState(final IState roleState)
	{
		map.put(EBotInformation.ROLE_STATE, roleState == null ? "" : roleState.getIdentifier());
	}
	
	
	public void setBotSkill(final String botSkill)
	{
		map.put(EBotInformation.BOT_SKILL, botSkill);
	}
	
	
	public void setMaxProcTime(final double maxProcTime)
	{
		map.put(EBotInformation.MAX_PROC_TIME, String.valueOf(Math.round(maxProcTime * 1000)));
	}
	
	
	public void setAvgProcTime(final double avgProcTime)
	{
		map.put(EBotInformation.AVG_PROC_TIME, String.valueOf(Math.round(avgProcTime * 1000)));
	}
}
