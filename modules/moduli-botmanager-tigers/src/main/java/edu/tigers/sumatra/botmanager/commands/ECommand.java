/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationACommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationAuth;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationBroadcast;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationCameraViewport;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationConfigV3;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationEthStats;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationPing;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.botmanager.commands.tiger.TigerSystemPing;
import edu.tigers.sumatra.botmanager.commands.tiger.TigerSystemPong;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemAck;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchCtrl;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigFileStructure;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigItemDesc;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigQueryFileList;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigRead;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerConfigWrite;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqBotModel;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqBotModelV2;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqDelays;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqMotorModel;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqSetMode;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerSystemVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * List of all commands with their class type.
 */
@Getter
@AllArgsConstructor
public enum ECommand implements IInstanceableEnum
{
	// ### SYSTEM ###
	CMD_SYSTEM_PING(0x01, new InstanceableClass<>(TigerSystemPing.class)),
	CMD_SYSTEM_PONG(0x02, new InstanceableClass<>(TigerSystemPong.class)),
	CMD_SYSTEM_CONSOLE_PRINT(0x03, new InstanceableClass<>(TigerSystemConsolePrint.class)),
	CMD_SYSTEM_CONSOLE_COMMAND(0x04, new InstanceableClass<>(TigerSystemConsoleCommand.class)),
	CMD_SYSTEM_MATCH_CTRL(0x05, new InstanceableClass<>(TigerSystemMatchCtrl.class)),
	CMD_SYSTEM_MATCH_FEEDBACK(0x06, new InstanceableClass<>(TigerSystemMatchFeedback.class)),
	CMD_SYSTEM_VERSION(0x07, new InstanceableClass<>(TigerSystemVersion.class)),
	CMD_SYSTEM_ACK(0x08, new InstanceableClass<>(TigerSystemAck.class)),

	// ### CONFIG ###
	CMD_CONFIG_QUERY_FILE_LIST(0x10, new InstanceableClass<>(TigerConfigQueryFileList.class)),
	CMD_CONFIG_FILE_STRUCTURE(0x11, new InstanceableClass<>(TigerConfigFileStructure.class)),
	CMD_CONFIG_ITEM_DESC(0x12, new InstanceableClass<>(TigerConfigItemDesc.class)),
	CMD_CONFIG_READ(0x13, new InstanceableClass<>(TigerConfigRead.class)),
	CMD_CONFIG_WRITE(0x14, new InstanceableClass<>(TigerConfigWrite.class)),

	// ### DATA_ACQ ###
	CMD_DATA_ACQ_MOTOR_MODEL(0x20, new InstanceableClass<>(TigerDataAcqMotorModel.class)),
	CMD_DATA_ACQ_BOT_MODEL(0x21, new InstanceableClass<>(TigerDataAcqBotModel.class)),
	CMD_DATA_ACQ_DELAYS(0x22, new InstanceableClass<>(TigerDataAcqDelays.class)),
	CMD_DATA_ACQ_SET_MODE(0x23, new InstanceableClass<>(TigerDataAcqSetMode.class)),
	CMD_DATA_ACQ_BOT_MODEL_V2(0x24, new InstanceableClass<>(TigerDataAcqBotModelV2.class)),

	// ### BASE_STATION ###
	CMD_BASE_ACOMMAND(0x60, new InstanceableClass<>(BaseStationACommand.class)),
	CMD_BASE_PING(0x61, new InstanceableClass<>(BaseStationPing.class)),
	CMD_BASE_AUTH(0x62, new InstanceableClass<>(BaseStationAuth.class)),
	CMD_BASE_WIFI_STATS(0x63, new InstanceableClass<>(BaseStationWifiStats.class)),
	CMD_BASE_ETH_STATS(0x64, new InstanceableClass<>(BaseStationEthStats.class)),
	CMD_BASE_CAM_VIEWPORT(0x65, new InstanceableClass<>(BaseStationCameraViewport.class)),
	CMD_BASE_CONFIG_V3(0x66, new InstanceableClass<>(BaseStationConfigV3.class)),
	CMD_BASE_BROADCAST(0x67, new InstanceableClass<>(BaseStationBroadcast.class));

	private final int id;
	private final InstanceableClass<?> instanceableClass;


	@Override
	public IInstanceableEnum parse(String value)
	{
		return valueOf(value);
	}
}
