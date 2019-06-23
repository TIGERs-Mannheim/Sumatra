/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
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
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.clock.FpsCounter;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.ModuliStateAdapter;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.IRecordObserver;
import edu.tigers.sumatra.persistence.RecordManager;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.telegram.TelegramNotificationController;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.tigers.sumatra.view.FpsPanel.EFpsType;
import edu.tigers.sumatra.view.toolbar.EStartStopButtonState;
import edu.tigers.sumatra.view.toolbar.IToolbarObserver;
import edu.tigers.sumatra.view.toolbar.ToolBar;
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
		IRefereeObserver
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
		
		if (SumatraModel.getInstance().isModuleLoaded(RecordManager.class))
		{
			SumatraModel.getInstance().getModule(RecordManager.class)
					.removeObserver(this);
		}
		
		if (SumatraModel.getInstance().isModuleLoaded(AAgent.class))
		{
			SumatraModel.getInstance().getModule(AAgent.class)
					.removeVisObserver(this);
		}
		
		if (SumatraModel.getInstance().isModuleLoaded(AWorldPredictor.class))
		{
			SumatraModel.getInstance().getModule(AWorldPredictor.class)
					.removeObserver(this);
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
		
		if (SumatraModel.getInstance().isModuleLoaded(RecordManager.class))
		{
			SumatraModel.getInstance().getModule(RecordManager.class)
					.addObserver(this);
		}
		
		if (SumatraModel.getInstance().isModuleLoaded(AAgent.class))
		{
			AAgent agent = SumatraModel.getInstance().getModule(AAgent.class);
			agent.addVisObserver(this);
			GlobalShortcuts.register(EShortcut.MATCH_MODE, () -> agent.changeMode(EAIControlState.MATCH_MODE));
		}
		
		if (SumatraModel.getInstance().isModuleLoaded(AWorldPredictor.class))
		{
			SumatraModel.getInstance().getModule(AWorldPredictor.class)
					.addObserver(this);
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
			RecordManager rm = SumatraModel.getInstance().getModule(
					RecordManager.class);
			rm.toggleRecording();
		} catch (ModuleNotFoundException err)
		{
			log.error("Can not toggle record", err);
		}
	}
	
	
	@Override
	public void onSwitchSides()
	{
		Geometry.setNegativeHalfTeam(Geometry.getNegativeHalfTeam().opposite());
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
		if (fpsCounterCam.newFrame(frame.gettCapture()))
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
			case BLUE:
				if (fpsCounterAiB.newFrame(frame.getTimestamp()))
				{
					toolbar.getFpsPanel().setFps(EFpsType.AI_B, fpsCounterAiB.getAvgFps());
				}
				break;
			case YELLOW:
				if (fpsCounterAiY.newFrame(frame.getTimestamp()))
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
			case BLUE:
				fpsCounterAiB.reset();
				toolbar.getFpsPanel().setFps(EFpsType.AI_B, 0);
				break;
			case YELLOW:
				fpsCounterAiY.reset();
				toolbar.getFpsPanel().setFps(EFpsType.AI_Y, 0);
				break;
			default:
				// not supported
				break;
		}
	}
}
