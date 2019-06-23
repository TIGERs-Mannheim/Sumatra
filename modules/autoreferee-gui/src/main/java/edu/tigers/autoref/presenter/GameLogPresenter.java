/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 24, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.presenter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;

import edu.tigers.autoref.model.gamelog.GameLogTableModel;
import edu.tigers.autoref.view.gamelog.GameLogPanel;
import edu.tigers.autoref.view.generic.SumatraViewPanel;
import edu.tigers.autoreferee.AutoRefModule.AutoRefState;
import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.IAutoRefStateObserver;
import edu.tigers.autoreferee.engine.log.GameLogEntry;
import edu.tigers.autoreferee.engine.log.GameLogEntry.ELogEntryType;
import edu.tigers.autoreferee.engine.log.IGameLog;
import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.components.EnumCheckBoxPanel;
import edu.tigers.sumatra.components.EnumCheckBoxPanel.IEnumPanelObserver;
import edu.tigers.sumatra.model.ModuliStateAdapter;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author "Lukas Magel"
 */
public class GameLogPresenter implements ISumatraViewPresenter, IModuliStateObserver, IAutoRefStateObserver,
		IEnumPanelObserver<ELogEntryType>
{
	private static final Set<ELogEntryType>	DEFAULT_ENABLED_BOXES;
	
	private SumatraViewPanel						mainPanel		= new SumatraViewPanel();
	private GameLogPanel								gameLogPanel	= new GameLogPanel();
	private EnumCheckBoxPanel<ELogEntryType>	logTypePanel;
	
	static
	{
		Set<ELogEntryType> types = new HashSet<>();
		types.add(ELogEntryType.COMMAND);
		types.add(ELogEntryType.GAME_EVENT);
		types.add(ELogEntryType.FOLLOW_UP);
		DEFAULT_ENABLED_BOXES = Collections.unmodifiableSet(types);
	}
	
	
	/**
	 * 
	 */
	public GameLogPresenter()
	{
		ModuliStateAdapter.getInstance().addObserver(this);
		
		logTypePanel = new EnumCheckBoxPanel<GameLogEntry.ELogEntryType>(ELogEntryType.class, null, BoxLayout.LINE_AXIS);
		
		logTypePanel.setSelectedBoxes(DEFAULT_ENABLED_BOXES);
		gameLogPanel.setActiveLogTypes(DEFAULT_ENABLED_BOXES);
		
		logTypePanel.addObserver(this);
		
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(gameLogPanel, BorderLayout.CENTER);
		mainPanel.add(logTypePanel, BorderLayout.PAGE_END);
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
				
				GameLogTableModel model = new GameLogTableModel(log);
				EventQueue.invokeLater(() -> gameLogPanel.setTableModel(model));
			});
		}
	}
	
	
	@Override
	public void onNewAutoRefFrame(final IAutoRefFrame frame)
	{
	}
	
	
	@Override
	public void onValueTicked(final ELogEntryType type, final boolean value)
	{
		gameLogPanel.setActiveLogTypes(logTypePanel.getValues());
	}
}
