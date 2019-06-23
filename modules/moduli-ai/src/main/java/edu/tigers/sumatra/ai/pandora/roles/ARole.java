/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.statemachine.IStateMachine;
import edu.tigers.sumatra.statemachine.StateMachine;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Abstract type for roles, all roles inherit from here
 * Already includes some generic implementations for:
 * <ul>
 * <li>list for {@link edu.tigers.sumatra.skillsystem.skills.ASkill}s</li>
 * </ul>
 * <p>
 * 
 * @author Gero
 * @author DanielW
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ARole
{
	private static final Logger log = Logger.getLogger(ARole.class.getName());
	
	private final ERole type;
	
	/** uninitialized BotID */
	private BotID botID = BotID.noBot();
	
	private boolean firstUpdate = true;
	
	/** This flag is used to tell the play which uses this role whether it is completed. */
	private boolean isCompleted = false;
	
	private transient AthenaAiFrame aiFrame = null;
	
	private transient ISkill currentSkill = new IdleSkill();
	private transient boolean newSkill = true;
	private final transient IStateMachine<IState> stateMachine;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param type of the role
	 */
	public ARole(final ERole type)
	{
		this.type = type;
		stateMachine = new StateMachine<>();
		stateMachine.setExtendedLogging(SumatraModel.getInstance().isTestMode());
	}
	
	
	// --------------------------------------------------------------------------
	// --- state machine --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
	 * Adds a wildward End Transition.
	 *
	 * @param event the event that triggers the transition
	 */
	protected final void addEndTransition(final IEvent event)
	{
		stateMachine.addTransition(null, event, null);
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
	public final void triggerEvent(final IEvent event)
	{
		stateMachine.triggerEvent(event);
	}
	
	
	/**
	 * Check if everything is fine for this role
	 * 
	 * @return true, if role is OK
	 */
	final boolean doSelfCheck()
	{
		return stateMachine.valid();
	}
	
	
	/**
	 * Identifier of the current state
	 * 
	 * @return the current state - can be null!
	 */
	public final IState getCurrentState()
	{
		return stateMachine.getCurrentState();
	}
	
	
	/**
	 * This method should only be called from the RoleAssigner!!!
	 * 
	 * @param newBotId the new id that should be assigned to this role
	 */
	public final void assignBotID(final BotID newBotId)
	{
		if (newBotId.isUninitializedID())
		{
			throw new IllegalArgumentException("Someone tries to initialize role '" + this
					+ "' with an UNINITIALIZED BotID!!!");
		}
		botID = newBotId;
	}
	
	
	/**
	 * @param curFrame current frame
	 */
	public final void updateBefore(final AthenaAiFrame curFrame)
	{
		aiFrame = curFrame;
	}
	
	
	/**
	 * @param curFrame current frame
	 */
	public final void update(final AthenaAiFrame curFrame)
	{
		updateBefore(curFrame);
		
		try
		{
			if (firstUpdate)
			{
				if (hasBeenAssigned())
				{
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
			
			afterUpdate();
		} catch (Exception err)
		{
			log.error("Exception on role update (" + getType() + ")", err);
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
	
	
	protected void onCompleted()
	{
	}
	
	
	/**
	 * @return type of role
	 */
	public final ERole getType()
	{
		return type;
	}
	
	
	/**
	 * @return bot id
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
	 */
	public final IVector2 getPos()
	{
		return getWFrame().getTiger(botID).getPos();
	}
	
	
	/**
	 * Get the tracked bot of this role
	 * 
	 * @return tracked bot
	 */
	public final ITrackedBot getBot()
	{
		return getWFrame().getTiger(getBotID());
	}
	
	
	/**
	 * @return Whether this role has been assigned to a real bot
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
			isCompleted = true;
			stateMachine.stop();
			if (currentSkill.getType() != ESkill.IDLE)
			{
				setNewSkill(new IdleSkill());
			}
			onCompleted();
		}
	}
	
	
	@Override
	public final String toString()
	{
		return type.toString();
	}
	
	
	/**
	 * @return the current ai frame
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
	 * @return the current ball
	 */
	protected final ITrackedBall getBall()
	{
		return getWFrame().getBall();
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
		currentSkill = newSkill;
		this.newSkill = true;
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
