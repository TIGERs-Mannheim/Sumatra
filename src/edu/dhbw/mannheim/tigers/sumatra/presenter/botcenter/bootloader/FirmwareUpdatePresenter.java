/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.06.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bootloader;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bootloader.FirmwareBotPresenter.IFirmwareBotPresenterObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bootloader.FirmwareUpdatePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bootloader.FirmwareUpdatePanel.IFirmwareUpdatePanelObserver;


/**
 * Firmware update presenter.
 * 
 * @author AndreR
 * 
 */
public class FirmwareUpdatePresenter implements IFirmwareUpdatePanelObserver, IFirmwareBotPresenterObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private FirmwareUpdatePanel			updatePanel	= new FirmwareUpdatePanel();
	private ABotManager						botManager	= null;
	private List<FirmwareBotPresenter>	targets		= new ArrayList<FirmwareBotPresenter>();
	private String								filePath;
	private boolean							targetMain;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param botManager
	 */
	public FirmwareUpdatePresenter(ABotManager botManager)
	{
		this.botManager = botManager;
		
		updatePanel.addObserver(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public void delete()
	{
		updatePanel.removeObserver(this);
	}
	
	
	@Override
	public void onStartFirmwareUpdate(String filePath, boolean main)
	{
		this.filePath = filePath;
		targetMain = main;
		
		updatePanel.removeAllBotPanels();
		targets.clear();
		
		for (ABot bot : botManager.getAllBots().values())
		{
			if ((bot.getType() != EBotType.TIGER_V2) || (bot.getNetworkState() != ENetworkState.ONLINE))
			{
				continue;
			}
			
			TigerBotV2 v2 = (TigerBotV2) bot;
			
			FirmwareBotPresenter botPresenter = new FirmwareBotPresenter(v2);
			
			updatePanel.addBotPanel(v2.getBotID(), botPresenter.getBotPanel());
			
			botPresenter.addObserver(this);
			
			targets.add(botPresenter);
		}
		
		// initiate flashing
		onFirmwareUpdateComplete();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the updatePanel
	 */
	public FirmwareUpdatePanel getUpdatePanel()
	{
		return updatePanel;
	}
	
	
	@Override
	public void onFirmwareUpdateComplete()
	{
		if (targets.isEmpty())
		{
			return;
		}
		
		FirmwareBotPresenter botPres = targets.remove(0);
		
		botPres.start(filePath, targetMain);
	}
}
