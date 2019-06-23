/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.06.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bootloader;

import java.io.File;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.Bootloader;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.Bootloader.EProcessorID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.Bootloader.IBootloaderObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderCheckForUpdates;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bootloader.FirmwareBotPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bootloader.FirmwareUpdatePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bootloader.FirmwareUpdatePanel.IFirmwareUpdatePanelObserver;


/**
 * Firmware update presenter.
 * 
 * @author AndreR
 */
public class FirmwareUpdatePresenter implements IFirmwareUpdatePanelObserver, IBootloaderObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final FirmwareUpdatePanel	updatePanel;
	private static final Logger			log	= Logger.getLogger(FirmwareUpdatePresenter.class.getName());
	
	
	/** */
	public FirmwareUpdatePresenter()
	{
		updatePanel = new FirmwareUpdatePanel();
		updatePanel.addObserver(this);
		
		onSelectFirmwareFolder(SumatraModel.getInstance().getUserProperty(
				FirmwareUpdatePanel.class.getCanonicalName() + ".firmwareFolder"));
	}
	
	
	/**
	 * @param updatePanel
	 */
	public FirmwareUpdatePresenter(final FirmwareUpdatePanel updatePanel)
	{
		this.updatePanel = updatePanel;
		this.updatePanel.addObserver(this);
		
		onSelectFirmwareFolder(SumatraModel.getInstance().getUserProperty(
				FirmwareUpdatePanel.class.getCanonicalName() + ".firmwareFolder"));
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
	public void onSelectFirmwareFolder(final String folderPath)
	{
		File testMain = new File(folderPath + "/release/app/run/main.bin");
		if (!testMain.exists())
		{
			log.warn("Compiled firmware binaries not found: " + folderPath);
		}
		
		Bootloader.setProgramFolder(folderPath + "/release/app/run");
		
		updatePanel.removeAllBotPanels();
	}
	
	
	@Override
	public void onStartFirmwareUpdate()
	{
		ABotManager botManager;
		
		try
		{
			botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			return;
		}
		
		for (ABot bot : botManager.getAllBots().values())
		{
			if (bot.getType() == EBotType.TIGER_V3)
			{
				TigerBotV3 botV3 = (TigerBotV3) bot;
				botV3.execute(new TigerBootloaderCheckForUpdates());
			}
		}
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
	public void onBootloaderProgress(final BotID botId, final EProcessorID procId, final long bytesRead,
			final long totalSize)
	{
		FirmwareBotPanel panel = updatePanel.getOrCreateBotPanel(botId);
		panel.setProcessorId(procId);
		panel.setProgress(bytesRead, totalSize);
		panel.setBotId(botId);
	}
}
