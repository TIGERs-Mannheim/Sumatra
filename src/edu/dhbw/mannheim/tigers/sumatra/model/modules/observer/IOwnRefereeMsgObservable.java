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
 * Interface to create own referee messages.
 * @author Malte
 * @see IOwnRefereeMsgObserver
 */
public interface IOwnRefereeMsgObservable
{
	public void addOwnRefMsgObserver(IOwnRefereeMsgObserver obs);
	public void removeOwnRefMsgObserver(IOwnRefereeMsgObserver obs);
	
	public void notifyNewOwnRefereeMsg(RefereeMsg refMsg);
}
