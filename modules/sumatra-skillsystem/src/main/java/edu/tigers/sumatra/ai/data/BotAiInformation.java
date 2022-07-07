/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.statemachine.IState;
import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;


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


	public void setBattery(final double percent)
	{
		map.put(EBotInformation.BATTERY, Integer.toString((int) (percent * 100)));
	}


	public void setVersion(final String version)
	{
		map.put(EBotInformation.VERSION, version);
	}


	public void setHwId(final int id)
	{
		map.put(EBotInformation.HW_ID, String.valueOf(id));
	}


	public void setLastFeedback(final long t)
	{
		map.put(EBotInformation.LAST_FEEDBACK, String.valueOf(t));
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


	public void setSkill(final String skill)
	{
		map.put(EBotInformation.SKILL, skill);
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


	public void setPrimaryDirection(final IVector2 primaryDirection)
	{
		map.put(EBotInformation.PRIMARY_DIR, primaryDirection.getSaveableString());
	}


	public void setMaxProcTime(final double maxProcTime)
	{
		map.put(EBotInformation.MAX_PROC_TIME, String.valueOf(Math.round(maxProcTime * 1000)));
	}


	public void setAvgProcTime(final double avgProcTime)
	{
		map.put(EBotInformation.AVG_PROC_TIME, String.valueOf(Math.round(avgProcTime * 1000)));
	}


	public void setBotStats(final BaseStationWifiStats.BotStats stats)
	{
		map.put(EBotInformation.RF_TX_PACKETS, String.valueOf(stats.rf.txPackets));
		map.put(EBotInformation.RF_RX_PACKETS, String.valueOf(stats.rf.rxPackets));
		map.put(EBotInformation.RF_BOT_RSSI, String.valueOf(stats.getBotRssi()));
		map.put(EBotInformation.RF_BS_RSSI, String.valueOf(stats.getBsRssi()));
		map.put(EBotInformation.RF_LINK_QUALITY, vel2Str(stats.getLinkQuality() * 100.0));
	}
}
