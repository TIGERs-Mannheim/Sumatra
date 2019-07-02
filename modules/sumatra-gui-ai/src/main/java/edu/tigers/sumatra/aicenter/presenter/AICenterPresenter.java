/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.aicenter.presenter;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import javax.swing.SwingUtilities;

import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.Ai;
import edu.tigers.sumatra.ai.IAIObserver;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ai.metis.ECalculator;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.aicenter.view.AICenterPanel;
import edu.tigers.sumatra.aicenter.view.ICalculatorObserver;
import edu.tigers.sumatra.aicenter.view.IRoleControlPanelObserver;
import edu.tigers.sumatra.aicenter.view.RoleControlPanel;
import edu.tigers.sumatra.aicenter.view.TeamPanel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.lookandfeel.ILookAndFeelStateObserver;
import edu.tigers.sumatra.lookandfeel.LookAndFeelStateAdapter;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * This is the presenter for the ai view in sumatra. It's core functionality is realized using a state-machine
 * representing the different modi of influence the AI-developer wants to use.
 */
public class AICenterPresenter extends ASumatraViewPresenter implements ILookAndFeelStateObserver, ISumatraView
{
	private final AICenterPanel aiCenterPanel = new AICenterPanel();
	private final AiObserver aiObserver = new AiObserver();
	private final Map<EAiTeam, TeamPresenter> teamPresenters = new EnumMap<>(EAiTeam.class);

	private Agent agent;
	private boolean centerPanelShown = true;


	public AICenterPresenter()
	{
		for (EAiTeam team : EAiTeam.values())
		{
			final TeamPanel teamPanel = aiCenterPanel.getTeamPanel(team);
			TeamPresenter teamPresenter = new TeamPresenter(team, teamPanel);
			teamPresenters.put(team, teamPresenter);

			GuiFeedbackObserver guiFeedbackObserver = new GuiFeedbackObserver(team);
			teamPanel.getRolePanel().addObserver(guiFeedbackObserver);
			teamPanel.getModeButtons()
					.forEach((mode, btn) -> btn.addActionListener(a -> updateAiControlState(team, mode)));
		}
		aiCenterPanel.getTeamTabbedPane().addChangeListener(changeEvent -> updateCenterPanelShown());

		LookAndFeelStateAdapter.getInstance().addObserver(this);

		aiCenterPanel.setActive(false);
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
	public void onStop()
	{
		if (SumatraModel.getInstance().isModuleLoaded(AAgent.class))
		{
			agent = SumatraModel.getInstance().getModule(Agent.class);
			agent.removeVisObserver(aiObserver);
			agent.removeObserver(aiObserver);
		}

		for (EAiTeam team : EAiTeam.values())
		{
			aiCenterPanel.getTeamPanel(team).getMetisPanel().setActive(false);
		}

		EventQueue.invokeLater(() -> aiCenterPanel.setActive(false));
	}


	@Override
	public void onStart()
	{
		if (SumatraModel.getInstance().isModuleLoaded(AAgent.class))
		{
			agent = SumatraModel.getInstance().getModule(Agent.class);
			agent.addVisObserver(aiObserver);
			agent.addObserver(aiObserver);
		}

		for (EAiTeam team : EAiTeam.values())
		{
			aiCenterPanel.getTeamPanel(team).getMetisPanel().setActive(true);
			updateAiControlStateForTeam(team);
		}

		EventQueue.invokeLater(() -> aiCenterPanel.setActive(true));
	}


	private void updateAiControlStateForTeam(EAiTeam team)
	{
		if (agent != null)
		{
			EAIControlState controlState = agent.getAi(team)
					.map(ai -> ai.getAthena().getControlState())
					.orElse(EAIControlState.OFF);
			EventQueue.invokeLater(() -> aiCenterPanel.setAiControlStateForAi(controlState, team));
		}
	}


	@Override
	public void onShown()
	{
		centerPanelShown = true;
		updateCenterPanelShown();
	}


	@Override
	public void onHidden()
	{
		centerPanelShown = false;
		updateCenterPanelShown();
	}


	private void updateCenterPanelShown()
	{
		for (EAiTeam team : EAiTeam.values())
		{
			TeamPanel panel = teamPresenters.get(team).getTeamPanel();
			final boolean panelActive = panel == aiCenterPanel.getActiveTeamPanel();
			teamPresenters.get(team).setTeamPanelShown(centerPanelShown && panelActive);
		}
	}


	@Override
	public void onLookAndFeelChanged()
	{
		SwingUtilities.invokeLater(() -> SwingUtilities.updateComponentTreeUI(aiCenterPanel));
	}


	@Override
	public Component getComponent()
	{
		return aiCenterPanel;
	}


	@Override
	public ISumatraView getSumatraView()
	{
		return this;
	}

	private class GuiFeedbackObserver implements IRoleControlPanelObserver, ICalculatorObserver
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
			getAI().ifPresent(ai -> ai.getAthena().getAthenaAdapter().getAiControl().addRole(role,
					BotID.createBotId(botId, ai.getAiTeam().getTeamColor())));
		}


		@Override
		public void removeRole(final ARole role)
		{
			getAI().ifPresent(ai -> ai.getAthena().getAthenaAdapter().getAiControl().removeRole(role));
		}


		@Override
		public void clearRoles()
		{
			getAI().ifPresent(ai -> ai.getAthena().getAthenaAdapter().getAiControl().clearRoles());
		}


		@Override
		public void onCalculatorStateChanged(final ECalculator eCalc, final boolean active)
		{
			getAI().ifPresent(ai -> ai.getMetis().setCalculatorActive(eCalc, active));
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
			SwingUtilities.invokeLater(() -> aiCenterPanel.setAiControlStateForAi(mode, aiTeam));
		}


		private void updatePanels(final AIInfoFrame lastFrame)
		{
			final RoleControlPanel rolePanel = aiCenterPanel.getTeamPanel(lastFrame.getAiTeam()).getRolePanel();

			for (APlay play : lastFrame.getPlayStrategy().getActivePlays())
			{
				if (play.getType() == EPlay.GUI_TEST)
				{
					rolePanel.setActiveRoles(play.getRoles());
					break;
				}
			}

			teamPresenters.get(lastFrame.getAiTeam()).getMetisPresenter().updateAIInfoFrame(lastFrame);
			teamPresenters.get(lastFrame.getAiTeam()).getAthenaPresenter().updateAIInfoFrame(lastFrame);
		}
	}
}
