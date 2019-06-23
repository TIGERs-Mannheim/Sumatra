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

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;


/**
 * @see IRefereeObserver
 * 
 * @author Gero
 */
public interface IRefereeObservable
{
	/**
	 * 
	 * @param obs
	 */
	void addRefMsgObserver(IRefereeObserver obs);
	
	
	/**
	 * 
	 * @param obs
	 */
	void removeRefMsgObserver(IRefereeObserver obs);
	
	
	/**
	 * 
	 * @param refMsg
	 */
	void notifyNewRefereeMsg(RefereeMsg refMsg);
}
