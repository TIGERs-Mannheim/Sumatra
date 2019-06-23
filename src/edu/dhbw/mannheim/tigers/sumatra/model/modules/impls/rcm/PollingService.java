/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.java.games.input.Controller;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.Agent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.RcmAction.EActionType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.GenericSkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.RecordManager;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;
import edu.dhbw.mannheim.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.proto.BotColorIdProtos.BotColorId;
import edu.dhbw.mannheim.tigers.sumatra.proto.BotColorIdProtos.BotColorId.Color;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass.NotCreateableException;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Polling Controller and send commands through ActionSender to bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PollingService
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger		log							= Logger.getLogger(PollingService.class.getName());
	private final Controller			controller;
	private final List<ExtComponent>	components;
	private final ActionSender			actionSender;
	private ScheduledExecutorService	execService;
	
	private static final int			PERIOD						= 20;
	private static final BotColorId	DUMMY_BOT_ID				= BotColorId.newBuilder().setBotId(0)
																						.setColor(Color.UNINITIALIZED).build();
	
	private Map<ExtComponent, Float>	lastPressedComponents	= new HashMap<ExtComponent, Float>();
	
	
	private GenericSkillSystem			skillSystem					= null;
	private Agent							agentYellow					= null;
	private Agent							agentBlue					= null;
	
	
	private long							timeSkillStarted			= SumatraClock.nanoTime();
	private long							timeLastInput				= SumatraClock.nanoTime();
	
	@Configurable(comment = "Timeout [s] after controller times out (bot gets unassigned)")
	private static int					controllerTimeout			= 180;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param config
	 * @param actionSender
	 */
	public PollingService(final RcmActionMap config, final ActionSender actionSender)
	{
		controller = config.getController();
		components = config.createComponents();
		this.actionSender = actionSender;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param components
	 */
	public void interpret(final List<ExtComponent> components)
	{
		BotActionCommand cmd = translate(components);
		actionSender.execute(cmd);
	}
	
	
	private boolean containsExtComponent(final ExtComponent extComp, final List<ExtComponent> dependentComponents)
	{
		boolean inDepComps = false;
		for (ExtComponent depExtComp : dependentComponents)
		{
			if (extComp.getBaseComponent().getIdentifier().getName()
					.equals(depExtComp.getBaseComponent().getIdentifier().getName()))
			{
				inDepComps = true;
				break;
			}
		}
		return inDepComps;
	}
	
	
	/**
	 * @param components
	 */
	private BotActionCommand translate(final List<ExtComponent> components)
	{
		BotActionCommand.Builder cmdBuilder = BotActionCommand.newBuilder();
		cmdBuilder.setBotId(DUMMY_BOT_ID);
		float forward = 0;
		float backward = 0;
		float left = 0;
		float right = 0;
		float rotateLeft = 0;
		float rotateRight = 0;
		
		ICommandInterpreter interpreter = actionSender.getCmdInterpreter();
		BotID botId = interpreter.getBot().getBotID();
		float deadzone = interpreter.getCompassThreshold();
		
		Map<ExtComponent, Float> pressedComponents = new HashMap<ExtComponent, Float>();
		List<ExtComponent> dependentComponents = new ArrayList<ExtComponent>();
		for (ExtComponent extComp : components)
		{
			float value = extComp.getPollData();
			float customDeadzone = 0;
			if (extComp.isAnalog())
			{
				customDeadzone = deadzone;
			}
			if (value > customDeadzone)
			{
				pressedComponents.put(extComp, value);
			}
			if (extComp.getBaseComponent().getPollData() > deadzone)
			{
				ExtComponent extCompDep = extComp.getDependentComp();
				while (extCompDep != null)
				{
					dependentComponents.add(extCompDep);
					extCompDep = extCompDep.getDependentComp();
				}
			}
		}
		
		Map<ExtComponent, Float> releasedComponents = new HashMap<ExtComponent, Float>(lastPressedComponents);
		pressedComponents.forEach((extComp, value) -> releasedComponents.remove(extComp));
		lastPressedComponents = pressedComponents;
		
		for (Map.Entry<ExtComponent, Float> entry : releasedComponents.entrySet())
		{
			ExtComponent extComp = entry.getKey();
			ExtComponent extCompDep = extComp.getDependentComp();
			while (extCompDep != null)
			{
				dependentComponents.add(extCompDep);
				extCompDep = extCompDep.getDependentComp();
			}
		}
		
		Map<ExtComponent, Float> components2BeProcessed = new HashMap<ExtComponent, Float>();
		for (Map.Entry<ExtComponent, Float> entry : releasedComponents.entrySet())
		{
			ExtComponent extComp = entry.getKey();
			Float value = entry.getValue();
			if (!containsExtComponent(extComp, dependentComponents))
			{
				components2BeProcessed.put(extComp, value);
			}
		}
		
		// process pressed components that are continues actions (forward,sideward,etc.)
		for (Map.Entry<ExtComponent, Float> entry : pressedComponents.entrySet())
		{
			ExtComponent extComp = entry.getKey();
			Float value = entry.getValue();
			if (!extComp.isContinuesAction())
			{
				continue;
			}
			if (!containsExtComponent(extComp, dependentComponents))
			{
				components2BeProcessed.put(extComp, value);
			}
		}
		
		if (!components2BeProcessed.isEmpty())
		{
			timeLastInput = SumatraClock.nanoTime();
		} else if ((SumatraClock.nanoTime() - timeLastInput) > (controllerTimeout * 1e9))
		{
			actionSender.notifyTimedout();
		}
		
		for (Map.Entry<ExtComponent, Float> entry : components2BeProcessed.entrySet())
		{
			ExtComponent extComp = entry.getKey();
			float value = entry.getValue();
			EActionType actionType = extComp.getMappedAction().getActionType();
			Enum<?> actionEnum = extComp.getMappedAction().getActionEnum();
			switch (actionType)
			{
				case EVENT:
					ERcmEvent event = (ERcmEvent) actionEnum;
					switch (event)
					{
						case SPEED_MODE_DISABLE:
							actionSender.getCmdInterpreter().setHighSpeedMode(false);
							break;
						case SPEED_MODE_ENABLE:
							actionSender.getCmdInterpreter().setHighSpeedMode(true);
							break;
						case SPEED_MODE_TOGGLE:
							actionSender.getCmdInterpreter().setHighSpeedMode(
									!actionSender.getCmdInterpreter().isHighSpeedMode());
							break;
						case NEXT_BOT:
							actionSender.notifyNextBot();
							break;
						case PREV_BOT:
							actionSender.notifyPrevBot();
							break;
						case UNASSIGN_BOT:
							actionSender.notifyBotUnassigned();
							break;
						case EMERGENCY_MODE:
							agentYellow.getAthena().changeMode(EAIControlState.EMERGENCY_MODE);
							agentBlue.getAthena().changeMode(EAIControlState.EMERGENCY_MODE);
							skillSystem.emergencyStop();
							break;
						case MATCH_MODE:
							agentYellow.getAthena().changeMode(EAIControlState.MATCH_MODE);
							agentBlue.getAthena().changeMode(EAIControlState.MATCH_MODE);
							break;
						case RECORD_START_STOP:
							RecordManager.toggleRecording(true);
							break;
						case CHARGE_BOT:
						{
							ABot bot = actionSender.getCmdInterpreter().getBot();
							bot.execute(new TigerKickerChargeAuto(bot.getKickerMaxCap()));
						}
							break;
						case DISCHARGE_BOT:
						{
							ABot bot = actionSender.getCmdInterpreter().getBot();
							bot.execute(new TigerKickerChargeAuto(0));
						}
							break;
						case UNASSIGNED:
							break;
					}
					break;
				case SIMPLE:
					if (botId.isBot() && (timeSkillStarted != 0) && ((SumatraClock.nanoTime() - timeSkillStarted) > 5e8))
					{
						skillSystem.reset(botId);
						timeSkillStarted = 0;
					}
					interpreter.setPaused(false);
					
					EControllerAction action = (EControllerAction) extComp.getMappedAction().getActionEnum();
					switch (action)
					{
						case CHIP_ARM:
							cmdBuilder.setChipArm(value);
							break;
						case CHIP_FORCE:
							cmdBuilder.setChipForce(value);
							break;
						case DISARM:
							cmdBuilder.setDisarm(true);
							break;
						case DRIBBLE:
							cmdBuilder.setDribble(value);
							break;
						case KICK_ARM:
							cmdBuilder.setKickArm(value);
							break;
						case KICK_FORCE:
							cmdBuilder.setKickForce(value);
							break;
						case BACKWARD:
							backward = value;
							break;
						case FORWARD:
							forward = value;
							break;
						case LEFT:
							left = value;
							break;
						case RIGHT:
							right = value;
							break;
						case ROTATE_LEFT:
							rotateLeft = value;
							break;
						case ROTATE_RIGHT:
							rotateRight = value;
							break;
						case UNDEFINED:
							break;
					}
					break;
				case SKILL:
					ESkillName skill = (ESkillName) extComp.getMappedAction().getActionEnum();
					final Agent agent;
					if (botId.getTeamColor() == ETeamColor.BLUE)
					{
						agent = agentBlue;
					} else
					{
						agent = agentYellow;
					}
					
					try
					{
						if (botId.isBot())
						{
							switch (skill)
							{
								case KICK:
									IVector2 target = agent.getLatestAiFrame().getTacticalField()
											.getBestDirectShootTarget();
									// target = new DynamicPosition(new TrackedTigerBot(BotID.createBotId(7, ETeamColor.YELLOW
									// ), AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, 0, 0, 0, 0, 0,
									// new DummyBot(), ETeamColor.YELLOW));
									skillSystem.execute(botId, new KickSkill(new DynamicPosition(target), EKickMode.MAX));
									break;
								default:
									skillSystem.execute(botId, (ISkill) skill.getInstanceableClass().newDefaultInstance());
									break;
							}
							interpreter.setPaused(true);
							timeSkillStarted = SumatraClock.nanoTime();
						}
					} catch (NotCreateableException err)
					{
						log.error("Could not create skill " + skill, err);
					} catch (Throwable err)
					{
						log.error("Could not create skill " + skill, err);
					}
					break;
			}
		}
		
		// forward - positive, backward - negative
		final float translateY = forward - backward;
		// right - positive, left - negative
		final float translateX = right - left;
		// rotateRight - positive, rotateLeft - negative
		final float rotate = rotateLeft - rotateRight;
		
		cmdBuilder.setTranslateX(translateX);
		cmdBuilder.setTranslateY(translateY);
		cmdBuilder.setRotate(rotate);
		
		return cmdBuilder.build();
	}
	
	
	/**
	 * 
	 */
	public void start()
	{
		if (execService == null)
		{
			try
			{
				skillSystem = (GenericSkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID);
			} catch (ModuleNotFoundException err)
			{
				log.error("Could not get skillSystem.");
			}
			
			try
			{
				agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
				agentBlue = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
			} catch (ModuleNotFoundException err)
			{
				log.error("Could not find agents");
			}
			
			execService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("PollingService_"
					+ controller.getName()));
			execService.scheduleAtFixedRate(new PollingThread(), 0, PERIOD, TimeUnit.MILLISECONDS);
			actionSender.startSending();
		} else
		{
			log.warn("start called more than once.");
		}
	}
	
	
	/**
	 * 
	 */
	public void stop()
	{
		if (execService != null)
		{
			actionSender.stopSending();
			execService.shutdown();
			execService = null;
			lastPressedComponents.clear();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	public ActionSender getActionSender()
	{
		return actionSender;
	}
	
	private class PollingThread implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				controller.poll();
				interpret(components);
			} catch (Throwable err)
			{
				log.error("Error in PollingThread.", err);
			}
		}
	}
}
