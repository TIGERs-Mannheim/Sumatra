/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.presenter;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.tigers.autoref.model.gamelog.GameLogTableModel;
import edu.tigers.autoref.view.gamelog.GameLogPanel;
import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.IAutoRefStateObserver;
import edu.tigers.autoreferee.engine.log.GameLogEntry.ELogEntryType;
import edu.tigers.autoreferee.engine.log.IGameLog;
import edu.tigers.autoreferee.module.AutoRefState;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.components.IEnumPanel;
import edu.tigers.sumatra.components.IEnumPanel.IEnumPanelObserver;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author "Lukas Magel"
 */
public class GameLogPresenter implements ISumatraViewPresenter, IAutoRefStateObserver,
		IEnumPanelObserver<ELogEntryType>
{
	public static final String ACTIVE_LOG_TYPES_KEY = GameLogPresenter.class + ".activeLogTypes";
	
	private GameLogPanel gameLogPanel = new GameLogPanel();
	
	
	/**
	 * Default
	 */
	public GameLogPresenter()
	{
		IEnumPanel<ELogEntryType> logPanel = gameLogPanel.getLogTypePanel();
		
		Set<ELogEntryType> types = new HashSet<>();
		String activeLogTypes = SumatraModel.getInstance().getUserProperty(ACTIVE_LOG_TYPES_KEY);
		if (activeLogTypes == null)
		{
			types.add(ELogEntryType.COMMAND);
			types.add(ELogEntryType.GAME_EVENT);
			types.add(ELogEntryType.FOLLOW_UP);
		} else
		{
			Arrays.stream(activeLogTypes.split(","))
					.map(ELogEntryType::valueOf)
					.forEach(types::add);
		}
		logPanel.setSelectedBoxes(types);
		gameLogPanel.setActiveLogTypes(types);
		
		logPanel.addObserver(this);
	}
	
	
	@Override
	public Component getComponent()
	{
		return gameLogPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return gameLogPanel;
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		if (state == ModulesState.ACTIVE)
		{
			AutoRefUtil.ifAutoRefModulePresent(module -> module.addObserver(this));
		}
	}
	
	
	@Override
	public void onAutoRefStateChanged(final AutoRefState state)
	{
		if (state == AutoRefState.STARTED)
		{
			AutoRefUtil.ifAutoRefModulePresent(module -> {
				IGameLog log = module.getEngine().getGameLog();
				EventQueue.invokeLater(() -> setGameLog(log));
				
			});
		}
	}
	
	
	/**
	 * @param log
	 */
	public void setGameLog(final IGameLog log)
	{
		GameLogTableModel model = new GameLogTableModel(log);
		gameLogPanel.setTableModel(model);
	}
	
	
	@Override
	public void onNewAutoRefFrame(final IAutoRefFrame frame)
	{
		// empty
	}
	
	
	@Override
	public void onValueTicked(final ELogEntryType type, final boolean value)
	{
		Set<ELogEntryType> activeTypes = gameLogPanel.getLogTypePanel().getValues();
		gameLogPanel.setActiveLogTypes(activeTypes);
		String propValue = StringUtils.join(activeTypes, ",");
		SumatraModel.getInstance().setUserProperty(ACTIVE_LOG_TYPES_KEY, propValue);
	}
}
