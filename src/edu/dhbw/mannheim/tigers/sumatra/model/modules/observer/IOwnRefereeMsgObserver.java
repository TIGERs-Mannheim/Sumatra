/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 15.01.2011
 * Author(s): Malte
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.observer;

import edu.dhbw.mannheim.tigers.sumatra.model.data.RefereeMsg;

/**
 * Interface to get Referee-Messages created by us, not by the RefereeBox. 
 * @author Malte
 * @see IOwnRefereeMsgObservable
 */
public interface IOwnRefereeMsgObserver
{	
	public void onNewOwnRefereeMsg(RefereeMsg refMsg);
}
