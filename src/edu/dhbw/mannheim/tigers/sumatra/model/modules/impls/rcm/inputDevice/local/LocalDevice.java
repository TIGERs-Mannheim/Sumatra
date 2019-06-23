/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.11.2011
 * Author(s): Manuel
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.local;

import java.util.Map;

import net.java.games.input.Controller;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.AInputDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.IPollingControll;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.controller.ControllerFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.controller.EControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.pollingthreads.ButtonPollingThread;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.pollingthreads.GamePadPollingThread;


/**
 * 
 * @author Manuel
 * 
 */
public class LocalDevice extends AInputDevice implements IPollingControll
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Controller				controller;
	private EControllerType					controllerType;
	private final ControllerInterpreter	ctlrInterpreter;
	
	private IPollingControll				pollingThread;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param bot
	 * @param controller
	 */
	public LocalDevice(ABot bot, Controller controller)
	{
		super(bot);
		
		ControllerFactory.getInstance().useController(controller);
		this.controller = controller;
		
		final Controller.Type type = controller.getType();
		if (type == Controller.Type.KEYBOARD)
		{
			controllerType = EControllerType.KEYBOARD;
		} else if (type == Controller.Type.GAMEPAD)
		{
			controllerType = EControllerType.GAMEPAD;
		} else if (type == Controller.Type.STICK)
		{
			controllerType = EControllerType.STICK;
		}
		
		ctlrInterpreter = new ControllerInterpreter(this);
		loadController();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setCurrentConfig(Map<String, String> currentConfig)
	{
		ctlrInterpreter.setCurrentConfig(currentConfig);
	}
	
	
	private void loadController()
	{
		if (controllerType == EControllerType.KEYBOARD)
		{
			pollingThread = new ButtonPollingThread(ctlrInterpreter, controller);
		} else if ((controllerType == EControllerType.GAMEPAD) || (controllerType == EControllerType.STICK))
		{
			pollingThread = new GamePadPollingThread(ctlrInterpreter, controller);
		}
		
		if (pollingThread != null)
		{
			final Thread t = (Thread) pollingThread;
			t.start();
		}
	}
	
	
	@Override
	public boolean startPolling()
	{
		return pollingThread.startPolling();
	}
	
	
	@Override
	public void stopPolling()
	{
		pollingThread.stopPolling();
		stopSending();
	}
	
	
	@Override
	public boolean isPolling()
	{
		return pollingThread.isPolling();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
