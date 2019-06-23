/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.ControllerParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.SensorFusionParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.Structure;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2.PingStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECtrlMoveType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorPidLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPowerLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetLogs;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemStatusMovement;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType.EControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerKickerStatusV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemStatusExt;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemStatusV2;


/**
 *
 */
public interface ITigerBotV2Observer extends IBotObserver
{
	/**
	 * 
	 * @param status
	 */
	void onNewSystemStatusV2(TigerSystemStatusV2 status);
	
	
	/**
	 * 
	 * @param print
	 */
	void onSystemConsolePrint(TigerSystemConsolePrint print);
	
	
	/**
	 * 
	 * @param status
	 */
	void onNewKickerStatusV3(TigerKickerStatusV3 status);
	
	
	/**
	 * 
	 * @param log
	 */
	void onNewMotorPidLog(TigerMotorPidLog log);
	
	
	/**
	 * @param status
	 */
	void onNewSystemStatusMovement(TigerSystemStatusMovement status);
	
	
	/**
	 * 
	 * @param log
	 */
	void onNewSystemPowerLog(TigerSystemPowerLog log);
	
	
	/**
	 * 
	 * @param logs
	 */
	void onLogsChanged(TigerSystemSetLogs logs);
	
	
	/**
	 * 
	 * @param params
	 */
	void onSensorFusionParamsChanged(SensorFusionParameters params);
	
	
	/**
	 * 
	 * @param params
	 */
	void onControllerParamsChanged(ControllerParameters params);
	
	
	/**
	 * 
	 * @param structure
	 */
	void onStructureChanged(Structure structure);
	
	
	/**
	 * 
	 * @param features
	 */
	void onBotFeaturesChanged(final Map<EFeature, EFeatureState> features);
	
	
	/**
	 * 
	 * @param type
	 */
	void onControllerTypeChanged(EControllerType type);
	
	
	/**
	 * 
	 * @param type
	 */
	void onCtrlMoveTypeChanged(ECtrlMoveType type);
	
	
	/**
	 * 
	 * @param stats
	 */
	void onNewPingStats(PingStats stats);
	
	
	/**
	 * @param status
	 */
	void onNewSystemStatusExt(TigerSystemStatusExt status);
}
