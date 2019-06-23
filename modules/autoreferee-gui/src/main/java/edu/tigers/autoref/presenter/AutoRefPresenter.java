/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 12, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoref.presenter;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import javax.swing.BoxLayout;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import edu.tigers.autoref.view.generic.SumatraViewPanel;
import edu.tigers.autoref.view.main.ActiveEnginePanel;
import edu.tigers.autoref.view.main.StartStopPanel;
import edu.tigers.autoref.view.main.ActiveEnginePanel.IActiveEnginePanelObserver;
import edu.tigers.autoref.view.main.StartStopPanel.IStartStopPanelObserver;
import edu.tigers.autoreferee.AutoRefModule;
import edu.tigers.autoreferee.AutoRefModule.AutoRefState;
import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.IAutoRefStateObserver;
import edu.tigers.autoreferee.engine.ActiveAutoRefEngine;
import edu.tigers.autoreferee.engine.ActiveAutoRefEngine.IAutoRefEngineObserver;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.IAutoRefEngine;
import edu.tigers.autoreferee.engine.IAutoRefEngine.AutoRefMode;
import edu.tigers.autoreferee.engine.events.IGameEventDetector.EGameEventDetectorType;
import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.components.EnumCheckBoxPanel;
import edu.tigers.sumatra.components.EnumCheckBoxPanel.IEnumPanelObserver;
import edu.tigers.sumatra.model.ModuliStateAdapter;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author Lukas Magel
 */
public class AutoRefPresenter implements ISumatraViewPresenter, IModuliStateObserver
{
	private static Logger										log					= Logger.getLogger(AutoRefPresenter.class);
	
	private SumatraViewPanel									mainPanel			= new SumatraViewPanel();
	private StartStopPanel										startStopPanel		= new StartStopPanel();
	private EnumCheckBoxPanel<EGameEventDetectorType>	gameEventPanel;
	private ActiveEnginePanel									activeEnginePanel	= new ActiveEnginePanel();
	
	private class AutoRefStateObserver implements IAutoRefStateObserver
	{
		
		@Override
		public void onAutoRefStateChanged(final AutoRefState state)
		{
			EventQueue.invokeLater(() -> startStopPanel.setState(state));
			
			switch (state)
			{
				case STOPPED:
					EventQueue.invokeLater(() -> activeEnginePanel.setPanelEnabled(false));
					break;
				case STARTED:
					Optional<AutoRefModule> optModule = AutoRefUtil.getAutoRefModule();
					if (optModule.isPresent())
					{
						AutoRefModule module = optModule.get();
						IAutoRefEngine engine = module.getEngine();
						if (engine.getMode() == AutoRefMode.ACTIVE)
						{
							EventQueue.invokeLater(() -> activeEnginePanel.setPanelEnabled(true));
							ActiveAutoRefEngine activeEngine = (ActiveAutoRefEngine) engine;
							activeEngine.addObserver(new AutoRefEngineObserver());
						}
					}
					break;
				default:
					break;
			}
		}
		
		
		@Override
		public void onNewAutoRefFrame(final IAutoRefFrame frame)
		{
		}
	}
	
	private class GameEventsPanelObserver implements IEnumPanelObserver<EGameEventDetectorType>
	{
		@Override
		public void onValueTicked(final EGameEventDetectorType type, final boolean value)
		{
			Set<EGameEventDetectorType> types = gameEventPanel.getValues();
			Optional<AutoRefModule> autoref = AutoRefUtil.getAutoRefModule();
			if (autoref.isPresent() && (autoref.get().getState() == AutoRefState.RUNNING))
			{
				autoref.get().getEngine().setActiveGameEvents(types);
			}
		}
	}
	
	private class AutoRefStarter implements Runnable
	{
		private final AutoRefMode	mode;
		
		
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
					optAutoref.get().start(mode);
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
			new Thread(new AutoRefStarter(startStopPanel.getModeSetting())).start();
		}
		
		
		@Override
		public void onStopButtonPressed()
		{
			AutoRefUtil.ifAutoRefModulePresent(autoref -> autoref.stop());
		}
		
		
		@Override
		public void onPauseButtonPressed()
		{
			AutoRefUtil.ifAutoRefModulePresent(module -> module.pause());
		}
		
		
		@Override
		public void onResumeButtonPressed()
		{
			AutoRefUtil.ifAutoRefModulePresent(module -> module.resume());
		}
	}
	
	private class AutoRefEngineObserver implements IAutoRefEngineObserver
	{
		
		@Override
		public void onStateChanged(final boolean proceedPossible)
		{
			EventQueue.invokeLater(() -> {
				activeEnginePanel.setProceedButtonEnabled(proceedPossible);
			});
		}
		
		
		@Override
		public void onFollowUpChanged(final FollowUpAction action)
		{
			EventQueue.invokeLater(() -> {
				activeEnginePanel.setNextAction(action);
			});
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
	
	
	/**
	 * 
	 */
	public AutoRefPresenter()
	{
		gameEventPanel = new EnumCheckBoxPanel<>(EGameEventDetectorType.class, "Game Events", BoxLayout.PAGE_AXIS);
		
		startStopPanel.addObserver(new StartStopPanelObserver());
		gameEventPanel.addObserver(new GameEventsPanelObserver());
		
		activeEnginePanel.setPanelEnabled(false);
		activeEnginePanel.addObserver(new ActiveEnginePanelObserver());
		
		mainPanel.setLayout(new MigLayout("center", "[320][]", "[][]"));
		mainPanel.add(startStopPanel, "grow x, top");
		mainPanel.add(gameEventPanel, "span 1 2, wrap");
		mainPanel.add(activeEnginePanel, "grow x, top");
		
		ModuliStateAdapter.getInstance().addObserver(this);
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
				break;
			case NOT_LOADED:
			case RESOLVED:
				setPanelsEnabledLater(false);
				break;
		}
	}
	
	
	private void setPanelsEnabledLater(final boolean enabled)
	{
		EventQueue.invokeLater(() -> setPanelsEnabled(enabled));
	}
	
	
	private void setPanelsEnabled(final boolean enabled)
	{
		Arrays.asList(startStopPanel, activeEnginePanel, gameEventPanel).forEach(
				panel -> panel.setPanelEnabled(enabled));
		if (enabled == true)
		{
			startStopPanel.setState(AutoRefState.STOPPED);
		}
	}
	
}
