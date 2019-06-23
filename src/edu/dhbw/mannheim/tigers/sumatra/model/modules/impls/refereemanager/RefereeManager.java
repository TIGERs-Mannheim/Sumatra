/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.05.2011
 * Author(s): Oliver Steinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.refereemanager;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ARefereeManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.moduli.exceptions.InitModuleException;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.exceptions.StartModuleException;


/**
 * TODO ai'ler add documentation
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class RefereeManager extends ARefereeManager
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	protected final Logger		log			= Logger.getLogger(getClass());
	
	// Source
	private final SumatraModel	model			= SumatraModel.getInstance();
	private AWorldPredictor		predictor	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void initModule() throws InitModuleException
	{
		try
		{
			// register for worldframes
			predictor = (AWorldPredictor) model.getModule(AWorldPredictor.MODULE_ID);
			predictor.setWorldFrameConsumer(this);
			

		} catch (ModuleNotFoundException err)
		{
			log.error("Unable to find one or more modules!");
		}
		
	}
	

	@Override
	public void deinitModule()
	{
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void startModule() throws StartModuleException
	{
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void stopModule()
	{
		// TODO Auto-generated method stub
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void onNewWorldFrame(WorldFrame worldFrame)
	{
		// TODO Auto-generated method stub
		
	}
}
