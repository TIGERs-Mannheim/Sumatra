/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botparams.presenter;

import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.botparams.BotParamsDatabase.IBotParamsDatabaseObserver;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.botparams.view.TeamEditor;
import edu.tigers.sumatra.botparams.view.TeamEditor.ITeamEditorObserver;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;


/**
 * Presenter for bot parameter view.
 */
@Log4j2
public class BotParamsPresenter implements ISumatraViewPresenter, ITeamEditorObserver, IBotParamsDatabaseObserver
{
	private final String autoSelectionKey = BotParamsPresenter.class.getCanonicalName() + ".enableAutoSelection";
	@Getter
	private final TeamEditor viewPanel = new TeamEditor();
	private BotParamsManager paramsManager;


	@Override
	public void onStartModuli()
	{
		ISumatraViewPresenter.super.onStartModuli();
		SumatraModel.getInstance().getModuleOpt(BotParamsManager.class).ifPresent(pm -> {
			paramsManager = pm;
			viewPanel.setDatabase(paramsManager.getDatabase());
			viewPanel.addObserver(this);
			paramsManager.getDatabase().addObserver(this);
			viewPanel.setInitialValueAutoSelection(
					Boolean.parseBoolean(SumatraModel.getInstance().getUserProperty(autoSelectionKey)));
		});
	}


	@Override
	public void onStopModuli()
	{
		ISumatraViewPresenter.super.onStopModuli();
		viewPanel.removeObserver(this);
		viewPanel.clear();
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
	public void onTeamCopied(final String teamName, final String copyTeam)
	{
		paramsManager.getDatabase().copyEntry(teamName, copyTeam);
	}


	@Override
	public void onEnableAutoOpponentChoice(boolean enable)
	{
		paramsManager.activateAutomaticChoiceOfOpponent(enable);
		SumatraModel.getInstance().setUserProperty(autoSelectionKey, String.valueOf(enable));
	}


	@Override
	public void onTeamDeleted(final String teamName)
	{
		paramsManager.getDatabase().deleteEntry(teamName);
	}


	@Override
	public void onEntryAdded(final String entry, final BotParams newParams)
	{
		viewPanel.setDatabase(paramsManager.getDatabase());
	}


	@Override
	public void onEntryUpdated(final String entry, final BotParams newParams)
	{
		if (newParams == null)
		{
			// team deleted => update whole table
			viewPanel.setDatabase(paramsManager.getDatabase());
		}
	}


	@Override
	public void onBotParamLabelUpdated(final EBotParamLabel label, final String newEntry)
	{
		viewPanel.setSelectedTeamForLabel(label, newEntry);
	}
}
