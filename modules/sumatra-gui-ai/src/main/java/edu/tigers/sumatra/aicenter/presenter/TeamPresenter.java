package edu.tigers.sumatra.aicenter.presenter;

import java.awt.Component;

import edu.tigers.sumatra.aicenter.view.AthenaControlPanel;
import edu.tigers.sumatra.aicenter.view.MetisPanel;
import edu.tigers.sumatra.aicenter.view.TeamPanel;
import edu.tigers.sumatra.ids.EAiTeam;


public class TeamPresenter
{
	private final TeamPanel teamPanel;
	private final MetisPresenter metisPresenter;
	private final AthenaPresenter athenaPresenter;

	private boolean teamPanelShown = true;


	public TeamPresenter(final EAiTeam team, final TeamPanel teamPanel)
	{
		this.teamPanel = teamPanel;
		this.metisPresenter = new MetisPresenter(team, teamPanel.getMetisPanel());
		this.athenaPresenter = new AthenaPresenter(team, teamPanel.getAthenaPanel());

		teamPanel.getTabbedPane().addChangeListener(changeEvent -> updateTeamPanelShown());

		updateTeamPanelShown();
	}


	public void setTeamPanelShown(boolean teamPanelShown)
	{
		this.teamPanelShown = teamPanelShown;
		updateTeamPanelShown();
	}


	private void updateTeamPanelShown()
	{
		if (teamPanelShown)
		{
			Component selectedComponent = teamPanel.getTabbedPane().getSelectedComponent();
			athenaPresenter.setShown((selectedComponent instanceof AthenaControlPanel));
			metisPresenter.setShown((selectedComponent instanceof MetisPanel));
		} else
		{
			athenaPresenter.setShown(false);
			metisPresenter.setShown(false);
		}
	}


	public TeamPanel getTeamPanel()
	{
		return teamPanel;
	}


	public MetisPresenter getMetisPresenter()
	{
		return metisPresenter;
	}


	public AthenaPresenter getAthenaPresenter()
	{
		return athenaPresenter;
	}
}
