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
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.Bootloader.EBootloaderState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.Bootloader.IBootloaderObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bootloader.FirmwareBotPanel;


/**
 * Firmware update presenter.
 * 
 * @author AndreR
 */
public class FirmwareBotPresenter implements IBootloaderObserver
{
	/** */
	public interface IFirmwareBotPresenterObserver
	{
		/** */
		void onFirmwareUpdateComplete();
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private TigerBotV2											bot			= null;
	private FirmwareBotPanel									botPanel		= new FirmwareBotPanel();
	
	private final List<IFirmwareBotPresenterObserver>	observers	= new ArrayList<IFirmwareBotPresenterObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param bot
	 */
	public FirmwareBotPresenter(final TigerBotV2 bot)
	{
		this.bot = bot;
		
		botPanel.setBotName("(" + bot.getBotID().getNumber() + ") " + bot.getName());
		
		bot.getBootloader().addObserver(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public void delete()
	{
		bot.getBootloader().removeObserver(this);
	}
	
	
	/**
	 * @param filePath
	 * @param target
	 */
	public void start(final String filePath, final int target)
	{
		bot.getBootloader().start(filePath, target);
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IFirmwareBotPresenterObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IFirmwareBotPresenterObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyFirmwareUpdateComplete()
	{
		synchronized (observers)
		{
			for (IFirmwareBotPresenterObserver observer : observers)
			{
				observer.onFirmwareUpdateComplete();
			}
		}
	}
	
	
	@Override
	public void onStateChanged(final EBootloaderState state)
	{
		botPanel.setState(state);
		
		if (state == EBootloaderState.IDLE)
		{
			notifyFirmwareUpdateComplete();
		}
	}
	
	
	@Override
	public void onProgressUpdate(final long current, final long total)
	{
		botPanel.setProgress(current, total);
	}
	
	
	/**
	 * @return the updatePanel
	 */
	public FirmwareBotPanel getBotPanel()
	{
		return botPanel;
	}
	
	
	/**
	 */
	public void cancel()
	{
		bot.getBootloader().cancel();
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
