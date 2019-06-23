/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.10.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationAuth;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationConfigV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationEthStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationPing;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationWifiStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.LimitedVelocityCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.TigerKickerKickV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeManual;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerIrLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerStatusV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderCheckForUpdates;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlResetCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetFilterParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetPIDParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSpline1D;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSpline2D;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlVisionPos;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerKickerStatusV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSkillCalibrateMotor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSkillKeeperCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSkillPositioningCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSkillShooterCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemAck;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemFeatures;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemMatchCtrl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemMatchPosVel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemQuery;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemQuery.EQueryType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemStatusExt;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemStatusV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerBootloaderCrc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerBootloaderRequestCrc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerBootloaderRequestData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerBootloaderRequestSize;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerBootloaderSize;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerConfigFileStructure;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerConfigItemDesc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerConfigQueryFileList;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerConfigRead;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerConfigWrite;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerKickerConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerStatusFeedbackPid;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerSystemBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerSystemPerformance;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableEnum;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableParameter;


/**
 * List of all commands with their class type.
 * 
 * @author AndreR
 */
public enum ECommand implements IInstanceableEnum
{
	/**  */
	CMD_SKILL_KEEPER(0x0A00, new InstanceableClass(TigerSkillKeeperCommand.class)),
	/**  */
	CMD_SKILL_SHOOTER(0x0A02, new InstanceableClass(TigerSkillShooterCommand.class)),
	/**  */
	CMD_SKILL_POSITIONING(0x0A03, new InstanceableClass(TigerSkillPositioningCommand.class)),
	/**  */
	CMD_SKILL_CALIBRATE_MOTOR(0x0A04, new InstanceableClass(TigerSkillCalibrateMotor.class, new InstanceableParameter(
			Float.TYPE, "alpha", "1.0"), new InstanceableParameter(IVector3.class, "speed", "0.7,0,0"),
			new InstanceableParameter(Float.TYPE, "length", "1.0"))),
	
	// ### 00 - SYSTEM ###
	/** */
	CMD_SYSTEM_POWER_LOG(0x0002, new InstanceableClass(TigerSystemPowerLog.class)),
	/** */
	CMD_SYSTEM_STATUS_MOVEMENT(0x0003, new InstanceableClass(TigerSystemStatusMovement.class)),
	/** */
	CMD_SYSTEM_SET_IDENTITY(0x0004, new InstanceableClass(TigerSystemSetIdentity.class)),
	/** */
	CMD_SYSTEM_ANNOUNCEMENT(0x0005, new InstanceableClass(TigerSystemAnnouncement.class)),
	/** */
	CMD_SYSTEM_PING(0x0006, new InstanceableClass(TigerSystemPing.class)),
	/** */
	CMD_SYSTEM_PONG(0x0007, new InstanceableClass(TigerSystemPong.class)),
	/** */
	CMD_SYSTEM_BIG_PING(0x0008, new InstanceableClass(TigerSystemBigPing.class)),
	/** */
	CMD_SYSTEM_SET_LOGS(0x0009, new InstanceableClass(TigerSystemSetLogs.class)),
	/** */
	CMD_SYSTEM_CONSOLE_PRINT(0x000A, new InstanceableClass(TigerSystemConsolePrint.class)),
	/** */
	CMD_SYSTEM_CONSOLE_COMMAND(0x000B, new InstanceableClass(TigerSystemConsoleCommand.class)),
	/** */
	CMD_SYSTEM_FEATURES(0x000C, new InstanceableClass(TigerSystemFeatures.class)),
	/** */
	CMD_SYSTEM_MATCH_CTRL(0x000D, new InstanceableClass(TigerSystemMatchCtrl.class)),
	/** */
	CMD_SYSTEM_MATCH_FEEDBACK(0x000E, new InstanceableClass(TigerSystemMatchFeedback.class)),
	/** */
	CMD_SYSTEM_PERFORMANCE(0x000F, new InstanceableClass(TigerSystemPerformance.class)),
	/** */
	CMD_SYSTEM_STATUS_V2(0x0080, new InstanceableClass(TigerSystemStatusV2.class)),
	/** */
	CMD_SYSTEM_STATUS_EXT(0x0081, new InstanceableClass(TigerSystemStatusExt.class)),
	/** */
	CMD_SYSTEM_ACK(0x00F0, new InstanceableClass(TigerSystemAck.class)),
	/**  */
	CMD_SYSTEM_QUERY(0x0010, new InstanceableClass(TigerSystemQuery.class, new InstanceableParameter(
			EQueryType.class, "CTRL_PID", "EQueryType"))),
	/**  */
	CMD_SYSTEM_MATCH_POS_VEL(0x0011, new InstanceableClass(TigerSystemMatchPosVel.class)),
	/**  */
	CMD_SYSTEM_BOT_SKILL(0x0012, new InstanceableClass(TigerSystemBotSkill.class)),
	/**  */
	CMD_SYSTEM_LIMITED_VEL(0x0013, new InstanceableClass(LimitedVelocityCommand.class)),
	
