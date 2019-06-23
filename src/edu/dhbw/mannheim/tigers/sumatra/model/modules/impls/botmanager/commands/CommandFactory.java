/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationAuth;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationPing;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationVisionConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeManual;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerIrLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerStatusV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorPidLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetManual;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetPidSp;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMovementLis3Log;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMulticastUpdateAllV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemAnnouncement;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemBigPing;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPing;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPong;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPowerLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetIdentity;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetLogs;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemStatusMovement;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderResponse;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetFilterParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetPIDParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSpline1D;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSpline2D;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlVisionPos;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerKickerStatusV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemStatusV2;


/**
 */
public final class CommandFactory
{
	// Logger
	private static final Logger	log	= Logger.getLogger(CommandFactory.class.getName());
	
	
	private CommandFactory()
	{
		
	}
	
	
	/**
	 * @param header
	 * @return
	 */
	public static ACommand createEmptyPacket(byte[] header)
	{
		if (header.length < CommandConstants.HEADER_SIZE)
		{
			log.error("Header too short");
			
			return null;
		}
		
		int command = ACommand.byteArray2UShort(header, 0);
		
		switch (command)
		{
			case CommandConstants.CMD_MOTOR_DRIBBLE:
				return new TigerDribble();
			case CommandConstants.CMD_MOTOR_PID_LOG:
				return new TigerMotorPidLog();
			case CommandConstants.CMD_MOTOR_SET_MANUAL:
				return new TigerMotorSetManual();
			case CommandConstants.CMD_MOTOR_SET_PID_SP:
				return new TigerMotorSetPidSp();
			case CommandConstants.CMD_MOTOR_SET_PARAMS:
				return new TigerMotorSetParams();
				
			case CommandConstants.CMD_KICKER_STATUSV2:
				return new TigerKickerStatusV2();
			case CommandConstants.CMD_KICKER_KICKV2:
				return new TigerKickerKickV2();
			case CommandConstants.CMD_KICKER_CHARGE_AUTO:
				return new TigerKickerChargeAuto();
			case CommandConstants.CMD_KICKER_CHARGE_MANUAL:
				return new TigerKickerChargeManual();
			case CommandConstants.CMD_KICKER_IR_LOG:
				return new TigerKickerIrLog();
			case CommandConstants.CMD_KICKER_STATUSV3:
				return new TigerKickerStatusV3();
				
			case CommandConstants.CMD_SYSTEM_STATUS_MOVEMENT:
				return new TigerSystemStatusMovement();
			case CommandConstants.CMD_SYSTEM_POWER_LOG:
				return new TigerSystemPowerLog();
			case CommandConstants.CMD_SYSTEM_SET_IDENTITY:
				return new TigerSystemSetIdentity();
			case CommandConstants.CMD_SYSTEM_ANNOUNCEMENT:
				return new TigerSystemAnnouncement();
			case CommandConstants.CMD_SYSTEM_PING:
				return new TigerSystemPing();
			case CommandConstants.CMD_SYSTEM_PONG:
				return new TigerSystemPong();
			case CommandConstants.CMD_SYSTEM_BIG_PING:
				return new TigerSystemBigPing();
			case CommandConstants.CMD_SYSTEM_SET_LOGS:
				return new TigerSystemSetLogs();
			case CommandConstants.CMD_SYSTEM_CONSOLE_PRINT:
				return new TigerSystemConsolePrint();
			case CommandConstants.CMD_SYSTEM_CONSOLE_COMMAND:
				return new TigerSystemConsoleCommand();
			case CommandConstants.CMD_SYSTEM_STATUS_V2:
				return new TigerSystemStatusV2();
				
			case CommandConstants.CMD_MOVEMENT_LIS3_LOG:
				return new TigerMovementLis3Log();
				
			case CommandConstants.CMD_MULTICAST_UPDATE_ALL_V2:
				return new TigerMulticastUpdateAllV2();
				
			case CommandConstants.CMD_CTRL_VISION_POS:
				return new TigerCtrlVisionPos();
			case CommandConstants.CMD_CTRL_SPLINE_2D:
				return new TigerCtrlSpline2D();
			case CommandConstants.CMD_CTRL_SPLINE_1D:
				return new TigerCtrlSpline1D();
			case CommandConstants.CMD_CTRL_SET_FILTER_PARAMS:
				return new TigerCtrlSetFilterParams();
			case CommandConstants.CMD_CTRL_SET_PID_PARAMS:
				return new TigerCtrlSetPIDParams();
			case CommandConstants.CMD_CTRL_SET_CONTROLLER_TYPE:
				return new TigerCtrlSetControllerType();
				
			case CommandConstants.CMD_BASE_ACOMMAND:
				return new BaseStationACommand();
			case CommandConstants.CMD_BASE_PING:
				return new BaseStationPing();
			case CommandConstants.CMD_BASE_AUTH:
				return new BaseStationAuth();
			case CommandConstants.CMD_BASE_STATS:
				return new BaseStationStats();
			case CommandConstants.CMD_BASE_VISION_CONFIG:
				return new BaseStationVisionConfig();
				
			case CommandConstants.CMD_BOOTLOADER_COMMAND:
				return new TigerBootloaderCommand();
			case CommandConstants.CMD_BOOTLOADER_DATA:
				return new TigerBootloaderData();
			case CommandConstants.CMD_BOOTLOADER_RESPONSE:
				return new TigerBootloaderResponse();
		}
		
		return null;
	}
	
	
	/**
	 * @param header
	 * @param data
	 * @return
	 */
	public static ACommand createPacket(byte[] header, byte[] data)
	{
		final ACommand cmd = createEmptyPacket(header);
		cmd.setData(data);
		
		return cmd;
	}
}
