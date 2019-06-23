/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.rcm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.instanceables.InstanceableClass.NotCreateableException;

import edu.dhbw.mannheim.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.proto.BotColorIdProtos.BotColorId;
import edu.dhbw.mannheim.tigers.sumatra.proto.BotColorIdProtos.BotColorId.Color;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistance.RecordManager;
import edu.tigers.sumatra.rcm.RcmAction.EActionType;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.skillsystem.VisionSkillWatcher;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill;
import edu.tigers.sumatra.skillsystem.skills.KickTestSkill;
import edu.tigers.sumatra.skillsystem.skills.RedirectSkill;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.wp.VisionWatcher;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;
import net.java.games.input.Controller;


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
	
	private static final Logger			log							= Logger.getLogger(PollingService.class.getName());
	private final Controller				controller;
	private final List<ExtComponent>		components;
	private final ActionSender				actionSender;
	private ScheduledExecutorService		execService;
	
	private static final int				PERIOD						= 20;
	private static final BotColorId		DUMMY_BOT_ID				= BotColorId.newBuilder().setBotId(0)
			.setColor(Color.UNINITIALIZED).build();
	
	private Map<ExtComponent, Double>	lastPressedComponents	= new HashMap<ExtComponent, Double>();
	
	
	private GenericSkillSystem				skillSystem					= null;
	private Agent								agentYellow					= null;
	private Agent								agentBlue					= null;
	private VisionWatcher					ballWatcher					= null;
	
	
	private long								timeSkillStarted			= System.nanoTime();
	private long								timeLastInput				= System.nanoTime();
	
	@Configurable(comment = "Timeout [s] after controller times out (bot gets unassigned)")
	private static int						controllerTimeout			= 180;
	
	
	static
	{
		ConfigRegistration.registerClass("rcm", PollingService.class);
	}
	
	
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
		double forward = 0;
		double backward = 0;
		double left = 0;
		double right = 0;
		double rotateLeft = 0;
		double rotateRight = 0;
		double accelerate = 0;
		double decelerate = 0;
		
		ICommandInterpreter interpreter = actionSender.getCmdInterpreter();
		BotID botId = interpreter.getBot().getBotId();
		double deadzone = interpreter.getCompassThreshold();
		
		Map<ExtComponent, Double> pressedComponents = new HashMap<ExtComponent, Double>();
		List<ExtComponent> dependentComponents = new ArrayList<ExtComponent>();
		for (ExtComponent extComp : components)
		{
			double value = extComp.getPollData();
			double customDeadzone = 0;
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
		
		Map<ExtComponent, Double> releasedComponents = new HashMap<ExtComponent, Double>(lastPressedComponents);
		pressedComponents.forEach((extComp, value) -> releasedComponents.remove(extComp));
		lastPressedComponents = pressedComponents;
		
		for (Map.Entry<ExtComponent, Double> entry : releasedComponents.entrySet())
		{
			ExtComponent extComp = entry.getKey();
			ExtComponent extCompDep = extComp.getDependentComp();
			while (extCompDep != null)
			{
				dependentComponents.add(extCompDep);
				extCompDep = extCompDep.getDependentComp();
			}
		}
		
		Map<ExtComponent, Double> components2BeProcessed = new HashMap<ExtComponent, Double>();
		for (Map.Entry<ExtComponent, Double> entry : releasedComponents.entrySet())
		{
			ExtComponent extComp = entry.getKey();
			Double value = entry.getValue();
			if (!containsExtComponent(extComp, dependentComponents))
			{
				components2BeProcessed.put(extComp, value);
			}
		}
		
		// process pressed components that are continues actions (forward,sideward,etc.)
		for (Map.Entry<ExtComponent, Double> entry : pressedComponents.entrySet())
		{
			ExtComponent extComp = entry.getKey();
			Double value = entry.getValue();
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
			timeLastInput = System.nanoTime();
		} else if ((System.nanoTime() - timeLastInput) > (controllerTimeout * 1e9))
		{
			actionSender.notifyTimedout();
		}
		
		for (Map.Entry<ExtComponent, Double> entry : components2BeProcessed.entrySet())
		{
			ExtComponent extComp = entry.getKey();
			float value = entry.getValue().floatValue();
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
							if (botId.isBot())
							{
								skillSystem.execute(botId, new IdleSkill());
							}
							actionSender.notifyNextBot();
							break;
						case PREV_BOT:
							if (botId.isBot())
							{
								skillSystem.execute(botId, new IdleSkill());
							}
							actionSender.notifyPrevBot();
							break;
						case UNASSIGN_BOT:
							if (botId.isBot())
							{
								skillSystem.execute(botId, new IdleSkill());
							}
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
							RecordManager rm;
							try
							{
								rm = (RecordManager) SumatraModel.getInstance().getModule(RecordManager.MODULE_ID);
								rm.toggleRecording(true);
							} catch (ModuleNotFoundException e)
							{
								log.error("Could not find record manager", e);
							}
							break;
						case CHARGE_BOT:
						{
							ABot bot = actionSender.getCmdInterpreter().getBot();
							bot.getMatchCtrl().setKickerAutocharge(true);
						}
							break;
						case DISCHARGE_BOT:
						{
							ABot bot = actionSender.getCmdInterpreter().getBot();
							bot.getMatchCtrl().setKickerAutocharge(false);
						}
							break;
						case UNASSIGNED:
							break;
						case RECORD_VISION_START:
						{
							if (ballWatcher != null)
							{
								log.warn("Last ball watcher was not removed?!");
								ballWatcher.stopExport();
							}
							log.info("Start recording.");
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
							String fileName = "rcm/" + sdf.format(new Date());
							ballWatcher = new VisionSkillWatcher(fileName);
							ballWatcher.setStopAutomatically(false);
							boolean started = ballWatcher.start();
							if (!started)
							{
								ballWatcher = null;
							}
						}
							break;
						case RECORD_VISION_STOP:
							if (ballWatcher != null)
							{
								ballWatcher.stopExport();
								log.info("Stopped recording. dataSize: " + ballWatcher.getDataSize());
								ballWatcher = null;
							}
							break;
					}
					break;
				case SIMPLE:
					if (botId.isBot() && (timeSkillStarted != 0) && ((System.nanoTime() - timeSkillStarted) > 5e8))
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
						case ACCELERATE:
							accelerate = value;
							break;
						case DECELERATE:
							decelerate = value;
							break;
						case UNDEFINED:
							break;
					}
					break;
				case SKILL:
					ESkill skill = (ESkill) extComp.getMappedAction().getActionEnum();
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
							DynamicPosition target;
							if (agent.isActive() && (agent.getLatestAiFrame() != null))
							{
								target = new DynamicPosition(agent.getLatestAiFrame().getTacticalField()
										.getBestDirectShootTarget());
							} else
							{
								target = new DynamicPosition(new Vector2(Geometry.getFieldLength() / 2, 0));
							}
							// TODO find a pass receiver
							// target = new DynamicPosition(new TrackedTigerBot(BotID.createBotId(7, ETeamColor.YELLOW
							// ), AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, 0, 0, 0, 0, 0,
							// new DummyBot(), ETeamColor.YELLOW));
							
							switch (skill)
							{
								case KICK:
									// target = new DynamicPosition(BotID.createBotId(3, ETeamColor.BLUE));
									// skillSystem.execute(botId, new KickSkill(target, EKickMode.PASS));
									skillSystem.execute(botId, new KickSkill(target));
									break;
								case KICK_TEST:
									IVector2 ballPos = agent.getLatestAiFrame().getWorldFrame().getBall().getPos();
									double speed = (new Random(System.currentTimeMillis()).nextInt(4000) + 2000) / 1000.0;
									Vector2 dest = new Vector2(Geometry.getFieldLength() / 2, Geometry.getFieldWidth() / 2);
									if (ballPos.x() > 0)
									{
										dest.setX(-dest.x());
									}
									if (ballPos.y() > 0)
									{
										dest.setY(-dest.y());
									}
									skillSystem.execute(botId,
											new KickTestSkill(new DynamicPosition(dest), speed));
									break;
								case REDIRECT:
									skillSystem.execute(botId, new RedirectSkill(target));
								default:
									skillSystem.execute(botId, (ISkill) skill.getInstanceableClass().newDefaultInstance());
									break;
							}
							interpreter.setPaused(true);
							timeSkillStarted = System.nanoTime();
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
		final double translateY = forward - backward;
		// right - positive, left - negative
		final double translateX = right - left;
		// rotateRight - positive, rotateLeft - negative
		final double rotate = rotateLeft - rotateRight;
		
		cmdBuilder.setTranslateX((float) translateX);
		cmdBuilder.setTranslateY((float) translateY);
		cmdBuilder.setRotate((float) rotate);
		cmdBuilder.setAccelerate((float) accelerate);
		cmdBuilder.setDecelerate((float) decelerate);
		
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
