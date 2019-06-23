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

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamGeometryFrame;
import edu.dhbw.mannheim.tigers.sumatra.util.observer.Observable;


/**
 * @see Observable
 * 
 * @author Gero
 */
public class CamGeomObservable extends Observable<ICamGeomObservable, ICamGeomObserver, CamGeometryFrame> implements
		ICamGeomObservable
{
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param initEvent
	 */
	public CamGeomObservable(CamGeometryFrame initEvent)
	{
		super(initEvent);
	}
}
