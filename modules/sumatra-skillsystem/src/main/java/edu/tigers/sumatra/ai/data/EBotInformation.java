/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data;

/**
 * Bot information for bots table
 */
public enum EBotInformation
{
	BATTERY("Battery [V]"),
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
	BOT_SKILL("Bot Skill"),
	PRIMARY_DIR("Primary Dir"),
	AVG_PROC_TIME("Avg Proc Time [ms]"),
	MAX_PROC_TIME("Max Proc Time [ms]"),
	VERSION("SW Version"),
	HW_ID("HW ID"),
	LAST_FEEDBACK("Last feedback"),
	NRF_TX_LOSS("Nrf tx loss"),
	NRF_TX_PACKETS("Nrf tx packets"),
	NRF_RX_LOSS("Nrf rx loss"),
	NRF_RX_PACKETS("Nrf rx packets"),
	NRF_LINK_QUALITY("Nrf link quality"),
	NRF_PACKETS_MAX_RT("Nrf packets max rt"),
	NRF_PACKETS_ACKED("Nrf packets acked"),

	;


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
