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

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.IPollingControll;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.controller.ControllerFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.local.ControllerInterpreter;


/**
 * 
 * @author Manuel
 * 
 */
public class ButtonPollingThread extends Thread implements IPollingControll
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger				log				= Logger.getLogger(ButtonPollingThread.class.getName());
	
	private static final int					SLEEP				= 50;
	
	private boolean								poll				= true;
	
	private static final float					THRESHOLD_BTN	= 0.3f;
	
	protected final Controller					controller;
	protected final ControllerInterpreter	interpreter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param interpreter
	 * @param controller
	 */
	public ButtonPollingThread(ControllerInterpreter interpreter, Controller controller)
	{
		this.interpreter = interpreter;
		this.controller = controller;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void run()
	{
		setName("Polling " + controller.getName());
		while (poll)
		{
			try
			{
				Thread.sleep(SLEEP);
			} catch (final InterruptedException e)
			{
				log.error("InterruptedException", e);
			}
			
			controller.poll();
			for (final Component comp : controller.getComponents())
			{
				doInputPoll(comp);
			}
			interpreter.fillActionMapWithNulls();
		}
	}
	
	
	protected void doInputPoll(Component comp)
	{
		if (comp.getPollData() > THRESHOLD_BTN)
		{
			interpreter.interpret(comp);
		}
	}
	
	
	@Override
	public boolean startPolling()
	{
		ControllerFactory.getInstance().useController(controller);
		poll = true;
		return poll;
	}
	
	
	@Override
	public void stopPolling()
	{
		poll = false;
		ControllerFactory.getInstance().unuseController(controller);
	}
	
	
	@Override
	public boolean isPolling()
	{
		return poll;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