	// ### 02 - MOTOR ###
	/** */
	CMD_MOTOR_DRIBBLE(0x0201, new InstanceableClass(TigerDribble.class)),
	// manual, pid, automatic
	/** */
	CMD_MOTOR_SET_MANUAL(0x0204, new InstanceableClass(TigerMotorSetManual.class)),
	/** */
	CMD_MOTOR_SET_PID_SP(0x0205, new InstanceableClass(TigerMotorSetPidSp.class)),
	/** */
	CMD_MOTOR_PID_LOG(0x0206, new InstanceableClass(TigerMotorPidLog.class)),
	/** */
	CMD_MOTOR_SET_PARAMS(0x0207, new InstanceableClass(TigerMotorSetParams.class)),
	/** */
	CMD_MOTOR_MOVE_V2(0x0208, new InstanceableClass(TigerMotorMoveV2.class)),
	
	// ### 04 - KICKER ###
	/** */
	CMD_KICKER_KICKV2(0x0407, new InstanceableClass(TigerKickerKickV2.class)),
	/** */
	CMD_KICKER_STATUSV2(0x0408, new InstanceableClass(TigerKickerStatusV2.class)),
	/** */
	CMD_KICKER_CHARGE_MANUAL(0x0409, new InstanceableClass(TigerKickerChargeManual.class)),
	/** */
	CMD_KICKER_CHARGE_AUTO(0x040A, new InstanceableClass(TigerKickerChargeAuto.class)),
	/** */
	CMD_KICKER_IR_LOG(0x040B, new InstanceableClass(TigerKickerIrLog.class)),
	/** */
	CMD_KICKER_STATUSV3(0x040C, new InstanceableClass(TigerKickerStatusV3.class)),
	/** */
	CMD_KICKER_CONFIG(0x040D, new InstanceableClass(TigerKickerConfig.class)),
	/**  */
	CMD_KICKER_KICKV3(0x040E, new InstanceableClass(TigerKickerKickV3.class)),
	
	// ### 05 - MOVEMENT ###
	/** */
	CMD_MOVEMENT_LIS3_LOG(0x0501, new InstanceableClass(TigerMovementLis3Log.class)),
	/** */
	
	// ### 06 - MULTICAST ###
	/** */
	CMD_MULTICAST_UPDATE_ALL_V2(0x0601, new InstanceableClass(TigerMulticastUpdateAllV2.class)),
	
	// ### 07 - CTRL ###
	/** */
	CMD_CTRL_VISION_POS(0x0700, new InstanceableClass(TigerCtrlVisionPos.class)),
	/** */
	CMD_CTRL_SPLINE_2D(0x0701, new InstanceableClass(TigerCtrlSpline2D.class)),
	/** */
	CMD_CTRL_SPLINE_1D(0x0702, new InstanceableClass(TigerCtrlSpline1D.class)),
	/** */
	CMD_CTRL_SET_FILTER_PARAMS(0x0703, new InstanceableClass(TigerCtrlSetFilterParams.class)),
	/** */
	CMD_CTRL_SET_PID_PARAMS(0x0704, new InstanceableClass(TigerCtrlSetPIDParams.class)),
	/** */
	CMD_CTRL_SET_CONTROLLER_TYPE(0x0705, new InstanceableClass(TigerCtrlSetControllerType.class)),
	/**  */
	CMD_CTRL_RESET(0x0707, new InstanceableClass(TigerCtrlResetCommand.class)),
	
