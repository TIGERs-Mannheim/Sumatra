/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Timer;

import org.apache.log4j.Logger;

import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.clock.FpsCounter;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.ModuliStateAdapter;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.IRecordObserver;
import edu.tigers.sumatra.persistence.RecordManager;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.TeamConfig;
import edu.tigers.sumatra.telegram.TelegramNotificationController;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.tigers.sumatra.view.FpsPanel.EFpsType;
import edu.tigers.sumatra.view.toolbar.EStartStopButtonState;
import edu.tigers.sumatra.view.toolbar.IToolbarObserver;
import edu.tigers.sumatra.view.toolbar.ToolBar;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.vision.IVisionFilterObserver;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Presenter class for some toolbar functionalities.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class ToolbarPresenter implements IModuliStateObserver, IToolbarObserver,
		IRecordObserver, IVisualizationFrameObserver, IWorldFrameObserver,
		IRefereeObserver, IVisionFilterObserver
{
	private static final Logger log = Logger.getLogger(ToolbarPresenter.class.getName());
	
	private final ToolBar toolbar;
	private ModulesState preState = ModulesState.NOT_LOADED;
	
	private final FpsCounter fpsCounterCam = new FpsCounter();
	private final FpsCounter fpsCounterWF = new FpsCounter();
	private final FpsCounter fpsCounterAiY = new FpsCounter();
	private final FpsCounter fpsCounterAiB = new FpsCounter();
	
	
	/**
	 * @param toolbar
	 */
	public ToolbarPresenter(final ToolBar toolbar)
	{
		this.toolbar = toolbar;
		toolbar.setStartStopButtonState(true, EStartStopButtonState.START);
		toolbar.setTelegramStatus(TelegramNotificationController.isBroadcastEnabled());
		
		ModuliStateAdapter.getInstance().addObserver(this);
		
		Timer heapUpdater = new Timer(1000, new HeapUpdater());
		heapUpdater.start();
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				startup();
				break;
			case RESOLVED:
				shutdown();
				break;
			default:
				break;
		}
		preState = state;
	}
	
	
	private void shutdown()
	{
		toolbar.getFpsPanel().clearFps();
		toolbar.setActive(false);
		
		try
		{
			RecordManager rm = (RecordManager) SumatraModel.getInstance().getModule(
					RecordManager.MODULE_ID);
			rm.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("RecordManager not found", err);
		}
		try
		{
			AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
			agent.removeVisObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Agent not found for adding IAIObserver", err);
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
		
		try
		{
			AVisionFilter visionFilter = (AVisionFilter) SumatraModel.getInstance().getModule(
					AVisionFilter.MODULE_ID);
			visionFilter.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("visionFilter not found", err);
		}
		
		if (preState == ModulesState.ACTIVE)
		{
			toolbar.setStartStopButtonState(true, EStartStopButtonState.START);
		}
		
		GlobalShortcuts.unregisterAll(EShortcut.MATCH_MODE);
	}
	
	
	private void startup()
	{
		toolbar.setActive(true);
		try
		{
			Agent agent = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
			agent.addVisObserver(this);
			GlobalShortcuts.register(EShortcut.MATCH_MODE, () -> agent.changeMode(EAIControlState.MATCH_MODE));
		} catch (ModuleNotFoundException err)
		{
			log.error("Agent yellow not found for adding IAIObserver", err);
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
		
		try
		{
			RecordManager rm = (RecordManager) SumatraModel.getInstance().getModule(
					RecordManager.MODULE_ID);
			rm.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("RecordManager not found", err);
		}
		
		try
		{
			AVisionFilter visionFilter = (AVisionFilter) SumatraModel.getInstance().getModule(
					AVisionFilter.MODULE_ID);
			visionFilter.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("visionFilter not found", err);
		}
		
		toolbar.setStartStopButtonState(true, EStartStopButtonState.STOP);
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
	
	
	private class StartStopThread implements Runnable
	{
		@Override
		public void run()
		{
			log.trace("Start StartStopThread");
			switch (SumatraModel.getInstance().getModulesState().get())
			{
				case NOT_LOADED:
				case RESOLVED:
					startModules();
					break;
				case ACTIVE:
					log.trace("Start stopping modules");
					SumatraModel.getInstance().stopModules();
					log.trace("Finished stopping modules");
					break;
				default:
					break;
			}
			log.trace("Finished StartStopThread");
		}
		
		
		private void startModules()
		{
			String moduliConfig = SumatraModel.getInstance().getCurrentModuliConfig();
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
		}
		
		
		/**
		 * @param filename
		 */
		private void onLoadModuliConfig(final String filename)
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
	}
	
	
	@Override
	public void onToggleRecord()
	{
		try
		{
			RecordManager rm = (RecordManager) SumatraModel.getInstance().getModule(
					RecordManager.MODULE_ID);
			rm.startStopRecording(!rm.isRecording());
		} catch (ModuleNotFoundException err)
		{
			log.error("Can not toggle record", err);
		}
	}
	
	
	@Override
	public void onSwitchSides()
	{
		TeamConfig.setLeftTeam(TeamConfig.getLeftTeam().opposite());
	}
	
	
	@Override
	public void onChangeTelegramMode(final boolean matchMode)
	{
		String status = matchMode ? "enabled" : "disabled";
		log.info("Telegram broadcasting is now " + status);
		SumatraModel.getInstance().setUserProperty("telegram_broadcasting", String.valueOf(matchMode));
	}
	
	
	@Override
	public void onStartStopRecord(final boolean recording)
	{
		toolbar.setRecordingEnabled(recording);
	}
	
	private class HeapUpdater implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			double curHeap = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0 / 1024.0;
			double totalMem = Runtime.getRuntime().totalMemory() / 1024.0 / 1024.0;
			double maxHeap = Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0;
			toolbar.getHeapBar().setMaximum((int) (totalMem));
			toolbar.getHeapBar().setValue((int) (curHeap));
			toolbar.getHeapLabel().setText(String.format("Mem: %6.1f/%6.1f|%6.1fMB", curHeap, totalMem, maxHeap));
		}
	}
	
	
	@Override
	public void onNewCamDetectionFrame(final ExtendedCamDetectionFrame frame)
	{
		// nothing to do
	}
	
	
	@Override
	public void onNewFilteredVisionFrame(final FilteredVisionFrame filteredVisionFrame)
	{
		if (fpsCounterCam.newFrame(System.nanoTime()))
		{
			toolbar.getFpsPanel().setFps(EFpsType.CAM, fpsCounterCam.getAvgFps());
		}
	}
	
	
	@Override
	public void onClearCamDetectionFrame()
	{
		fpsCounterCam.reset();
		toolbar.getFpsPanel().setFps(EFpsType.CAM, 0);
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfWrapper)
	{
		if (fpsCounterWF.newFrame(wfWrapper.getSimpleWorldFrame().getTimestamp()))
		{
			toolbar.getFpsPanel().setFps(EFpsType.WP, fpsCounterWF.getAvgFps());
		}
	}
	
	
	@Override
	public void onClearWorldFrame()
	{
		fpsCounterWF.reset();
		toolbar.getFpsPanel().setFps(EFpsType.WP, 0);
	}
	
	
	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		switch (frame.getAiTeam())
		{
			case BLUE_PRIMARY:
				if (fpsCounterAiB.newFrame(frame.getWorldFrame().getTimestamp()))
				{
					toolbar.getFpsPanel().setFps(EFpsType.AI_B, fpsCounterAiB.getAvgFps());
				}
				break;
			case YELLOW_PRIMARY:
				if (fpsCounterAiY.newFrame(frame.getWorldFrame().getTimestamp()))
				{
					toolbar.getFpsPanel().setFps(EFpsType.AI_Y, fpsCounterAiY.getAvgFps());
				}
				break;
			default:
				break;
		}
	}
	
	
	@Override
	public void onClearVisualizationFrame(final EAiTeam team)
	{
		switch (team)
		{
			case BLUE_PRIMARY:
				fpsCounterAiB.reset();
				toolbar.getFpsPanel().setFps(EFpsType.AI_B, 0);
				break;
			case YELLOW_PRIMARY:
				fpsCounterAiY.reset();
				toolbar.getFpsPanel().setFps(EFpsType.AI_Y, 0);
				break;
			case BLUE_SECONDARY:
			case YELLOW_SECONDARY:
			default:
				// not supported
				break;
		}
	}
}
