/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.gui;

import org.apache.log4j.Logger;

import edu.tigers.autoref.AutoRefReplayPresenter;
import edu.tigers.autoref.gui.view.AutoRefMainFrame;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.AMainPresenter;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.IRecordObserver;
import edu.tigers.sumatra.persistence.RecordManager;


/**
 * @author "Lukas Magel"
 */
public class AutoRefMainPresenter extends AMainPresenter
{
	private static final Logger log = Logger.getLogger(AutoRefMainPresenter.class);
	private static final String LAST_LAYOUT_FILENAME = "last.ly";
	private static final String DEFAULT_LAYOUT = "default.ly";
	private static final String KEY_LAYOUT_PROP = AutoRefMainPresenter.class.getName() + ".layout";
	private static final String DEFAULT_MODULI_FILENAME = "moduli.xml";
	
	private RecordManagerObserver recordManagerObserver;
	
	
	/**
	 * Default
	 */
	public AutoRefMainPresenter()
	{
		super(new AutoRefMainFrame());
		final AutoRefMainFrame mainFrame = (AutoRefMainFrame) getMainFrame();
		
		startupModuli();
		
		mainFrame.activate();
		
		Runtime.getRuntime().addShutdownHook(new Thread(this::onExit));
	}
	
	
	private void startupModuli()
	{
		String moduliFile = SumatraModel.getInstance().getCurrentModuliConfig();
		if (SumatraModel.MODULI_CONFIG_FILE_DEFAULT.equals(moduliFile))
		{
			moduliFile = DEFAULT_MODULI_FILENAME;
		}
		
		SumatraModel.getInstance().loadModulesSafe(moduliFile);
		try
		{
			SumatraModel.getInstance().startModules();
		} catch (InitModuleException | StartModuleException e)
		{
			log.error("Module startup exception --- The referee might not function correctly", e);
		}
		initRecordManagerBinding();
	}
	
	
	private void shutdownModuli()
	{
		deinitRecordManagerBinding();
		SumatraModel.getInstance().stopModules();
	}
	
	
	@Override
	protected String getLastLayoutFile()
	{
		return LAST_LAYOUT_FILENAME;
	}
	
	
	@Override
	protected String getLayoutKey()
	{
		return KEY_LAYOUT_PROP;
	}
	
	
	@Override
	protected String getDefaultLayout()
	{
		return DEFAULT_LAYOUT;
	}
	
	
	@Override
	public void onExit()
	{
		super.onExit();
		
		shutdownModuli();
		SumatraModel.getInstance().saveUserProperties();
	}
	
	
	private void initRecordManagerBinding()
	{
		try
		{
			RecordManager recordManager = SumatraModel.getInstance().getModule(RecordManager.class);
			recordManagerObserver = new RecordManagerObserver();
			recordManager.addObserver(recordManagerObserver);
		} catch (ModuleNotFoundException e)
		{
			log.debug("There is no record manager. Wont't add observer", e);
		}
	}
	
	
	private void deinitRecordManagerBinding()
	{
		try
		{
			RecordManager recordManager = SumatraModel.getInstance().getModule(RecordManager.class);
			recordManager.removeObserver(recordManagerObserver);
			recordManagerObserver = null;
		} catch (ModuleNotFoundException e)
		{
			log.debug("There is no record manager. Wont't add observer", e);
		}
	}
	
	private static class RecordManagerObserver implements IRecordObserver
	{
		@Override
		public void onStartStopRecord(final boolean recording)
		{
			// nothing to do here
		}
		
		
		@Override
		public void onViewReplay(final BerkeleyDb persistence, final long startTime)
		{
			new AutoRefReplayPresenter().start(persistence, startTime);
		}
	}
	
}
