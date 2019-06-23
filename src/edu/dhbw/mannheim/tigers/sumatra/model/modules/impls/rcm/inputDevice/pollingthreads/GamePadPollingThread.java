/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.11.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.pollingthreads;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.local.ControllerInterpreter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.local.NegativeAxis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.local.POVToButton;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.local.PositiveAxis;


/**
 * 
 * @author Manuel
 * 
 */
public class GamePadPollingThread extends ButtonPollingThread
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final float	THRESHOLD_ANALOG	= 0.03f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param interpreter
	 * @param controller
	 */
	public GamePadPollingThread(ControllerInterpreter interpreter, Controller controller)
	{
		super(interpreter, controller);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void doInputPoll(Component comp)
	{
		// --- get POV components ---
		if (comp.getIdentifier() == Component.Identifier.Axis.POV)
		{
			if (comp.getPollData() != 0)
			{
				float povDir = comp.getPollData();
				POVToButton pov = new POVToButton(comp, povDir);
				interpreter.interpret(pov);
			}
		} else
		{
			if (comp.isAnalog())
			{
				// --- Axis ---
				float pollData = comp.getPollData();
				if (pollData >= 0)
				{
					// --- positive Axis ---
					if (pollData > THRESHOLD_ANALOG)
					{
						PositiveAxis pAxis = new PositiveAxis(comp);
						interpreter.interpret(pAxis);
					}
				} else
				{
					// --- negative Axis ---
					if (pollData < -THRESHOLD_ANALOG)
					{
						NegativeAxis nAxis = new NegativeAxis(comp);
						interpreter.interpret(nAxis);
						
					}
				}
			} else
			// (comp.isAnalog())
			{
				super.doInputPoll(comp);
			}
		}
		
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
