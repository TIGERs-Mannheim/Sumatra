/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s):
 * Andre Ryll,
 * Gero
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.LedControl;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.IMatchCommand;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.MovementCon;
import edu.tigers.sumatra.skillsystem.driver.IPathDriver;
import edu.tigers.sumatra.statemachine.EventStatePair;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.statemachine.IStateMachine;
import edu.tigers.sumatra.statemachine.StateMachine;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ShapeMap;
import edu.tigers.sumatra.wp.data.TrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * This is the base class for every skill, which provides subclasses with the newest data and handles their lifecycle
 * 
 * @author Ryan, Gero
 */
public abstract class ASkill implements ISkill
{
	@SuppressWarnings("unused")
	private static final Logger			log				= Logger.getLogger(ASkill.class.getName());
	
	private final ESkill						skillName;
	private final MovementCon				moveCon			= new MovementCon();
	
	private boolean							initialized		= false;
	private WorldFrame						worldFrame		= null;
	private RefereeMsg						refereeMsg		= null;
	private EGameStateTeam					gameState		= EGameStateTeam.UNKNOWN;
	private ITrackedBot						tBot				= null;
	private ABot								bot				= null;
	
	private long								lastUpdate		= 0;
	private double								dt					= 1;
	private double								minDt				= 0.008;
	
	private final IStateMachine<IState>	stateMachine	= new StateMachine<IState>(
			new DefaultIdleState());
	
