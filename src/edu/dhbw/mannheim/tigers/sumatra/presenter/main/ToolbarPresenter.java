/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 26, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.main;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.DependencyException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.LoadModulesException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.StartModuleException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar.EStartStopButtonState;
import edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar.IToolbarObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar.InformationPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar.ToolBar;


/**
 * Presenter class for some toolbar functionalities.
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class ToolbarPresenter implements IWorldPredictorObserver, IAIObserver, IModuliStateObserver, IToolbarObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log	= Logger.getLogger(ToolbarPresenter.class.getName());
	
	private ToolBar					toolbar;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param toolbar
	 */
	public ToolbarPresenter(ToolBar toolbar)
	{
		this.toolbar = toolbar;
		toolbar.setStartStopButtonState(true, EStartStopButtonState.START);
		
		ModuliStateAdapter.getInstance().addObserver(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onNewAIInfoFrame(final AIInfoFrame lastAIInfoframe)
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
	public void onLoadModuliConfig(String filename)
	{
		// --- set new config-file and load it ---
		SumatraModel.getInstance().setCurrentModuliConfig(filename);
		
		// --- load modules into Sumatra ---
		// --- module-handle ---
		try
		{
			// --- get modules from configuration-file ---
			SumatraModel.getInstance().loadModules(
					SumatraModel.MODULI_CONFIG_PATH + SumatraModel.getInstance().getCurrentModuliConfig());
			log.debug("Loaded config: " + filename);
		} catch (final LoadModulesException e)
		{
			log.error(e.getMessage() + " (moduleConfigFile: '" + SumatraModel.getInstance().getCurrentModuliConfig()
					+ "') ");
		} catch (final DependencyException e)
		{
			log.error(e.getMessage() + " (moduleConfigFile: '" + SumatraModel.getInstance().getCurrentModuliConfig()
					+ "') ");
		}
	}
	
	
	@Override
	public void onAIException(Exception ex, IRecordFrame frame, IRecordFrame prevFrame)
	{
		final InformationPanel informationPanel = toolbar.getInformationPanel();
		informationPanel.setAIException(ex);
	}
	
	
	@Override
	public void onNewWorldFrame(final SimpleWorldFrame wf)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				toolbar.getFpsPanel().setFpsCam(wf.getCamFps());
				toolbar.getFpsPanel().setFpsWF(wf.getWfFps());
			}
		});
	}
	
	
	@Override
	public void onVisionSignalLost(SimpleWorldFrame emptyWf)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				toolbar.getFpsPanel().setFpsCam(0);
				toolbar.getFpsPanel().setFpsWF(0);
				toolbar.getFpsPanel().setFpsAIF(0, ETeamColor.YELLOW);
				toolbar.getFpsPanel().setFpsAIF(0, ETeamColor.BLUE);
			}
		});
	}
	
	
	@Override
	public void onNewCamDetectionFrame(CamDetectionFrame frame)
	{
	}
	
	
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				toolbar.setEmergencyStopButtonEnabled(true);
				try
				{
					AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
					agent.addObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Agent yellow not found for adding IAIObserver", err);
				}
				try
				{
					AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
					agent.addObserver(this);
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
				break;
			case RESOLVED:
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						toolbar.getFpsPanel().setFpsAIF(0.0f, ETeamColor.YELLOW);
						toolbar.getFpsPanel().setFpsAIF(0.0f, ETeamColor.BLUE);
						toolbar.getFpsPanel().setFpsCam(0.0f);
						toolbar.getFpsPanel().setFpsWF(0.0f);
						toolbar.setEmergencyStopButtonEnabled(false);
					}
				});
				break;
			default:
				break;
		}
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
	
	
	private void onModuliStateChangeDone(ModulesState state)
	{
		// --- set control modules, graphical buttons, etc. ---
		switch (state)
		{
			case NOT_LOADED:
			case RESOLVED:
				toolbar.setStartStopButtonState(true, EStartStopButtonState.START);
				break;
			
			case ACTIVE:
				toolbar.setStartStopButtonState(true, EStartStopButtonState.STOP);
				break;
		}
	}
	
	
	private void onModuliStateChangeStarted()
	{
		toolbar.setEmergencyStopButtonEnabled(false);
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
						SumatraModel.getInstance().getModulesState().set(ModulesState.ACTIVE);
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
			onModuliStateChangeDone(SumatraModel.getInstance().getModulesState().get());
			log.trace("Finished StartStopThread");
		}
	}
}
