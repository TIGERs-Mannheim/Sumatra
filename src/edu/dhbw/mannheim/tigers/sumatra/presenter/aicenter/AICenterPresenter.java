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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.ApollonControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.AthenaControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ECalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
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
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.CalculatorList;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.IApollonControlPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.ICalculatorObserver;
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
public class AICenterPresenter implements IModuliStateObserver, IAIObserver, IBotManagerObserver,
		ILookAndFeelStateObserver, IAICenterState, IModuleControlPanelObserver, ISkillSystemObserver,
		ICalculatorObserver, IApollonControlPanelObserver
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log							= Logger.getLogger(AICenterPresenter.class.getName());
	
	
	// for limiting update frequency [in milliseconds]
	private static final long		VISUALIZATION_FREQUENCY	= 500;
	private long						start							= System.nanoTime();
	
	// Modules
	private final SumatraModel		model							= SumatraModel.getInstance();
	private AAgent						aiAgent						= null;
	private ABotManager				botManager					= null;
	private ASkillSystem				skillSystem					= null;
	
	private AICenterPanel			aiCenterPanel				= null;
	
	// Athena-control
	private final AthenaControl	aiControl;
	private AICenterState			currentState				= null;
	private final ApollonControl	apollonControl;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public AICenterPresenter()
	{
		aiCenterPanel = new AICenterPanel();
		
		aiControl = new AthenaControl();
		apollonControl = new ApollonControl();
		sendApollonControl();
		
		aiCenterPanel.getModulesPanel().addObserver(this);
		aiCenterPanel.getModulesPanel().getRolePanel().addObserver(this);
		aiCenterPanel.getModulesPanel().getPlayPanel().addObserver(this);
		aiCenterPanel.getModulesPanel().getApollonControlPanel().addObserver(this);
		
		CalculatorList calulatorList = aiCenterPanel.getModulesPanel().getMetisCalculatorsPanel().getCalculatorList();
		calulatorList.addObserver(this);
		for (ECalculator calc : ECalculator.values())
		{
			calulatorList.addElement(calc.name(), calc.isInitiallyActive());
		}
		
		ModuliStateAdapter.getInstance().addObserver(this);
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		
		currentState = new PlayTestState(this);
		
		aiCenterPanel.clearView();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
			{
				try
				{
					aiAgent = (AAgent) model.getModule(AAgent.MODULE_ID);
					
					// get AIInfoFrames
					aiAgent.addObserver(this);
					
					skillSystem = (ASkillSystem) model.getModule(ASkillSystem.MODULE_ID);
					skillSystem.addObserver(this);
					
					
				} catch (final ModuleNotFoundException err)
				{
					log.fatal("AI Module not found");
				}
				
				try
				{
					botManager = (ABotManager) model.getModule(ABotManager.MODULE_ID);
					botManager.addObserver(this);
					
					final BotFullOverviewPanel botPanel = aiCenterPanel.getBotOverviewPanel();
					synchronized (botPanel)
					{
						for (final ABot bot : botManager.getAllBots().values())
						{
							if (bot.isActive())
							{
								botPanel.addBotPanel(bot.getBotID(), bot.getName());
							}
						}
					}
					
				} catch (final ModuleNotFoundException err)
				{
					log.fatal("Botmanager not found");
				}
				
				// --- update custom-menu ---
				aiCenterPanel.getModulesPanel().onStart();
				
				break;
			}
			
			case RESOLVED:
			{
				if (aiAgent != null)
				{
					aiAgent.removeObserver(this);
				}
				aiAgent = null;
				
				if (skillSystem != null)
				{
					skillSystem.removeObserver(this);
				}
				skillSystem = null;
				
				if (botManager != null)
				{
					botManager.removeObserver(this);
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							final BotFullOverviewPanel botPanel = aiCenterPanel.getBotOverviewPanel();
							synchronized (botPanel)
							{
								botPanel.removeAllBotPanels();
							}
							SwingUtilities.updateComponentTreeUI(aiCenterPanel);
						}
					});
				}
				botManager = null;
				
				aiControl.clear();
				aiCenterPanel.clearView();
				break;
			}
			case NOT_LOADED:
			default:
				break;
		}
		
	}
	
	
	@Override
	public void onBotConnectionChanged(ABot bot)
	{
		if (bot.getNetworkState() != ENetworkState.OFFLINE)
		{
			onBotAdded(bot);
		} else
		{
			onBotRemoved(bot);
		}
	}
	
	
	@Override
	public void onBotAdded(ABot bot)
	{
		if (bot.getNetworkState() == ENetworkState.OFFLINE)
		{
			return;
		}
		final BotFullOverviewPanel botPanel = aiCenterPanel.getBotOverviewPanel();
		synchronized (botPanel)
		{
			botPanel.addBotPanel(bot.getBotID(), bot.getName());
		}
	}
	
	
	@Override
	public void onBotRemoved(ABot bot)
	{
		final BotFullOverviewPanel botPanel = aiCenterPanel.getBotOverviewPanel();
		synchronized (botPanel)
		{
			botPanel.removeBotPanel(bot.getBotID());
		}
	}
	
	
	@Override
	public void onBotIdChanged(BotID oldId, BotID newId)
	{
		final BotFullOverviewPanel botPanel = aiCenterPanel.getBotOverviewPanel();
		synchronized (botPanel)
		{
			botPanel.updatePanels();
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
	
	
	// -------------------------------------------------------------------------
	// --- ai observers --------------------------------------------------------
	// -------------------------------------------------------------------------
	@Override
	public void onNewAIInfoFrame(AIInfoFrame lastFrame)
	{
		final long now = System.nanoTime();
		final long timePassed = now - start;
		final long freq = TimeUnit.MILLISECONDS.toNanos(VISUALIZATION_FREQUENCY);
		if (timePassed > freq)
		{
			// Update InformationPanel
			final InformationPanel informationPanel = aiCenterPanel.getInformationPanel();
			informationPanel.setPlayBehavior(lastFrame.playStrategy.getMatchBehavior());
			
			// Update TacticalFieldControlPanel
			final TacticalFieldControlPanel tacticalFieldPanel = aiCenterPanel.getModulesPanel()
					.getTacticalFieldControlPanel();
			tacticalFieldPanel.setBallPossession(lastFrame.tacticalInfo.getBallPossession());
			tacticalFieldPanel.setClosestTeamToBall(lastFrame.tacticalInfo.getTeamClosestToBall());
			tacticalFieldPanel.setBotLastTouchedBall(lastFrame.tacticalInfo.getBotLastTouchedBall());
			
			tacticalFieldPanel.setTigersScoringChance(lastFrame.tacticalInfo.getTigersScoringChance());
			tacticalFieldPanel.setOpponentScoringChance(lastFrame.tacticalInfo.getOpponentScoringChance());
			tacticalFieldPanel.setTigersApproximateScoringChance(lastFrame.tacticalInfo
					.getTigersApproximateScoringChance());
			tacticalFieldPanel.setOpponentApproximateScoringChance(lastFrame.tacticalInfo
					.getOpponentApproximateScoringChance());
			
			// Update PlayPanel and bot:play
			final PlayControlPanel playPanel = aiCenterPanel.getModulesPanel().getPlayPanel();
			
			playPanel.setBotsWithoutRole(calcFreeBots(lastFrame));
			
			playPanel.setActivePlays(lastFrame.playStrategy.getActivePlays());
			
			final BotFullOverviewPanel botPanel = aiCenterPanel.getBotOverviewPanel();
			synchronized (botPanel)
			{
				illustrateBots(lastFrame, botPanel);
			}
			start = System.nanoTime();
		}
	}
	
	
	@Override
	public void onAIException(Exception ex, AIInfoFrame frame, AIInfoFrame prevFrame)
	{
		final InformationPanel informationPanel = aiCenterPanel.getInformationPanel();
		informationPanel.setAIException(ex);
		
	}
	
	
	private int	lastAssignmentCount	= 0;
	
	
	private void illustrateBots(AIInfoFrame lastAIInfoframe, BotFullOverviewPanel botPanel)
	{
		if (lastAssignmentCount != lastAIInfoframe.getAssigendERoles().size())
		{
			botPanel.clearBotViews();
		}
		
		for (final Entry<BotID, ARole> entry : lastAIInfoframe.getAssigendRoles())
		{
			final ARole role = entry.getValue();
			final BotID botId = entry.getKey();
			final TrackedTigerBot bot = lastAIInfoframe.worldFrame.tigerBotsVisible.getWithNull(botId);
			if (bot == null)
			{
				continue;
			}
			
			final BotOverviewPanel botOverview = botPanel.getBotPanel(botId);
			
			if (botOverview != null)
			{
				botOverview.setRole(role);
				botOverview.setState(role.getCurrentState());
				// Gather condition status
				final List<ACondition> newConditions = new ArrayList<ACondition>();
				for (final ACondition con : role.getConditions().values())
				{
					if (con.getType() != ECondition.DESTINATION)
					{
						newConditions.add(con);
					}
				}
				
				botOverview.setConditions(role.checkAllConditions(lastAIInfoframe.worldFrame), newConditions);
				
				// Set destination condition
				final ACondition destCon = role.getConditions().get(ECondition.DESTINATION);
				if ((destCon == null) || !destCon.isActive())
				{
					botOverview.calcDestinationStatus(Vector2.ZERO_VECTOR, EConditionState.DISABLED);
				} else
				{
					botOverview.calcDestinationStatus(role.getDestination(),
							destCon.checkCondition(lastAIInfoframe.worldFrame, botId));
				}
				botOverview.setBallContact(bot.hasBallContact());
				
			} else
			{
				log.warn("Bot overview panel for Bot " + botId.getNumber() + " does not exist!");
			}
		}
		
		lastAssignmentCount = lastAIInfoframe.getAssigendERoles().size();
	}
	
	
	private int calcFreeBots(AIInfoFrame aiFrame)
	{
		int counter = aiFrame.worldFrame.tigerBotsAvailable.size();
		
		for (final APlay play : aiFrame.playStrategy.getActivePlays())
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
	public void onSkillStarted(ASkill skill, BotID botID)
	{
		final BotFullOverviewPanel allBotPanel = aiCenterPanel.getBotOverviewPanel();
		synchronized (allBotPanel)
		{
			final BotOverviewPanel botPanel = allBotPanel.getBotPanel(botID);
			if (botPanel == null)
			{
				return;
			}
			botPanel.setSkill(skill.getSkillName());
		}
	}
	
	
	@Override
	public void onSkillCompleted(ASkill skill, BotID botID)
	{
		final BotFullOverviewPanel allBotPanel = aiCenterPanel.getBotOverviewPanel();
		synchronized (allBotPanel)
		{
			final BotOverviewPanel botPanel = allBotPanel.getBotPanel(botID);
			if (botPanel == null)
			{
				return;
			}
			botPanel.unSetSkill();
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
		aiAgent.onNewAthenaControl(aiControl);
	}
	
	
	private ApollonControl getApollonControl()
	{
		return apollonControl;
	}
	
	
	private void sendApollonControl()
	{
		// aiAgent is null when Moduli isn't started. The Apollon Config is stored and send when Moduli is started.
		aiCenterPanel.getModulesPanel().getApollonControlPanel().onNewApollonControl(apollonControl);
		if (aiAgent != null)
		{
			aiAgent.onNewApollonControl(new ApollonControl(apollonControl));
		}
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
			case MIXED_TEAM_MODE:
				sendApollonControl();
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
	}
	
	
	@Override
	public void onMixedTeamMode()
	{
		if (aiControl.getControlState() != EAIControlState.MIXED_TEAM_MODE)
		{
			changeGuiState(EAIControlState.MIXED_TEAM_MODE);
		}
	}
	
	
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
	public void addNewPlay(EPlay play, int numRolesToAssign)
	{
		currentState.addNewPlay(play, numRolesToAssign);
	}
	
	
	@Override
	public void removePlay(APlay play)
	{
		currentState.removePlay(play);
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
	public void addRole(ERole role, BotID botId)
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
	
	
	@Override
	public void onKnowledgeBaseNameChanged(String newName)
	{
		getApollonControl().setKnowledgeBaseName(newName);
		sendApollonControl();
	}
	
	
	@Override
	public void onAcceptableMatchChanged(int newAccMatch)
	{
		double acceptableMatch = newAccMatch / 100;
		getApollonControl().setAcceptableMatch(acceptableMatch);
		sendApollonControl();
	}
	
	
	@Override
	public void onDatabasePathChanged(String newPath)
	{
		getApollonControl().setDatabasePath(newPath);
		sendApollonControl();
	}
	
	
	@Override
	public void onPersistStrategyChanged(boolean merge)
	{
		getApollonControl().setPersistStrategyMerge(merge);
		sendApollonControl();
	}
	
	
	@Override
	public void onSaveOnCloseChanged(boolean saveOnClose)
	{
		getApollonControl().setSaveOnClose(saveOnClose);
		sendApollonControl();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ISumatraView getView()
	{
		return aiCenterPanel;
	}
	
	
	@Override
	public void selectedCalculatorsChanged(List<String> values)
	{
		if (aiAgent != null)
		{
			List<ECalculator> calculators = new ArrayList<ECalculator>(values.size());
			for (String value : values)
			{
				calculators.add(ECalculator.valueOf(value));
			}
			aiAgent.setActiveCalculators(calculators);
		}
	}
	
	
	@Override
	public void onSaveKbNow()
	{
		if (aiAgent != null)
		{
			aiAgent.onSaveKnowledgeBase();
		}
	}
	
	
	@Override
	public void onCleanKbNow()
	{
		
	}
}
