/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.rcm;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;
import edu.tigers.sumatra.proto.BotColorIdProtos.BotColorId;
import edu.tigers.sumatra.proto.BotColorIdProtos.BotColorId.Color;
import edu.tigers.sumatra.rcm.RcmAction.EActionType;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.skillsystem.skills.BotSkillWrapperSkill;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.ManualControlSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import lombok.extern.log4j.Log4j2;
import net.java.games.input.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Polling Controller and send commands through ActionSender to bot
 */
@Log4j2
public class PollingService
{
	private final Controller controller;
	private final List<ExtComponent> components;
	private final ActionSender actionSender;
	private final RcmActionMap config;
	private ScheduledExecutorService execService;

	private static final int PERIOD = 20;
	private static final BotColorId DUMMY_BOT_ID = BotColorId.newBuilder().setBotId(0)
			.setColor(Color.UNINITIALIZED).build();

	private Map<ExtComponent, Double> lastPressedComponents = new HashMap<>();


	private GenericSkillSystem skillSystem = null;


	private long timeSkillStarted = System.nanoTime();
	private long timeLastInput = System.nanoTime();

	@Configurable(comment = "Timeout [s] after controller times out (bot gets unassigned)", defValue = "180")
	private static int controllerTimeout = 180;


	static
	{
		ConfigRegistration.registerClass("rcm", PollingService.class);
	}


	/**
	 * @param config
	 * @param actionSender
	 */
	public PollingService(final RcmActionMap config, final ActionSender actionSender)
	{
		this.config = config;
		controller = config.getController();
		components = config.createComponents();
		this.actionSender = actionSender;
	}


	/**
	 * @param components
	 */
	public void interpret(final List<ExtComponent> components)
	{
		BotID botId = actionSender.getCmdInterpreter().getBot().getBotId();
		BotActionCommand cmd = translate(components);
		ABotSkill skill = actionSender.execute(cmd);
		if (!botId.isBot())
		{
			return;
		}
		skillSystem.execute(
				botId,
				new BotSkillWrapperSkill(skill)
		);
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
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
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
		double deadZone = interpreter.getCompassThreshold();

		Map<ExtComponent, Double> pressedComponents = new HashMap<>();
		List<ExtComponent> dependentComponents = new ArrayList<>();
		for (ExtComponent extComp : components)
		{
			double value = extComp.getPollData();
			double customDeadZone = 0;
			if (extComp.isAnalog())
			{
				customDeadZone = deadZone;
			}
			if (value > customDeadZone)
			{
				pressedComponents.put(extComp, value);
			}
			if (extComp.getBaseComponent().getPollData() > deadZone)
			{
				ExtComponent extCompDep = extComp.getDependentComp();
				while (extCompDep != null)
				{
					dependentComponents.add(extCompDep);
					extCompDep = extCompDep.getDependentComp();
				}
			}
		}

		Map<ExtComponent, Double> releasedComponents = new HashMap<>(lastPressedComponents);
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

		Map<ExtComponent, Double> components2BeProcessed = new HashMap<>();
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
			actionSender.notifyTimedOut();
		}

		for (Map.Entry<ExtComponent, Double> entry : components2BeProcessed.entrySet())
		{
			ExtComponent extComp = entry.getKey();
			float value = entry.getValue().floatValue();
			EActionType actionType = extComp.getMappedAction().getActionType();
			Enum<?> actionEnum = extComp.getMappedAction().getActionEnum();
			if (actionType == EActionType.EVENT)
			{
				handleEvent(botId, (ERcmEvent) actionEnum);
				ERcmEvent action = (ERcmEvent) extComp.getMappedAction().getActionEnum();
				boolean stopbot =
						action == ERcmEvent.NEXT_BOT || action == ERcmEvent.PREV_BOT || action == ERcmEvent.UNASSIGNED;

				if (stopbot)
				{
					forward = 0;
					backward = 0;
					left = 0;
					right = 0;
					rotateLeft = 0;
					rotateRight = 0;
					accelerate = 0;
					decelerate = 0;
				}
			} else if (actionType == EActionType.SKILL)
			{
				handleSkill(interpreter, botId, extComp);
			} else if (actionType == EActionType.SIMPLE)
			{
				if (botId.isBot() && (timeSkillStarted != 0) && ((System.nanoTime() - timeSkillStarted) > 5e8))
				{
					skillSystem.execute(botId, new ManualControlSkill());
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


	private void handleEvent(final BotID botId, final ERcmEvent actionEnum)
	{
		ERcmEvent event = actionEnum;
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
				chooseNextBot(botId);
				break;
			case PREV_BOT:
				choosePrevBot(botId);
				break;
			case UNASSIGN_BOT:
				unassignBot(botId);
				break;
			case UNASSIGNED:
				break;
		}
	}


	private void chooseNextBot(final BotID botId)
	{
		if (botId.isBot())
		{
			skillSystem.execute(botId, new IdleSkill());
			timeSkillStarted = System.nanoTime();
		}
		actionSender.notifyNextBot();
	}


	private void choosePrevBot(final BotID botId)
	{
		if (botId.isBot())
		{
			skillSystem.execute(botId, new IdleSkill());
			timeSkillStarted = System.nanoTime();
		}
		actionSender.notifyPrevBot();
	}


	private void unassignBot(final BotID botId)
	{
		if (botId.isBot())
		{
			skillSystem.execute(botId, new IdleSkill());
			timeSkillStarted = System.nanoTime();
		}
		actionSender.notifyBotUnassigned();
	}


	private void handleSkill(final ICommandInterpreter interpreter, final BotID botId, final ExtComponent extComp)
	{
		ESkill skill = (ESkill) extComp.getMappedAction().getActionEnum();

		try
		{
			if (botId.isBot())
			{
				var target = Geometry.getGoalTheir().getCenter();

				if (skill == ESkill.TOUCH_KICK)
				{
					skillSystem.execute(botId, new TouchKickSkill(target, KickParams.maxStraight()));
				} else
				{
					skillSystem.execute(botId, (ISkill) skill.getInstanceableClass().newDefaultInstance());
				}
				interpreter.setPaused(true);
				timeSkillStarted = System.nanoTime();
			}
		} catch (Throwable err)
		{
			log.error("Could not create skill " + skill, err);
		}
	}


	/**
	 * Start service
	 */
	public void start()
	{
		if (execService == null)
		{
			try
			{
				skillSystem = (GenericSkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.class);
			} catch (ModuleNotFoundException err)
			{
				log.error("Could not get skillSystem.", err);
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
	 * Stop service
	 */
	public void stop()
	{
		if (execService != null)
		{
			skillSystem.emergencyStop();
			execService.shutdown();
			execService = null;
			lastPressedComponents.clear();
			timeSkillStarted = System.nanoTime();
		}
	}


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
			if (!config.isEnabled())
			{
				return;
			}
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
