/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s):
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.moduli.AModule;


/**
 * This is the base class for every agent who wants to control our robots!
 * 
 * @author Gero
 * 
 */
public abstract class AAgent extends AModule implements IWorldFrameConsumer, IRefereeMsgConsumer, IAthenaControlHandler
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	public static final String				MODULE_TYPE			= "AAgent";
	public static final String				MODULE_ID			= "ai";
	
	// --- config ---
	public final static String				AI_CONFIG_PATH		= "./config/ai/";
	public final static String				AI_DEFAULT_CONFIG	= "ai_default.xml";
	public static String						currentConfig		= AI_DEFAULT_CONFIG;
	
	// --- tactic ---
	public final static String				TACTICS_CONFIG_PATH		= "./config/tactics/";
	public final static String				TACTICS_DEFAULT_CONFIG	= "training.xml";
	public static String						currentTactics		= TACTICS_DEFAULT_CONFIG;
	
	protected static final int				SIGNAL_COUNT		= 0;
	protected CountDownLatch				startSignal;
	
	// AI visualization
	protected final List<IAIObserver>	observers			= new ArrayList<IAIObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public void addObserver(IAIObserver o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
		
	}
	

	public void removeObserver(IAIObserver o)
	{
		synchronized (observers)
		{
			observers.remove(o);
		}
		
	}
	

	protected void resetCountDownLatch()
	{
		startSignal = new CountDownLatch(SIGNAL_COUNT);
	}
}