	// ### 08 - BASE_STATION ###
	/** */
	CMD_BASE_ACOMMAND(0x0800, new InstanceableClass(BaseStationACommand.class)),
	/** */
	CMD_BASE_PING(0x0801, new InstanceableClass(BaseStationPing.class)),
	/** */
	CMD_BASE_AUTH(0x0802, new InstanceableClass(BaseStationAuth.class)),
	/** */
	CMD_BASE_STATS(0x0803, new InstanceableClass(BaseStationStats.class)),
	/** */
	CMD_BASE_CONFIG(0x0804, new InstanceableClass(BaseStationConfig.class)),
	/** */
	CMD_BASE_WIFI_STATS(0x0806, new InstanceableClass(BaseStationWifiStats.class)),
	/** */
	CMD_BASE_ETH_STATS(0x0807, new InstanceableClass(BaseStationEthStats.class)),
	/** */
	CMD_BASE_CONFIG_V2(0x0808, new InstanceableClass(BaseStationConfigV2.class)),
	
	// ### 09 - BOOTLOADER ###
	/** */
	CMD_BOOTLOADER_CHECK_FOR_UPDATES(0x0900, new InstanceableClass(TigerBootloaderCheckForUpdates.class)),
	/** */
	CMD_BOOTLOADER_REQUEST_SIZE(0x0901, new InstanceableClass(TigerBootloaderRequestSize.class)),
	/** */
	CMD_BOOTLOADER_REQUEST_CRC(0x0902, new InstanceableClass(TigerBootloaderRequestCrc.class)),
	/** */
	CMD_BOOTLOADER_REQUEST_DATA(0x0903, new InstanceableClass(TigerBootloaderRequestData.class)),
	/** */
	CMD_BOOTLOADER_SIZE(0x0904, new InstanceableClass(TigerBootloaderSize.class)),
	/** */
	CMD_BOOTLOADER_CRC(0x0905, new InstanceableClass(TigerBootloaderCrc.class)),
	/** */
	CMD_BOOTLOADER_DATA(0x0906, new InstanceableClass(TigerBootloaderData.class)),
	
	// ### 11 - CONFIG ###
	/** */
	CMD_CONFIG_QUERY_FILE_LIST(0x0B00, new InstanceableClass(TigerConfigQueryFileList.class)),
	/** */
	CMD_CONFIG_FILE_STRUCTURE(0x0B01, new InstanceableClass(TigerConfigFileStructure.class)),
	/** */
	CMD_CONFIG_ITEM_DESC(0x0B02, new InstanceableClass(TigerConfigItemDesc.class)),
	/** */
	CMD_CONFIG_READ(0x0B03, new InstanceableClass(TigerConfigRead.class)),
	/** */
	CMD_CONFIG_WRITE(0x0B04, new InstanceableClass(TigerConfigWrite.class)),
	
	// ### 12 - status feedback ###
	/**  */
	CMD_STATUS_FEEDBACK_PID(0x0C00, new InstanceableClass(TigerStatusFeedbackPid.class)), ;
	private final InstanceableClass	clazz;
	private final int						id;
	
	
	/**
	 */
	private ECommand(final int id, final InstanceableClass clazz)
	{
		this.clazz = clazz;
		this.id = id;
	}
	
	
	/**
	 * @return the paramImpls
	 */
	@Override
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
	
	
	/**
	 * @return
	 */
	public Class<?> getClazz()
	{
		return clazz.getImpl();
	}
	
	
	/**
	 * @return
	 */
	public int getId()
	{
		return id;
	}
}
