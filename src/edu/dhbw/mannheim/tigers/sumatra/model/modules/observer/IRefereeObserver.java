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
 * @see IRefereeObservable
 * 
 * @author Gero
 */
public interface IRefereeObserver
{
	public void onNewRefereeMsg(RefereeMsg refMsg);
}
