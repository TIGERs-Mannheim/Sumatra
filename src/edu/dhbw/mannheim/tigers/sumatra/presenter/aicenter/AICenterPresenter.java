/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.AthenaControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.exceptions.LoadConfigException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillSystemObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.AICenterPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.InformationPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.botoverview.BotFullOverviewPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.botoverview.BotOverviewPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.IModuleControlPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.PlayControlPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.TacticalFieldControlPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.listenerVariables.ModulesState;


/**
 * This is the presenter for the ai view in sumatra. It's core functionality is realized using a state-machine
 * representing the different modi of influence the AI-developer wants to use.
 * These modi are {@link EAIControlState}s. Their implementations are:
 * <ul>
 * <li> {@link MatchModeState}</li>
 * <li> {@link PlayTestState}</li>
 * <li> {@link RoleTestState}</li>
 * </ul>
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>, Gero
 */
public class AICenterPresenter implements ISumatraView, IModuliStateObserver, IAIObserver, IBotManagerObserver,
		ILookAndFeelStateObserver, IAICenterState, IModuleControlPanelObserver, ISkillSystemObserver
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final int		ID					= 3;
	private static final String	TITLE				= "AI Center";
	

	private final Logger				log				= Logger.getLogger(getClass());
	
	// Modules
	private AAgent						aiAgent			= null;
	private ABotManager				botManager		= null;
	private ASkillSystem				skillSystem		= null;
	
	private AICenterPanel			aiCenterPanel	= null;
	
	private final JMenu				aiConfigMenu;
	private List<JMenuItem>			aiConfigs		= new ArrayList<JMenuItem>();
	
	private final JMenu				tacticsMenu;
	private List<JMenuItem>			tacticsConfigs	= new ArrayList<JMenuItem>();
	
	// Athena-control
	private final AthenaControl	aiControl;
	private AICenterState			currentState	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public AICenterPresenter()
	{
		// --- ai config menu ---
		aiConfigMenu = new JMenu("AI-configuration");
		
		// --- tactic config menu ---
		tacticsMenu = new JMenu("Tactics");
		
		aiCenterPanel = new AICenterPanel();
		
		aiControl = new AthenaControl();
		
		aiCenterPanel.getModulesPanel().addObserver(this);
		aiCenterPanel.getModulesPanel().getRolePanel().addObserver(this);
		aiCenterPanel.getModulesPanel().getPlayPanel().addObserver(this);
		
		ModuliStateAdapter.getInstance().addObserver(this);
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		
		currentState = new PlayTestState(this);
		
		aiCenterPanel.clearView();
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public int getID()
	{
		return ID;
	}
	

	@Override
	public String getTitle()
	{
		return TITLE;
	}
	

	@Override
	public Component getViewComponent()
	{
		return aiCenterPanel;
	}
	

	@Override
	public List<JMenu> getCustomMenus()
	{
		List<JMenu> menus = new ArrayList<JMenu>();
		
		menus.add(aiConfigMenu);
		menus.add(tacticsMenu);
		
		updateAIConfigMenu();
		updateTacticsConfigMenu();
		
		return menus;
	}
	

	@Override
	public void onFocused()
	{
	}
	

	@Override
	public void onFocusLost()
	{
	}
	

	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
			{
				SumatraModel model = SumatraModel.getInstance();
				try
				{
					aiAgent = (AAgent) model.getModule(AAgent.MODULE_ID);
					
					// get AIInfoFrames
					aiAgent.addObserver(this);
					
					skillSystem = (ASkillSystem) model.getModule(ASkillSystem.MODULE_ID);
					skillSystem.addObserver(this);
					
				} catch (ModuleNotFoundException err)
				{
					log.fatal("AI Module not found");
				}
				
				try
				{
					botManager = (ABotManager) model.getModule(ABotManager.MODULE_ID);
					botManager.addObserver(this);
					
					BotFullOverviewPanel botPanel = aiCenterPanel.getBotOverviewPanel();
					synchronized (botPanel)
					{
						for (ABot bot : botManager.getAllBots().values())
						{
							if (bot.isActive())
							{
								botPanel.addBotPanel(bot.getBotId(), bot.getName());
							}
						}
					}
					
				} catch (ModuleNotFoundException err)
				{
					log.fatal("Botmanager not found");
				}
				
				// --- update custom-menu ---
				updateAIConfigMenu();
				updateTacticsConfigMenu();
				
				aiCenterPanel.getModulesPanel().onStart();
				
				break;
			}
				
			default:
			{
				if (aiAgent != null)
				{
					aiAgent.removeObserver(this);
				}
				aiAgent = null;
				
				if (skillSystem != null)
				{
					skillSystem.addObserver(this);
				}
				skillSystem = null;
				
				if (botManager != null)
				{
					botManager.removeObserver(this);
					BotFullOverviewPanel botPanel = aiCenterPanel.getBotOverviewPanel();
					synchronized (botPanel)
					{
						botPanel.removeAllBotPanels();
					}
					SwingUtilities.updateComponentTreeUI(aiCenterPanel);
				}
				botManager = null;
				
				aiControl.clear();
				aiCenterPanel.clearView();
				break;
			}
		}
		
	}
	

	@Override
	public void onBotAdded(ABot bot)
	{
		BotFullOverviewPanel botPanel = aiCenterPanel.getBotOverviewPanel();
		synchronized (botPanel)
		{
			botPanel.addBotPanel(bot.getBotId(), bot.getName());
		}
	}
	

	@Override
	public void onBotRemoved(ABot bot)
	{
		BotFullOverviewPanel botPanel = aiCenterPanel.getBotOverviewPanel();
		synchronized (botPanel)
		{
			botPanel.removeAllBotPanels();
		}
	}
	

	@Override
	public void onBotIdChanged(int oldId, int newId)
	{
		// Nothing to do here
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
	

	// -------------------------------------------------------------------------
	// --- ai observers --------------------------------------------------------
	// -------------------------------------------------------------------------
	
	@Override
	public void onNewFieldRaster(int columnSize, int rowSize, int columnSizeAnalysing, int rowSizeAnalysing)
	{
		// / Nothing to do here
	}
	

	@Override
	public void onNewAIInfoFrame(AIInfoFrame lastFrame)
	{
		// Update InformationPanel
		InformationPanel informationPanel = aiCenterPanel.getInformationPanel();
		informationPanel.setPlayBehavior(lastFrame.playStrategy.getMatchBehavior());
		
		// Update TacticalFieldControlPanel
		TacticalFieldControlPanel tacticalFieldPanel = aiCenterPanel.getModulesPanel().getTacticalFieldControlPanel();
		tacticalFieldPanel.setBallPossession(lastFrame.tacticalInfo.getBallPossesion());
		tacticalFieldPanel.setClosestTeamToBall(lastFrame.tacticalInfo.getTeamClosestToBall());
		
		tacticalFieldPanel.setTigersScoringChance(lastFrame.tacticalInfo.getTigersScoringChance());
		tacticalFieldPanel.setOpponentScoringChance(lastFrame.tacticalInfo.getOpponentScoringChance());
		tacticalFieldPanel.setTigersApproximateScoringChance(lastFrame.tacticalInfo.getTigersApproximateScoringChance());
		tacticalFieldPanel.setOpponentApproximateScoringChance(lastFrame.tacticalInfo.getOpponentApproximateScoringChance());

		// still missing:
//		lastFrame.tacticalFieldInfo.getDefGoalPoints();
//		lastFrame.tacticalFieldInfo.getEnemiesOnOwnSubfield();
//		lastFrame.tacticalFieldInfo.getOffCarrierPoints();
//		lastFrame.tacticalFieldInfo.getOffLeftReceiverPoints();
//		lastFrame.tacticalFieldInfo.getOffRightReceiverPoints();

		// Update PlayPanel and bot:play
		PlayControlPanel playPanel = aiCenterPanel.getModulesPanel().getPlayPanel();
		
		playPanel.setBotsWithoutRole(calcFreeBots(lastFrame));
		
		playPanel.setBestPlays(lastFrame.playStrategy.getBestPlays());
		playPanel.setActivePlays(lastFrame.playStrategy.getActivePlays());
		
		BotFullOverviewPanel botPanel = aiCenterPanel.getBotOverviewPanel();
		synchronized (botPanel)
		{
			illustrateBots(lastFrame, botPanel);
		}
	}
	
	
	private int	lastAssignmentCount	= 0;
	
	
	private void illustrateBots(AIInfoFrame lastAIInfoframe, BotFullOverviewPanel botPanel)
	{
		if (lastAssignmentCount != lastAIInfoframe.assignedRoles.values().size())
		{
			botPanel.clearBotViews();
		}
		
		final WorldFrame wf = lastAIInfoframe.worldFrame;
		for (Entry<Integer, ARole> entry : lastAIInfoframe.assignedRoles.entrySet())
		{
			ARole role = entry.getValue();
			int botId = entry.getKey();
			
			BotOverviewPanel botOverview = botPanel.getBotPanel(botId);
			
			if (botOverview != null)
			{
				// final long start = System.nanoTime();
				botOverview.setRole(role);
				// final long afterSetRole = System.nanoTime();
				
				// Gather condition status
				final List<ACondition> newConditions = new ArrayList<ACondition>();
				for (ACondition con : role.getConditions().values())
				{
					if (con.getType() != ECondition.DESTINATION)
					{
						newConditions.add(con);
					}
				}
				
				botOverview.setConditions(role.checkAllConditions(lastAIInfoframe), newConditions);
				// final long afterSetConditions = System.nanoTime();
				
				// Set destination condition
				ACondition destCon = role.getConditions().get(ECondition.DESTINATION);
				botOverview.calcDestinationStatus(role.getDestination(),
						destCon == null ? null : destCon.checkCondition(wf));
				
				// final long afterSetDest = System.nanoTime();
				//
				// if (botId == 3)
				// {
				// if (i % 100 == 0)
				// {
				// final long durSetRole = afterSetRole - start;
				// final long durSetConditions = afterSetConditions - afterSetRole;
				// final long durSetDest = afterSetDest - afterSetConditions;
				//
				// System.out.println("############################################");
				// System.out.println("SetRole:       " + df.format(durSetRole));
				// System.out.println("SetConditions: " + df.format(durSetConditions));
				// System.out.println("SetDest:       " + df.format(durSetDest));
				// }
				// i++;
				// }
			} else
			{
				log.fatal("Threading problems with '" + Thread.currentThread().getName() + "', notify Gero!");
			}
		}
		
		lastAssignmentCount = lastAIInfoframe.assignedRoles.values().size();
	}
	

	// private int i = 0;
	// private final DecimalFormat df = new DecimalFormat("00,000,000,000");
	
	private int calcFreeBots(AIInfoFrame aiFrame)
	{
		int counter = aiFrame.worldFrame.tigerBots.size();
		
		for (APlay play : aiFrame.playStrategy.getActivePlays())
		{
			counter -= play.getRoles().size();
		}
		
		return counter;
	}
	

	@Override
	public void onNewPath(Path path)
	{
		// Nothing to do here
	}
	

	// -------------------------------------------------------------------------
	// --- skill-system observers ----------------------------------------------
	// -------------------------------------------------------------------------
	@Override
	public void onSkillStarted(ASkill skill, int botID)
	{
		BotFullOverviewPanel allBotPanel = aiCenterPanel.getBotOverviewPanel();
		synchronized (allBotPanel)
		{
			BotOverviewPanel botPanel = allBotPanel.getBotPanel(botID);
			if (botPanel == null)
			{
				return;
			}
			
			switch (skill.getGroup())
			{
				case MOVE:
					botPanel.setSkillMove(skill.getSkillName());
					
					break;
				
				case DRIBBLE:
					botPanel.setSkillDribble(skill.getSkillName());
					
					break;
				
				case KICK:
					botPanel.setSkillShooter(skill.getSkillName());
					
					break;
			}
		}
	}
	

	@Override
	public void onSkillCompleted(ASkill skill, int botID)
	{
		BotFullOverviewPanel allBotPanel = aiCenterPanel.getBotOverviewPanel();
		synchronized (allBotPanel)
		{
			BotOverviewPanel botPanel = allBotPanel.getBotPanel(botID);
			if (botPanel == null)
			{
				return;
			}
			
			switch (skill.getGroup())
			{
				case MOVE:
					botPanel.unSetSkillMove();
					
					break;
				
				case DRIBBLE:
					botPanel.unSetSkillDribble();
					
					break;
				
				case KICK:
					botPanel.unSetSkillShooter();
					
					break;
			}
		}
	}
	

	// -------------------------------------------------------------------------
	// --- gui-controls --------------------------------------------------------
	// -------------------------------------------------------------------------
	AthenaControl getControl()
	{
		return aiControl;
	}
	

	void sendControl()
	{
		aiAgent.onNewAthenaControl(aiControl);	//new AthenaControl(aiControl));
	}
	

	// -------------------------------------------------------------------------
	// --- state handling/IModulePanelObserver ---------------------------------
	// -------------------------------------------------------------------------
	private void changeGuiState(EAIControlState state)
	{
		aiControl.setControlState(state);
		
		switch (aiControl.getControlState())
		{
			case MATCH_MODE:
				currentState = new MatchModeState(this);
				aiCenterPanel.getModulesPanel().setMatchMode();
				break;
			
			case PLAY_TEST_MODE:
				currentState = new PlayTestState(this);
				aiCenterPanel.getModulesPanel().setPlayTestMode();
				break;
			
			case ROLE_TEST_MODE:
				currentState = new RoleTestState(this);
				aiCenterPanel.getModulesPanel().setRoleTestMode();
				break;
			
			case EMERGENCY_MODE:
				currentState = new EmergencyState(this);
				aiCenterPanel.getModulesPanel().setEmergencyMode();
				break;
		}
		
		// Prepare
		currentState.init();
	}
	

	@Override
	public void onMatchMode()
	{
		if (aiControl.getControlState() != EAIControlState.MATCH_MODE)
		{
			changeGuiState(EAIControlState.MATCH_MODE);
		}
	};
	

	@Override
	public void onPlayTestMode()
	{
		if (aiControl.getControlState() != EAIControlState.PLAY_TEST_MODE)
		{
			changeGuiState(EAIControlState.PLAY_TEST_MODE);
		}
	}
	

	@Override
	public void onRoleTestMode()
	{
		if (aiControl.getControlState() != EAIControlState.ROLE_TEST_MODE)
		{
			changeGuiState(EAIControlState.ROLE_TEST_MODE);
		}
	}
	

	@Override
	public void onEmergencyMode()
	{
		if (aiControl.getControlState() != EAIControlState.EMERGENCY_MODE)
		{
			changeGuiState(EAIControlState.EMERGENCY_MODE);
		}
		
	}
	

	// -------------------------------------------------------------------------
	@Override
	public void addPlay(EPlay play)
	{
		currentState.addPlay(play);
	}
	

	@Override
	public void removePlay(List<EPlay> oddPlays)
	{
		currentState.removePlay(oddPlays);
	}
	

	@Override
	public void forceNewDecision()
	{
		currentState.forceNewDecision();
	}
	
	

	// -------------------------------------------------------------------------
	
	@Override
	public void addRole(ERole role)
	{
		currentState.addRole(role);
	}
	

	@Override
	public void addRole(ERole role, int botId)
	{
		currentState.addRole(role, botId);
	}
	

	@Override
	public void removeRole(ERole role)
	{
		currentState.removeRole(role);
	}
	

	@Override
	public void clearRoles()
	{
		currentState.clearRoles();
	}
	
	// -------------------------------------------------------------------------
	// --- menu handling -------------------------------------------------------
	// -------------------------------------------------------------------------
	
	protected class LoadAIConfig implements ActionListener
	{
		private String	filename;
		
		
		public LoadAIConfig(String filename)
		{
			this.filename = filename;
		}
		

		@Override
		public void actionPerformed(ActionEvent e)
		{
			AAgent.currentConfig = filename;
			
			if (aiAgent == null)
			{
				return;
			}
			
			try
			{
				AIConfig.getInstance().loadAIConfig(AAgent.AI_CONFIG_PATH + filename);
			} catch (LoadConfigException err)
			{
				log.error("Can't load AI-Config:" + AAgent.AI_CONFIG_PATH + filename);
			}
			
			updateAIConfigMenu();
		}
	}
	
	protected class LoadTacticsConfig implements ActionListener
	{
		private String	filename;
		
		
		public LoadTacticsConfig(String filename)
		{
			this.filename = filename;
		}
		

		@Override
		public void actionPerformed(ActionEvent e)
		{
			AAgent.currentTactics = filename;
			
			if (aiAgent == null)
			{
				return;
			}
			
			try
			{
				AIConfig.getInstance().loadTacticsConfig(AAgent.TACTICS_CONFIG_PATH + filename);
			} catch (LoadConfigException err)
			{
				log.error("Can't load Tactics-Config:" + AAgent.TACTICS_CONFIG_PATH + filename);
			}
			
			updateTacticsConfigMenu();
		}
	}
	
	
	private void updateAIConfigMenu()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				for (JMenuItem item : aiConfigs)
				{
					aiConfigMenu.remove(item);
				}
				
				aiConfigs.clear();
				
				ButtonGroup group = new ButtonGroup();
				File dir = new File(AAgent.AI_CONFIG_PATH);
				File[] fileList = dir.listFiles();
				for (File f : fileList)
				{
					if (!f.isHidden())
					{
						String name = f.getName();
						
						JMenuItem item = new JRadioButtonMenuItem(name);
						group.add(item);
						
						if (AAgent.currentConfig != null && name.equals(AAgent.currentConfig))
						{
							item.setSelected(true);
						}
						
						item.addActionListener(new LoadAIConfig(name));
						
						aiConfigs.add(item);
						aiConfigMenu.add(item);
					}
				}
			}
		});
	}
	

	private void updateTacticsConfigMenu()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				for (JMenuItem item : tacticsConfigs)
				{
					tacticsMenu.remove(item);
				}
				
				tacticsConfigs.clear();
				
				ButtonGroup group = new ButtonGroup();
				File dir = new File(AAgent.TACTICS_CONFIG_PATH);
				File[] fileList = dir.listFiles();
				for (File f : fileList)
				{
					if (!f.isHidden())
					{
						String name = f.getName();
						
						JMenuItem item = new JRadioButtonMenuItem(name);
						group.add(item);
						
						if (AAgent.currentTactics != null && name.equals(AAgent.currentTactics))
						{
							item.setSelected(true);
						}
						
						item.addActionListener(new LoadTacticsConfig(name));
						
						tacticsConfigs.add(item);
						tacticsMenu.add(item);
					}
				}
			}
		});
	}
	

	@Override
	public void onShown()
	{
		
	}
	

	@Override
	public void onHidden()
	{
		
	}
	
}
