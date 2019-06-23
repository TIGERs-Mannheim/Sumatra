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
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.MatchPlayFinder;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.PlayTuple;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Tactics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Tactics.PlayConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ICriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERoleBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;


/**
 * This is the abstract play class. all plays inherit from here.<br/>
 * This type already includes a list for contained roles ({@link #roles} and the
 * corresponding update mechanism ({@link #update(AIInfoFrame)}).
 * 
 * @author DanielW, Oliver, Gero
 */
public abstract class APlay implements Serializable
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 4097739690156132078L;
	
	protected final Logger		log					= Logger.getLogger(getClass());
	
	public enum EPlayState
	{
		RUNNING,
		SUCCEEDED,
		FAILED
	};
	
	private EPlayState					state;
	
	private final EPlay					type;
	private final AIInfoFrame			aiFrame;
	private final List<ARole>			roles;
	
	private boolean						firstUpdate			= true;
	
	private final List<ICriterion>	criteria;
	
	// PlayableScore
	private int								basicPlayableScore;
	private int								maxPlayableScore;
	private int								minPlayableScore;
	
	// Timing
	private static int					TIME_NOT_STARTED	= -1;
	/** [s] @see {@link #setTimeout(long)} */
	private static int					DEFAULT_TIMEOUT	= 10;
	
	/** [ns] */
	private long							timeStart			= TIME_NOT_STARTED;
	/** [ns] */
	private long							timeout				= TIME_NOT_STARTED;
	
	
	// --------------------------------------------------------------------------
	// --- getInstance/constructor(s) -------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @see APlay
	 * @param type
	 */
	public APlay(EPlay type, AIInfoFrame aiFrame)
	{
		this.type = type;
		this.aiFrame = aiFrame;
		this.roles = new ArrayList<ARole>();
		this.state = EPlayState.RUNNING;
		this.criteria = new ArrayList<ICriterion>();
		
		setTimeout(DEFAULT_TIMEOUT);
		
		basicPlayableScore = AIConfig.getTactics().getPlay(type).getBasicPlayableScore();
		maxPlayableScore = AIConfig.getTactics().getMaxPlayableScore();
		minPlayableScore = AIConfig.getTactics().getMinPlayableScore();
	}
	

	/**
	 * @see APlay
	 * @param type
	 * @param aiFrame
	 * @param basicPlayableScore 
	 */
	public APlay(EPlay type, AIInfoFrame aiFrame, int basicPlayableScore)
	{
		this(type, aiFrame);
		
		this.basicPlayableScore = basicPlayableScore;
	}
	

	// --------------------------------------------------------------
	// --- roles ----------------------------------------------------
	// --------------------------------------------------------------
	/**
	 * Adds the given role with {@link ERoleBehavior#DEFENSIVE} and {@link AIConfig#US_GOAL_MID} as destination.
	 * <p>
	 * <strong>NOTE: <u>Is there really no better position available???</u></strong>
	 * </p>
	 * @param role
	 */
	protected void addDefensiveRole(ARole role)
	{
		addDefensiveRole(role, AIConfig.getGeometry().getGoalOur().getGoalCenter());
	}
	

	/**
	 * Adds the given role with {@link ERoleBehavior#DEFENSIVE} and the given {@link IVector2}.
	 * 
	 * @param role
	 * @param initDestination
	 */
	protected void addDefensiveRole(ARole role, IVector2 initDestination)
	{
		role.setBehavior(ERoleBehavior.DEFENSIVE);
		role.initDestination(initDestination);
		roles.add(role);
	}
	

	/**
	 * Adds the given role with {@link ERoleBehavior#AGGRESSIVE} and the given {@link IVector2}.
	 * 
	 * @param role
	 * @param initDestination
	 */
	protected void addCreativeRole(ARole role, IVector2 initDestination)
	{
		role.setBehavior(ERoleBehavior.CREATIVE);
		role.initDestination(initDestination);
		roles.add(role);
	}
	

	/**
	 * Adds the given role with {@link ERoleBehavior#AGGRESSIVE} and the ball-position of the current frame.
	 * <p>
	 * <strong>NOTE: <u>Is there really no better position available???</u></strong>
	 * </p>
	 * @param role
	 */
	protected void addAggressiveRole(ARole role)
	{
		addAggressiveRole(role, aiFrame.worldFrame.ball.pos);
	}
	

	/**
	 * Adds the given role with {@link ERoleBehavior#AGGRESSIVE} and the given {@link IVector2}.
	 * 
	 * @param role
	 * @param initDestination
	 */
	protected void addAggressiveRole(ARole role, IVector2 initDestination)
	{
		role.setBehavior(ERoleBehavior.AGGRESSIVE);
		role.initDestination(initDestination);
		roles.add(role);
	}
	

	/**
	 * Checks, if the given Play has a Keeper Role
	 * 
	 * @author MalteM
	 */
	public boolean hasKeeperRole()
	{
		for (ARole role : roles)
		{
			if (role.isKeeper())
			{
				return true;
			}
		}
		return false;
	}
	

	/**
	 * Defines, whether or not the play is ball-carrying, used by play-matching {@link MatchPlayFinder}.
	 * Override with return true if your play carries the ball.
	 * 
	 * @author GuntherB
	 */
	public boolean isBallCarrying()
	{
		return false;
	}
	

	/**
	 * deletes oldRole from the assignedRoles, gives newRole the botID of the oldRole
	 * and puts botId/newRole into the assignedRoles-Map
	 * 
	 * i.e. A {@link BallGetterRole} has finished its task and shall now shoot a goal, therefore
	 * a new ShooterRole is created by the play and the roles are switched by using this function.
	 * Also, the oldRole's botID is automatically set in the newRole
	 * @author GuntherB, GeroL
	 */
	protected void switchRoles(ARole oldRole, ARole newRole, AIInfoFrame aIInfoFrame)
	{
		newRole.assignBotID(oldRole.getBotID());
		
		roles.remove(oldRole);
		roles.add(newRole);
		
		aIInfoFrame.assignedRoles.put(newRole.getBotID(), newRole);
		
		// Important: Update role to guarantee that update() has been at least once before calcSkills!!!
		newRole.doUpdate(aIInfoFrame);
		
		
		// Gero: At this point the ERoleBehavior of the assigned roles MAY BE inconsistent. This doesn't bother us as
		// role-assigning is done. Even if this newRole will be reused in next cycle, it will be assigned properly, but...
		// it's just not very nice (Gero)
	}
	

	// --------------------------------------------------------------------------
	// --- state ----------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Changes the play state to succeeded.
	 */
	protected void changeToSucceeded()
	{
		state = EPlayState.SUCCEEDED;
	}
	

	/**
	 * Changes the play state to failed.
	 */
	protected void changeToFailed()
	{
		state = EPlayState.FAILED;
	}
	

	/**
	 * Returns the actual play state.
	 */
	public EPlayState getPlayState()
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
	 */
	public EPlayState update(AIInfoFrame currentFrame)
	{
		// Call beforeFirstUpdate(...) once for initialization purposes
		if (firstUpdate)
		{
			timeStart = System.nanoTime();
			
			for (ARole role : roles)
			{
				role.firstUpdate(currentFrame);
			}
			
			beforeFirstUpdate(currentFrame);
			firstUpdate = false;
		}
		

		// Set targets/goals
		beforeUpdate(currentFrame);
		
		// Update roles and conditions
		for (ARole role : roles)
		{
			if (role.getBotID() == ARole.UNINITIALIZED_BOTID)
			{
				log.fatal("Role [" + role.getType() + "] is missing its botId, it has not been assigned to a bot!!!");
			}
			role.doUpdate(currentFrame);
		}
		
		// Define state and reaction
		afterUpdate(currentFrame);
		

		// Timeout?
		if (getPlayState() == EPlayState.RUNNING)
		{
			final long now = System.nanoTime();
			if (now - timeStart > timeout)
			{
				timedOut(currentFrame);
			}
		}
		
		return state;
	}
	

	/**
	 * Called when the current play run longer then defined by {@link #setTimeout(long)} (or {@link #DEFAULT_TIMEOUT})
	 */
	protected void timedOut()
	{
	}
	
	protected void timedOut(AIInfoFrame currentFrame){
		timedOut();
	}
	

	/**
	 * This is only called once (if {@link #firstUpdate} == true). This is useful for initialization-purposes after the
	 * role-assignment. ({@link #beforeUpdate(AIInfoFrame)} is called afterwards, like every other
	 * {@link #update(AIInfoFrame)}!)
	 * 
	 * @see #update(AIInfoFrame)
	 * @param currentFrame
	 */
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
	}
	

	/**
	 * @see #update(AIInfoFrame)
	 * @param currentFrame
	 */
	protected void beforeUpdate(AIInfoFrame frame)
	{
	}
	

	/**
	 * @see #update(AIInfoFrame)
	 * @param currentFrame
	 */
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
	}
	

	/**
	 * @param timeoutS [s] The timeout for this play. Default value is {@link #DEFAULT_TIMEOUT}
	 */
	protected void setTimeout(long timeoutS)
	{
		this.timeout = TimeUnit.SECONDS.toNanos(timeoutS);
	}
	

	/**
	 * resets the timeStart to current system time ( timing for timeout will restart)
	 */
	protected void resetTimer()
	{
		this.timeStart = System.nanoTime();
	}
	

	// --------------------------------------------------------------
	// --- PlayableScore --------------------------------------------
	// --------------------------------------------------------------
	protected void addCriterion(ICriterion crit)
	{
		this.criteria.add(crit);
	}
	

	/**
	 * 
	 * Calculates the playable score of the given play based on its criteria.
	 * Is used to calculate scores for whole {@link PlayTuple PlayTuples}.
	 * @param currentFrame
	 * @return score value (range 0 to 100)
	 */
	public int calcPlayableScore(AIInfoFrame currentFrame)
	{
		float result = this.basicPlayableScore;
		// mulitply all criteria values
		for (ICriterion crit : this.criteria)
		{
			result *= crit.doCheckCriterion(currentFrame);
		}
		
		// 0 < result < 100 ?
		if (result > maxPlayableScore)
		{
			result = maxPlayableScore;
		} else if (result < minPlayableScore)
		{
			result = minPlayableScore;
		}
		
		return (int) result;
	}
	

	protected void setBasicPlayableScore(int score)
	{
		// make sure score is in permitted interval
		if (score > maxPlayableScore)
		{
			this.basicPlayableScore = maxPlayableScore;
		} else if (score < minPlayableScore)
		{
			this.basicPlayableScore = minPlayableScore;
		} else
		{
			this.basicPlayableScore = score;
		}
	}
	

	protected int getBasicPlayableScore()
	{
		return this.basicPlayableScore;
	}
	

	/**
	 * Dynamically loads the penalty factors for all criteria of
	 * that play from the tactics-xml file.
	 * If a criteria is not found in the xml, the penalty factor won't change.
	 * @author Malte
	 */
	public void loadPenaltyFactors()
	{
		PlayConfig play = AIConfig.getTactics().getPlay(type);
		for (ICriterion crit : criteria)
		{
			float penaltyFactor = play.getPenaltyFactor(crit.getType());
			if (penaltyFactor != Tactics.UNINITIALIZED_PENALTY_FACTOR)
			{
				crit.setPenaltyFactor(penaltyFactor);
			}
			
		}
	}
	

	// --------------------------------------------------------------
	// --- setter/getter --------------------------------------------
	// --------------------------------------------------------------
	public List<ARole> getRoles()
	{
		return roles;
	}
	

	public EPlay getType()
	{
		return type;
	}
	

	@Override
	public String toString()
	{
		return type.toString();
	}
	

	/**
	 * @return {@link #getRoles().size()}
	 */
	public int getRoleCount()
	{
		return getRoles().size();
	}
}
