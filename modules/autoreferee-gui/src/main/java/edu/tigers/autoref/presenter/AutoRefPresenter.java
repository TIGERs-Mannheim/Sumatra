/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
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

import javax.swing.SwingUtilities;


public class AutoRefPresenter implements ISumatraViewPresenter, IStartStopPanelObserver, IAutoRefObserver
{
	@Getter
	private AutoRefMainPanel viewPanel = new AutoRefMainPanel();
	private final GameEventDetectorObserver gameEventDetectorObserver = new GameEventDetectorObserver();


	@Override
	public void onModuliStarted()
	{
		ISumatraViewPresenter.super.onModuliStarted();
		SumatraModel.getInstance().getModuleOpt(AutoRefModule.class).ifPresent(autoRef -> autoRef.addObserver(this));
		SumatraModel.getInstance().getModuleOpt(AutoRefModule.class)
				.ifPresent(autoRef -> viewPanel.getStartStopPanel().setAutoRefMode(autoRef.getMode())
				);
		SwingUtilities.invokeLater(() -> viewPanel.setEnabled(true));
		viewPanel.getStartStopPanel().addObserver(this);
		viewPanel.getGameEventDetectorPanel().addObserver(gameEventDetectorObserver);
		viewPanel.getGameEventDetectorPanel().setSelectedBoxes(EGameEventDetectorType.valuesEnabledByDefault());
	}


	@Override
	public void onModuliStopped()
	{
		ISumatraViewPresenter.super.onModuliStopped();
		SumatraModel.getInstance().getModuleOpt(AutoRefModule.class).ifPresent(autoRef -> autoRef.removeObserver(this));
		viewPanel.getStartStopPanel().removeObserver(this);
		viewPanel.getGameEventDetectorPanel().removeObserver(gameEventDetectorObserver);
		SwingUtilities.invokeLater(() -> viewPanel.setEnabled(false));
	}


	@Override
	public void onAutoRefModeChanged(EAutoRefMode mode)
	{
		SwingUtilities.invokeLater(() -> viewPanel.getStartStopPanel().setAutoRefMode(mode));
	}


	@Override
	public void changeMode(final EAutoRefMode mode)
	{
		SwingUtilities.invokeLater(() -> SumatraModel.getInstance().getModuleOpt(AutoRefModule.class)
				.ifPresent(autoRef -> autoRef.changeMode(mode)));
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
			SumatraModel.getInstance().getModuleOpt(AutoRefModule.class)
					.ifPresent(autoRef -> autoRef.setGameEventDetectorActive(type, value));
		}
	}
}
