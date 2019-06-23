/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.aicenter;

import java.awt.Component;
import java.util.Map;
import java.util.Optional;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.Ai;
import edu.tigers.sumatra.ai.IAIObserver;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.ai.lachesis.RoleFinderInfo;
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
	private static final Logger log = Logger.getLogger(AICenterPresenter.class.getName());
	
	private Agent agent;
	private AICenterPanel aiCenterPanel = null;
	private final AiObserver aiObserver = new AiObserver();
	
	
	/**
	 * New instance
	 */
	public AICenterPresenter()
	{
		aiCenterPanel = new AICenterPanel();
		
		
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
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				start();
				break;
			case RESOLVED:
				stop();
				break;
			case NOT_LOADED:
			default:
				break;
		}
	}
	
	
	private void stop()
	{
		try
		{
			agent = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
			agent.removeVisObserver(aiObserver);
			agent.removeObserver(aiObserver);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not get agent module", err);
		}
		
		for (EAiTeam team : EAiTeam.values())
		{
			aiCenterPanel.getMetisCalculatorsPanelForAi(team).setActive(false);
		}
		
		aiCenterPanel.setActive(false);
	}
	
	
	private void start()
	{
		try
		{
			agent = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
			agent.addVisObserver(aiObserver);
			agent.addObserver(aiObserver);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not get agent module", err);
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
		Optional<Ai> ai = agent.getAi(team);
		if (ai.isPresent())
		{
			aiCenterPanel.setAiControlStateForAi(ai.get().getAthena().getControlState(), team);
		} else
		{
			aiCenterPanel.setAiControlStateForAi(EAIControlState.OFF, team);
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
		public void addPlay(final APlay play)
		{
			getAI().ifPresent(ai -> ai.getAthena().getAthenaAdapter().getAiControl().addPlay(play));
		}
		
		
		@Override
		public void removePlay(final APlay play)
		{
			getAI().ifPresent(ai -> ai.getAthena().getAthenaAdapter().getAiControl().removePlay(play));
		}
		
		
		@Override
		public void addRoles2Play(final APlay play, final int numRoles)
		{
			getAI().ifPresent(ai -> ai.getAthena().getAthenaAdapter().getAiControl().addRoles2Play(play, numRoles));
		}
		
		
		@Override
		public void removeRolesFromPlay(final APlay play, final int numRoles)
		{
			getAI().ifPresent(ai -> ai.getAthena().getAthenaAdapter().getAiControl().removeRolesFromPlay(play, numRoles));
		}
		
		
		@Override
		public void addRole(final ARole role, final BotID botId)
		{
			getAI().ifPresent(ai -> ai.getAthena().getAthenaAdapter().getAiControl().addRole(role, botId));
		}
		
		
		@Override
		public void removeRole(final ARole role)
		{
			getAI().ifPresent(ai -> ai.getAthena().getAthenaAdapter().getAiControl().removeRole(role));
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
		public void onNewRoleFinderInfos(final Map<EPlay, RoleFinderInfo> infos)
		{
			getAI().ifPresent(ai -> ai.getAthena().getAthenaAdapter().getAiControl().getRoleFinderInfos().clear());
			getAI().ifPresent(ai -> ai.getAthena().getAthenaAdapter().getAiControl().getRoleFinderInfos().putAll(infos));
		}
		
		
		@Override
		public void onNewRoleFinderUseAiFlags(final Map<EPlay, Boolean> overrides)
		{
			getAI().ifPresent(ai -> ai.getAthena().getAthenaAdapter().getAiControl().getRoleFinderUseAiFlags().clear());
			getAI().ifPresent(
					ai -> ai.getAthena().getAthenaAdapter().getAiControl().getRoleFinderUseAiFlags().putAll(overrides));
		}
		
		
		@Override
		public void onCalculatorStateChanged(final ECalculator eCalc, final boolean active)
		{
			getAI().ifPresent(ai -> ai.getMetis().setCalculatorActive(eCalc, active));
		}
	}
	
	
	/**
	 * Update with latest frame
	 * 
	 * @param frame
	 */
	public void update(VisualizationFrame frame)
	{
		aiCenterPanel.getAthenaPanelForAi(frame.getAiTeam()).updateVisualizationFrame(frame);
	}
	
	private class AiObserver implements IVisualizationFrameObserver, IAIObserver
	{
		@Override
		public void onNewVisualizationFrame(final VisualizationFrame frame)
		{
			update(frame);
		}
		
		
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
