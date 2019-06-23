/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.11.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.NoObjectWithThisIDException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EMovingSpeed;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IdleSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.DoneState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.EventStatePair;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IStateMachine;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.StateMachine;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillEventObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillSystemObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;


/**
 * Abstract type for roles, all roles inherit from here
 * Already includes some generic implementations for:
 * <ul>
 * <li>list for {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill}s</li>
 * </ul>
 * <p>
 * 
 * @author Gero
 * @author DanielW
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ARole implements ISkillSystemObserver, ISkillEventObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final Logger								log				= Logger.getLogger(ARole.class.getName());
	
	private static final IRoleState							DONE_STATE		= new DoneState();
	
	private ERole													type;
	
	/** uninitialized BotID */
	private BotID													botID				= BotID.createBotId();
	
	private boolean												firstUpdate		= true;
	
	/** This flag is used to tell the play which uses this role whether it is completed. */
	private boolean												isCompleted		= false;
	
	/** bot type of the bot associated to this role. Will be set before first update */
	private EBotType												botType			= EBotType.UNKNOWN;
	
	private transient AthenaAiFrame							aiFrame			= null;
	
	private transient ISkill									currentSkill	= null;
	private transient boolean									newSkill			= true;
	private final transient IStateMachine<IRoleState>	stateMachine;
	
	private IVector2												lastPos;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param type
	 */
	public ARole(final ERole type)
	{
		this.type = type;
		stateMachine = new StateMachine<IRoleState>();
	}
	
	
	// --------------------------------------------------------------------------
	// --- state machine --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Add a transition
	 * 
	 * @param stateId current state
	 * @param event event that occurred
	 * @param state the next state
	 */
	public final void addTransition(final Enum<?> stateId, final Enum<?> event, final IRoleState state)
	{
		stateMachine.addTransition(new EventStatePair(event, stateId), state);
	}
	
	
	/**
	 * Add a wildcard transition
	 * 
	 * @param event event that occurred
	 * @param state the next state
	 */
	public final void addTransition(final Enum<?> event, final IRoleState state)
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
	public final void addTransition(final IRoleState fromState, final Enum<?> event, final IRoleState toState)
	{
		stateMachine.addTransition(new EventStatePair(event, fromState), toState);
	}
	
	
	/**
	 * Adds an End Transition.
	 * 
	 * @param stateId current state
	 * @param event event that have to occurs, that the statemachine ends
	 */
	public final void addEndTransition(final Enum<?> stateId, final Enum<?> event)
	{
		addTransition(stateId, event, DONE_STATE);
	}
	
	
	/**
	 * Adds a wildcard End Transition.
	 * 
	 * @param event event that have to occurs, that the statemachine ends
	 */
	public final void addEndTransition(final Enum<?> event)
	{
		addTransition(event, DONE_STATE);
	}
	
	
	/**
	 * Adds an End Transition.
	 * 
	 * @param state current state
	 * @param event event that have to occurs, that the statemachine ends
	 */
	public final void addEndTransition(final IRoleState state, final Enum<?> event)
	{
		addTransition(state, event, DONE_STATE);
	}
	
	
	/**
	 * Set the initial state to start with. This is mandatory for all roles!
	 * 
	 * @param initialState
	 */
	public final void setInitialState(final IRoleState initialState)
	{
		stateMachine.setInitialState(initialState);
	}
	
	
	/**
	 * Go to next state
	 * 
	 * @param event
	 */
	public final void triggerEvent(final Enum<? extends Enum<?>> event)
	{
		if (event != null)
		{
			stateMachine.triggerEvent(event);
		}
	}
	
	
	/**
	 * Check if everything is fine for this role
	 * 
	 * @return
	 */
	public final boolean doSelfCheck()
	{
		return stateMachine.valid();
	}
	
	
	/**
	 * Identifier of the current state
	 * 
	 * @return
	 */
	public final Enum<?> getCurrentState()
	{
		if (stateMachine.getCurrentState() == null)
		{
			return DoneState.EStateId.DONE;
		}
		return stateMachine.getCurrentState().getIdentifier();
	}
	
	
	// --------------------------------------------------------------------------
	// --- interface ------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Needed for Role assigning. If your role is a keeper
	 * override this method and return true.
	 * 
	 * @return keeper?
	 * @author Malte
	 */
	public boolean isKeeper()
	{
		return false;
	}
	
	
	/**
	 * fill the passed list with all {@link EFeature} that you role needs. {@link EFeature#MOVE} is already set for you.
	 * If you do not even want to move,
	 * remove it again.
	 * 
	 * @param features
	 */
	public abstract void fillNeededFeatures(List<EFeature> features);
	
	
	@Override
	public final void onSkillStarted(final ISkill skill, final BotID botID)
	{
		IRoleState state = stateMachine.getCurrentState();
		if (state != null)
		{
			state.onSkillStarted(skill, botID);
		}
	}
	
	
	@Override
	public final void onSkillCompleted(final ISkill skill, final BotID botID)
	{
		IRoleState state = stateMachine.getCurrentState();
		if ((state != null) && botID.equals(getBotID()))
		{
			state.onSkillCompleted(skill, botID);
		}
	}
	
	
	/**
	 * Used by {@link APlay} to remove skill observer on role switch
	 */
	public final void removeSkillObserver()
	{
		try
		{
			((ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID)).removeObserver(this);
		} catch (final ModuleNotFoundException err)
		{
			log.fatal("ModuleNotFoundException", err);
		}
	}
	
	
	@Override
	public final void onNewEvent(final Enum<? extends Enum<?>> event)
	{
		triggerEvent(event);
	}
	
	
	/**
	 * Used by {@link APlay} to put skill observer on role switch
	 */
	public final void putSkillObserver()
	{
		// register as a skill-system observer to observe skill-complete events
		try
		{
			((ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID)).addObserver(this);
		} catch (final ModuleNotFoundException err)
		{
			log.fatal("ModuleNotFoundException", err);
		}
	}
	
	
	/**
	 * This method should only be called from the RoleAssigner!!!
	 * 
	 * @param newBotId
	 * @param frame
	 */
	public final void assignBotID(final BotID newBotId, final MetisAiFrame frame)
	{
		if (newBotId.isUninitializedID())
		{
			throw new IllegalArgumentException("Someone tries to initialize role '" + this
					+ "' with an UNINITIALIZED BotID!!!");
		}
		
		BotID oldBotId = botID;
		botID = newBotId;
		if (!oldBotId.isUninitializedID())
		{
			if (currentSkill != null)
			{
				stateMachine.restart();
			}
		}
	}
	
	
	/**
	 * @param curFrame
	 */
	public final void updateBefore(final AthenaAiFrame curFrame)
	{
		aiFrame = curFrame;
		lastPos = getPos();
	}
	
	
	/**
	 * @param curFrame
	 */
	public final void update(final AthenaAiFrame curFrame)
	{
		updateBefore(curFrame);
		
		if (curFrame.getWorldFrame().tigerBotsAvailable.getWithNull(botID) == null)
		{
			// bot is not there atm. update would result in exceptions...
			// in match mode, this should not happen, because the playfinder will
			// find new plays immediately after one bot was lost
			return;
		}
		
		try
		{
			if (firstUpdate)
			{
				if (hasBeenAssigned())
				{
					botType = curFrame.getWorldFrame().tigerBotsVisible.get(botID).getBotType();
					beforeFirstUpdate();
					firstUpdate = false;
				} else
				{
					log.error(this + ": Update was called although role has not been assigned!");
					return;
				}
			}
			
			if (isCompleted())
			{
				return;
			}
			
			beforeUpdate();
			
			if (stateMachine.getCurrentState() == null)
			{
				setCompleted();
			} else
			{
				stateMachine.update();
			}
			
			if (currentSkill != null)
			{
				// we will cast to AMoveSkill here. It is not ensured that all skills are based on AMoveSkill,
				// but for skills used in roles, they actually should be!
				reduceSpeedOnStop();
				if (isKeeper())
				{
					AMoveSkill moveSkill = (AMoveSkill) currentSkill;
					moveSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
				}
			}
			
			afterUpdate();
		} catch (Exception err)
		{
			log.error("Exception on role update (" + getType() + ")", err);
		}
	}
	
	
	private void reduceSpeedOnStop()
	{
		if (aiFrame.getTacticalField().getGameState() == EGameState.STOPPED)
		{
			AMoveSkill moveSkill = (AMoveSkill) currentSkill;
			float maxSpeed = AIConfig.getGeometry().getStopSpeed();
			if (moveSkill.getMoveCon().getSpeed() > 1e-4f)
			{
				maxSpeed = Math.min(moveSkill.getMoveCon().getSpeed(), maxSpeed);
			}
			moveSkill.getMoveCon().setSpeed(EMovingSpeed.SLOW, maxSpeed);
		}
	}
	
	
	/**
	 * This method is called after the state machine update.
	 * Use this for role-global actions. <br>
	 * This method will also be called after the role completed but was not removed yet
	 */
	protected void afterUpdate()
	{
	}
	
	
	/**
	 * This method is called before the state machine update.
	 * Use this for role-global actions. <br>
	 */
	protected void beforeUpdate()
	{
	}
	
	
	/**
	 * This method is called before the state machine update and only once after role instantiation
	 * Use this for role-global actions. <br>
	 */
	protected void beforeFirstUpdate()
	{
	}
	
	
	/**
	 * This is called, when the game state has changed.
	 * It is called before {@link APlay#doUpdate(AthenaAiFrame)}.
	 * 
	 * @param gameState
	 */
	public void onGameStateChanged(final EGameState gameState)
	{
	}
	
	
	/**
	 * Start a timeout after which the role will be set so be completed
	 * 
	 * @param timeMs [ms]
	 */
	public final void setCompleted(final int timeMs)
	{
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(
				"SetRoleCompleted"));
		executor.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				setCompleted();
			}
		}, timeMs, TimeUnit.MILLISECONDS);
	}
	
	
	/**
	 * @return
	 */
	public final ERole getType()
	{
		return type;
	}
	
	
	/**
	 * @return
	 */
	public final BotID getBotID()
	{
		return botID;
	}
	
	
	/**
	 * Returns the current position of the bot associated with this role.<br>
	 * <strong>WARNING: Use only after role has been assigned!!!</strong> (Makes no sense otherwise...)
	 * 
	 * @return position
	 * @throws IllegalStateException if role has not been initialized yet
	 */
	public final IVector2 getPos()
	{
		if (botID.isUninitializedID())
		{
			throw new IllegalStateException("Role '" + this + "' has not been initialized yet!!!");
		}
		try
		{
			return getAiFrame().getWorldFrame().getTiger(botID).getPos();
		} catch (NoObjectWithThisIDException err)
		{
			return lastPos;
		}
	}
	
	
	/**
	 * Get the TrackedTigerBot of this role
	 * 
	 * @return
	 */
	public final TrackedTigerBot getBot()
	{
		return getWFrame().tigerBotsVisible.get(getBotID());
	}
	
	
	/**
	 * @return Whether this role has been assigned to a bot (by
	 *         {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Lachesis})
	 */
	public final boolean hasBeenAssigned()
	{
		return !botID.isUninitializedID();
	}
	
	
	/**
	 * @return true when this role is completed
	 */
	public final boolean isCompleted()
	{
		return isCompleted;
	}
	
	
	/**
	 * Sets this role to completed.
	 */
	public final void setCompleted()
	{
		if (!isCompleted)
		{
			log.trace(this + ": Completed");
		}
		isCompleted = true;
		removeSkillObserver();
		if ((currentSkill != null) && (currentSkill.getSkillName() != ESkillName.IDLE))
		{
			setNewSkill(new IdleSkill());
		}
		stateMachine.stop();
	}
	
	
	@Override
	public final String toString()
	{
		return type.toString();
	}
	
	
	/**
	 * @return the current ai frame
	 * @throws NullPointerException if called before {@link #update(AthenaAiFrame)}
	 */
	public final AthenaAiFrame getAiFrame()
	{
		return aiFrame;
	}
	
	
	/**
	 * @return the current worldframe
	 */
	public final WorldFrame getWFrame()
	{
		return aiFrame.getWorldFrame();
	}
	
	
	/**
	 * @return the currentSkill
	 */
	public final ISkill getCurrentSkill()
	{
		return currentSkill;
	}
	
	
	/**
	 * @param newSkill the currentSkill to set
	 */
	public final void setNewSkill(final ISkill newSkill)
	{
		if (currentSkill != null)
		{
			currentSkill.removeObserver(this);
		}
		currentSkill = newSkill;
		if (currentSkill != null)
		{
			reduceSpeedOnStop();
			currentSkill.addObserver(this);
		}
		this.newSkill = true;
	}
	
	
	/**
	 * @return the botType
	 */
	public final EBotType getBotType()
	{
		return botType;
	}
	
	
	/**
	 * Get all {@link EFeature}s that are needed by this role
	 * 
	 * @return
	 */
	public final List<EFeature> getNeededFeatures()
	{
		List<EFeature> features = new LinkedList<EFeature>();
		features.add(EFeature.MOVE);
		fillNeededFeatures(features);
		
		return features;
	}
	
	
	/**
	 * DO NOT call this from AI. This is used by SkillExecuter!
	 * 
	 * @return the newSkill
	 */
	public final ISkill getNewSkill()
	{
		if (newSkill)
		{
			newSkill = false;
			return currentSkill;
		}
		return null;
	}
}
