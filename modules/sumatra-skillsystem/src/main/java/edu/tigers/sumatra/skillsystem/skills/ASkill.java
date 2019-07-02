/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.IMatchCommand;
import edu.tigers.sumatra.botmanager.botskills.data.MultimediaControl;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.statemachine.IStateMachine;
import edu.tigers.sumatra.statemachine.StateMachine;
import edu.tigers.sumatra.time.AverageTimeMeasure;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * This is the base class for every skill, which provides subclasses with the newest data and handles their lifecycle
 *
 * @author Ryan, Gero
 */
public abstract class ASkill implements ISkill
{
	protected static final IState IDLE_STATE = new DefaultState();
	private final ESkill skillName;
	private final MovementCon moveCon = new MovementCon();
	private final IStateMachine<IState> stateMachine = new StateMachine<>(IDLE_STATE);
	private boolean initialized = false;
	private ABot bot;
	private ShapeMap shapeMap = new ShapeMap();
	private AverageTimeMeasure averageTimeMeasure = new AverageTimeMeasure();
	
	
	/**
	 * @param skill skillName
	 */
	protected ASkill(final ESkill skill)
	{
		skillName = skill;
		stateMachine.setExtendedLogging(SumatraModel.getInstance().isTestMode());
		averageTimeMeasure.setAveragingTime(0.5);
	}
	
	
	/**
	 * Add a transition.
	 *
	 * @param currentState the state for which the transition should be triggered, can be null for wildcard
	 * @param event the event that triggers the transition
	 * @param nextState the resulting state
	 */
	protected final void addTransition(final IState currentState, final IEvent event, final IState nextState)
	{
		stateMachine.addTransition(currentState, event, nextState);
	}
	
	
	/**
	 * Add a wildcard transition
	 *
	 * @param event the event that triggers the transition
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
	
	
	@Override
	public final IState getCurrentState()
	{
		return stateMachine.getCurrentState();
	}
	
	
	@Override
	public final ESkill getType()
	{
		return skillName;
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
	public void calcActions(final long timestamp)
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
		getBot().setCurrentTrajectory(null);
	}
	
	
	@Override
	public void calcEntryActions()
	{
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
		return getType().toString();
	}
	
	
	/**
	 * @return the bot
	 */
	protected final ABot getBot()
	{
		return bot;
	}
	
	
	@Override
	public final MovementCon getMoveCon()
	{
		return moveCon;
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
	
	
	@Override
	public AverageTimeMeasure getAverageTimeMeasure()
	{
		return averageTimeMeasure;
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
		aiInfo.setKickerCharge(bot.getKickerLevel() / bot.getKickerLevelMax());
		Set<EFeature> brokenFeatures = bot.getBotFeatures().entrySet().stream()
				.filter(entry -> entry.getValue() == EFeatureState.KAPUT)
				.map(Map.Entry::getKey).collect(Collectors.toSet());
		aiInfo.setBrokenFeatures(brokenFeatures);
		
		aiInfo.setVelocityLimit(getMatchCtrl().getSkill().getMoveConstraints().getVelMax());
		aiInfo.setAccelerationLimit(getMatchCtrl().getSkill().getMoveConstraints().getAccMax());
		aiInfo.setDribblerSpeed(getMatchCtrl().getSkill().getDribbleSpeed());
		aiInfo.setKickerSpeed(
				getMatchCtrl().getSkill().getMode() == EKickerMode.DISARM ? -1 : getMatchCtrl().getSkill().getKickSpeed());
		aiInfo.setKickerDevice(getMatchCtrl().getSkill().getDevice());
		aiInfo.setBotSkill(getMatchCtrl().getSkill().getType().name());
		aiInfo.setSkill(getType());
		IState skillState = getCurrentState();
		aiInfo.setSkillState(skillState);
		return aiInfo;
	}
	
	/**
	 * Default idle state
	 *
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	private static class DefaultState extends AState
	{
	}
	
}
