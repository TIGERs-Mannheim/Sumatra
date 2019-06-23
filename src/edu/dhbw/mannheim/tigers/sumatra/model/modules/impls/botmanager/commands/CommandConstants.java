/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands;

/**
 * Constants for sections and commands.
 * 
 * @author AndreR
 * 
 */
public final class CommandConstants
{
	// Size
	/** */
	public static final int	HEADER_SIZE							= 4;
	
	// Sections
	/** */
	public static final int	SECTION_SYSTEM						= 0x00;
	/** */
	public static final int	SECTION_DECT						= 0x01;
	/** */
	public static final int	SECTION_MOTOR						= 0x02;
	/** */
	@Deprecated
	public static final int	SECTION_CT							= 0x03;
	/** */
	public static final int	SECTION_KICKER						= 0x04;
	/** */
	public static final int	SECTION_MOVEMENT					= 0x05;
	/** */
	public static final int	SECTION_MULTICAST					= 0x06;
	/** */
	public static final int	SECTION_CTRL						= 0x07;
	/** */
	public static final int	SECTION_BASE_STATION				= 0x08;
	/** */
	public static final int	SECTION_BOOTLOADER				= 0x09;
	
	// Commands
	// ### 03 - CT ###
	/** */
	@Deprecated
	public static final int	CMD_CT_SET_SPEED					= 0x0300;
	/** */
	@Deprecated
	public static final int	CMD_CT_STATUS						= 0x0301;
	/** */
	@Deprecated
	public static final int	CMD_CT_CALIBRATE					= 0x0302;
	/** */
	@Deprecated
	public static final int	CMD_CT_SET_PID						= 0x0303;
	/** */
	@Deprecated
	public static final int	CMD_CT_PID_HISTORY				= 0x0304;
	
	// ### 00 - SYSTEM ###
	/** */
	public static final int	CMD_SYSTEM_STATUS					= 0x0001;
	/** */
	public static final int	CMD_SYSTEM_POWER_LOG				= 0x0002;
	/** */
	public static final int	CMD_SYSTEM_STATUS_MOVEMENT		= 0x0003;
	/** */
	public static final int	CMD_SYSTEM_SET_IDENTITY			= 0x0004;
	/** */
	public static final int	CMD_SYSTEM_ANNOUNCEMENT			= 0x0005;
	/** */
	public static final int	CMD_SYSTEM_PING					= 0x0006;
	/** */
	public static final int	CMD_SYSTEM_PONG					= 0x0007;
	/** */
	public static final int	CMD_SYSTEM_BIG_PING				= 0x0008;
	/** */
	public static final int	CMD_SYSTEM_SET_LOGS				= 0x0009;
	/** */
	public static final int	CMD_SYSTEM_CONSOLE_PRINT		= 0x000A;
	/** */
	public static final int	CMD_SYSTEM_CONSOLE_COMMAND		= 0x000B;
	/** */
	public static final int	CMD_SYSTEM_STATUS_V2				= 0x0080;
	
	// ### 02 - MOTOR ###
	/** */
	@Deprecated
	public static final int	CMD_MOTOR_MOVE						= 0x0200;
	/** */
	public static final int	CMD_MOTOR_DRIBBLE					= 0x0201;
	/** */
	public static final int	CMD_MOTOR_SET_PID_PARAMS		= 0x0202;
	// manual, pid, automatic
	/** */
	public static final int	CMD_MOTOR_SET_MODE				= 0x0203;
	/** */
	public static final int	CMD_MOTOR_SET_MANUAL				= 0x0204;
	/** */
	public static final int	CMD_MOTOR_SET_PID_SP				= 0x0205;
	/** */
	public static final int	CMD_MOTOR_PID_LOG					= 0x0206;
	/** */
	public static final int	CMD_MOTOR_SET_PARAMS				= 0x0207;
	/** */
	public static final int	CMD_MOTOR_MOVE_V2					= 0x0208;
	
	// ### 04 - KICKER ###
	/** */
	@Deprecated
	public static final int	CMD_KICKER_KICKV1					= 0x0401;
	/** */
	@Deprecated
	public static final int	CMD_KICKER_STATUS					= 0x0402;
	/** */
	@Deprecated
	public static final int	CMD_KICKER_SET_AUTOLOAD			= 0x0403;
	/** */
	@Deprecated
	public static final int	CMD_KICKER_SET_MAX_CAP_LEVEL	= 0x0404;
	/** */
	@Deprecated
	public static final int	CMD_KICKER_CHARGE					= 0x0405;
	/** */
	@Deprecated
	public static final int	CMD_KICKER_SET_DUTY_CYCLE		= 0x0406;
	/** */
	public static final int	CMD_KICKER_KICKV2					= 0x0407;
	/** */
	public static final int	CMD_KICKER_STATUSV2				= 0x0408;
	/** */
	public static final int	CMD_KICKER_CHARGE_MANUAL		= 0x0409;
	/** */
	public static final int	CMD_KICKER_CHARGE_AUTO			= 0x040A;
	/** */
	public static final int	CMD_KICKER_IR_LOG					= 0x040B;
	/** */
	public static final int	CMD_KICKER_STATUSV3				= 0x040C;
	
	// ### 05 - MOVEMENT ###
	/** */
	@Deprecated
	public static final int	CMD_MOVEMENT_LIS3_LOG_RAW		= 0x0500;
	/** */
	public static final int	CMD_MOVEMENT_LIS3_LOG			= 0x0501;
	/** */
	public static final int	CMD_MOVEMENT_SET_PARAMS			= 0x0502;
	
	// ### 06 - MULTICAST ###
	/** */
	@Deprecated
	public static final int	CMD_MULTICAST_UPDATE_ALL		= 0x0600;
	/** */
	public static final int	CMD_MULTICAST_UPDATE_ALL_V2	= 0x0601;
	
	// ### 07 - CTRL ###
	/** */
	public static final int	CMD_CTRL_VISION_POS				= 0x0700;
	/** */
	public static final int	CMD_CTRL_SPLINE_2D				= 0x0701;
	/** */
	public static final int	CMD_CTRL_SPLINE_1D				= 0x0702;
	/** */
	public static final int	CMD_CTRL_SET_FILTER_PARAMS		= 0x0703;
	/** */
	public static final int	CMD_CTRL_SET_PID_PARAMS			= 0x0704;
	/** */
	public static final int	CMD_CTRL_SET_CONTROLLER_TYPE	= 0x0705;
	
	// ### 08 - BASE_STATION ###
	/** */
	public static final int	CMD_BASE_ACOMMAND					= 0x0800;
	/** */
	public static final int	CMD_BASE_PING						= 0x0801;
	/** */
	public static final int	CMD_BASE_AUTH						= 0x0802;
	/** */
	public static final int	CMD_BASE_STATS						= 0x0803;
	/** */
	public static final int	CMD_BASE_CONFIG					= 0x0804;
	/** */
	public static final int	CMD_BASE_VISION_CONFIG			= 0x0805;
	
	// ### 09 - BOOTLOADER ###
	/** */
	public static final int	CMD_BOOTLOADER_COMMAND			= 0x0900;
	/** */
	public static final int	CMD_BOOTLOADER_DATA				= 0x0901;
	/** */
	public static final int	CMD_BOOTLOADER_RESPONSE			= 0x0902;
	
	
	private CommandConstants()
	{
		
	}
}
