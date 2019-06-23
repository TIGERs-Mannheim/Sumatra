/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.IMatchCommand;
import edu.tigers.sumatra.botmanager.commands.MultimediaControl;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.statemachine.IStateMachine;
import edu.tigers.sumatra.statemachine.StateMachine;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * This is the base class for every skill, which provides subclasses with the newest data and handles their lifecycle
 *
 * @author Ryan, Gero
 */
public abstract class ASkill implements ISkill
{
	@SuppressWarnings("unused")
	private static final Logger			log					= Logger.getLogger(ASkill.class.getName());
	protected static final IState			IDLE_STATE			= new DefaultState();
	private final ESkill						skillName;
	private final MovementCon				moveCon				= new MovementCon();
	private final IStateMachine<IState>	stateMachine		= new StateMachine<>(IDLE_STATE);
	private long								lastUpdate			= 0;
	private double								dt						= 1;
	private double								minDt					= 0.008;
	private boolean							initialized			= false;
	private ABot								bot;
	private ShapeMap							exportedShapeMap	= new ShapeMap();
	private ShapeMap							shapeMap				= new ShapeMap();
	
	
	/**
	 * @param skill skillName
	 */
	protected ASkill(final ESkill skill)
	{
		skillName = skill;
		stateMachine.setExtendedLogging(SumatraModel.getInstance().isTestMode());
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
	public void update(final WorldFrameWrapper wfw, final ABot bot)
	{
		this.bot = bot;
	}
	
	
	@Override
	public void calcActions(final long timestamp)
	{
		dt = Math.abs(timestamp - lastUpdate) * 1e-9f;
		// skip update if we get too many frames
		if ((dt >= minDt) || (dt < 0))
		{
			lastUpdate = timestamp;
			doCalcActionsBeforeStateUpdate();
			stateMachine.update();
			doCalcActionsAfterStateUpdate();
			exportedShapeMap = shapeMap;
			shapeMap = new ShapeMap();
			bot.sendMatchCommand();
			initialized = true;
		}
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
		ConfigRegistration.applySpezis(this, "skills", bot.getType().name());
		setMinDt();
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
	final ABot getBot()
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
	public final boolean isInitialized()
	{
		return initialized;
	}
	
	
	@Override
	public final ShapeMap exportShapeMap()
	{
		return exportedShapeMap;
	}
	
	
	protected final ShapeMap getShapes()
	{
		return shapeMap;
	}
	
	
	/**
	 * @return dt since last update in [s]
	 */
	final double getDt()
	{
		return dt;
	}
	
	
	/**
	 * @param minDt the minDt to set
	 */
	final void setMinDt(final double minDt)
	{
		double min = 1f / getBot().getUpdateRate();
		this.minDt = Math.max(minDt, min);
	}
	
	
	/**
	 * Set the minimum possible minDt
	 */
	final void setMinDt()
	{
		setMinDt(0);
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
		
		aiInfo.setBallContact(bot.isBarrierInterrupted());
		aiInfo.setBattery(bot.getBatteryRelative());
		aiInfo.setKickerCharge(bot.getKickerLevel() / bot.getKickerLevelMax());
		Set<EFeature> brokenFeatures = bot.getBotFeatures().entrySet().stream()
				.filter(entry -> entry.getValue() == EFeatureState.KAPUT)
				.map(Map.Entry::getKey).collect(Collectors.toSet());
		aiInfo.setBrokenFeatures(brokenFeatures);
		
		aiInfo.setVelocityLimit(getMatchCtrl().getSkill().getMoveConstraints().getVelMax());
		aiInfo.setAccelerationLimit(getMatchCtrl().getSkill().getMoveConstraints().getAccMax());
		aiInfo.setDribblerSpeed(getMatchCtrl().getSkill().getDribbleSpeed());
		aiInfo.setKickerSpeed(getMatchCtrl().getSkill().getKickSpeed());
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
	private static class DefaultState implements IState
	{
	}
	
}
