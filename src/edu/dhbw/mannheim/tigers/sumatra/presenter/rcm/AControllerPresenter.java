/*
 * *********************************************************
 * Copyright (c) 2009 DHBW Mannheim - Tigers Mannheim
 * Project: tigers-robotControlUtility
 * Date: 19.11.2010
 * Authors: Clemens Teichmann <clteich@gmx.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.rcm;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextField;

import net.java.games.input.Component;
import net.java.games.input.Controller;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.controller.ControllerFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.controller.EControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.local.ConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.local.LocalDevice;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.ControllerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.ShowRCMMainPanel;


/**
 *
 */
public abstract class AControllerPresenter
{
	// --------------------------------------------------------------------------
	// --- class variables ------------------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log				= Logger.getLogger(AControllerPresenter.class.getName());
	
	private Map<String, String>	currentConfig	= new HashMap<String, String>();
	private Controller				controller;
	private final ControllerPanel	cPanel;
	
	private BotID						botId;
	private LocalDevice				localDevice		= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param newController
	 */
	AControllerPresenter(Controller newController)
	{
		controller = newController;
		
		cPanel = ShowRCMMainPanel.getInstance().addControllerPanel(controller, AControllerPresenter.this);
		loadDefaultConfig();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return Controller
	 */
	public Controller getController()
	{
		return controller;
	}
	
	
	/**
	 * recognize when new component selected. changes component to string and calls setCurrentConfig
	 * @param newComponent
	 * @param action
	 */
	public void onNewSelectedButton(Component newComponent, String action)
	{
		setCurrentConfig(newComponent.getIdentifier().toString(), action);
	}
	
	
	/**
	 * sets current config on new component
	 * @param key (Component)
	 * @param value (Action)
	 */
	private void setCurrentConfig(String key, String value)
	{
		// --- get textfield map with all actions (key) and textfields (value)
		final Map<String, JTextField> tfMap = cPanel.getTextFieldMap();
		
		// --- iterate trough components ---
		for (final Object ccKey : currentConfig.keySet())
		{
			// --- if currentConfig contains action - should always be entered ---
			if (value.equals(currentConfig.get(ccKey)))
			{
				// --- remove old entry ---
				currentConfig.remove(ccKey);
				
				// --- if currentConfig contains component ---
				if (currentConfig.containsKey(key))
				{
					// --- swap components ---
					currentConfig.put(ccKey.toString(), currentConfig.get(key));
					// --- swap textfield text ---
					tfMap.get(currentConfig.get(key)).setText(ccKey.toString());
				}
				// --- put new component and action in current configuration ---
				currentConfig.put(key, value);
				break;
			}
		}
		log.info("HashMapConfig: " + currentConfig.toString());
	}
	
	
	/**
	 * invokes saveConfig in ConfigManager
	 * @param file
	 */
	public void saveCurrentConfig(File file)
	{
		ConfigManager.getInstance().saveConfig(file, currentConfig);
	}
	
	
	/**
	 * 
	 * @param file
	 */
	public void loadCurrentConfig(File file)
	{
		// --- temporary HashMap - equals to currentConfig ---
		final Map<String, String> tempMap = new HashMap<String, String>();
		
		// --- get config from file ---
		currentConfig = ConfigManager.getInstance().loadConfig(file);
		
		// --- iterate trough components ---
		for (final Object key : currentConfig.keySet())
		{
			// --- iterate trough components of controller ---
			for (final Component comp : controller.getComponents())
			{
				// --- check for negative components (axis)
				if (key.toString().substring(0, 1).equals("-"))
				{
					if (key.toString().substring(1).equals(comp.getIdentifier().toString()))
					{
						tempMap.put(key.toString(), currentConfig.get(key));
						break;
					}
				}
				if (key.toString().length() > 2)
				{
					if (key.toString().substring(0, 3).equals(comp.getIdentifier().toString()))
					{
						tempMap.put(key.toString(), currentConfig.get(key));
						log.info(tempMap);
						break;
					}
				}
				// --- check for all other components ---
				else
				{
					// --- if component found on controller, save in tempMap ---
					if (key.toString().equals(comp.getIdentifier().toString()))
					{
						tempMap.put(key.toString(), currentConfig.get(key));
						break;
					}
				}
			}
		}
		log.info("Succesfully loaded " + file.getName());
		cPanel.showConfig(tempMap);
	}
	
	
	/**
	 * load Default Config on startUp
	 */
	public final void loadDefaultConfig()
	{
		currentConfig.putAll(ConfigManager.getInstance().loadDefaultConfig(this));
		cPanel.showConfig(currentConfig);
	}
	
	
	/**
	 * starts polling for controller
	 * @return activation status
	 */
	boolean startPolling()
	{
		if (ControllerFactory.getInstance().isUsed(controller))
		{
			return true;
		}
		
		botId = cPanel.getBotNumber();
		
		if (botId.isBot())
		{
			for (final ABot bot : RCMPresenter.getInstance().getAllBots())
			{
				if (bot.getBotID().equals(botId))
				{
					if ((controller.getType() == Controller.Type.KEYBOARD)
							|| (controller.getType() == Controller.Type.GAMEPAD)
							|| (controller.getType() == Controller.Type.STICK))
					{
						localDevice = new LocalDevice(bot, controller);
						localDevice.setCurrentConfig(currentConfig);
						return localDevice.startPolling();
					}
				}
			}
		}
		return false;
	}
	
	
	/**
	 * stops polling for controller
	 */
	void stopPolling()
	{
		if (localDevice != null)
		{
			localDevice.stopPolling();
		}
	}
	
	
	/**
	 * Set a new controller. polling will be stopped and started if it is currently active
	 * 
	 * @param newController
	 */
	public void setController(Controller newController)
	{
		boolean isPolling = (localDevice == null ? false : localDevice.isPolling());
		if (isPolling)
		{
			stopPolling();
		}
		controller = newController;
		if (isPolling)
		{
			startPolling();
		}
		log.trace("New controller set: " + newController.getName());
	}
	
	
	/**
	 * @return EControllerType
	 */
	public abstract EControllerType getType();
	
	
	/**
	 * @return the botNumber
	 */
	public final BotID getBotNumber()
	{
		return botId;
	}
	
}
