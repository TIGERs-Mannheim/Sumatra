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
 * @see IRefereeObservable
 * 
 * @author Gero
 */
public interface IRefereeObserver
{
	/**
	 * 
	 * @param refMsg
	 */
	void onNewRefereeMsg(RefereeMsg refMsg);
}
