/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 26, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Timer;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.Simulation;
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
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.ModuliStateAdapter;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistance.IRecordObserver;
import edu.tigers.sumatra.persistance.RecordManager;
import edu.tigers.sumatra.referee.IRefereeObserver;
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
	private static final Logger	log				= Logger.getLogger(ToolbarPresenter.class.getName());
																
	private final ToolBar			toolbar;
	private ModulesState				preState			= ModulesState.NOT_LOADED;
																
	private final FpsCounter		fpsCounterCam	= new FpsCounter();
	private final FpsCounter		fpsCounterWF	= new FpsCounter();
	private final FpsCounter		fpsCounterAiY	= new FpsCounter();
	private final FpsCounter		fpsCounterAiB	= new FpsCounter();
																
																
	/**
	 * @param toolbar
	 */
	public ToolbarPresenter(final ToolBar toolbar)
	{
		this.toolbar = toolbar;
		toolbar.setStartStopButtonState(true, EStartStopButtonState.START);
		
		ModuliStateAdapter.getInstance().addObserver(this);
		
		Timer heapUpdater = new Timer(1000, new HeapUpdater());
		heapUpdater.start();
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
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				toolbar.setActive(true);
				try
				{
					Agent agent = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
					agent.addVisObserver(this);
					GlobalShortcuts.register(EShortcut.MATCH_MODE, new Runnable()
					{
						@Override
						public void run()
						{
							agent.getAthena().changeMode(EAIControlState.MATCH_MODE);
						}
					});
				} catch (ModuleNotFoundException err)
				{
					log.error("Agent yellow not found for adding IAIObserver", err);
				}
				try
				{
					Agent agent = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
					agent.addVisObserver(this);
					GlobalShortcuts.register(EShortcut.MATCH_MODE, new Runnable()
					{
						@Override
						public void run()
						{
							agent.getAthena().changeMode(EAIControlState.MATCH_MODE);
						}
					});
				} catch (ModuleNotFoundException err)
				{
					log.error("Agent blue not found for adding IAIObserver", err);
				}
				
				try
				{
					AWorldPredictor worldPredictor = (AWorldPredictor) SumatraModel.getInstance().getModule(
							AWorldPredictor.MODULE_ID);
					worldPredictor.addWorldFrameConsumer(this);
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
				
				if (Simulation.isSimulationRunning())
				{
					toolbar.setStartStopButtonState(false, EStartStopButtonState.STOP);
				} else
				{
					toolbar.setStartStopButtonState(true, EStartStopButtonState.STOP);
				}
				break;
			case RESOLVED:
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
					worldPredictor.removeWorldFrameConsumer(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Worldpredictor not found for adding IWorldPredictorObserver", err);
				}
				
				if (preState == ModulesState.ACTIVE)
				{
					toolbar.setStartStopButtonState(true, EStartStopButtonState.START);
				}
				
				GlobalShortcuts.unregisterAll(EShortcut.MATCH_MODE);
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
	public void onToggleRecord()
	{
		try
		{
			RecordManager rm = (RecordManager) SumatraModel.getInstance().getModule(
					RecordManager.MODULE_ID);
			rm.startStopRecording(!rm.isRecording());
		} catch (ModuleNotFoundException err)
		{
			log.error("RecordManager not found", err);
		}
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
		toolbar.getFpsPanel().setFps(EFpsType.WP, 0);
	}
	
	
	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		switch (frame.getTeamColor())
		{
			case BLUE:
				if (fpsCounterAiB.newFrame(frame.getWorldFrame().getTimestamp()))
				{
					toolbar.getFpsPanel().setFps(EFpsType.AI_B, fpsCounterAiB.getAvgFps());
				}
				break;
			case YELLOW:
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
	public void onClearVisualizationFrame(final ETeamColor teamColor)
	{
		switch (teamColor)
		{
			case BLUE:
				toolbar.getFpsPanel().setFps(EFpsType.AI_B, 0);
				break;
			case YELLOW:
				toolbar.getFpsPanel().setFps(EFpsType.AI_Y, 0);
				break;
			default:
				break;
		}
	}
	
	
	@Override
	public void onAIException(final Throwable ex, final VisualizationFrame frame, final VisualizationFrame prevFrame)
	{
	}
}
