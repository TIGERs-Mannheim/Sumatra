/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.presenter;

import edu.tigers.autoref.view.main.AutoRefMainPanel;
import edu.tigers.autoref.view.main.StartStopPanel.IStartStopPanelObserver;
import edu.tigers.autoreferee.IAutoRefObserver;
import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.engine.detector.EGameEventDetectorType;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.components.EnumCheckBoxPanel.IEnumPanelObserver;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;

import java.awt.EventQueue;


public class AutoRefPresenter implements ISumatraViewPresenter, IStartStopPanelObserver, IAutoRefObserver
{
	@Getter
	private AutoRefMainPanel viewPanel = new AutoRefMainPanel();
	private final GameEventDetectorObserver gameEventDetectorObserver = new GameEventDetectorObserver();


	@Override
	public void onStartModuli()
	{
		ISumatraViewPresenter.super.onStartModuli();
		SumatraModel.getInstance().getModuleOpt(AutoRefModule.class).ifPresent(autoRef -> autoRef.addObserver(this));
		SumatraModel.getInstance().getModuleOpt(AutoRefModule.class)
				.ifPresent(autoRef -> viewPanel.getStartStopPanel().setAutoRefMode(autoRef.getMode())
				);
		EventQueue.invokeLater(() -> viewPanel.setEnabled(true));
		viewPanel.getStartStopPanel().addObserver(this);
		viewPanel.getGameEventDetectorPanel().addObserver(gameEventDetectorObserver);
		viewPanel.getGameEventDetectorPanel().setSelectedBoxes(EGameEventDetectorType.valuesEnabledByDefault());
	}


	@Override
	public void onStopModuli()
	{
		ISumatraViewPresenter.super.onStopModuli();
		SumatraModel.getInstance().getModuleOpt(AutoRefModule.class).ifPresent(autoRef -> autoRef.removeObserver(this));
		viewPanel.getStartStopPanel().removeObserver(this);
		viewPanel.getGameEventDetectorPanel().removeObserver(gameEventDetectorObserver);
		EventQueue.invokeLater(() -> viewPanel.setEnabled(false));
	}


	@Override
	public void onAutoRefModeChanged(EAutoRefMode mode)
	{
		EventQueue.invokeLater(() -> viewPanel.getStartStopPanel().setAutoRefMode(mode));
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
