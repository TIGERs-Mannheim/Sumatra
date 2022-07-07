/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.aicenter.presenter;

import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.Ai;
import edu.tigers.sumatra.ai.IAIObserver;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.aicenter.view.AICenterPanel;
import edu.tigers.sumatra.aicenter.view.IRoleControlPanelObserver;
import edu.tigers.sumatra.aicenter.view.RoleControlPanel;
import edu.tigers.sumatra.aicenter.view.TeamPanel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.lookandfeel.ILookAndFeelStateObserver;
import edu.tigers.sumatra.lookandfeel.LookAndFeelStateAdapter;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;

import javax.swing.SwingUtilities;
import java.awt.EventQueue;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;


/**
 * This is the presenter for the ai view in sumatra. It's core functionality is realized using a state-machine
 * representing the different modi of influence the AI-developer wants to use.
 */
public class AICenterPresenter implements ISumatraViewPresenter, ILookAndFeelStateObserver
{
	@Getter
	private final AICenterPanel viewPanel = new AICenterPanel();
	private final AiObserver aiObserver = new AiObserver();
	private final Map<EAiTeam, TeamPresenter> teamPresenters = new EnumMap<>(EAiTeam.class);

	private Agent agent;


	public AICenterPresenter()
	{
		for (EAiTeam team : EAiTeam.values())
		{
			final TeamPanel teamPanel = viewPanel.getTeamPanel(team);
			TeamPresenter teamPresenter = new TeamPresenter(team, teamPanel);
			teamPresenters.put(team, teamPresenter);

			GuiFeedbackObserver guiFeedbackObserver = new GuiFeedbackObserver(team);
			teamPanel.getRolePanel().addObserver(guiFeedbackObserver);
			teamPanel.getModeButtons()
					.forEach((mode, btn) -> btn.addActionListener(a -> updateAiControlState(team, mode)));
		}
		viewPanel.getTeamTabbedPane().addChangeListener(changeEvent -> updateCenterPanelShown(true));

		LookAndFeelStateAdapter.getInstance().addObserver(this);

		viewPanel.setActive(false);
	}


	private void updateAiControlState(final EAiTeam team, final EAIControlState mode)
	{
		if (agent != null)
		{
			agent.changeMode(team, mode);
		}

		updateAiControlStateForTeam(team);
	}


	@Override
	public void onStopModuli()
	{
		if (SumatraModel.getInstance().isModuleLoaded(AAgent.class))
		{
			agent = SumatraModel.getInstance().getModule(Agent.class);
			agent.removeVisObserver(aiObserver);
			agent.removeObserver(aiObserver);
		}

		for (EAiTeam team : EAiTeam.values())
		{
			viewPanel.getTeamPanel(team).getMetisPanel().setActive(false);
		}

		EventQueue.invokeLater(() -> viewPanel.setActive(false));
	}


	@Override
	public void onStartModuli()
	{
		if (SumatraModel.getInstance().isModuleLoaded(AAgent.class))
		{
			agent = SumatraModel.getInstance().getModule(Agent.class);
			agent.addVisObserver(aiObserver);
			agent.addObserver(aiObserver);
		}

		EventQueue.invokeLater(() -> viewPanel.setActive(true));

		for (EAiTeam team : EAiTeam.values())
		{
			viewPanel.getTeamPanel(team).getMetisPanel().setActive(true);
			updateAiControlStateForTeam(team);
		}
	}


	private void updateAiControlStateForTeam(EAiTeam team)
	{
		if (agent != null)
		{
			EAIControlState controlState = agent.getAi(team)
					.map(ai -> ai.getAthena().getControlState())
					.orElse(EAIControlState.OFF);
			EventQueue.invokeLater(() -> viewPanel.setAiControlStateForAi(controlState, team));
		}
	}


	@Override
	public void onShown()
	{
		updateCenterPanelShown(true);
	}


	@Override
	public void onHidden()
	{
		updateCenterPanelShown(false);
	}


	private void updateCenterPanelShown(boolean shown)
	{
		for (EAiTeam team : EAiTeam.values())
		{
			TeamPanel panel = teamPresenters.get(team).getTeamPanel();
			final boolean panelActive = panel == viewPanel.getActiveTeamPanel();
			teamPresenters.get(team).setTeamPanelShown(shown && panelActive);
		}
	}


	@Override
	public void onLookAndFeelChanged()
	{
		SwingUtilities.invokeLater(() -> SwingUtilities.updateComponentTreeUI(viewPanel));
	}


	private class GuiFeedbackObserver implements IRoleControlPanelObserver
	{
		private final EAiTeam registeredTeam;


		GuiFeedbackObserver(EAiTeam team)
		{
			registeredTeam = team;
		}


		private Optional<Ai> getAI()
		{
			if (agent == null)
			{
				return Optional.empty();
			}
			return agent.getAi(registeredTeam);
		}


		@Override
		public void addRole(final ARole role, final int botId)
		{
			getAI().ifPresent(ai -> {
				role.assignBotID(BotID.createBotId(botId, ai.getAiTeam().getTeamColor()));
				ai.getAthena().getAthenaGuiInput().getRoles().add(role);
			});
		}


		@Override
		public void removeRole(final ARole role)
		{
			getAI().ifPresent(ai -> ai.getAthena().getAthenaGuiInput().getRoles().remove(role));
		}


		@Override
		public void clearRoles()
		{
			getAI().ifPresent(ai -> ai.getAthena().getAthenaGuiInput().getRoles().clear());
		}
	}

	private class AiObserver implements IVisualizationFrameObserver, IAIObserver
	{
		@Override
		public void onNewAIInfoFrame(final AIInfoFrame lastFrame)
		{
			updatePanels(lastFrame);
		}


		@Override
		public void onAiModeChanged(final EAiTeam aiTeam, final EAIControlState mode)
		{
			SwingUtilities.invokeLater(() -> viewPanel.setAiControlStateForAi(mode, aiTeam));
		}


		private void updatePanels(final AIInfoFrame lastFrame)
		{
			final RoleControlPanel rolePanel = viewPanel.getTeamPanel(lastFrame.getAiTeam()).getRolePanel();
			rolePanel.setActiveRoles(lastFrame.getPlayStrategy().getActiveRoles(EPlay.GUI_TEST));

			teamPresenters.get(lastFrame.getAiTeam()).getTeamPanel().getStatemachinePanel().onUpdate(lastFrame);
			teamPresenters.get(lastFrame.getAiTeam()).getMetisPresenter().updateAIInfoFrame(lastFrame);
			teamPresenters.get(lastFrame.getAiTeam()).getAthenaPresenter().update();
		}
	}
}
