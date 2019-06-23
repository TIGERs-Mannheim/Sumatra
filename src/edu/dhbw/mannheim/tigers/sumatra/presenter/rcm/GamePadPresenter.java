/*
 * *********************************************************
 * Copyright (c) 2009 DHBW Mannheim - Tigers Mannheim
 * Project: tigers-robotControlUtility
 * Date: 19.11.2010
 * Authors: Clemens Teichmann <clteich@gmx.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.rcm;

import net.java.games.input.Controller;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.controller.EControllerType;


/**
 */
public class GamePadPresenter extends AControllerPresenter
{
	// --------------------------------------------------------------------------
	// --- class variables ------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// -----------------------------------------------------------------
	// ----- Constructor -----------------------------------------------
	// -----------------------------------------------------------------
	/**
	 * @param newController
	 */
	public GamePadPresenter(Controller newController)
	{
		super(newController);
	}
	
	
	// -----------------------------------------------------------------
	// ----- Methods ---------------------------------------------------
	// -----------------------------------------------------------------
	@Override
	public EControllerType getType()
	{
		return EControllerType.GAMEPAD;
	}
}
