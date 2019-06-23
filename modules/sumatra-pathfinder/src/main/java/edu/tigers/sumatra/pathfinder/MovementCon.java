/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * This is a condition which can be used to check if a role/bot has reached its destination
 * and its view angle.
 * 
 * @author Oliver Steinbrecher
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MovementCon
{
	@SuppressWarnings("unused")
	private static final Logger	log										= Logger.getLogger(MovementCon.class.getName());
	
	private MoveConstraints			moveConstraints						= new MoveConstraints();
	private IVector2					destination								= null;
	private Double						targetAngle								= null;
	private DynamicPosition			lookAtTarget							= null;
	private PathFinderPrioMap		prioMap									= null;
	private Double						minDistToBall							= null;
	
	private boolean					penaltyAreaAllowedOur				= false;
	private boolean					destinationOutsideFieldAllowed	= false;
	private boolean					penaltyAreaAllowedTheir				= false;
	private boolean					isBallObstacle							= true;
	private boolean					isTheirBotsObstacle					= true;
	private boolean					isOurBotsObstacle						= true;
	private boolean					isGoalPostObstacle					= false;
	private double kickSpeed = 0;
	private double dribblerSpeed = 0;
	private boolean					emergencyBreak							= false;
	private boolean					ignoreGameStateObstacles			= false;
	private Set<BotID>				ignoredBots								= new HashSet<>();
	
	
	private boolean					isInit									= false;
	
	
	/**
	 * Update dynamic targets/positions
	 * 
	 * @param swf current worldframe
	 * @param bot that is associated with this moveCon
	 */
	public final void update(final SimpleWorldFrame swf, final ITrackedBot bot)
	{
		if (destination == null)
		{
			destination = bot.getPos();
		}
		
		if (targetAngle == null)
		{
			targetAngle = bot.getOrientation();
		}
		
		if (lookAtTarget != null)
		{
			lookAtTarget.update(swf);
			targetAngle = lookAtTarget.subtractNew(destination).getAngle(0);
		}
		
		if (!isInit)
		{
			prioMap = PathFinderPrioMap.byBotId(bot.getTeamColor());
			MoveConstraints newMoveConstraints = new MoveConstraints(
					bot.getRobotInfo().getBotParams().getMovementLimits());
			newMoveConstraints.mergeWith(moveConstraints);
			moveConstraints = newMoveConstraints;
			isInit = true;
		}
	}
	
	
	/**
	 * @param destination to set
	 */
	public void updateDestination(final IVector2 destination)
	{
		if (destination == null)
		{
			assert false : "destination is null!!!";
			return;
		}
		if (SumatraModel.getInstance().isTestMode())
		{
			if (!penaltyAreaAllowedOur && Geometry.getPenaltyAreaOur().isPointInShape(destination, Geometry
					.getBotRadius()))
			{
				log.warn("Destination is inside PenaltyArea: " + destination, new Exception());
			}
			if (destination.equals(lookAtTarget))
			{
				log.warn("lookAtTarget is equal to destination: " + lookAtTarget, new Exception());
			}
			if (!destinationOutsideFieldAllowed && !Geometry.getFieldWReferee().isPointInShape(destination,
					Geometry.getBotRadius()))
			{
				log.warn("Destination is outside of field: " + destination, new Exception());
			}
		}
		
		this.destination = destination;
	}
	
	
	/**
	 * @param angle [rad]
	 */
	public void updateTargetAngle(final double angle)
	{
		targetAngle = angle;
	}
	
	
	/**
	 * Updates the angle the bot should look at.
	 * 
	 * @param lookAtTarget to set
	 */
	public void updateLookAtTarget(final DynamicPosition lookAtTarget)
	{
		this.lookAtTarget = lookAtTarget;
		if (SumatraModel.getInstance().isTestMode() && (destination != null) && destination.equals(lookAtTarget))
		{
			log.warn("lookAtTarget is equal to destination: " + lookAtTarget, new Exception());
		}
	}
	
	
	/**
	 * @param object to set
	 */
	public void updateLookAtTarget(final ITrackedObject object)
	{
		updateLookAtTarget(new DynamicPosition(object));
	}
	
	
	/**
	 * Updates the angle the bot should look at.
	 * 
	 * @param lookAtTarget to set
	 */
	public void updateLookAtTarget(final IVector2 lookAtTarget)
	{
		updateLookAtTarget(new DynamicPosition(lookAtTarget));
	}
	
	
	/**
	 * @return the penaltyAreaCheck
	 */
	public final boolean isPenaltyAreaForbiddenOur()
	{
		return !penaltyAreaAllowedOur;
	}
	
	
	public final void setDestinationOutsideFieldAllowed(final boolean destinationOutside)
	{
		destinationOutsideFieldAllowed = destinationOutside;
	}
	
	
	/**
	 * @param penaltyAreaAllowed the penaltyAreaCheck to set
	 */
	public final void setPenaltyAreaAllowedOur(final boolean penaltyAreaAllowed)
	{
		penaltyAreaAllowedOur = penaltyAreaAllowed;
	}
	
	
	/**
	 * @return the penaltyAreaCheck
	 */
	public final boolean isPenaltyAreaForbiddenTheir()
	{
		return !penaltyAreaAllowedTheir;
	}
	
	
	/**
	 * @param penaltyAreaAllowed the penaltyAreaCheck to set
	 */
	public final void setPenaltyAreaAllowedTheir(final boolean penaltyAreaAllowed)
	{
		penaltyAreaAllowedTheir = penaltyAreaAllowed;
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
	public final void setBallObstacle(final boolean isBallObstacle)
	{
		this.isBallObstacle = isBallObstacle;
	}
	
	
	/**
	 * @param isBotsObstacle the isBotsObstacle to set
	 */
	public void setBotsObstacle(final boolean isBotsObstacle)
	{
		isTheirBotsObstacle = isBotsObstacle;
		isOurBotsObstacle = isBotsObstacle;
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
	public void setGoalPostObstacle(final boolean isGoalPostObstacle)
	{
		this.isGoalPostObstacle = isGoalPostObstacle;
	}
	
	
	/**
	 * @return the destination
	 */
	public final IVector2 getDestination()
	{
		return destination;
	}
	
	
	/**
	 * @return the targetAngle
	 */
	public final Double getTargetAngle()
	{
		return targetAngle;
	}
	
	
	/**
	 * @return the prioMap
	 */
	public final PathFinderPrioMap getPrioMap()
	{
		return prioMap;
	}
	
	
	/**
	 * @param prioMap the prioMap to set
	 */
	public final void setPrioMap(final PathFinderPrioMap prioMap)
	{
		this.prioMap = prioMap;
	}
	
	
	/**
	 * @return the kickSpeed
	 */
	public boolean isArmChip()
	{
		return kickSpeed > 0;
	}
	
	
	/**
	 * @param armChip the kickSpeed to set
	 */
	public void setArmChip(final boolean armChip)
	{
		this.kickSpeed = armChip ? 8 : 0;
	}
	
	
	/**
	 * @param kickSpeed
	 */
	public void setArmChip(final double kickSpeed)
	{
		this.kickSpeed = kickSpeed;
	}
	
	
	/**
	 * @return the kick speed for chip kicks
	 */
	public double getKickSpeed()
	{
		return kickSpeed;
	}
	
	
	/**
	 * @return the dribbler speed for chip kicks
	 */
	public double getDribblerSpeed()
	{
		return dribblerSpeed;
	}
	
	
	/**
	 * @param dribblerSpeed
	 */
	public void setDribblerSpeed(final double dribblerSpeed)
	{
		this.dribblerSpeed = dribblerSpeed;
	}
	
	
	/**
	 * @return the isTheirBotsObstacle
	 */
	public boolean isTheirBotsObstacle()
	{
		return isTheirBotsObstacle;
	}
	
	
	/**
	 * @param isBotsObstacle the isBotsObstacle to set
	 */
	public void setTheirBotsObstacle(final boolean isBotsObstacle)
	{
		isTheirBotsObstacle = isBotsObstacle;
	}
	
	
	/**
	 * @return the isOurBotsObstacle
	 */
	public boolean isOurBotsObstacle()
	{
		return isOurBotsObstacle;
	}
	
	
	/**
	 * @param isBotsObstacle the isBotsObstacle to set
	 */
	public void setOurBotsObstacle(final boolean isBotsObstacle)
	{
		isOurBotsObstacle = isBotsObstacle;
	}
	
	
	/**
	 * @return the moveConstraints
	 */
	public MoveConstraints getMoveConstraints()
	{
		return moveConstraints;
	}
	
	
	/**
	 * @return the emergencyBreak
	 */
	public boolean isEmergencyBreak()
	{
		return emergencyBreak;
	}
	
	
	/**
	 * @param emergencyBreak the emergencyBreak to set
	 */
	public void setEmergencyBreak(final boolean emergencyBreak)
	{
		this.emergencyBreak = emergencyBreak;
	}
	
	
	public boolean isFastPosMode()
	{
		return moveConstraints.isFastMove();
	}
	
	
	public void setFastPosMode(final boolean fastPosMode)
	{
		moveConstraints.setFastMove(fastPosMode);
	}
	
	
	public boolean isIgnoreGameStateObstacles()
	{
		return ignoreGameStateObstacles;
	}
	
	
	public void setIgnoreGameStateObstacles(final boolean ignoreGameStateObstacles)
	{
		this.ignoreGameStateObstacles = ignoreGameStateObstacles;
	}
	
	
	public Optional<Double> getMinDistToBall()
	{
		return Optional.ofNullable(minDistToBall);
	}
	
	
	public void setMinDistToBall(final double minDistToBall)
	{
		this.minDistToBall = minDistToBall;
	}
	
	
	public Set<BotID> getIgnoredBots()
	{
		return ignoredBots;
	}
	
	
	public void setIgnoredBots(final Set<BotID> ignoredBots)
	{
		this.ignoredBots = ignoredBots;
	}
}