	private LedControl						ledControl		= new LedControl();
	
	
	/**
	 * @param skill skillName
	 */
	protected ASkill(final ESkill skill)
	{
		skillName = skill;
	}
	
	
	/**
	 * Add a transition
	 * 
	 * @param fromState current state
	 * @param event event that occurred
	 * @param toState the next state
	 */
	protected final void addTransition(final Enum<?> fromState, final Enum<?> event, final IState toState)
	{
		stateMachine.addTransition(new EventStatePair(event, fromState), toState);
	}
	
	
	/**
	 * Add a wildcard transition
	 * 
	 * @param event event that occurred
	 * @param state the next state
	 */
	protected final void addTransition(final Enum<?> event, final IState state)
	{
		stateMachine.addTransition(new EventStatePair(event), state);
	}
	
	
	/**
	 * Add a transition
	 * 
	 * @param fromState
	 * @param event event that occurred
	 * @param toState the next state
	 */
	protected final void addTransition(final IState fromState, final Enum<?> event, final IState toState)
	{
		stateMachine.addTransition(new EventStatePair(event, fromState), toState);
	}
	
	
	/**
	 * Set the initial state to start with. This is mandatory for all roles!
	 * 
	 * @param initialState
	 */
	protected final void setInitialState(final IState initialState)
	{
		stateMachine.setInitialState(initialState);
	}
	
	
	/**
	 * Go to next state
	 * 
	 * @param event
	 */
	protected final void triggerEvent(final Enum<? extends Enum<?>> event)
	{
		if (event != null)
		{
			stateMachine.triggerEvent(event);
		}
	}
	
	
	/**
	 * @return
	 */
	@Override
	public final IState getCurrentState()
	{
		return stateMachine.getCurrentStateId();
	}
	
	
	/**
	 * @return
	 */
	@Override
	public final ESkill getType()
	{
		return skillName;
	}
	
	
	/**
	 * @return
	 */
	protected final WorldFrame getWorldFrame()
	{
		return worldFrame;
	}
	
	
	/**
	 * @return the refereeMsg
	 */
	protected RefereeMsg getRefereeMsg()
	{
		return refereeMsg;
	}
	
	
	/**
	 * @return the gameState
	 */
	protected EGameStateTeam getGameState()
	{
		return gameState;
	}
	
	
	/**
	 * @return the matchCtrl
	 */
	protected final IMatchCommand getMatchCtrl()
	{
		return bot.getMatchCtrl();
	}
	
	
	/**
	 * @param wfw
	 */
	@Override
	public final void update(final WorldFrameWrapper wfw, final ABot bot)
	{
		assert wfw != null;
		worldFrame = wfw.getWorldFrame(bot.getColor());
		refereeMsg = wfw.getRefereeMsg();
		gameState = EGameStateTeam.fromNeutral(wfw.getGameState(), bot.getColor());
		assert worldFrame != null;
		this.bot = bot;
		ITrackedBot newTbot = worldFrame.getBot(bot.getBotId());
		if (newTbot != null)
		{
			tBot = newTbot;
		} else if (tBot == null)
		{
			tBot = new TrackedBot(worldFrame.getTimestamp(), bot.getBotId());
		}
		assert tBot != null;
	}
	
	
	@Override
	public final void setBotId(final BotID botId)
	{
		tBot = new TrackedBot(0, botId);
	}
	
	
	/**
	 * @return the pathDriver
	 */
	@Override
	public IPathDriver getPathDriver()
	{
		return null;
	}
	
	
	/**
	 */
	@Override
	public final void calcActions()
	{
		dt = (getWorldFrame().getTimestamp() - lastUpdate) * 1e-9f;
		// skip update if we get too many frames
		if ((dt >= minDt) || (dt < 0))
		{
			if (dt < 0)
			{
				log.warn("Negative dt: " + dt + "s");
			}
			
			lastUpdate = getWorldFrame().getTimestamp();
			doCalcActionsBeforeStateUpdate();
			stateMachine.update();
			doCalcActionsAfterStateUpdate();
			getMatchCtrl().setLEDs(ledControl.isLeftRed(), ledControl.isLeftGreen(), ledControl.isRightRed(),
					ledControl.isRightGreen());
			getMatchCtrl().setSongFinalCountdown(ledControl.isInsane());
			bot.sendMatchCommand();
			initialized = true;
		}
		
		
	}
	
	
	@Override
	public final void calcExitActions()
	{
		stateMachine.stop();
		getMatchCtrl().setKick(0, EKickerDevice.STRAIGHT, EKickerMode.DISARM);
		getMatchCtrl().setDribblerSpeed(0);
		onSkillFinished();
	}
	
	
	@Override
	public final void calcEntryActions()
	{
		ConfigRegistration.applySpezis(this, "skills", getBotType().name());
		setMinDt(1f / bot.getUpdateRate());
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
	
	
	protected final IVector2 getPos()
	{
		return tBot.getPos();
	}
	
	
	protected final double getAngle()
	{
		return tBot.getAngle();
	}
	
	
	protected final IVector2 getVel()
	{
		return tBot.getVel();
	}
	
	
	protected final double getaVel()
	{
		return tBot.getaVel();
	}
	
	
	protected final EBotType getBotType()
	{
		return tBot.getBot().getType();
	}
	
	
	protected final boolean hasBallContact()
	{
		return tBot.hasBallContact();
	}
	
	
	@Override
	public String toString()
	{
		return getType().toString();
	}
	
	
	/**
	 * Careful, may be null if no vision
	 * 
	 * @return
	 */
	protected final ITrackedBot getTBot()
	{
		return tBot;
	}
	
	
	/**
	 * @return
	 */
	protected ABot getBot()
	{
		return bot;
	}
	
	
	/**
	 * @return dt since last update in [s]
	 */
	protected final double getDt()
	{
		return dt;
	}
	
	
	/**
	 * @param minDt the minDt to set
	 */
	protected final void setMinDt(final double minDt)
	{
		this.minDt = minDt;
	}
	
	
	/**
	 * @return the moveCon
	 */
	@Override
	public final MovementCon getMoveCon()
	{
		return moveCon;
	}
	
	
	@Override
	public final BotID getBotId()
	{
		return tBot.getBotId();
	}
	
	
	/**
	 * @return the initialized
	 */
	@Override
	public final boolean isInitialized()
	{
		return initialized;
	}
	
	
	@Override
	public ShapeMap getShapes()
	{
		return new ShapeMap();
	}
	
	
	/**
	 * @return the ledControl
	 */
	@Override
	public LedControl getLedControl()
	{
		return ledControl;
	}
	
	
	/**
	 * @param ledControl the ledControl to set
	 */
	@Override
	public void setLedControl(final LedControl ledControl)
	{
		this.ledControl = ledControl;
	}
}
