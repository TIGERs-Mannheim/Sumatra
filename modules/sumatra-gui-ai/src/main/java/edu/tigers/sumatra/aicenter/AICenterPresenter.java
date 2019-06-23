/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.tigers.sumatra.aicenter;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.IAIObserver;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.athena.IAIModeChanged;
import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.ai.lachesis.RoleFinderInfo;
import edu.tigers.sumatra.ai.metis.ECalculator;
import edu.tigers.sumatra.ai.metis.support.RedirectPosGPUCalc.EScoringTypes;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.aicenter.view.AICenterPanel;
import edu.tigers.sumatra.aicenter.view.IAthenaControlPanelObserver;
import edu.tigers.sumatra.aicenter.view.ICalculatorObserver;
import edu.tigers.sumatra.aicenter.view.PlayControlPanel;
import edu.tigers.sumatra.aicenter.view.RoleControlPanel;
import edu.tigers.sumatra.aicenter.view.SupporterGridPanel.ISupporterGridPanelObserver;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
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
public class AICenterPresenter extends ASumatraViewPresenter implements IAIModeChanged, ILookAndFeelStateObserver,
		IVisualizationFrameObserver, IAIObserver
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log				= Logger.getLogger(AICenterPresenter.class.getName());
	
	// Modules
	private Agent						aiAgent			= null;
	
	private AICenterPanel			aiCenterPanel	= null;
	
	private final ETeamColor		team;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param team
	 */
	public AICenterPresenter(final ETeamColor team)
	{
		this.team = team;
		aiCenterPanel = new AICenterPanel();
		GuiFeedbackObserver guiFeedbackObserver = new GuiFeedbackObserver();
		aiCenterPanel.getModulesPanel().getRolePanel().addObserver(guiFeedbackObserver);
		aiCenterPanel.getModulesPanel().getPlayPanel().addObserver(guiFeedbackObserver);
		aiCenterPanel.getModulesPanel().addObserver(guiFeedbackObserver);
		aiCenterPanel.getModulesPanel().getAthenaPanel().addObserver(guiFeedbackObserver);
		aiCenterPanel.getModulesPanel().getSupporterGridPanel().addObserver(guiFeedbackObserver);
		
		aiCenterPanel.getChkAiActive().addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				JCheckBox chkBox = (JCheckBox) e.getSource();
				
				if (aiAgent == null)
				{
					chkBox.setSelected(false);
					return;
				}
				aiAgent.setActive(chkBox.isSelected());
				if (chkBox.isSelected())
				{
					aiCenterPanel.getModulesPanel().onStart();
				} else
				{
					aiCenterPanel.getModulesPanel().onStop();
				}
			}
		});
		
		aiCenterPanel.getModulesPanel().getMetisCalculatorsPanel().addObserver(guiFeedbackObserver);
		
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		
		aiCenterPanel.clearView();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
			{
				try
				{
					Agent agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
					agentYellow.addVisObserver(this);
					agentYellow.addObserver(this);
					Agent agentBlue = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
					agentBlue.addVisObserver(this);
					agentBlue.addObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not get agent module");
				}
				
				try
				{
					switch (team)
					{
						case BLUE:
							aiAgent = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
							break;
						case YELLOW:
							aiAgent = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
							break;
						default:
							throw new IllegalStateException();
							
					}
					
					aiAgent.getAthena().addObserver(this);
					
					aiCenterPanel.getChkAiActive().setSelected(aiAgent.isActive());
					if (aiAgent.isActive())
					{
						aiCenterPanel.getModulesPanel().onStart();
					}
					aiCenterPanel.getModulesPanel().getMetisCalculatorsPanel().setActive(true);
					
				} catch (final ModuleNotFoundException err)
				{
					log.fatal("AI Module not found");
				}
				
				aiCenterPanel.getModulesPanel().onStart();
				onAiModeChanged(EAIControlState.MATCH_MODE);
				
				break;
			}
			
			case RESOLVED:
			{
				try
				{
					Agent agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
					agentYellow.removeVisObserver(this);
					agentYellow.removeObserver(this);
					Agent agentBlue = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
					agentBlue.removeVisObserver(this);
					agentBlue.removeObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not get agent module");
				}
				
				aiAgent = null;
				aiCenterPanel.getModulesPanel().getMetisCalculatorsPanel().setActive(false);
				aiCenterPanel.getModulesPanel().onStop();
				aiCenterPanel.clearView();
				break;
			}
			case NOT_LOADED:
			default:
				break;
		}
		
	}
	
	
	@Override
	public void onLookAndFeelChanged()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				SwingUtilities.updateComponentTreeUI(aiCenterPanel);
			}
		});
	}
	
	
	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		if (team == frame.getTeamColor())
		{
			aiCenterPanel.getModulesPanel().getAthenaPanel().onNewVisualizationFrame(frame);
		}
	}
	
	
	@Override
	public void onNewAIInfoFrame(final AIInfoFrame lastFrame)
	{
		if (lastFrame.getTeamColor() != team)
		{
			return;
		}
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Update PlayPanel and bot:play
				final PlayControlPanel playPanel = aiCenterPanel.getModulesPanel().getPlayPanel();
				final RoleControlPanel rolePanel = aiCenterPanel.getModulesPanel().getRolePanel();
				
				playPanel.setBotsWithoutRole(calcFreeBots(lastFrame));
				
				playPanel.setActivePlays(lastFrame.getPlayStrategy().getActivePlays());
				for (APlay play : lastFrame.getPlayStrategy().getActivePlays())
				{
					if (play.getType() == EPlay.GUI_TEST)
					{
						rolePanel.setActiveRoles(play.getRoles());
						break;
					}
				}
				
				aiCenterPanel.getModulesPanel().getAthenaPanel().onNewAIInfoFrame(lastFrame);
				aiCenterPanel.getModulesPanel().getMetisCalculatorsPanel().onNewAIInfoFrame(lastFrame);
			}
		});
	}
	
	
	private int calcFreeBots(final AIInfoFrame aiFrame)
	{
		int counter = aiFrame.getWorldFrame().getTigerBotsAvailable().size();
		
		for (final APlay play : aiFrame.getPlayStrategy().getActivePlays())
		{
			counter -= play.getRoles().size();
		}
		
		return counter;
	}
	
	
	@Override
	public void onAiModeChanged(final EAIControlState mode)
	{
		aiCenterPanel.getModulesPanel().onAiModeChanged(mode);
	}
	
	
	/**
	 * @return
	 */
	public ISumatraView getView()
	{
		return aiCenterPanel;
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
	
	private class GuiFeedbackObserver implements IAICenterObserver, ICalculatorObserver, IAthenaControlPanelObserver,
			ISupporterGridPanelObserver
	{
		
		@Override
		public void addPlay(final APlay play)
		{
			aiAgent.getAthena().getAthenaAdapter().getAiControl().addPlay(play);
		}
		
		
		@Override
		public void removePlay(final APlay play)
		{
			aiAgent.getAthena().getAthenaAdapter().getAiControl().removePlay(play);
		}
		
		
		@Override
		public void addRoles2Play(final APlay play, final int numRoles)
		{
			aiAgent.getAthena().getAthenaAdapter().getAiControl().addRoles2Play(play, numRoles);
		}
		
		
		@Override
		public void removeRolesFromPlay(final APlay play, final int numRoles)
		{
			aiAgent.getAthena().getAthenaAdapter().getAiControl().removeRolesFromPlay(play, numRoles);
		}
		
		
		@Override
		public void addRole(final ARole role, final BotID botId)
		{
			aiAgent.getAthena().getAthenaAdapter().getAiControl().addRole(role, botId);
		}
		
		
		@Override
		public void removeRole(final ARole role)
		{
			aiAgent.getAthena().getAthenaAdapter().getAiControl().removeRole(role);
		}
		
		
		@Override
		public void onAiModeChanged(final EAIControlState mode)
		{
			aiAgent.getAthena().changeMode(mode);
		}
		
		
		@Override
		public void onNewRoleFinderInfos(final Map<EPlay, RoleFinderInfo> infos)
		{
			aiAgent.getAthena().getAthenaAdapter().getAiControl().getRoleFinderInfos().clear();
			aiAgent.getAthena().getAthenaAdapter().getAiControl().getRoleFinderInfos().putAll(infos);
		}
		
		
		@Override
		public void onNewRoleFinderOverrides(final Map<EPlay, Boolean> overrides)
		{
			aiAgent.getAthena().getAthenaAdapter().getAiControl().getRoleFinderOverrides().clear();
			aiAgent.getAthena().getAthenaAdapter().getAiControl().getRoleFinderOverrides().putAll(overrides);
		}
		
		
		@Override
		public void onCalculatorStateChanged(final ECalculator eCalc, final boolean active)
		{
			if (aiAgent != null)
			{
				aiAgent.getMetis().setCalculatorActive(eCalc, active);
			}
		}
		
		
		@Override
		public void onWeightChanged(final EScoringTypes type, final double value)
		{
			if (aiAgent == null)
			{
				return;
			}
			// RedirectPosGPUCalc calc = (RedirectPosGPUCalc)
			// aiAgent.getMetis().getCalculator(ECalculator.REDIRECT_POS_GPU);
			// calc.updateWeight(type, value);
			// for (Map.Entry<EScoringTypes, Double> entry : calc.getWeights().entrySet())
			// {
			// aiCenterPanel.getModulesPanel().getSupporterGridPanel().setWeighting(entry.getKey(), entry.getValue());
			// }
		}
		
		
		@Override
		public void onQueryWeights()
		{
			if (aiAgent == null)
			{
				return;
			}
			// RedirectPosGPUCalc calc = (RedirectPosGPUCalc)
			// aiAgent.getMetis().getCalculator(ECalculator.REDIRECT_POS_GPU);
			// for (Map.Entry<EScoringTypes, Double> entry : calc.getWeights().entrySet())
			// {
			// aiCenterPanel.getModulesPanel().getSupporterGridPanel().setWeighting(entry.getKey(), entry.getValue());
			// }
		}
		
		
		@Override
		public void onSaveWeights(final String name)
		{
			if (aiAgent == null)
			{
				return;
			}
			// RedirectPosGPUCalc calc = (RedirectPosGPUCalc)
			// aiAgent.getMetis().getCalculator(ECalculator.REDIRECT_POS_GPU);
			// calc.saveWeights(name);
		}
		
		
		@Override
		public void onLoadWeights(final String name)
		{
			if (aiAgent == null)
			{
				return;
			}
			// RedirectPosGPUCalc calc = (RedirectPosGPUCalc)
			// aiAgent.getMetis().getCalculator(ECalculator.REDIRECT_POS_GPU);
			// calc.loadWeights(name);
		}
	}
}
