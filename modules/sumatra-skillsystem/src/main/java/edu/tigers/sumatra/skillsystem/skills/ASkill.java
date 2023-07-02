/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.TigerBot;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.IMatchCommand;
import edu.tigers.sumatra.botmanager.botskills.data.MultimediaControl;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.statemachine.IStateMachine;
import edu.tigers.sumatra.statemachine.StateMachine;
import edu.tigers.sumatra.time.AverageTimeMeasure;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This is the base class for every skill.
 * In contains the core-features of skills, like state machine, time measurement, drawing, bot assignment.
 * It does not contain any positioning data from vision. It could be used for basic skills that do not
 * need vision and that only use custom bot skills. For all other purposes, use {@link AMoveSkill}.
 */
public abstract class ASkill implements ISkill
{
	protected static final IState IDLE_STATE = new DefaultState();

	private final IStateMachine<IState> stateMachine = new StateMachine<>(this.getClass().getSimpleName());
	@Getter
	private final AverageTimeMeasure averageTimeMeasure = new AverageTimeMeasure();

	@Getter
	@Setter(AccessLevel.PROTECTED)
	private ESkillState skillState = ESkillState.IN_PROGRESS;

	private boolean initialized = false;
	private ABot bot;
	private ShapeMap shapeMap = new ShapeMap();


	protected ASkill()
	{
		stateMachine.setInitialState(IDLE_STATE);
		averageTimeMeasure.setAveragingTime(0.5);
	}


	/**
	 * Add a transition.
	 *
	 * @param currentState the state for which the transition should be triggered, can be null for wildcard
	 * @param event        the event that triggers the transition
	 * @param nextState    the resulting state
	 */
	protected final void addTransition(final IState currentState, final IEvent event, final IState nextState)
	{
		stateMachine.addTransition(currentState, event, nextState);
	}


	/**
	 * Add a wildcard transition
	 *
	 * @param event     the event that triggers the transition
	 * @param nextState the resulting state
	 */
	protected final void addTransition(final IEvent event, final IState nextState)
	{
		stateMachine.addTransition(null, event, nextState);
	}


	/**
	 * Set the initial state to start with. This is mandatory for all roles!
	 *
	 * @param initialState first state to set
	 */
	protected final void setInitialState(final IState initialState)
	{
		stateMachine.setInitialState(initialState);
	}


	/**
	 * Go to next state
	 *
	 * @param event to be triggered
	 */
	protected final void triggerEvent(final IEvent event)
	{
		stateMachine.triggerEvent(event);
	}


	/**
	 * Go to specified state
	 *
	 * @param state to be switched to
	 */
	protected final void changeState(final IState state)
	{
		stateMachine.changeState(state);
	}


	protected final IState getCurrentState()
	{
		return stateMachine.getCurrentState();
	}


	/**
	 * @return the matchCtrl
	 */
	protected final IMatchCommand getMatchCtrl()
	{
		return bot.getMatchCtrl();
	}


	@Override
	public void update(final WorldFrameWrapper wfw, final ABot bot, final ShapeMap shapeMap)
	{
		this.bot = bot;
		this.shapeMap = shapeMap;
	}


	@Override
	public final void calcActions(final long timestamp)
	{
		averageTimeMeasure.resetMeasure();
		averageTimeMeasure.startMeasure();
		doCalcActionsBeforeStateUpdate();
		stateMachine.update();
		doCalcActionsAfterStateUpdate();
		initialized = true;
		averageTimeMeasure.stopMeasure();
	}


	@Override
	public final void calcExitActions()
	{
		stateMachine.stop();
		onSkillFinished();
	}


	@Override
	public final void calcEntryActions()
	{
		stateMachine.setName("Skill " + getClass().getSimpleName() + " " + getBotId());
		onSkillStarted();
	}


	protected void doCalcActionsBeforeStateUpdate()
	{
	}


	protected void doCalcActionsAfterStateUpdate()
	{
	}


	protected void onSkillStarted()
	{
	}


	protected void onSkillFinished()
	{
	}


	@Override
	public final String toString()
	{
		return getClass().getSimpleName();
	}


	/**
	 * @return the bot
	 */
	protected final ABot getBot()
	{
		return bot;
	}


	@Override
	public final BotID getBotId()
	{
		return bot.getBotId();
	}


	@Override
	public boolean isAssigned()
	{
		return bot != null;
	}


	@Override
	public final boolean isInitialized()
	{
		return initialized;
	}


	protected final ShapeMap getShapes()
	{
		return shapeMap;
	}


	/**
	 * @param control the mediaControl
	 */
	@Override
	public final void setMultimediaControl(final MultimediaControl control)
	{
		getMatchCtrl().setMultimediaControl(control);
	}


	@Override
	public BotAiInformation getBotAiInfo()
	{
		BotAiInformation aiInfo = new BotAiInformation();

		if (bot == null)
		{
			return aiInfo;
		}

		aiInfo.setBallContact(bot.isBarrierInterrupted() ? "BARRIER" : "NO");
		aiInfo.setBattery(bot.getBatteryRelative());
		aiInfo.setVersion(bot.getVersionString());
		aiInfo.setHwId(bot.getHardwareId());
		aiInfo.setLastFeedback(bot.getLastFeedback());
		aiInfo.setKickerCharge(bot.getKickerLevel());
		Set<EFeature> brokenFeatures = bot.getBotFeatures().entrySet().stream()
				.filter(entry -> entry.getValue() == EFeatureState.KAPUT)
				.map(Map.Entry::getKey).collect(Collectors.toSet());
		aiInfo.setBrokenFeatures(brokenFeatures);

		aiInfo.setVelocityLimit(getMatchCtrl().getSkill().getMoveConstraints().getVelMax());
		aiInfo.setAccelerationLimit(getMatchCtrl().getSkill().getMoveConstraints().getAccMaxDerived());
		aiInfo.setDribblerSpeed(getMatchCtrl().getSkill().getDribbleSpeed());
		aiInfo.setKickerSpeed(
				getMatchCtrl().getSkill().getMode() == EKickerMode.DISARM ? -1 : getMatchCtrl().getSkill().getKickSpeed());
		aiInfo.setKickerDevice(getMatchCtrl().getSkill().getDevice());
		aiInfo.setBotSkill(getMatchCtrl().getSkill().getType().name());
		aiInfo.setPrimaryDirection(getMatchCtrl().getSkill().getMoveConstraints().getPrimaryDirection());
		aiInfo.setSkill(getClass().getSimpleName());
		aiInfo.setSkillState(getCurrentState());
		aiInfo.setMaxProcTime(averageTimeMeasure.getMaxTime());
		aiInfo.setAvgProcTime(averageTimeMeasure.getAverageTime());
		aiInfo.setDribbleCurrent(bot.getDribblerCurrent());
		aiInfo.setDribbleCurrentMax(getMatchCtrl().getSkill().getKickerDribbler().getDribblerMaxCurrent());

		if (bot.getClass().equals(TigerBot.class))
		{
			TigerBot tigerBot = (TigerBot) bot;
			aiInfo.setBotStats(tigerBot.getStats());
		}
		return aiInfo;
	}


	/**
	 * Default idle state
	 */
	private static class DefaultState extends AState
	{
	}

	public Map<IEvent, Map<IState, IState>> getStateGraph()
	{
		return stateMachine.getTransitions();
	}
}
