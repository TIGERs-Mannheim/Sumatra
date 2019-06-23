/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.presenter;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.tigers.autoref.view.main.AutoRefMainPanel;
import edu.tigers.autoref.view.main.IActiveEnginePanel;
import edu.tigers.autoref.view.main.IActiveEnginePanel.IActiveEnginePanelObserver;
import edu.tigers.autoref.view.main.IStartStopPanel.IStartStopPanelObserver;
import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.IAutoRefStateObserver;
import edu.tigers.autoreferee.engine.ActiveAutoRefEngine;
import edu.tigers.autoreferee.engine.ActiveAutoRefEngine.IAutoRefEngineObserver;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.IAutoRefEngine;
import edu.tigers.autoreferee.engine.IAutoRefEngine.AutoRefMode;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.autoreferee.module.AutoRefState;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.components.IEnumPanel.IEnumPanelObserver;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author Lukas Magel
 */
public class AutoRefPresenter implements ISumatraViewPresenter
{
	private static final Logger log = Logger.getLogger(AutoRefPresenter.class);
	
	private AutoRefMainPanel mainPanel = new AutoRefMainPanel();
	
	
	/**
	 *
	 */
	public AutoRefPresenter()
	{
		mainPanel.getStartStopPanel().addObserver(new StartStopPanelObserver());
		mainPanel.getGameEventDetectorPanel().addObserver(new GameEventDetectorsPanelObserver());
		mainPanel.getGameEventPanel().addObserver(new GameEventsPanelObserver());
		
		IActiveEnginePanel enginePanel = mainPanel.getEnginePanel();
		enginePanel.setPanelEnabled(false);
		enginePanel.addObserver(new ActiveEnginePanelObserver());
	}
	
	
	@Override
	public Component getComponent()
	{
		return mainPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return mainPanel;
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				Optional<AutoRefModule> optModule = AutoRefUtil.getAutoRefModule();
				optModule.ifPresent(autoref -> autoref.addObserver(new AutoRefStateObserver()));
				setPanelsEnabledLater(true);
				performAutostart();
				break;
			case NOT_LOADED:
			case RESOLVED:
				setPanelsEnabledLater(false);
				break;
		}
	}
	
	
	private void performAutostart()
	{
		String autoRefMode = System.getProperty("autoref.mode");
		if (autoRefMode != null)
		{
			try
			{
				AutoRefMode mode = AutoRefMode.valueOf(autoRefMode);
				mainPanel.getStartStopPanel().setModeSetting(mode);
				startAutoRef();
			} catch (IllegalArgumentException e)
			{
				log.warn("Could not parse autoRef mode: " + autoRefMode, e);
			}
		}
	}
	
	
	private void setPanelsEnabledLater(final boolean enabled)
	{
		EventQueue.invokeLater(() -> setPanelsEnabled(enabled));
	}
	
	
	private void setPanelsEnabled(final boolean enabled)
	{
		mainPanel.setPanelsEnabled(enabled);
		if (enabled)
		{
			mainPanel.getStartStopPanel().setState(AutoRefState.STOPPED);
		}
	}
	
	
	private void startAutoRef()
	{
		new Thread(new AutoRefStarter(mainPanel.getStartStopPanel().getModeSetting())).start();
	}
	
	private class AutoRefStateObserver implements IAutoRefStateObserver
	{
		
		@Override
		public void onAutoRefStateChanged(final AutoRefState state)
		{
			EventQueue.invokeLater(() -> mainPanel.getStartStopPanel().setState(state));
			
			switch (state)
			{
				case STOPPED:
					EventQueue.invokeLater(() -> mainPanel.getEnginePanel().setPanelEnabled(false));
					break;
				case STARTED:
					onAutoRefStateChangedToStarted();
					break;
				default:
					break;
			}
		}
		
		
		private void onAutoRefStateChangedToStarted()
		{
			Optional<AutoRefModule> optModule = AutoRefUtil.getAutoRefModule();
			if (optModule.isPresent())
			{
				AutoRefModule module = optModule.get();
				IAutoRefEngine engine = module.getEngine();
				if (engine.getMode() == AutoRefMode.ACTIVE)
				{
					EventQueue.invokeLater(() -> mainPanel.getEnginePanel().setPanelEnabled(true));
					ActiveAutoRefEngine activeEngine = (ActiveAutoRefEngine) engine;
					activeEngine.addObserver(new AutoRefEngineObserver());
				}
			}
		}
		
		
		@Override
		public void onNewAutoRefFrame(final IAutoRefFrame frame)
		{
			// We are only interested in state changes
		}
	}
	
	private class GameEventDetectorsPanelObserver implements IEnumPanelObserver<EGameEventDetectorType>
	{
		@Override
		public void onValueTicked(final EGameEventDetectorType type, final boolean value)
		{
			Set<EGameEventDetectorType> types = mainPanel.getGameEventDetectorPanel().getValues();
			Optional<AutoRefModule> autoref = AutoRefUtil.getAutoRefModule();
			if (autoref.isPresent() && (autoref.get().getState() == AutoRefState.RUNNING))
			{
				autoref.get().getEngine().setActiveGameEventDetectors(types);
			}
		}
	}
	
	private class GameEventsPanelObserver implements IEnumPanelObserver<EGameEvent>
	{
		@Override
		public void onValueTicked(final EGameEvent type, final boolean value)
		{
			Set<EGameEvent> types = mainPanel.getGameEventPanel().getValues();
			Optional<AutoRefModule> autoref = AutoRefUtil.getAutoRefModule();
			if (autoref.isPresent() && (autoref.get().getState() == AutoRefState.RUNNING))
			{
				autoref.get().getEngine().setActiveGameEvents(types);
			}
		}
	}
	
	private class AutoRefStarter implements Runnable
	{
		private final AutoRefMode mode;
		
		
		/**
		 * @param mode
		 */
		public AutoRefStarter(final AutoRefMode mode)
		{
			this.mode = mode;
		}
		
		
		@Override
		public void run()
		{
			try
			{
				Optional<AutoRefModule> optAutoref = AutoRefUtil.getAutoRefModule();
				if (optAutoref.isPresent())
				{
					AutoRefModule autoref = optAutoref.get();
					autoref.start(mode);
					autoref.getEngine().setActiveGameEventDetectors(mainPanel.getGameEventDetectorPanel().getValues());
					autoref.getEngine().setActiveGameEvents(mainPanel.getGameEventPanel().getValues());
				} else
				{
					log.error("AutoRef module not found");
				}
			} catch (StartModuleException e)
			{
				log.error("Error during Autoref startup: " + e.getMessage(), e);
			}
		}
		
	}
	
	private class StartStopPanelObserver implements IStartStopPanelObserver
	{
		@Override
		public void onStartButtonPressed()
		{
			startAutoRef();
		}
		
		
		@Override
		public void onStopButtonPressed()
		{
			AutoRefUtil.ifAutoRefModulePresent(AutoRefModule::stop);
		}
		
		
		@Override
		public void onPauseButtonPressed()
		{
			AutoRefUtil.ifAutoRefModulePresent(AutoRefModule::pause);
		}
		
		
		@Override
		public void onResumeButtonPressed()
		{
			AutoRefUtil.ifAutoRefModulePresent(AutoRefModule::resume);
		}
	}
	
	private class AutoRefEngineObserver implements IAutoRefEngineObserver
	{
		
		@Override
		public void onStateChanged(final boolean proceedPossible)
		{
			EventQueue.invokeLater(() -> mainPanel.getEnginePanel().setProceedButtonEnabled(proceedPossible));
		}
		
		
		@Override
		public void onFollowUpChanged(final FollowUpAction action)
		{
			EventQueue.invokeLater(() -> mainPanel.getEnginePanel().setNextAction(action));
		}
	}
	
	private class ActiveEnginePanelObserver implements IActiveEnginePanelObserver
	{
		
		@Override
		public void onResetButtonPressed()
		{
			AutoRefUtil.ifAutoRefModulePresent(module -> module.getEngine().reset());
		}
		
		
		@Override
		public void onProceedButtonPressed()
		{
			Optional<AutoRefModule> optModule = AutoRefUtil.getAutoRefModule();
			if (optModule.isPresent())
			{
				AutoRefModule module = optModule.get();
				
				IAutoRefEngine engine = module.getEngine();
				if ((engine != null) && (engine.getMode() == AutoRefMode.ACTIVE))
				{
					ActiveAutoRefEngine activeEngine = (ActiveAutoRefEngine) engine;
					activeEngine.proceed();
				}
			}
		}
		
	}
	
}
