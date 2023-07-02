/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data;

/**
 * Bot information for bots table
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
	DRIBBLE_CURRENT("Dribble current [A]"),
	DRIBBLE_CURRENT_MAX("Dribble current max [A]"),
	DRIBBLE_TRACTION("Dribble traction"),
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
	RF_TX_PACKETS("RF Tx packets"),
	RF_RX_PACKETS("RF Rx packets"),
	RF_LINK_QUALITY("RF link quality"),
	RF_BOT_RSSI("RF bot RSSI"),
	RF_BS_RSSI("RF BS RSSI"),

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
