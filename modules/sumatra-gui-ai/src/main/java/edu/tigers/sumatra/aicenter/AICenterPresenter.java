/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.aicenter;

import java.awt.Component;
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
import edu.tigers.sumatra.ai.athena.roleassigner.RoleMapping;
import edu.tigers.sumatra.ai.metis.ECalculator;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.aicenter.view.AICenterPanel;
import edu.tigers.sumatra.aicenter.view.IAthenaControlPanelObserver;
import edu.tigers.sumatra.aicenter.view.ICalculatorObserver;
import edu.tigers.sumatra.aicenter.view.RoleControlPanel;
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
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>, Gero
 */
public class AICenterPresenter extends ASumatraViewPresenter implements ILookAndFeelStateObserver
{
	private Agent agent;
	private final AICenterPanel aiCenterPanel = new AICenterPanel();
	private final AiObserver aiObserver = new AiObserver();
	
	
	/**
	 * New instance
	 */
	public AICenterPresenter()
	{
		for (EAiTeam team : EAiTeam.values())
		{
			GuiFeedbackObserver guiFeedbackObserver = new GuiFeedbackObserver(team);
			
			aiCenterPanel.getRolePanelForAi(team).addObserver(guiFeedbackObserver);
			aiCenterPanel.addObserverForAi(guiFeedbackObserver, team);
			aiCenterPanel.getAthenaPanelForAi(team).addObserver(guiFeedbackObserver);
			aiCenterPanel.getMetisCalculatorsPanelForAi(team).addObserver(guiFeedbackObserver);
		}
		
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		
		setActive(false);
	}
	
	
	/**
	 * @param active
	 */
	public void setActive(boolean active)
	{
		aiCenterPanel.setActive(active);
	}
	
	
	@Override
	public void onStop()
	{
		if (SumatraModel.getInstance().isModuleLoaded(AAgent.class))
		{
			agent = (Agent) SumatraModel.getInstance().getModule(AAgent.class);
			agent.removeVisObserver(aiObserver);
			agent.removeObserver(aiObserver);
		}
		
		for (EAiTeam team : EAiTeam.values())
		{
			aiCenterPanel.getMetisCalculatorsPanelForAi(team).setActive(false);
		}
		
		aiCenterPanel.setActive(false);
	}
	
	
	@Override
	public void onStart()
	{
		if (SumatraModel.getInstance().isModuleLoaded(AAgent.class))
		{
			agent = (Agent) SumatraModel.getInstance().getModule(AAgent.class);
			agent.addVisObserver(aiObserver);
			agent.addObserver(aiObserver);
		}
		
		for (EAiTeam team : EAiTeam.values())
		{
			aiCenterPanel.getMetisCalculatorsPanelForAi(team).setActive(true);
			updateAiControlStateForTeam(team);
		}
		
		aiCenterPanel.setActive(true);
	}
	
	
	private void updateAiControlStateForTeam(EAiTeam team)
	{
		if (agent != null)
		{
			Optional<Ai> ai = agent.getAi(team);
			if (ai.isPresent())
			{
				aiCenterPanel.setAiControlStateForAi(ai.get().getAthena().getControlState(), team);
			} else
			{
				aiCenterPanel.setAiControlStateForAi(EAIControlState.OFF, team);
			}
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
		return aiCenterPanel;
	}
	
	private class GuiFeedbackObserver implements IAICenterObserver, ICalculatorObserver, IAthenaControlPanelObserver
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
		public void onAiModeChanged(final EAIControlState mode)
		{
			if (agent != null)
			{
				agent.changeMode(registeredTeam, mode);
			}
			
			updateAiControlStateForTeam(registeredTeam);
		}
		
		
		@Override
		public void onNewRoleMapping(final Map<EPlay, RoleMapping> infos)
		{
			getAI().ifPresent(ai -> ai.getAthena().getAthenaAdapter().getAiControl().getRoleMapping().clear());
			getAI().ifPresent(ai -> ai.getAthena().getAthenaAdapter().getAiControl().getRoleMapping().putAll(infos));
		}
		
		
		@Override
		public void onNewUseAiFlags(final Map<EPlay, Boolean> useAiFlags)
		{
			getAI().ifPresent(ai -> ai.getAthena().getAthenaAdapter().getAiControl().getUseAiFlags().clear());
			getAI().ifPresent(
					ai -> ai.getAthena().getAthenaAdapter().getAiControl().getUseAiFlags().putAll(useAiFlags));
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
			SwingUtilities.invokeLater(() -> updatePanels(lastFrame));
		}
		
		
		@Override
		public void onAiModeChanged(final EAiTeam aiTeam, final EAIControlState mode)
		{
			aiCenterPanel.setAiControlStateForAi(mode, aiTeam);
		}
		
		
		private void updatePanels(final AIInfoFrame lastFrame)
		{
			final RoleControlPanel rolePanel = aiCenterPanel.getRolePanelForAi(lastFrame.getAiTeam());
			
			for (APlay play : lastFrame.getPlayStrategy().getActivePlays())
			{
				if (play.getType() == EPlay.GUI_TEST)
				{
					rolePanel.setActiveRoles(play.getRoles());
					break;
				}
			}
			
			aiCenterPanel.getMetisCalculatorsPanelForAi(lastFrame.getAiTeam()).updateAIInfoFrame(lastFrame);
		}
	}
}
