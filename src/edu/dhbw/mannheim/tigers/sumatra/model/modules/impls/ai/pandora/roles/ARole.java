/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.11.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon.EDestFreeMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.exceptions.RoleNullInitDestinationException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.exceptions.RoleUnitializedBotIDException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.DoneState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.EventStatePair;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IStateMachine;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.StateMachine;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillEventObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillSystemObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.moduli.exceptions.ModuleNotFoundException;


/**
 * Abstract type for roles, all roles inherit from here
 * Already includes some generic implementations for:
 * <ul>
 * <li>list for {@link java.util.concurrent.locks.Condition}s</li>
 * <li>list for {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill}s</li>
 * <li>checkAllConditions</li>
 * <li>{@link MovementCon} and implementations for {@link #initDestination(IVector2)} and {@link #getDestination()}</li>
 * </ul>
 * <p>
 * 
 * @author Gero
 * @author DanielW
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public abstract class ARole implements ISkillSystemObserver, ISkillEventObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final Logger									log				= Logger.getLogger(ARole.class.getName());
	
	private static final IRoleState								DONE_STATE		= new DoneState();
	
	private ERole														type;
	
	/** uninitialized BotID */
	private BotID														botID				= new BotID();
	
	private ERoleBehavior											behavior			= ERoleBehavior.UNKNOWN;
	
	private transient final Map<ECondition, ACondition>	conditions;
	
	private boolean													firstUpdate		= true;
	
	/** This flag is used to tell the play which uses this role whether it is completed. */
	private boolean													isCompleted		= false;
	
	/** The {@link ARole}s {@link MovementCon}. <b>Already added, do not add it again!!!</b> */
	private transient final MovementCon							moveCon;
	
	/** bot type of the bot associated to this role. Will be set before first update */
	private EBotType													botType			= EBotType.UNKNOWN;
	
	private transient AIInfoFrame									aiFrame			= null;
	
	private transient AMoveSkill									currentSkill	= null;
	private transient boolean										newSkill			= true;
	private transient final IStateMachine<IRoleState>		stateMachine;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * New role with penaltyAreaCheck
	 * 
	 * @param type
	 */
	public ARole(ERole type)
	{
		this(type, false);
	}
	
	
	/**
	 * @param type
	 * @param penaltyAreaAllowed checks if destination of this role is in conflict with
	 *           the penalty area. When true the destination wont be updated.
	 */
	public ARole(ERole type, boolean penaltyAreaAllowed)
	{
		this.type = type;
		conditions = new EnumMap<ECondition, ACondition>(ECondition.class);
		
		// new movement condition without an destination.
		// it will also not consider the orientation until an updateLookAtTarget or updateTargetAngle
		moveCon = new MovementCon();
		moveCon.updateDestinationFree(EDestFreeMode.IGNORE);
		moveCon.setPenaltyAreaAllowed(penaltyAreaAllowed);
		// add condition, but do not use addCondition() because this is not allowed for moveConditions
		conditions.put(moveCon.getType(), moveCon);
		
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
	public final void addTransition(Enum<?> stateId, Enum<?> event, IRoleState state)
	{
		stateMachine.getTransititions().put(new EventStatePair(event, stateId), state);
	}
	
	
	/**
	 * Add a transition
	 * @param transitions
	 */
	public final void addAllTransitions(Map<EventStatePair, IRoleState> transitions)
	{
		stateMachine.getTransititions().putAll(transitions);
	}
	
	
	/**
	 * Adds an End Transition.
	 * @param stateId current state
	 * @param event event that have to occurs, that the statemachine ends
	 */
	public void addEndTransition(Enum<?> stateId, Enum<?> event)
	{
		addTransition(stateId, event, DONE_STATE);
	}
	
	
	/**
	 * Set the initial state to start with. This is mandatory for all roles!
	 * 
	 * @param initialState
	 */
	public final void setInitialState(IRoleState initialState)
	{
		stateMachine.setInitialState(initialState);
	}
	
	
	/**
	 * Go to next state
	 * 
	 * @param event
	 */
	public final void nextState(Enum<? extends Enum<?>> event)
	{
		stateMachine.nextState(event);
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
	public Enum<?> getCurrentState()
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
	public final void onSkillStarted(ASkill skill, BotID botID)
	{
		synchronized (stateMachine)
		{
			IRoleState state = stateMachine.getCurrentState();
			if (state != null)
			{
				state.onSkillStarted(skill, botID);
			}
		}
	}
	
	
	@Override
	public final void onSkillCompleted(ASkill skill, BotID botID)
	{
		synchronized (stateMachine)
		{
			IRoleState state = stateMachine.getCurrentState();
			if ((state != null) && botID.equals(getBotID()))
			{
				state.onSkillCompleted(skill, botID);
			}
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
	public void onNewEvent(Enum<? extends Enum<?>> event)
	{
		nextState(event);
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
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Sets the initial value for the result of {@link #getDestination()} before the first call of
	 * {@link #update(AIInfoFrame)}<br>
	 * <strong>Please</strong> do not call this. Its called for you when adding a role!
	 * 
	 * @param destination
	 */
	public final void initDestination(IVector2 destination)
	{
		if (destination == null)
		{
			throw new RoleNullInitDestinationException("The given destination was 'null'!");
		}
		updateDestination(destination);
	}
	
	
	/**
	 * Adds a condition to a role. This method should be
	 * used in the constructor in derived classes of this.
	 * 
	 * @param condition
	 * @throws IllegalArgumentException when trying to add {@link MovementCon}.
	 */
	protected final void addCondition(ACondition condition)
	{
		if (condition.getType() != ECondition.MOVEMENT)
		{
			conditions.put(condition.getType(), condition);
		} else
		{
			throw new IllegalArgumentException(
					"Movement condition cannot be added again!. It is already a member of ABaseRole!");
		}
	}
	
	
	/**
	 * May only be called <strong>ONCE</strong>, further calls will have no effect.
	 * 
	 * @param behavior
	 */
	public final void setBehavior(ERoleBehavior behavior)
	{
		if (this.behavior == ERoleBehavior.UNKNOWN)
		{
			this.behavior = behavior;
		} else
		{
			log.warn("Change of behavior in role " + this + " denied!");
		}
	}
	
	
	/**
	 * This method should only be called from the
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Assigner} or
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay#switchRoles(ARole, ARole, AIInfoFrame)}
	 * !!!
	 * 
	 * @param newBotId
	 */
	public final void assignBotID(BotID newBotId)
	{
		if (newBotId.isUninitializedID())
		{
			throw new RoleUnitializedBotIDException("Someone tries to initialize role '" + this
					+ "' with an UNINITIALIZED BotID!!!");
		}
		
		if (!botID.isUninitializedID())
		{
			log.warn("Change of BotID in role " + this + " denied, it already has an assigned BotID!!!");
		} else
		{
			botID = new BotID(newBotId);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- updater --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param curFrame
	 */
	public final void updateAiInfoFrame(AIInfoFrame curFrame)
	{
		aiFrame = curFrame;
	}
	
	
	/**
	 * @param curFrame
	 */
	public final void update(AIInfoFrame curFrame)
	{
		aiFrame = curFrame;
		
		if (curFrame.worldFrame.tigerBotsVisible.getWithNull(botID) == null)
		{
			// bot is not there atm. update would result in exceptions...
			// in match mode, this should not happen, because the playfinder will
			// find new plays immediately after one bot was lost
			return;
		}
		
		if (firstUpdate)
		{
			if (hasBeenAssigned())
			{
				botType = curFrame.worldFrame.tigerBotsVisible.get(botID).getBotType();
				firstUpdate = false;
			} else
			{
				log.error(this + ": Update was called although role has not been assigned!");
				return;
			}
		}
		
		synchronized (stateMachine)
		{
			if (stateMachine.getCurrentState() == null)
			{
				setCompleted();
			} else
			{
				stateMachine.update();
			}
		}
		afterUpdate(curFrame);
	}
	
	
	/**
	 * This method is called after the state machine update.
	 * Use this for role-global actions. <br>
	 * This method will also be called after the role completed but was not removed yet
	 * 
	 * @param aiFrame
	 */
	protected void afterUpdate(AIInfoFrame aiFrame)
	{
		
	}
	
	
	/**
	 * Checks if destination is within penalty area. When position
	 * is within this area a near random point is set as new destination.
	 * 
	 * @param position
	 */
	public final void updateDestination(IVector2 position)
	{
		if (!isPenaltyAreaAllowed() && AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(position))
		{
			IVector2 nearestOutside = AIConfig.getGeometry().getPenaltyAreaOur().nearestPointOutside(position);
			moveCon.updateDestination(nearestOutside);
			// log.warn("You set a destination inside the penalty area! (" + position +
			// ") Nearest point outside is chosen: "
			// + nearestOutside, new IllegalArgumentException());
			return;
		}
		
		
		if (!AIConfig.getGeometry().getFieldWBorders().isPointInShape(position))
		{
			// log.warn("Destination out of field: " + position);
			moveCon.updateDestination(AIConfig.getGeometry().getFieldWBorders().nearestPointInside(position));
			return;
		}
		
		moveCon.updateDestination(position);
	}
	
	
	/**
	 * Set the angle, the bot should look to.
	 * <b>Hint: </b> It may be easier to set the look at target. {@link #updateLookAtTarget(IVector2) }
	 * 
	 * @param angle [rad]
	 */
	public final void updateTargetAngle(float angle)
	{
		moveCon.updateTargetAngle(angle);
	}
	
	
	/**
	 * @param destFreeMode
	 */
	public final void updateDestinationFree(EDestFreeMode destFreeMode)
	{
		moveCon.updateDestinationFree(destFreeMode);
	}
	
	
	/**
	 * Updates the target the bot should look at. When target is a zero-vector the bot keeps the actual angle.
	 * 
	 * @param aimingTarget
	 */
	public final void updateLookAtTarget(IVector2 aimingTarget)
	{
		moveCon.updateLookAtTarget(aimingTarget);
	}
	
	
	// --------------------------------------------------------------------------
	// --- conditions -----------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Iterates over every {@link ACondition} of this {@link ARole} and checks whether <strong>every single one</strong>
	 * is <code>true</code>, else returning <code>false</code>. <br>
	 * <strong>WARNING:</strong> Consider using something more specific, because you will miss situations, where the
	 * condition is
	 * blocked!
	 * 
	 * @param wFrame
	 * @return state
	 */
	public final boolean checkAllConditions(WorldFrame wFrame)
	{
		if (botID.isUninitializedID())
		{
			throw new RoleUnitializedBotIDException("Role '" + this + "' has not been initialized yet!!!");
		}
		
		for (final ACondition condition : conditions.values())
		{
			// a condition is false
			EConditionState state = condition.checkCondition(wFrame, getBotID());
			if ((state == EConditionState.PENDING) || (state == EConditionState.BLOCKED))
			{
				return false;
			}
		}
		
		// all conditions are true
		return true;
	}
	
	
	/**
	 * Check if bot is at the desired destination and looks at desired target.
	 * Consider using checkMovementCondition to get more information about the state of the condition
	 * 
	 * @param worldFrame
	 * @return
	 * @see ARole#checkMovementCondition(WorldFrame)
	 */
	public final boolean checkMoveCondition(WorldFrame worldFrame)
	{
		return moveCon.checkCondition(worldFrame, getBotID()) == EConditionState.FULFILLED;
	}
	
	
	/**
	 * @return
	 * @see #checkMoveCondition(WorldFrame)
	 */
	public final boolean checkMoveCondition()
	{
		return checkMoveCondition(aiFrame.worldFrame);
	}
	
	
	/**
	 * Check all movement conditions, that are active.
	 * You will get an enum with information about the state of the condition.
	 * Most interesting are {@link EConditionState#FULFILLED} and {@link EConditionState#PENDING}.
	 * Also consider using {@link EConditionState#BLOCKED} to catch situations, where e.g. the destination is blocked by
	 * another bot
	 * 
	 * @param worldFrame
	 * @return
	 */
	public final EConditionState checkMovementCondition(WorldFrame worldFrame)
	{
		return moveCon.checkCondition(worldFrame, getBotID());
	}
	
	
	/**
	 * @return
	 * @see #checkMovementCondition(WorldFrame)
	 */
	public final EConditionState checkMovementCondition()
	{
		return checkMovementCondition(getAiFrame().worldFrame);
	}
	
	
	/**
	 * Start a timeout after which the role will be set so be completed
	 * 
	 * @param timeMs [ms]
	 */
	public final void setCompleted(int timeMs)
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
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * MovementCon (contains destination information, etc.)<br>
	 * <strong>Please</strong> only use for passing to skill, etc.<br>
	 * Use accessor methods of {@link ARole} to manipulate {@link MovementCon}
	 * 
	 * @return {@link MovementCon}
	 */
	public final MovementCon getMoveCon()
	{
		return moveCon;
	}
	
	
	/**
	 * @return
	 */
	public final IVector2 getDestination()
	{
		return moveCon.getDestCon().getDestination();
	}
	
	
	/**
	 * @return targeted rotation angle
	 */
	public final float getTargetAngle()
	{
		return moveCon.getAngleCon().getTargetAngle();
	}
	
	
	/**
	 * @return targeted rotation angle
	 */
	public final IVector2 getLookAtTarget()
	{
		return moveCon.getLookAtTarget();
	}
	
	
	/**
	 * @return the penaltyAreaCheck
	 */
	public final boolean isPenaltyAreaAllowed()
	{
		return moveCon.isPenaltyAreaAllowed();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public final ERole getType()
	{
		return type;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public final ERoleBehavior getBehavior()
	{
		return behavior;
	}
	
	
	/**
	 * 
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
	 * @throws RoleUnitializedBotIDException if role has not been initialized yet
	 */
	public final IVector2 getPos()
	{
		if (botID.isUninitializedID())
		{
			throw new RoleUnitializedBotIDException("Role '" + this + "' has not been initialized yet!!!");
		}
		return getAiFrame().worldFrame.getTiger(botID).getPos();
	}
	
	
	/**
	 * Get the TrackedTigerBot of this role
	 * 
	 * @param wFrame
	 * @return
	 */
	public final TrackedTigerBot getBot(final WorldFrame wFrame)
	{
		return wFrame.tigerBotsVisible.get(getBotID());
	}
	
	
	/**
	 * Get the TrackedTigerBot of this role
	 * 
	 * @return
	 */
	public final TrackedTigerBot getBot()
	{
		return getBot(getAiFrame().worldFrame);
	}
	
	
	/**
	 * @return
	 */
	public final boolean hasBallContact()
	{
		return getAiFrame().worldFrame.getTiger(botID).hasBallContact();
	}
	
	
	/**
	 * Returns an immutable version of the internal {@link #conditions}-map
	 * @return
	 */
	public final Map<ECondition, ACondition> getConditions()
	{
		return Collections.unmodifiableMap(conditions);
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
		if (currentSkill != null)
		{
			currentSkill.removeObserver(this);
		}
	}
	
	
	/**
	 * Allows the role to enter penaltyArea
	 * <strong> Note:</strong> Only set false for a keeper
	 * 
	 * @param penaltyAreaAllowed
	 */
	public final void setPenaltyAreaAllowed(boolean penaltyAreaAllowed)
	{
		moveCon.setPenaltyAreaAllowed(penaltyAreaAllowed);
	}
	
	
	@Override
	public String toString()
	{
		return type.toString();
	}
	
	
	/**
	 * @return the latestWorldFrame
	 * @throws NullPointerException if called before {@link #update(AIInfoFrame)}
	 */
	public final AIInfoFrame getAiFrame()
	{
		return aiFrame;
	}
	
	
	/**
	 * @return the currentSkill
	 */
	public final AMoveSkill getCurrentSkill()
	{
		return currentSkill;
	}
	
	
	/**
	 * @param newSkill the currentSkill to set
	 */
	public final void setNewSkill(AMoveSkill newSkill)
	{
		if (currentSkill != null)
		{
			currentSkill.removeObserver(this);
		}
		currentSkill = newSkill;
		currentSkill.addObserver(this);
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
		
		if (features.contains(EFeature.CHIP_KICKER) || features.contains(EFeature.STRAIGHT_KICKER))
		{
			features.add(EFeature.BARRIER);
		}
		return features;
	}
	
	
	/**
	 * @return the newSkill
	 */
	public final AMoveSkill getNewSkill()
	{
		if (newSkill)
		{
			newSkill = false;
			return currentSkill;
		}
		return null;
	}
}
