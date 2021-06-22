/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.presenter;

import edu.tigers.autoref.view.main.AutoRefMainPanel;
import edu.tigers.autoref.view.main.StartStopPanel.IStartStopPanelObserver;
import edu.tigers.autoreferee.IAutoRefObserver;
import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.engine.detector.EGameEventDetectorType;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.components.EnumCheckBoxPanel.IEnumPanelObserver;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.views.ISumatraViewPresenter;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.Optional;


public class AutoRefPresenter
		implements ISumatraViewPresenter, IStartStopPanelObserver, IAutoRefObserver
{
	private AutoRefMainPanel mainPanel = new AutoRefMainPanel();
	private final GameEventDetectorObserver gameEventDetectorObserver = new GameEventDetectorObserver();


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
		Optional<AutoRefModule> optModule = SumatraModel.getInstance().getModuleOpt(AutoRefModule.class);
		if (optModule.isEmpty())
		{
			return;
		}

		switch (state)
		{
			case ACTIVE:
				optModule.ifPresent(autoRef -> autoRef.addObserver(this));
				EventQueue.invokeLater(() -> mainPanel.setEnabled(true));
				mainPanel.getStartStopPanel().addObserver(this);
				mainPanel.getGameEventDetectorPanel().addObserver(gameEventDetectorObserver);
				mainPanel.getGameEventDetectorPanel().setSelectedBoxes(EGameEventDetectorType.valuesEnabledByDefault());
				optModule.ifPresent(autoRef -> mainPanel.getStartStopPanel().setAutoRefMode(autoRef.getMode()));
				break;
			case NOT_LOADED:
			case RESOLVED:
				optModule.ifPresent(autoRef -> autoRef.removeObserver(this));
				mainPanel.getStartStopPanel().removeObserver(this);
				mainPanel.getGameEventDetectorPanel().removeObserver(gameEventDetectorObserver);
				EventQueue.invokeLater(() -> mainPanel.setEnabled(false));
				break;
		}
	}


	@Override
	public void onAutoRefModeChanged(EAutoRefMode mode)
	{
		EventQueue.invokeLater(() -> mainPanel.getStartStopPanel().setAutoRefMode(mode));
	}


	@Override
	public void changeMode(final EAutoRefMode mode)
	{
		EventQueue.invokeLater(() -> SumatraModel.getInstance().getModule(AutoRefModule.class).changeMode(mode));
	}


	@Override
	public void onNewGameEventDetected(final IGameEvent gameEvent)
	{
		// empty
	}

	private static class GameEventDetectorObserver implements IEnumPanelObserver<EGameEventDetectorType>
	{
		@Override
		public void onValueTicked(final EGameEventDetectorType type, final boolean value)
		{
			AutoRefModule autoRef = SumatraModel.getInstance().getModule(AutoRefModule.class);
			autoRef.setGameEventDetectorActive(type, value);
		}
	}
}
