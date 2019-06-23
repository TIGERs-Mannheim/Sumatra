/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.06.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bootloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.Bootloader.EBootloaderState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bootloader.FirmwareBotPresenter.IFirmwareBotPresenterObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bootloader.FirmwareUpdatePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bootloader.FirmwareUpdatePanel.IFirmwareUpdatePanelObserver;


/**
 * Firmware update presenter.
 * 
 * @author AndreR
 */
public class FirmwareUpdatePresenter implements IFirmwareUpdatePanelObserver, IFirmwareBotPresenterObserver,
		IBotManagerObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private FirmwareUpdatePanel							updatePanel		= new FirmwareUpdatePanel();
	private List<FirmwareBotPresenter>					targets			= new ArrayList<FirmwareBotPresenter>();
	private String												filePath;
	// private FirmwareBotPresenter currentBotFlashing = null;
	private final Map<BotID, FirmwareBotPresenter>	botPresenter	= new HashMap<BotID, FirmwareBotPresenter>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param botManager
	 */
	public FirmwareUpdatePresenter(final ABotManager botManager)
	{
		updatePanel.addObserver(this);
		
		for (ABot bot : botManager.getAllBots().values())
		{
			onBotConnectionChanged(bot);
		}
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
	public void onStartFirmwareUpdate(final String filePath, final int target)
	{
		this.filePath = filePath;
		
		targets.clear();
		
		for (FirmwareBotPresenter presenter : botPresenter.values())
		{
			if (presenter.getBotPanel().getChkEnabled())
			{
				targets.add(presenter);
				presenter.start(this.filePath, target);
			}
		}
		updatePanel.setFlashing(true);
		
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
			// currentBotFlashing = null;
			updatePanel.setFlashing(false);
			return;
		}
		
		// currentBotFlashing = targets.remove(0);
		targets.remove(0);
		// currentBotFlashing.start(filePath, targetMain);
	}
	
	
	@Override
	public void onBotAdded(final ABot bot)
	{
	}
	
	
	@Override
	public void onBotRemoved(final ABot bot)
	{
	}
	
	
	@Override
	public void onBotIdChanged(final BotID oldId, final BotID newId)
	{
	}
	
	
	@Override
	public void onBotConnectionChanged(final ABot bot)
	{
		if ((bot.getType() == EBotType.TIGER_V2))
		{
			FirmwareBotPresenter presenter = botPresenter.get(bot.getBotID());
			if ((bot.getNetworkState() == ENetworkState.ONLINE))
			{
				if (presenter == null)
				{
					FirmwareBotPresenter pres = new FirmwareBotPresenter((TigerBotV2) bot);
					updatePanel.addBotPanel(pres.getBotPanel());
					botPresenter.put(bot.getBotID(), pres);
					pres.addObserver(this);
				}
			}
			// else
			// {
			// if ((presenter != null) && (presenter != currentBotFlashing))
			// {
			// updatePanel.removeBotPanel(presenter.getBotPanel());
			// botPresenter.remove(bot.getBotID());
			// presenter.removeObserver(this);
			// }
			// }
		}
	}
	
	
	@Override
	public void onCancel()
	{
		targets.clear();
		// currentBotFlashing = null;
		for (FirmwareBotPresenter presenter : botPresenter.values())
		{
			presenter.cancel();
			presenter.onStateChanged(EBootloaderState.IDLE);
		}
		updatePanel.setFlashing(false);
	}
}
