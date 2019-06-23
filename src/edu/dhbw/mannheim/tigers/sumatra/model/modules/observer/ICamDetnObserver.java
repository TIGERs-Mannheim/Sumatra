/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.observer;

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.util.observer.IObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.observer.Observable;

/**
 * @see Observable
 * 
 * @author Gero
 */
public interface ICamDetnObserver extends IObserver<ICamDetnObservable, ICamDetnObserver, CamDetectionFrame>
{
	
}
