/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s):
 * Gunther Berthold
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;


/**
 * This is a condition which can be used to check if a role/bot has reached its destination
 * and its view angle.
 * 
 * @author Oliver Steinbrecher
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class MovementCon extends ACondition
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final ViewAngleCondition			angleCon;
	private final DestinationCondition		destCon;
	private final DestinationFreeCondition	destFreeCon;
	/** save lookAtTarget, as it is not available anywhere else */
	private IVector2								lookAtTarget;
	
	private boolean								activateDestFreeCon	= false;
	
	/** role allowed to enter penalty area? Normally its not allowed! */
	private boolean								penaltyAreaAllowed	= false;
	private boolean								isBallObstacle			= true;
	private boolean								isBotsObstacle			= true;
	private boolean								isGoalPostObstacle	= true;
	
	
	private float									speed						= 0;
	/** velocity at the destination in m/s */
	private IVector2								velAtDestination		= new Vector2(0, 0);
	
	private List<IVector2>						intermediateStops		= new ArrayList<IVector2>();
	
	private List<TrackedBot>					ignoredBots				= new ArrayList<TrackedBot>();
	
	private boolean								isOptimizationWanted	= true;
	
	private boolean								forceNewSpline			= false;
	
	private boolean								shoot						= false;
	private boolean								pass						= false;
	
	
	/**
	 * @return the forceNewSpline
	 */
	public boolean isForceNewSpline()
	{
		return forceNewSpline;
	}
	
	
	/**
	 * @param forceNewSpline the forceNewSpline to set
	 */
	public void setForceNewSpline(boolean forceNewSpline)
	{
		this.forceNewSpline = forceNewSpline;
	}
	
	
	/**
	 * Modes for checking for free destination
	 */
	public enum EDestFreeMode
	{
		/**  */
		IGNORE,
		/**  */
		FREE_OF_TIGERS,
		/**  */
		FREE_OF_BOTS;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Creates a base MovementCon. Use the update methods to manipulate it.
	 * Note, that without updating anything, this condition is always true.
	 */
	public MovementCon()
	{
		super(ECondition.MOVEMENT);
		
		angleCon = new ViewAngleCondition();
		destCon = new DestinationCondition();
		destFreeCon = new DestinationFreeCondition();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public EConditionState doCheckCondition(WorldFrame worldFrame, BotID botID)
	{
		// do the check first
		final EConditionState angleConCheck = angleCon.checkCondition(worldFrame, botID);
		final EConditionState destConCheck = destCon.checkCondition(worldFrame, botID);
		final EConditionState destFreeConCheck = destFreeCon.checkCondition(worldFrame, botID);
		
		List<EConditionState> cons = new ArrayList<EConditionState>(3);
		cons.add(angleConCheck);
		cons.add(destConCheck);
		cons.add(destFreeConCheck);
		
		// after the check, the condition strings are current
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("destination:\n");
		strBuilder.append(destCon.getCondition());
		strBuilder.append("\nangle:\n");
		strBuilder.append(angleCon.getCondition());
		strBuilder.append("\ndestFree:\n");
		strBuilder.append(destFreeCon.getCondition());
		
		setCondition(strBuilder.toString());
		
		EConditionState finalState = EConditionState.FULFILLED;
		for (EConditionState state : cons)
		{
			if (state == EConditionState.BLOCKED)
			{
				// this has highest priority
				return EConditionState.BLOCKED;
			}
			if (state == EConditionState.PENDING)
			{
				// one is pending, so overall we are not done
				finalState = EConditionState.PENDING;
			}
		}
		
		return finalState;
	}
	
	
	@Override
	public void resetCache()
	{
		super.resetCache();
		destCon.resetCache();
		angleCon.resetCache();
		destFreeCon.resetCache();
	}
	
	
	@Override
	protected boolean compareContent(ACondition condition)
	{
		final MovementCon con = (MovementCon) condition;
		return destCon.compare(con.getDestCon()) && angleCon.compare(con.getAngleCon())
				&& destFreeCon.compare(con.getDestFreeCon());
	}
	
	
	/**
	 * @param destination
	 */
	public void updateDestination(IVector2 destination)
	{
		destCon.updateDestination(destination);
		if (destFreeCon.isActive() || activateDestFreeCon)
		{
			destFreeCon.updateDestination(destination);
			activateDestFreeCon = false;
		}
		resetCache();
	}
	
	
	/**
	 * @param angle [rad]
	 */
	public void updateTargetAngle(float angle)
	{
		angleCon.updateTargetAngle(angle);
		resetCache();
	}
	
	
	/**
	 * Active or deactivate destination free condition
	 * 
	 * @param destFreeMode
	 */
	public void updateDestinationFree(final EDestFreeMode destFreeMode)
	{
		switch (destFreeMode)
		{
			case FREE_OF_BOTS:
				destFreeCon.setConsiderFoeBots(true);
				if (destCon.isActive())
				{
					destFreeCon.updateDestination(destCon.getDestination());
				} else
				{
					activateDestFreeCon = true;
				}
				break;
			case FREE_OF_TIGERS:
				destFreeCon.setConsiderFoeBots(false);
				if (destCon.isActive())
				{
					destFreeCon.updateDestination(destCon.getDestination());
				} else
				{
					activateDestFreeCon = true;
				}
				break;
			case IGNORE:
			default:
				destFreeCon.updateDestination(null);
				break;
		}
		resetCache();
	}
	
	
	/**
	 * Updates the angle the bot should look at.
	 * 
	 * @param target
	 */
	public void updateLookAtTarget(IVector2 target)
	{
		if (!getDestCon().isActive())
		{
			throw new IllegalStateException(
					"updateLookAtTarget depends on an active destination condition. Call updateDestination before updateLookAtTarget!");
		}
		final IVector2 tmp = target.subtractNew(getDestCon().getDestination());
		if (!tmp.isZeroVector())
		{
			final float angle = tmp.getAngle();
			updateTargetAngle(angle);
			resetCache();
		}
		lookAtTarget = target;
		// lookAtTarget and destination are equal => can not calc angle.
		// as this happens quite frequently, just keep the old angle and do not complain
	}
	
	
	/**
	 * @return the angleCon
	 */
	public ViewAngleCondition getAngleCon()
	{
		return angleCon;
	}
	
	
	/**
	 * @return the destCon
	 */
	public DestinationCondition getDestCon()
	{
		return destCon;
	}
	
	
	/**
	 * @return the destFreeCon
	 */
	public DestinationFreeCondition getDestFreeCon()
	{
		return destFreeCon;
	}
	
	
	/**
	 * @return the lookAtTarget
	 */
	public final IVector2 getLookAtTarget()
	{
		return lookAtTarget;
	}
	
	
	/**
	 * @return the penaltyAreaCheck
	 */
	public final boolean isPenaltyAreaAllowed()
	{
		return penaltyAreaAllowed;
	}
	
	
	/**
	 * @param penaltyAreaAllowed the penaltyAreaCheck to set
	 */
	public final void setPenaltyAreaAllowed(boolean penaltyAreaAllowed)
	{
		this.penaltyAreaAllowed = penaltyAreaAllowed;
	}
	
	
	/**
	 * @return the isBallObstacle
	 */
	public final boolean isBallObstacle()
	{
		return isBallObstacle;
	}
	
	
	/**
	 * @param isBallObstacle the isBallObstacle to set
	 */
	public final void setBallObstacle(boolean isBallObstacle)
	{
		this.isBallObstacle = isBallObstacle;
	}
	
	
	/**
	 * @return the speed [m/s]
	 */
	public final float getSpeed()
	{
		return speed;
	}
	
	
	/**
	 * @param speed the speed to set [m/s]
	 */
	public final void setSpeed(float speed)
	{
		this.speed = speed;
	}
	
	
	/**
	 * @return the intermediateStops
	 */
	public List<IVector2> getIntermediateStops()
	{
		return intermediateStops;
	}
	
	
	/**
	 * @param intermediateStops the intermediateStops to set
	 */
	public void setIntermediateStops(List<IVector2> intermediateStops)
	{
		this.intermediateStops = intermediateStops;
	}
	
	
	/**
	 * @return the isOptimizationWanted
	 */
	public boolean isOptimizationWanted()
	{
		return isOptimizationWanted;
	}
	
	
	/**
	 * @param isOptimizationWanted the isOptimizationWanted to set
	 */
	public void setOptimizationWanted(boolean isOptimizationWanted)
	{
		this.isOptimizationWanted = isOptimizationWanted;
	}
	
	
	/**
	 * @return the isBotsObstacle
	 */
	public boolean isBotsObstacle()
	{
		return isBotsObstacle;
	}
	
	
	/**
	 * @param isBotsObstacle the isBotsObstacle to set
	 */
	public void setBotsObstacle(boolean isBotsObstacle)
	{
		this.isBotsObstacle = isBotsObstacle;
	}
	
	
	/**
	 * @return the ignoredBots
	 */
	public List<TrackedBot> getIgnoredBots()
	{
		return ignoredBots;
	}
	
	
	/**
	 * @param ignoredBots the ignoredBots to set
	 */
	public void setIgnoredBots(List<TrackedBot> ignoredBots)
	{
		this.ignoredBots = ignoredBots;
	}
	
	
	/**
	 * @return the isGoalPostObstacle
	 */
	public boolean isGoalPostObstacle()
	{
		return isGoalPostObstacle;
	}
	
	
	/**
	 * @param isGoalPostObstacle the isGoalPostObstacle to set
	 */
	public void setGoalPostObstacle(boolean isGoalPostObstacle)
	{
		this.isGoalPostObstacle = isGoalPostObstacle;
	}
	
	
	/**
	 * @return the velAtDestination
	 */
	public IVector2 getVelAtDestination()
	{
		return velAtDestination;
	}
	
	
	/**
	 * @param velAtDestination the velAtDestination to set
	 */
	public void setVelAtDestination(IVector2 velAtDestination)
	{
		this.velAtDestination = velAtDestination;
	}
	
	
	/**
	 * @return the shoot
	 */
	public final boolean isShoot()
	{
		return shoot;
	}
	
	
	/**
	 * @param shoot the shoot to set
	 */
	public final void setShoot(boolean shoot)
	{
		this.shoot = shoot;
	}
	
	
	/**
	 * @return the pass
	 */
	public final boolean isPass()
	{
		return pass;
	}
	
	
	/**
	 * @param pass the pass to set
	 */
	public final void setPass(boolean pass)
	{
		this.pass = pass;
	}
	
}
