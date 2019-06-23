/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
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
@Persistent(version = 2)
public class BotAiInformation
{
	private final Map<EBotInformation, String> map = new EnumMap<>(EBotInformation.class);
	
	
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
	
	
	public void setBattery(double percent)
	{
		map.put(EBotInformation.BATTERY, Integer.toString((int) (percent * 100)));
	}
	
	
	public void setKickerCharge(double voltage)
	{
		map.put(EBotInformation.KICKER_CHARGE, Integer.toString((int) voltage));
	}
	
	
	public void setBrokenFeatures(Set<EFeature> features)
	{
		map.put(EBotInformation.BROKEN_FEATURES, StringUtils.join(features, ","));
	}
	
	
	public void setBallContact(boolean contact)
	{
		map.put(EBotInformation.BALL_CONTACT, contact ? "YES" : "NO");
	}
	
	
	public void setKickerSpeed(double speed)
	{
		map.put(EBotInformation.KICKER_SPEED, vel2Str(speed));
	}
	
	
	public void setKickerDevice(EKickerDevice device)
	{
		map.put(EBotInformation.KICKER_DEVICE, device.name());
	}
	
	
	public void setDribblerSpeed(double speed)
	{
		map.put(EBotInformation.DRIBBLER_SPEED, Integer.toString((int) speed));
	}
	
	
	public void setVelocityCurrent(double velocity)
	{
		map.put(EBotInformation.VELOCITY_CURRENT, vel2Str(velocity));
	}
	
	
	public void setVelocityMax(double velocityMax)
	{
		map.put(EBotInformation.VELOCITY_MAX, vel2Str(velocityMax));
	}
	
	
	public void setVelocityLimit(double velocityLimit)
	{
		map.put(EBotInformation.VELOCITY_LIMIT, vel2Str(velocityLimit));
	}
	
	
	public void setAccelerationLimit(double accelerationLimit)
	{
		map.put(EBotInformation.ACCELERATION_LIMIT, vel2Str(accelerationLimit));
	}
	
	
	public void setPlay(String play)
	{
		map.put(EBotInformation.PLAY, play);
	}
	
	
	public void setRole(String role)
	
	{
		map.put(EBotInformation.ROLE, role);
	}
	
	
	public void setSkill(ESkill skill)
	{
		map.put(EBotInformation.SKILL, skill.name());
	}
	
	
	private String vel2Str(double vel)
	{
		return String.format(Locale.ENGLISH, "%.1f", vel);
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
}
