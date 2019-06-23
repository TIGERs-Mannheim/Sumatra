/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s):
 * Gunther Berthold
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem;

import edu.tigers.sumatra.ai.sisyphus.finder.traj.PathFinderPrioMap;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;
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
	private IVector2				destination								= null;
	private Double					targetAngle								= null;
	private DynamicPosition		lookAtTarget							= null;
	
	private boolean				penaltyAreaAllowedOur				= false;
	private boolean				destinationOutsideFieldAllowed	= false;
	private boolean				penaltyAreaAllowedTheir				= false;
	private boolean				isBallObstacle							= true;
	private boolean				isTheirBotsObstacle					= true;
	private boolean				isOurBotsObstacle						= true;
	private boolean				isGoalPostObstacle					= false;
	private boolean				refereeStop								= false;
	private boolean				armChip									= false;
	private boolean				emergencyBreak							= false;
	
	private MoveConstraints		moveConstraints						= new MoveConstraints();
	
	private PathFinderPrioMap	prioMap									= null;
	
	private boolean				isInit									= false;
	
	private boolean				optimizeOrientation					= false;
	
	
	/**
	 * Update dynamic targets/positions in this {@link MovementCon}
	 * 
	 * @param swf
	 * @param bot
	 */
	public final void update(final SimpleWorldFrame swf, final ITrackedBot bot)
	{
		if (destination == null)
		{
			destination = bot.getPos();
		}
		
		if (targetAngle == null)
		{
			targetAngle = bot.getAngle();
		}
		
		if (lookAtTarget != null)
		{
			lookAtTarget.update(swf);
			targetAngle = lookAtTarget.subtractNew(destination).getAngle();
		}
		
		if (!isInit)
		{
			prioMap = PathFinderPrioMap.byBotId(bot.getTeamColor());
			moveConstraints = new MoveConstraints(bot.getBot().getMoveConstraints());
			isInit = true;
		}
	}
	
	
	/**
	 * @param destination
	 */
	public void updateDestination(final IVector2 destination)
	{
		if (destination == null)
		{
			assert false : "destination is null!!!";
			return;
		}
		// assert penaltyAreaAllowedOur
		// || !Geometry.getPenaltyAreaOur().isPointInShape(destination, Geometry
		// .getBotRadius()) : "Destination is inside PenaltyArea: " + destination;
		assert (lookAtTarget == null)
				|| (GeoMath.distancePP(lookAtTarget, destination) > 1e-4f) : "lookAtTarget is equal to destination: "
						+ lookAtTarget;
		if (!destinationOutsideFieldAllowed)
		{
			assert Geometry.getFieldWReferee().isPointInShape(destination,
					Geometry.getBotRadius()) : "Destination is outside of field!!";
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
	 * @param lookAtTarget
	 */
	public void updateLookAtTarget(final DynamicPosition lookAtTarget)
	{
		this.lookAtTarget = lookAtTarget;
		assert (destination == null)
				|| (GeoMath.distancePP(lookAtTarget, destination) > 1e-4f) : "lookAtTarget is equal to destination: "
						+ lookAtTarget;
	}
	
	
	/**
	 * @param object
	 */
	public void updateLookAtTarget(final ITrackedObject object)
	{
		updateLookAtTarget(new DynamicPosition(object));
	}
	
	
	/**
	 * Updates the angle the bot should look at.
	 * 
	 * @param lookAtTarget
	 */
	public void updateLookAtTarget(final IVector2 lookAtTarget)
	{
		updateLookAtTarget(new DynamicPosition(lookAtTarget));
	}
	
	
	/**
	 * @return the penaltyAreaCheck
	 */
	public final boolean isPenaltyAreaAllowedOur()
	{
		return penaltyAreaAllowedOur;
	}
	
	
	/**
	 * @return destinationOutsideFieldAllowed
	 */
	public final boolean isDestinationOutsideFieldAllowed()
	{
		return destinationOutsideFieldAllowed;
	}
	
	
	/**
	 * @param destinationOutside
	 */
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
	public final boolean isPenaltyAreaAllowedTheir()
	{
		return penaltyAreaAllowedTheir;
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
	 * @param isBotsObstacle the isBotsObstacle to set
	 */
	public void setTheirBotsObstacle(final boolean isBotsObstacle)
	{
		isTheirBotsObstacle = isBotsObstacle;
	}
	
	
	/**
	 * @param isBotsObstacle the isBotsObstacle to set
	 */
	public void setOurBotsObstacle(final boolean isBotsObstacle)
	{
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
	 * @return the refereeStop
	 */
	public boolean isRefereeStop()
	{
		return refereeStop;
	}
	
	
	/**
	 * @param refereeStop the refereeStop to set
	 */
	public void setRefereeStop(final boolean refereeStop)
	{
		this.refereeStop = refereeStop;
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
	 * @return the armChip
	 */
	public boolean isArmChip()
	{
		return armChip;
	}
	
	
	/**
	 * @param armChip the armChip to set
	 */
	public void setArmChip(final boolean armChip)
	{
		this.armChip = armChip;
	}
	
	
	/**
	 * @return the isTheirBotsObstacle
	 */
	public boolean isTheirBotsObstacle()
	{
		return isTheirBotsObstacle;
	}
	
	
	/**
	 * @return the isOurBotsObstacle
	 */
	public boolean isOurBotsObstacle()
	{
		return isOurBotsObstacle;
	}
	
	
	/**
	 * @return the moveConstraints
	 */
	public MoveConstraints getMoveConstraints()
	{
		return moveConstraints;
	}
	
	
	/**
	 * @param moveConstraints the moveConstraints to set
	 */
	public void setMoveConstraints(final MoveConstraints moveConstraints)
	{
		this.moveConstraints = moveConstraints;
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
	
	
	/**
	 * @return the optimizeOrientation
	 */
	public boolean isOptimizeOrientation()
	{
		return optimizeOrientation;
	}
	
	
	/**
	 * @param optimizeOrientation the optimizeOrientation to set
	 */
	public void setOptimizeOrientation(final boolean optimizeOrientation)
	{
		this.optimizeOrientation = optimizeOrientation;
	}
}
