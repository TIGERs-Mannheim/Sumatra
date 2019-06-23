/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.Agent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ECalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.AICenterPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.CalculatorList;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.IAIModeChanged;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.ICalculatorObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.PlayControlPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.RoleControlPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.TacticalFieldControlPanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * This is the presenter for the ai view in sumatra. It's core functionality is realized using a state-machine
 * representing the different modi of influence the AI-developer wants to use.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>, Gero
 */
public class AICenterPresenter implements IModuliStateObserver, IAIObserver, ILookAndFeelStateObserver,
		ISumatraViewPresenter, IAIModeChanged
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
		
		CalculatorList calulatorList = aiCenterPanel.getModulesPanel().getMetisCalculatorsPanel().getCalculatorList();
		calulatorList.addObserver(guiFeedbackObserver);
		for (ECalculator calc : ECalculator.values())
		{
			calulatorList.addElement(calc.name(), calc.isInitiallyActive());
		}
		
		ModuliStateAdapter.getInstance().addObserver(this);
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
					
					aiAgent.addObserver(this);
					aiAgent.getAthena().addObserver(this);
					
					aiCenterPanel.getChkAiActive().setSelected(aiAgent.isActive());
					if (aiAgent.isActive())
					{
						aiCenterPanel.getModulesPanel().onStart();
					}
					
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
				if (aiAgent != null)
				{
					aiAgent.removeObserver(this);
				}
				aiAgent = null;
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
	public void onNewAIInfoFrame(final AIInfoFrame lastFrame)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Update TacticalFieldControlPanel
				final TacticalFieldControlPanel tacticalFieldPanel = aiCenterPanel.getModulesPanel()
						.getTacticalFieldControlPanel();
				tacticalFieldPanel.setBallPossession(lastFrame.getTacticalField().getBallPossession());
				tacticalFieldPanel.setBotLastTouchedBall(lastFrame.getTacticalField().getBotLastTouchedBall());
				
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
			}
		});
	}
	
	
	@Override
	public void onAIException(final Exception ex, final IRecordFrame frame, final IRecordFrame prevFrame)
	{
	}
	
	
	private int calcFreeBots(final AIInfoFrame aiFrame)
	{
		int counter = aiFrame.getWorldFrame().tigerBotsAvailable.size();
		
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
	
	
	@Override
	public void onEmergencyStop()
	{
		if ((aiAgent != null))
		{
			aiAgent.getAthena().changeMode(EAIControlState.EMERGENCY_MODE);
		}
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
	
	private class GuiFeedbackObserver implements IAICenterObserver, ICalculatorObserver
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
		public void selectedCalculatorsChanged(final List<String> values)
		{
			if (aiAgent != null)
			{
				List<ECalculator> calculators = new ArrayList<ECalculator>(values.size());
				for (String value : values)
				{
					calculators.add(ECalculator.valueOf(value));
				}
				aiAgent.getMetis().setActiveCalculators(calculators);
			}
		}
	}
}
