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

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
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
	
	private static final Logger			log						= Logger.getLogger(MovementCon.class.getName());
	private final ViewAngleCondition		angleCon;
	private final DestinationCondition	destCon;
	
	/** role allowed to enter penalty area? Normally its not allowed! */
	private boolean							penaltyAreaAllowed	= false;
	private boolean							isBallObstacle			= true;
	private boolean							isBotsObstacle			= true;
	private boolean							isGoalPostObstacle	= true;
	private boolean							armKicker				= false;
	
	
	private float								speed						= 0;
	/** velocity at the destination in m/s */
	private IVector2							velAtDestination		= new Vector2(0, 0);
	
	
	private List<IVector2>					intermediateStops		= new ArrayList<IVector2>();
	
	private boolean							isOptimizationWanted	= true;
	
	private boolean							forceNewSpline			= false;
	
	
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
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public EConditionState doCheckCondition(SimpleWorldFrame worldFrame, BotID botId)
	{
		update(worldFrame, botId);
		
		// do the check first
		final EConditionState angleConCheck = angleCon.checkCondition(worldFrame, botId);
		final EConditionState destConCheck = destCon.checkCondition(worldFrame, botId);
		
		List<EConditionState> cons = new ArrayList<EConditionState>(3);
		cons.add(angleConCheck);
		cons.add(destConCheck);
		
		// after the check, the condition strings are current
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("destination:\n");
		strBuilder.append(destCon.getCondition());
		strBuilder.append("\nangle:\n");
		strBuilder.append(angleCon.getCondition());
		
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
	}
	
	
	@Override
	protected boolean compareContent(ACondition condition)
	{
		final MovementCon con = (MovementCon) condition;
		return destCon.compare(con.getDestCon()) && angleCon.compare(con.getAngleCon());
	}
	
	
	/**
	 * Update dynamic targets/positions in this {@link MovementCon}
	 * 
	 * @param swf
	 */
	@Override
	public final void update(SimpleWorldFrame swf, BotID botId)
	{
		super.update(swf, botId);
		destCon.update(swf, botId);
		angleCon.update(swf, botId);
		angleCon.update(destCon.getDestination(), swf);
		resetCache();
		checkConstraints(swf, botId);
	}
	
	
	/**
	 * Check if all necessary constraints are fulfilled.
	 * - not in penalty area
	 * - distance to ball on referee cmd
	 * - not outside field
	 * - not equal to other bot position
	 * @param wframe
	 * @param botId
	 */
	public final void checkConstraints(SimpleWorldFrame wframe, BotID botId)
	{
		// 1. Penalty Area
		PenaltyArea penArea = AIConfig.getGeometry().getPenaltyAreaOur();
		if (!penaltyAreaAllowed && penArea.isPointInShape(destCon.getDestination()))
		{
			log.warn("Destination is inside PenaltyArea. Changing it to nearest Point outside");
			IVector2 botPos = wframe.getBot(botId).getPos();
			// behind penArea?
			if (botPos.x() <= (-AIConfig.getGeometry().getFieldLength() / 2))
			{
				// this will result in an acceptable new destination
				botPos = Vector2.ZERO_VECTOR;
			}
			IVector2 nearestPointOutside = penArea.nearestPointOutside(destCon.getDestination(), botPos);
			destCon.updateDestination(nearestPointOutside);
		}
		
		// 2. STOP radius
		// switch (frame.getTacticalField().getGameState())
		// {
		// case CORNER_KICK_THEY:
		// case GOAL_KICK_THEY:
		// case PREPARE_KICKOFF_THEY:
		// case PREPARE_PENALTY_THEY:
		// case STOPPED:
		// case THROW_IN_THEY:
		// float dist = GeoMath.distancePP(destCon.getDestination(), frame.getWorldFrame().getBall().getPos());
		// float should = AIConfig.getGeometry().getBotToBallDistanceStop() + AIConfig.getGeometry().getBotRadius();
		// if (dist < should)
		// {
		// log.warn("Destination is too near to ball: " + dist + "<" + should);
		// IVector2 newDest = GeoMath.stepAlongLine(frame.getWorldFrame().getBall().getPos(),
		// destCon.getDestination(), should);
		// destCon.updateDestination(newDest);
		// }
		// break;
		// default:
		// // nothing to check
		// }
		
		// 3. outside field
		if (!AIConfig.getGeometry().getFieldWBorders().isPointInShape(destCon.getDestination()))
		{
			log.warn("Destination is outside of field!");
			IVector2 newDest = AIConfig.getGeometry().getFieldWBorders().nearestPointInside(destCon.getDestination());
			destCon.updateDestination(newDest);
		}
		
		// 4. equal to others own bot pos
		// float speedTolerance = 0.2f;
		// for (TrackedTigerBot bot : wframe.getBots().values())
		// {
		// if (bot.getId().getTeamColor() != botId.getTeamColor())
		// {
		// continue;
		// }
		// if (!bot.getId().equals(botId) && (bot.getVel().getLength2() < speedTolerance))
		// {
		// float tolerance = (AIConfig.getGeometry().getBotRadius() * 2) - 20;
		// if (bot.getPos().equals(destCon.getDestination(), tolerance))
		// {
		// log.warn("Destination equals position of bot " + bot.getId().getNumber()
		// + " which is stopped or moves slowly!");
		// IVector2 newDest = GeoMath.stepAlongLine(bot.getPos(), destCon.getDestination(), tolerance + 20);
		// destCon.updateDestination(newDest);
		// }
		// }
		// }
	}
	
	
	/**
	 * @param destination
	 */
	public void updateDestination(IVector2 destination)
	{
		IVector2 dest = destination;
		destCon.updateDestination(dest);
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
	 * Updates the angle the bot should look at.
	 * 
	 * @param lookAtTarget
	 */
	public void updateLookAtTarget(DynamicPosition lookAtTarget)
	{
		angleCon.updateLookAtTarget(lookAtTarget);
	}
	
	
	/**
	 * 
	 * @param object
	 */
	public void updateLookAtTarget(ATrackedObject object)
	{
		updateLookAtTarget(new DynamicPosition(object));
	}
	
	
	/**
	 * Updates the angle the bot should look at.
	 * 
	 * @param lookAtTarget
	 */
	public void updateLookAtTarget(IVector2 lookAtTarget)
	{
		angleCon.updateLookAtTarget(new DynamicPosition(lookAtTarget));
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
	 * 
	 * @param armKicker should bot arm kicker.
	 */
	public void setArmKicker(boolean armKicker)
	{
		this.armKicker = armKicker;
	}
	
	
	/**
	 * @return is kicker armed.
	 */
	public boolean isKickerArmed()
	{
		return armKicker;
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
}
