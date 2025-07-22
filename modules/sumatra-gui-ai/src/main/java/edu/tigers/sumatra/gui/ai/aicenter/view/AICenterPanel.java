/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ai.aicenter.view;


import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.components.BetterScrollPane;
import edu.tigers.sumatra.ids.EAiTeam;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;


/**
 * This panel controls the ai subtract-modules.
 */
public class AICenterPanel extends JPanel
{
	private static final long serialVersionUID = -2509991904665753934L;

	private final JTabbedPane teamTabbedPane = new JTabbedPane();


	public AICenterPanel()
	{
		setLayout(new BorderLayout());

		JPanel componentPanel = new JPanel();
		componentPanel.setLayout(new BorderLayout());

		for (EAiTeam team : EAiTeam.values())
		{
			teamTabbedPane.addTab(team.name(), new TeamPanel());
		}

		componentPanel.add(teamTabbedPane, BorderLayout.CENTER);

		BetterScrollPane scrollPane = new BetterScrollPane(componentPanel);
		add(scrollPane, BorderLayout.CENTER);

		setActive(false);
	}


	public JTabbedPane getTeamTabbedPane()
	{
		return teamTabbedPane;
	}


	public TeamPanel getActiveTeamPanel()
	{
		return (TeamPanel) teamTabbedPane.getSelectedComponent();
	}


	public TeamPanel getTeamPanel(EAiTeam team)
	{
		return (TeamPanel) teamTabbedPane.getComponentAt(team.ordinal());
	}


	public void setAiControlStateForAi(final EAIControlState mode, final EAiTeam team)
	{
		final TeamPanel updatedPanel = getTeamPanel(team);

		updatedPanel.getModeButtons().get(mode).setSelected(true);

		if (mode == EAIControlState.MATCH_MODE)
		{
			updatedPanel.getTabbedPane().setSelectedComponent(updatedPanel.getAthenaPanel());
		}

		updatedPanel.getRolePanel().setAiControlState(mode);
		updatedPanel.getAthenaPanel().setAiControlState(mode);
	}


	public void setActive(final boolean enable)
	{
		for (EAiTeam team : EAiTeam.values())
		{
			TeamPanel teamPanel = getTeamPanel(team);
			teamPanel.getModeButtons().values().forEach(b -> b.setEnabled(enable));
			teamPanel.getRolePanel().setEnabled(enable);
			teamPanel.getAthenaPanel().setEnabled(enable);
			teamPanel.getMetisPanel().setEnabled(enable);
		}

		setEnabled(enable);
	}
}
