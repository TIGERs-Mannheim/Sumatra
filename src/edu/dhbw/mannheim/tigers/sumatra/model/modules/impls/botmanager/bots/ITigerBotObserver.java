/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDPObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerIrLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerStatusV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorPidLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPong;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPowerLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetLogs;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemStatusMovement;


/**
 *
 */
public interface ITigerBotObserver extends IBotObserver, ITransceiverUDPObserver
{
	/**
	 * 
	 * @param ip
	 */
	void onIpChanged(String ip);
	
	
	/**
	 * 
	 * @param port
	 */
	void onPortChanged(int port);
	
	
	/**
	 * 
	 * @param status
	 */
	void onNewKickerStatusV2(TigerKickerStatusV2 status);
	
	
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
	 * @param port
	 */
	void onServerPortChanged(int port);
	
	
	/**
	 * 
	 * @param cpuId
	 */
	void onCpuIdChanged(String cpuId);
	
	
	/**
	 * 
	 * @param mac
	 */
	void onMacChanged(String mac);
	
	
	/**
	 * 
	 * @param pong
	 */
	void onNewSystemPong(TigerSystemPong pong);
	
	
	/**
	 * 
	 * @param useUpdateAll
	 */
	void onUseUpdateAllChanged(boolean useUpdateAll);
	
	
	/**
	 * 
	 * @param logs
	 */
	void onLogsChanged(TigerSystemSetLogs logs);
	
	
	/**
	 * 
	 * @param params
	 */
	void onMotorParamsChanged(TigerMotorSetParams params);
	
	
	/**
	 * 
	 * @param log
	 */
	void onNewKickerIrLog(TigerKickerIrLog log);
	
	
	/**
	 * 
	 * @param enable
	 */
	void onOofCheckChanged(boolean enable);
	
	
	/**
	 * 
	 * @param features
	 */
	void onBotFeaturesChanged(final Map<EFeature, EFeatureState> features);
}
