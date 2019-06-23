/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.11.2011
 * Author(s): Sven Frank
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.rcm;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.java.games.input.Controller;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.controller.ControllerFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.RobotControlManagerPanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;


/**
 * This is the presenter for the robot control manager in sumatra
 * 
 * @author Sven Frank
 * 
 */
public final class RCMPresenter implements ILookAndFeelStateObserver, IModuliStateObserver, IBotManagerObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger					log						= Logger.getLogger(RCMPresenter.class.getName());
	
	private static volatile RCMPresenter		instance					= null;
	
	// --- Controllers ---
	private final List<AControllerPresenter>	controllerPresenterS	= new ArrayList<AControllerPresenter>();
	
	// view
	private final RobotControlManagerPanel		rcmPanel;
	private ABotManager								botManager				= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private RCMPresenter()
	{
		rcmPanel = new RobotControlManagerPanel();
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		
		setUpController();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public static RCMPresenter getInstance()
	{
		if (instance == null)
		{
			instance = new RCMPresenter();
		}
		return instance;
	}
	
	
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
			{
				try
				{
					botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
				} catch (ModuleNotFoundException err)
				{
					log.error("Botmanager module not found. Strange...");
				}
				
				rcmPanel.start();
				break;
			}
			
			case RESOLVED:
			{
				rcmPanel.stop();
				break;
			}
			case NOT_LOADED:
			default:
				break;
		}
		
	}
	
	
	@Override
	public void onLookAndFeelChanged()
	{
	}
	
	
	@Override
	public void onBotAdded(ABot bot)
	{
	}
	
	
	@Override
	public void onBotRemoved(ABot bot)
	{
	}
	
	
	@Override
	public void onBotIdChanged(BotID oldId, BotID newId)
	{
	}
	
	
	/**
	 */
	public void setUpController()
	{
		ControllerFactory.getInstance().updateControllers();
		final List<Controller> controllers = ControllerFactory.getInstance().getAllControllers();
		boolean controllerFound = false;
		
		int controllerCount = 0;
		for (final Controller controller : controllers)
		{
			if (controllerPresenterS.size() <= controllerCount)
			{
				if (addController(controller))
				{
					controllerFound = true;
				}
			} else
			{
				controllerPresenterS.get(controllerCount).setController(controller);
				controllerFound = true;
			}
			controllerCount++;
		}
		while (controllerPresenterS.size() > controllerCount)
		{
			controllerPresenterS.get(controllerCount).stopPolling();
			controllerPresenterS.remove(controllerCount);
		}
		
		if (!controllerFound)
		{
			log.info("No controller found. For Linux: ls -l /dev/input/by-id/ for finding controller and sudo chmod o+r /dev/input/eventX to enable");
		} else
		{
			log.info(controllers.size() + " controllers found.");
		}
	}
	
	
	private boolean addController(Controller controller)
	{
		final Controller.Type type = controller.getType();
		AControllerPresenter presenter = null;
		if (type == Controller.Type.KEYBOARD)
		{
			presenter = new KeyboardPresenter(controller);
		} else if (type == Controller.Type.GAMEPAD)
		{
			presenter = new GamePadPresenter(controller);
		} else if (type == Controller.Type.STICK)
		{
			presenter = new GamePadPresenter(controller);
		}
		if (presenter != null)
		{
			controllerPresenterS.add(presenter);
			return true;
		}
		return false;
	}
	
	
	/**
	 * Restart all controllers
	 */
	public void onStartStopButtonPressed()
	{
		// Stop
		for (final AControllerPresenter cP : controllerPresenterS)
		{
			cP.stopPolling();
		}
		// Start
		for (final AControllerPresenter cP : controllerPresenterS)
		{
			if (!cP.startPolling())
			{
				log.warn("Controller " + cP.getController().getName() + " could not be started");
			}
		}
	}
	
	
	/**
	 * Start or stop sending and polling.
	 * @param activeState if start, false if stop
	 * @return Returns an ArrayList<Boolean>. True if single connection from controller to bot successful set up.
	 */
	public List<Boolean> onStartStopButtonPressed(boolean activeState)
	{
		final List<Boolean> returnList = new ArrayList<Boolean>();
		
		// --- Start polling when start-button pressed ---
		if (activeState)
		{
			int i = 0;
			for (final AControllerPresenter cP : controllerPresenterS)
			{
				returnList.add(true);
				if (!cP.startPolling())
				{
					if (cP.getBotNumber().isBot())
					{
						returnList.set(i, false);
					}
				}
				i++;
			}
		}
		
		// --- Stop polling when stop-button pressed ---
		else
		{
			for (final AControllerPresenter cP : controllerPresenterS)
			{
				cP.stopPolling();
			}
		}
		return returnList;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * gets a list of all bots known by the botmanager
	 * @return list of all bots known by the botmanager
	 */
	public Collection<ABot> getAllBots()
	{
		if (botManager != null)
		{
			return botManager.getAllBots().values();
		}
		return new ArrayList<ABot>();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ISumatraView getView()
	{
		return rcmPanel;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Component getComponent()
	{
		return rcmPanel;
	}
	
	
	@Override
	public void onBotConnectionChanged(ABot bot)
	{
	}
}
