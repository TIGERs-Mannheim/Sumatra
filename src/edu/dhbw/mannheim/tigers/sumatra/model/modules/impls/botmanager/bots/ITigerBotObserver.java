/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDPObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerIrLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerStatusV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorPidLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMovementLis3LogRaw;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPong;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPowerLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetLogs;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemStatusMovement;

public interface ITigerBotObserver extends IBotObserver, ITransceiverUDPObserver
{
	public void onNewKickerStatusV2(TigerKickerStatusV2 status);
	public void onNewMotorPidLog(TigerMotorPidLog log);
	public void onNewSystemStatusMovement(TigerSystemStatusMovement status);
	public void onNewSystemPowerLog(TigerSystemPowerLog log);
	public void onNewMovementLis3LogRaw(TigerMovementLis3LogRaw log);
	public void	onServerPortChanged(int port);
	public void onCpuIdChanged(String cpuId);
	public void	onMacChanged(String mac);
	public void onNewSystemPong(TigerSystemPong pong);
	public void onUseUpdateAllChanged(boolean useUpdateAll);
	public void onLogsChanged(TigerSystemSetLogs logs);
	public void onMotorParamsChanged(TigerMotorSetParams params);
	public void onNewKickerIrLog(TigerKickerIrLog log);
	public void onOofCheckChanged(boolean enable);
}
