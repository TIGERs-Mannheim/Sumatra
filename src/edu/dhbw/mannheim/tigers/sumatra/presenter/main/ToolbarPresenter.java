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

import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar.IToolbarObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar.ToolBar;
import edu.moduli.exceptions.InitModuleException;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.exceptions.StartModuleException;
import edu.moduli.listenerVariables.ModulesState;


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
	private static final Logger		log							= Logger.getLogger(ToolbarPresenter.class.getName());
	
	private static final ImageIcon	loadingIcon					= new ImageIcon(
																						ClassLoader.getSystemResource("Loading.gif"));
	
	/** in milliseconds */
	private static final long			VISUALIZATION_FREQUENCY	= 1;
	
	private long							startAi						= System.nanoTime();
	private long							startWf						= System.nanoTime();
	
	private ToolBar						toolbar;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param toolbar
	 */
	public ToolbarPresenter(ToolBar toolbar)
	{
		this.toolbar = toolbar;
		toolbar.setStartStopButtonState(false, new ImageIcon(ClassLoader.getSystemResource("start.png")));
		
		ModuliStateAdapter.getInstance().addObserver(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onNewAIInfoFrame(final AIInfoFrame lastAIInfoframe)
	{
		long curTime = System.nanoTime();
		if ((curTime - startAi) > TimeUnit.MILLISECONDS.toNanos(VISUALIZATION_FREQUENCY))
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					toolbar.getFpsPanel().setFpsAIF(lastAIInfoframe.getFps());
				}
			});
			startAi = curTime;
		}
	}
	
	
	@Override
	public void onAIException(Exception ex, AIInfoFrame frame, AIInfoFrame prevFrame)
	{
		// nothing to do
	}
	
	
	@Override
	public void onNewPath(Path path)
	{
		// nothing to do
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrame wf)
	{
		long curTime = System.nanoTime();
		if ((curTime - startWf) > TimeUnit.MILLISECONDS.toNanos(VISUALIZATION_FREQUENCY))
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
			startWf = curTime;
		}
	}
	
	
	@Override
	public void onVisionSignalLost(WorldFrame emptyWf)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				toolbar.getFpsPanel().setFpsCam(0);
				toolbar.getFpsPanel().setFpsWF(0);
				toolbar.getFpsPanel().setFpsAIF(0);
			}
		});
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
					AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
					agent.addObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Agent not found for adding IAIObserver", err);
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
						toolbar.getFpsPanel().setFpsAIF(0.0f);
						toolbar.getFpsPanel().setFpsCam(0.0f);
						toolbar.getFpsPanel().setFpsWF(0.0f);
						toolbar.setEmergencyStopButtonEnabled(false);
						toolbar.setStartStopButtonState(true);
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
	
	
	private void onModuliStateChangeDone(ModulesState state)
	{
		// --- set control modules, graphical buttons, etc. ---
		switch (state)
		{
			case NOT_LOADED:
				toolbar.setStartStopButtonState(false);
				break;
			
			case RESOLVED:
				toolbar.setStartStopButtonState(true, new ImageIcon(ClassLoader.getSystemResource("start.png")));
				break;
			
			case ACTIVE:
				toolbar.setStartStopButtonState(true, new ImageIcon(ClassLoader.getSystemResource("stop.png")));
				break;
		}
	}
	
	
	private void onModuliStateChangeStarted()
	{
		toolbar.setStartStopButtonState(false, loadingIcon);
	}
	
	
	@Override
	public void onEmergencyStop()
	{
		// this is done in MainPresenter
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
			switch (SumatraModel.getInstance().getModulesState().get())
			{
				case RESOLVED:
					try
					{
						log.trace("Start modules");
						SumatraModel.getInstance().startModules();
						log.trace("Finished start modules");
					} catch (final InitModuleException err)
					{
						log.error("Cannot init modules: " + err.getMessage());
					} catch (final StartModuleException err)
					{
						log.error("Cannot start modules: " + err.getMessage());
					}
					SumatraModel.getInstance().getModulesState().set(ModulesState.ACTIVE);
					break;
				case ACTIVE:
					log.trace("Start stopping modules");
					SumatraModel.getInstance().stopModules();
					log.trace("Finished stopping modules");
					break;
				case NOT_LOADED:
				default:
					break;
			}
			onModuliStateChangeDone(SumatraModel.getInstance().getModulesState().get());
			log.trace("Finished StartStopThread");
		}
	}
}
