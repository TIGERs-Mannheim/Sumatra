/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.08.2010
 * Author(s): Gero
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.observer;

import edu.dhbw.mannheim.tigers.sumatra.model.data.RefereeMsg;

/**
 * @see IRefereeObserver
 * 
 * @author Gero
 */
public interface IRefereeObservable
{
	public void addRefMsgObserver(IRefereeObserver obs);
	public void removeRefMsgObserver(IRefereeObserver obs);
	
	public void notifyNewRefereeMsg(RefereeMsg refMsg);
}
