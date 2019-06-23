/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.botparams.presenter;

import java.awt.Component;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.botparams.BotParamsDatabase.IBotParamsDatabaseObserver;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.botparams.view.TeamEditor;
import edu.tigers.sumatra.botparams.view.TeamEditor.ITeamEditorObserver;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BotParamsPresenter extends ASumatraViewPresenter
		implements ISumatraView, ITeamEditorObserver, IBotParamsDatabaseObserver
{
	private static final Logger	log		= Logger
			.getLogger(BotParamsPresenter.class.getName());
	
	private BotParamsManager		paramsManager;
	private TeamEditor				editor	= new TeamEditor();
	
	
	@Override
	public Component getComponent()
	{
		return editor;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return this;
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		super.onModuliStateChanged(state);
		switch (state)
		{
			case ACTIVE:
				moduliStateChangedActive();
				break;
			case RESOLVED:
				moduliStateChangedResolved();
				break;
			case NOT_LOADED:
			default:
				break;
		}
	}
	
	
	private void moduliStateChangedResolved()
	{
		editor.removeObserver(this);
		editor.clear();
	}
	
	
	private void moduliStateChangedActive()
	{
		try
		{
			paramsManager = SumatraModel.getInstance().getModule(BotParamsManager.class);
			editor.setDatabase(paramsManager.getDatabase());
			editor.addObserver(this);
			paramsManager.getDatabase().addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find bot params manager", err);
		}
	}
	
	
	@Override
	public void onTeamUpdated(final String teamName, final BotParams newParams)
	{
		paramsManager.getDatabase().updateEntry(teamName, newParams);
	}
	
	
	@Override
	public void onTeamSelectedForLabel(final EBotParamLabel label, final String teamName)
	{
		paramsManager.getDatabase().setTeamForLabel(label, teamName);
	}
	
	
	@Override
	public void onTeamAdded(final String teamName)
	{
		paramsManager.getDatabase().addEntry(teamName);
	}
	
	
	@Override
	public void onTeamDeleted(final String teamName)
	{
		paramsManager.getDatabase().deleteEntry(teamName);
	}
	
	
	@Override
	public void onEntryAdded(final String entry, final BotParams newParams)
	{
		editor.setDatabase(paramsManager.getDatabase());
	}
	
	
	@Override
	public void onEntryUpdated(final String entry, final BotParams newParams)
	{
		if (newParams == null)
		{
			// team deleted => update whole table
			editor.setDatabase(paramsManager.getDatabase());
		}
	}
	
	
	@Override
	public void onBotParamLabelUpdated(final EBotParamLabel label, final String newEntry)
	{
		editor.setSelectedTeamForLabel(label, newEntry);
	}
}
