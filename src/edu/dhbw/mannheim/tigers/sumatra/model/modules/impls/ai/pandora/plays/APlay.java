/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s):
 * Oliver Steinbrecher
 * Daniel Waigand
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.ESelectionReason;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERoleBehavior;


/**
 * This is the abstract play class. all plays inherit from here.<br/>
 * This type already includes a list for contained roles ({@link #roles} and the
 * corresponding update mechanism ({@link #update(AIInfoFrame)}).
 * 
 * @author DanielW, Oliver, Gero
 */
public abstract class APlay implements Comparable<APlay>
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger		log					= Logger.getLogger(APlay.class.getName());
	
	private EPlayState					state;
	
	/**
	 * play type will be set by a setter from {@link PlayFactory}. It must be null here, because {@link EPlay} is too
	 * complex as we could simple add an NOT_SET constant
	 */
	private EPlay							type					= EPlay.UNITIALIZED;
	private final List<ARole>			roles;
	
	private boolean						firstUpdate			= true;
	
	private final List<ACriterion>	criteria;
	
	// Timing
	private static final int			TIME_NOT_STARTED	= -1;
	/** [s] @see {@link #setTimeout(long)} */
	public static final int				DEFAULT_TIMEOUT	= 10;
	
	/** [ns] */
	private long							timeStart			= TIME_NOT_STARTED;
	/** [ns] */
	private long							timeout				= TIME_NOT_STARTED;
	
	/** number of roles assigned to this play by PlayFinder */
	private final int						numAssignedRoles;
	
	private ESelectionReason			selectionReason	= ESelectionReason.UNKNOWN;
	
	
	// --------------------------------------------------------------------------
	// --- getInstance/constructor(s) -------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public APlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		roles = new ArrayList<ARole>();
		state = EPlayState.RUNNING;
		criteria = new ArrayList<ACriterion>();
		this.numAssignedRoles = numAssignedRoles;
		setTimeout(Long.MAX_VALUE);
	}
	
	
	// --------------------------------------------------------------
	// --- roles ----------------------------------------------------
	// --------------------------------------------------------------
	
	
	/**
	 * Adds the given role with {@link ERoleBehavior#DEFENSIVE} and the given {@link IVector2}.
	 * 
	 * @param role
	 * @param initDestination
	 */
	protected final void addDefensiveRole(ARole role, IVector2 initDestination)
	{
		role.setBehavior(ERoleBehavior.DEFENSIVE);
		role.initDestination(initDestination);
		addRole(role);
	}
	
	
	/**
	 * Adds the given role with {@link ERoleBehavior#AGGRESSIVE} and the given {@link IVector2}.
	 * 
	 * @param role
	 * @param initDestination
	 */
	protected final void addCreativeRole(ARole role, IVector2 initDestination)
	{
		role.setBehavior(ERoleBehavior.CREATIVE);
		role.initDestination(initDestination);
		addRole(role);
	}
	
	
	/**
	 * Adds the given role with {@link ERoleBehavior#AGGRESSIVE} and the given {@link IVector2}.
	 * 
	 * @param role
	 * @param initDestination
	 */
	protected final void addAggressiveRole(ARole role, IVector2 initDestination)
	{
		role.setBehavior(ERoleBehavior.AGGRESSIVE);
		role.initDestination(initDestination);
		addRole(role);
	}
	
	
	/**
	 * Add role to roles, but check if maxNumRoles is already reached
	 * 
	 * @param role
	 */
	private void addRole(ARole role)
	{
		if (roles.size() >= numAssignedRoles)
		{
			log.error(this + ": Can not add role " + role + ". Max num roles already reached!");
		} else
		{
			roles.add(role);
			role.putSkillObserver();
		}
	}
	
	
	/**
	 * Checks, if the given Play has a Keeper Role
	 * 
	 * @author MalteM
	 * @return play has keeper role
	 */
	public boolean hasKeeperRole()
	{
		for (final ARole role : roles)
		{
			if (role.isKeeper())
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Defines, whether or not the play is ball-carrying.
	 * Override with return true if your play carries the ball.
	 * 
	 * @author GuntherB
	 * @return
	 */
	public boolean isBallCarrying()
	{
		return false;
	}
	
	
	/**
	 * deletes oldRole from the assignedRoles, gives newRole the botID of the oldRole
	 * and puts botId/newRole into the assignedRoles-Map
	 * 
	 * i.e. A {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole} has
	 * finished its task and shall now shoot a goal, therefore
	 * a new ShooterRole is created by the play and the roles are switched by using this function.
	 * Also, the oldRole's botID is automatically set in the newRole
	 * @author GuntherB, GeroL
	 */
	protected final void switchRoles(ARole oldRole, ARole newRole, AIInfoFrame aIInfoFrame)
	{
		newRole.assignBotID(oldRole.getBotID());
		
		roles.remove(oldRole);
		roles.add(newRole);
		
		oldRole.removeSkillObserver();
		newRole.putSkillObserver();
		
		// Removes the old assignment as the map below associates the old id with the new role
		aIInfoFrame.putAssignedRole(newRole);
		
		// Important: Update role to guarantee that update() has been at least once before calcSkills!!!
		newRole.update(aIInfoFrame);
		
		
		// Gero: At this point the ERoleBehavior of the assigned roles MAY BE inconsistent. This doesn't bother us as
		// role-assigning is done. Even if this newRole will be reused in next cycle, it will be assigned properly, but...
		// it's just not very nice (Gero)
	}
	
	
	private void onPlayFinished()
	{
		log.trace(getType() + " finished");
		for (ARole role : getRoles())
		{
			role.setCompleted();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- state ----------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Changes the play state to finished. For non offensive plays, the result
	 * is not important. Just say, you are done. If you have a offensive play, than
	 * use AOffensivePlay#changeToSucceeded() or AOffensivePlay#changeToFailed() if you can
	 * determine the result. If not, just finish the play.
	 * e.g. if you tried to score a goal you should not wait for the result in the play ;)
	 * <b>Do not call this before adding roles!!!</b>
	 */
	protected final void changeToFinished()
	{
		state = EPlayState.FINISHED;
		onPlayFinished();
	}
	
	
	/**
	 * Cancel play. This method is public so that plays can be removed from Atena.<br>
	 * Don't call this from a play.
	 * <b>Do not call this before adding roles!!!</b>
	 * @see APlay#changeToFinished()
	 */
	public final void changeToCanceled()
	{
		state = EPlayState.FAILED;
		onPlayFinished();
	}
	
	
	/**
	 * Returns the actual play state.
	 * @return
	 */
	public final EPlayState getPlayState()
	{
		return state;
	}
	
	
	// --------------------------------------------------------------------------
	// --- update process -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Updates the play in three steps:<br/>
	 * <ol>
	 * <li>Set new goals for this play ({@link #beforeUpdate(AIInfoFrame)})</li>
	 * <li>Updating the roles and their conditions ({@link ARole#update(AIInfoFrame)})</li>
	 * <li>Defining the state of the play and how to react by checking the roles' conditions (
	 * {@link #afterUpdate(AIInfoFrame)})</li>
	 * </ol>
	 * <p>
	 * <strong>Note:</strong> Do NOT override, use {@link #beforeUpdate(AIInfoFrame)} and
	 * {@link #afterUpdate(AIInfoFrame)} instead!
	 * </p>
	 * @param currentFrame
	 * @return
	 */
	public final EPlayState update(AIInfoFrame currentFrame)
	{
		if (state != EPlayState.RUNNING)
		{
			return state;
		}
		
		// set ai frame in all roles
		for (ARole role : roles)
		{
			role.updateAiInfoFrame(currentFrame);
		}
		
		// Call beforeFirstUpdate(...) once for initialization purposes
		if (firstUpdate)
		{
			timeStart = System.nanoTime();
			
			for (final ARole role : roles)
			{
				role.update(currentFrame);
			}
			beforeFirstUpdate(currentFrame);
			firstUpdate = false;
		}
		
		// Set targets/goals
		beforeUpdate(currentFrame);
		
		// Update roles and conditions
		for (ARole role : roles)
		{
			role.update(currentFrame);
		}
		
		// Define state and reaction
		afterUpdate(currentFrame);
		
		// Timeout?
		if (getPlayState() == EPlayState.RUNNING)
		{
			final long now = System.nanoTime();
			if ((now - timeStart) > timeout)
			{
				log.debug(this + ": Play timed out");
				timedOut(currentFrame);
			}
		}
		
		return state;
	}
	
	
	/**
	 * Called when the current play run longer then defined by {@link #setTimeout(long)} (or {@link #DEFAULT_TIMEOUT})
	 * 
	 * @param currentFrame
	 */
	protected void timedOut(AIInfoFrame currentFrame)
	{
		changeToFinished();
	}
	
	
	/**
	 * This is only called once (if {@link #firstUpdate} == true). This is useful for initialization-purposes after the
	 * role-assignment. ({@link #beforeUpdate(AIInfoFrame)} is called afterwards, like every other
	 * {@link #update(AIInfoFrame)}!)
	 * 
	 * @see #update(AIInfoFrame)
	 * @param frame
	 */
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
	}
	
	
	/**
	 * @see #update(AIInfoFrame)
	 * @param frame
	 */
	protected abstract void beforeUpdate(AIInfoFrame frame);
	
	
	/**
	 * @see #update(AIInfoFrame)
	 * @param currentFrame
	 */
	protected abstract void afterUpdate(AIInfoFrame currentFrame);
	
	
	/**
	 * @param timeoutS [s] The timeout for this play. Default value is {@link #DEFAULT_TIMEOUT}
	 */
	protected final void setTimeout(long timeoutS)
	{
		timeout = TimeUnit.SECONDS.toNanos(timeoutS);
		resetTimer();
	}
	
	
	/**
	 * resets the timeStart to current system time ( timing for timeout will restart)
	 */
	protected final void resetTimer()
	{
		timeStart = System.nanoTime();
	}
	
	
	/**
	 * Adds a criterion that decides about selection of the play during a match
	 * 
	 * @param crit
	 */
	protected final void addCriterion(ACriterion crit)
	{
		criteria.add(crit);
	}
	
	
	/**
	 * Calculates the playable score of the given play based on its criteria.
	 * FYI: It is not allowed to override this anymore, because everything should be done with Criteria
	 * 
	 * @param currentFrame
	 * @return score value (range 0 to 1)
	 */
	public final float calcPlayableScore(AIInfoFrame currentFrame)
	{
		float result = 1.0f;
		// Multiply all criteria values
		for (final ACriterion crit : criteria)
		{
			final float score = crit.checkCriterion(currentFrame);
			log.trace("Playable score for " + getType() + "/" + crit + ": " + score);
			result *= score;
		}
		
		return result;
	}
	
	
	@Override
	public int compareTo(APlay o)
	{
		int typeThis = getType().getType().getOrder();
		int typeO = o.getType().getType().getOrder();
		if (typeThis < typeO)
		{
			return -1;
		} else if (typeThis == typeO)
		{
			return 0;
		}
		return 1;
	}
	
	
	// --------------------------------------------------------------
	// --- setter/getter --------------------------------------------
	// --------------------------------------------------------------
	
	/**
	 * Return all roles of this play
	 * 
	 * @return
	 */
	public final List<ARole> getRoles()
	{
		return roles;
	}
	
	
	/**
	 * The EPlay-type of this play.
	 * 
	 * @return EPlay type
	 */
	public final EPlay getType()
	{
		return type;
	}
	
	
	@Override
	public String toString()
	{
		return type.toString();
	}
	
	
	/**
	 * Number of currently added roles.
	 * 
	 * @return number of roles added to this play
	 */
	public final int getRoleCount()
	{
		return getRoles().size();
	}
	
	
	/**
	 * Number of assigned roles for this play.
	 * This is NOT the number of actually added roles
	 * you can define your min and max desired number of roles in EPlay,
	 * the PlayFinder will then decide how many roles you can use.
	 * this is a new feature! Use it to avoid duplicated plays ;)
	 * 
	 * @see APlay#getRoleCount()
	 * @return
	 */
	public final int getNumAssignedRoles()
	{
		return numAssignedRoles;
	}
	
	
	/**
	 * Do NOT use this. This is only for AOffensivePlay!!
	 * @param state the state to set
	 */
	protected final void setState(EPlayState state)
	{
		this.state = state;
	}
	
	
	/**
	 * This setter may only be used by PlayFactory!
	 * @param type the type to set
	 * @throws IllegalStateException if you try to set the type a second time
	 */
	public final void setType(EPlay type)
	{
		if ((this.type != null) && (this.type != EPlay.UNITIALIZED))
		{
			throw new IllegalStateException("Play type may not be set twice!");
		}
		this.type = type;
	}
	
	
	/**
	 * @return the selectionReason
	 */
	public final ESelectionReason getSelectionReason()
	{
		return selectionReason;
	}
	
	
	/**
	 * @param selectionReason the selectionReason to set
	 */
	public final void setSelectionReason(ESelectionReason selectionReason)
	{
		this.selectionReason = selectionReason;
	}
}
