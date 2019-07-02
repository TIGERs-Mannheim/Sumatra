/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botcenter.presenter;

import org.apache.log4j.Logger;

import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botcenter.view.bootloader.FirmwareBotPanel;
import edu.tigers.sumatra.botcenter.view.bootloader.FirmwareUpdatePanel;
import edu.tigers.sumatra.botcenter.view.bootloader.FirmwareUpdatePanel.IFirmwareUpdatePanelObserver;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.Bootloader;
import edu.tigers.sumatra.botmanager.Bootloader.EProcessorID;
import edu.tigers.sumatra.botmanager.Bootloader.IBootloaderObserver;
import edu.tigers.sumatra.botmanager.basestation.TigersBaseStation;
import edu.tigers.sumatra.botmanager.bots.TigerBot;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerBootloaderCheckForUpdates;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * Firmware update presenter.
 *
 * @author AndreR
 */
public class FirmwareUpdatePresenter implements IFirmwareUpdatePanelObserver, IBootloaderObserver, IModuliStateObserver
{
	private static final Logger log = Logger.getLogger(FirmwareUpdatePresenter.class.getName());
	private final Bootloader bootloader;
	private final TigersBaseStation baseStation;
	private final FirmwareUpdatePanel updatePanel;


	/**
	 * @param updatePanel
	 * @param baseStation
	 */
	public FirmwareUpdatePresenter(final FirmwareUpdatePanel updatePanel, final TigersBaseStation baseStation)
	{
		this.updatePanel = updatePanel;
		this.baseStation = baseStation;
		bootloader = new Bootloader(baseStation);

		onSelectFirmwareFolder(SumatraModel.getInstance().getUserProperty(
				FirmwareUpdatePanel.class.getCanonicalName() + ".firmwareFolder", ""));
	}


	@Override
	public void onSelectFirmwareFolder(final String folderPath)
	{
		Bootloader.setProgramFolder(folderPath + "/release/app/run");

		updatePanel.removeAllBotPanels();
	}


	@Override
	public void onStartFirmwareUpdate()
	{
		ABotManager botManager;

		try
		{
			botManager = SumatraModel.getInstance().getModule(ABotManager.class);
		} catch (ModuleNotFoundException err)
		{
			log.error("ABotManager not found", err);
			return;
		}

		for (IBot bot : botManager.getBots().values())
		{
			if (bot.getType() == EBotType.TIGERS)
			{
				TigerBot botV3 = (TigerBot) bot;
				botV3.execute(new TigerBootloaderCheckForUpdates());
			}
		}
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


	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				baseStation.addTigersBsObserver(bootloader);
				updatePanel.addObserver(this);
				bootloader.addObserver(this);
				break;
			case RESOLVED:
				baseStation.removeTigersBsObserver(bootloader);
				updatePanel.removeObserver(this);
				bootloader.removeObserver(this);
				break;
			case NOT_LOADED:
			default:
				break;
		}
	}
}
