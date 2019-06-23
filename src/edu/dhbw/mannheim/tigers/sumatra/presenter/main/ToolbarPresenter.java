/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 26, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.main;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.StartModuleException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrameWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.Simulation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.IRecordObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.RecordManager;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar.EStartStopButtonState;
import edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar.IToolbarObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar.InformationPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar.ToolBar;


/**
 * Presenter class for some toolbar functionalities.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class ToolbarPresenter implements IWorldPredictorObserver, IAIObserver, IModuliStateObserver, IToolbarObserver,
		IRecordObserver
{
	private static final Logger				log		= Logger.getLogger(ToolbarPresenter.class.getName());
	
	private ToolBar								toolbar;
	private ModulesState							preState	= ModulesState.NOT_LOADED;
	private final ScheduledExecutorService	service	= Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(
																			"HeapUpdater"));
	
	
	/**
	 * @param toolbar
	 */
	public ToolbarPresenter(final ToolBar toolbar)
	{
		this.toolbar = toolbar;
		toolbar.setStartStopButtonState(true, EStartStopButtonState.START);
		
		ModuliStateAdapter.getInstance().addObserver(this);
		
		service.scheduleAtFixedRate(new HeapUpdater(), 0, 1000, TimeUnit.MILLISECONDS);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onNewAIInfoFrame(final IRecordFrame lastAIInfoframe)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				toolbar.getFpsPanel().setFpsAIF(lastAIInfoframe.getFps(), lastAIInfoframe.getTeamColor());
			}
		});
	}
	
	
	/**
	 * @param filename
	 */
	public void onLoadModuliConfig(final String filename)
	{
		final String selFilename;
		if (!new File(SumatraModel.MODULI_CONFIG_PATH + "/" + filename).exists())
		{
			log.warn("Could not find moduli config: " + filename);
			selFilename = SumatraModel.MODULI_CONFIG_FILE_DEFAULT;
		} else
		{
			selFilename = filename;
		}
		
		// --- set new config-file and load it ---
		SumatraModel.getInstance().setCurrentModuliConfig(selFilename);
		
		// --- load modules into Sumatra ---
		SumatraModel.getInstance().loadModulesSafe(selFilename);
	}
	
	
	@Override
	public void onAIException(final Throwable ex, final IRecordFrame frame, final IRecordFrame prevFrame)
	{
		final InformationPanel informationPanel = toolbar.getInformationPanel();
		informationPanel.setAIException(ex);
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfWrapper)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				toolbar.getFpsPanel().setFpsCamIn(wfWrapper.getSimpleWorldFrame().getCamInFps());
				toolbar.getFpsPanel().setFpsCam(wfWrapper.getSimpleWorldFrame().getCamFps());
				toolbar.getFpsPanel().setFpsWF(wfWrapper.getSimpleWorldFrame().getWfFps());
			}
		});
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				toolbar.setActive(true);
				try
				{
					AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
					agent.addVisObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Agent yellow not found for adding IAIObserver", err);
				}
				try
				{
					AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
					agent.addVisObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Agent blue not found for adding IAIObserver", err);
				}
				
				try
				{
					AWorldPredictor worldPredictor = (AWorldPredictor) SumatraModel.getInstance().getModule(
							AWorldPredictor.MODULE_ID);
					worldPredictor.addObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Worldpredictor not found for adding IWorldPredictorObserver", err);
				}
				
				RecordManager.addObserver(this);
				
				if (Simulation.isSimulationRunning())
				{
					toolbar.setStartStopButtonState(false, EStartStopButtonState.STOP);
				} else
				{
					toolbar.setStartStopButtonState(true, EStartStopButtonState.STOP);
				}
				break;
			case RESOLVED:
				toolbar.getFpsPanel().setFpsAIF(0.0f, ETeamColor.YELLOW);
				toolbar.getFpsPanel().setFpsAIF(0.0f, ETeamColor.BLUE);
				toolbar.getFpsPanel().setFpsCam(0.0f);
				toolbar.getFpsPanel().setFpsCamIn(0.0f);
				toolbar.getFpsPanel().setFpsWF(0.0f);
				toolbar.setActive(false);
				RecordManager.removeObserver(this);
				try
				{
					AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
					agent.removeVisObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Agent yellow not found for adding IAIObserver", err);
				}
				try
				{
					AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
					agent.removeVisObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Agent blue not found for adding IAIObserver", err);
				}
				
				try
				{
					AWorldPredictor worldPredictor = (AWorldPredictor) SumatraModel.getInstance().getModule(
							AWorldPredictor.MODULE_ID);
					worldPredictor.removeObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Worldpredictor not found for adding IWorldPredictorObserver", err);
				}
				
				if (preState == ModulesState.ACTIVE)
				{
					toolbar.setStartStopButtonState(true, EStartStopButtonState.START);
				}
				break;
			default:
				break;
		}
		preState = state;
	}
	
	
	@Override
	public void onStartStopModules()
	{
		onModuliStateChangeStarted();
		new Thread(new StartStopThread(), "ModuliStateChanged").start();
	}
	
	
	@Override
	public void onEmergencyStop()
	{
		// nothing to do
	}
	
	
	private void onModuliStateChangeStarted()
	{
		toolbar.setActive(false);
		toolbar.setStartStopButtonState(false, EStartStopButtonState.LOADING);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private class StartStopThread implements Runnable
	{
		@Override
		public void run()
		{
			log.trace("Start StartStopThread");
			String moduliConfig = SumatraModel.getInstance().getCurrentModuliConfig();
			switch (SumatraModel.getInstance().getModulesState().get())
			{
				case NOT_LOADED:
				case RESOLVED:
					onLoadModuliConfig(moduliConfig);
					try
					{
						log.trace("Start modules");
						SumatraModel.getInstance().startModules();
						log.trace("Finished start modules");
					} catch (final InitModuleException err)
					{
						log.error("Cannot init modules: ", err);
						SumatraModel.getInstance().getModulesState().set(ModulesState.RESOLVED);
					} catch (final StartModuleException err)
					{
						log.error("Cannot start modules.", err);
						SumatraModel.getInstance().getModulesState().set(ModulesState.RESOLVED);
					}
					break;
				case ACTIVE:
					log.trace("Start stopping modules");
					SumatraModel.getInstance().stopModules();
					log.trace("Finished stopping modules");
					break;
				default:
					break;
			}
			// onModuliStateChangeDone(SumatraModel.getInstance().getModulesState().get());
			log.trace("Finished StartStopThread");
		}
	}
	
	
	@Override
	public void onRecordAndSave(final boolean active)
	{
		RecordManager.startStopRecording(active, true);
	}
	
	
	@Override
	public void onRecord(final boolean active)
	{
		RecordManager.startStopRecording(active, false);
	}
	
	
	@Override
	public void onStartStopRecord(final boolean recording, final boolean persisting)
	{
		toolbar.setRecordingEnabled(recording, persisting);
	}
	
	private class HeapUpdater implements Runnable
	{
		@Override
		public void run()
		{
			float curHeap = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024f / 1024f;
			float totalMem = Runtime.getRuntime().totalMemory() / 1024f / 1024f;
			float maxHeap = Runtime.getRuntime().maxMemory() / 1024f / 1024f;
			toolbar.getHeapBar().setMaximum((int) (totalMem));
			toolbar.getHeapBar().setValue((int) (curHeap));
			toolbar.getHeapLabel().setText(String.format("Mem: %6.1f/%6.1f|%6.1fMB", curHeap, totalMem, maxHeap));
		}
	}
}
