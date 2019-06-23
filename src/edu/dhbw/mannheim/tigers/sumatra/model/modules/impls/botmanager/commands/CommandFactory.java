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

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTCalibrate;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTPIDHistory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTSetPID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTSetSpeed;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTStatus;
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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMovementLis3LogRaw;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMulticastUpdateAllV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemAnnouncement;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemBigPing;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPing;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPong;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPowerLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetIdentity;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetLogs;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemStatusMovement;


public class CommandFactory
{
	private static final Logger log = Logger.getLogger(CommandFactory.class);
	
	public static ACommand createEmptyPacket(byte[] header)
	{
		if (header.length < CommandConstants.HEADER_SIZE)
		{
			log.error("Header too short");

			return null;
		}

		int command = 0;
		command += ACommand.byte2Int(header[0]);
		command += ACommand.byte2Int(header[1]) << 8;

		switch (command)
		{
		case CommandConstants.CMD_CT_CALIBRATE: return new CTCalibrate();
		case CommandConstants.CMD_CT_SET_PID: return new CTSetPID();
		case CommandConstants.CMD_CT_SET_SPEED: return new CTSetSpeed();
		case CommandConstants.CMD_CT_STATUS: return new CTStatus();
		case CommandConstants.CMD_CT_PID_HISTORY: return new CTPIDHistory();
		
		case CommandConstants.CMD_MOTOR_DRIBBLE: return new TigerDribble();
		case CommandConstants.CMD_MOTOR_PID_LOG: return new TigerMotorPidLog();
		case CommandConstants.CMD_MOTOR_SET_MANUAL: return new TigerMotorSetManual();
		case CommandConstants.CMD_MOTOR_SET_PID_SP: return new TigerMotorSetPidSp();
		case CommandConstants.CMD_MOTOR_SET_PARAMS: return new TigerMotorSetParams();
		
		case CommandConstants.CMD_KICKER_STATUSV2: return new TigerKickerStatusV2();
		case CommandConstants.CMD_KICKER_KICKV2: return new TigerKickerKickV2();
		case CommandConstants.CMD_KICKER_CHARGE_AUTO: return new TigerKickerChargeAuto();
		case CommandConstants.CMD_KICKER_CHARGE_MANUAL: return new TigerKickerChargeManual();
		case CommandConstants.CMD_KICKER_IR_LOG: return new TigerKickerIrLog();
		
		case CommandConstants.CMD_SYSTEM_STATUS_MOVEMENT: return new TigerSystemStatusMovement();
		case CommandConstants.CMD_SYSTEM_POWER_LOG: return new TigerSystemPowerLog();
		case CommandConstants.CMD_SYSTEM_SET_IDENTITY: return new TigerSystemSetIdentity();
		case CommandConstants.CMD_SYSTEM_ANNOUNCEMENT: return new TigerSystemAnnouncement();
		case CommandConstants.CMD_SYSTEM_PING: return new TigerSystemPing();
		case CommandConstants.CMD_SYSTEM_PONG: return new TigerSystemPong();
		case CommandConstants.CMD_SYSTEM_BIG_PING: return new TigerSystemBigPing();
		case CommandConstants.CMD_SYSTEM_SET_LOGS: return new TigerSystemSetLogs();
		
		case CommandConstants.CMD_MOVEMENT_LIS3_LOG_RAW: return new TigerMovementLis3LogRaw();
		
		case CommandConstants.CMD_MULTICAST_UPDATE_ALL_V2: return new TigerMulticastUpdateAllV2();
		}

		return null;
	}
	
	public static ACommand createPacket(byte[] header, byte[] data)
	{
		ACommand cmd = createEmptyPacket(header);
		cmd.setData(data);
		
		return cmd;
	}
}
