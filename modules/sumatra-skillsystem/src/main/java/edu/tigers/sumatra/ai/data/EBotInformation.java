/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data;

/**
 * Bot information for bots table
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EBotInformation
{
	BATTERY("Battery [%]"),
	KICKER_CHARGE("Kicker [V]"),
	BROKEN_FEATURES("Broken Features"),
	BALL_CONTACT("Ball Contact"),
	KICKER_SPEED("Kicker Speed [m/s]"),
	KICKER_DEVICE("Kicker Device"),
	DRIBBLER_SPEED("Dribbler Speed [rpm]"),
	VELOCITY_CURRENT("Velocity [m/s]"),
	VELOCITY_MAX("Max Velocity [m/s]"),
	VELOCITY_LIMIT("Velocity Limit [m/s]"),
	ACCELERATION_LIMIT("Acceleration Limit [m/s²]"),
	PLAY("Play"),
	ROLE("Role"),
	ROLE_STATE("Role State"),
	SKILL("Skill"),
	SKILL_STATE("Skill State"),
	BOT_SKILL("Bot Skill");
	
	
	private final String label;
	
	
	EBotInformation(final String label)
	{
		this.label = label;
	}
	
	
	public String getLabel()
	{
		return label;
	}
}
