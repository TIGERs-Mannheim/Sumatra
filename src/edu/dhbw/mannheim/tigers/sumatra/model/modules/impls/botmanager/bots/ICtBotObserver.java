/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.tcp.ITransceiverTCPObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTPIDHistory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTStatus;

public interface ICtBotObserver extends IBotObserver, ITransceiverTCPObserver
{
	public void onNewStatus(CTStatus status);
	public void onNewPIDHistory(CTPIDHistory history);
	public void onPidChanged(float kp[], float ki[], float kd[], int delay); 
}
