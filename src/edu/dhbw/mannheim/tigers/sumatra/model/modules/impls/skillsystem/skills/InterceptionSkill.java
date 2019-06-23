/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.05.2014
 * Author(s): <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;


import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * tries to intercept ball
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public class InterceptionSkill extends BlockSkill
{
	private IVector2 nearestEnemyBotPos = null;
	
	
	/**
	 */
	public InterceptionSkill()
	{
		super(ESkillName.INTERCEPTION);
	}
	
	
	@Override
	protected IVector2 calcDefendingDestination()
	{
		if (nearestEnemyBotPos == null)
		{
			nearestEnemyBotPos = new Vector2(0, 0);
		}
		
		IVector2 destination;
		boolean overAccelerationNecessary = false;
		IVector2 intersectPoint;
		
		IVector2 ballPos = getWorldFrame().getBall().getPos();
		IVector2 botToBall = ballPos.subtractNew(nearestEnemyBotPos).normalizeNew();
		
		intersectPoint = ballPos.addNew(botToBall.multiplyNew(AIConfig.getGeometry().getBotToBallDistanceStop()
			+ AIConfig.getGeometry().getBotRadius() + 50));
			
		destination = intersectPoint; // GeoMath.leadPointOnLine(getPos(), getWorldFrame().getBall().getPos(),
										// intersectPoint);
		
		float distance = GeoMath.distancePP(destination, getPos());
		
		// if we are already blocking the ball we can do the fine tuning: position on the exact shooting line and
		if (distance < (AIConfig.getGeometry().getBotRadius() / 2))
		{
			overAccelerationNecessary = false;
		}
		
		if (overAccelerationNecessary)
		{
			destination = getAccelerationTarget(getWorldFrame().getTiger(getBot().getBotID()), destination);
		}
		if (isMoveTargetValid(destination))
		{
			return destination;
		}
		return getPos();
	}
	
	
	private boolean isMoveTargetValid(final IVector2 destination)
	{
		float marginPenalty = 350f;
		if (!AIConfig.getGeometry().getField().isPointInShape(destination))
		{
			return false;
		}
		if (AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(destination, marginPenalty))
		{
			return false;
		}
		if (AIConfig.getGeometry().getPenaltyAreaTheir().isPointInShape(destination, marginPenalty))
		{
			return false;
		}
		return true;
	}
	
	
	/**
	 * @param nearestEnemyBot
	 */
	public void setNearestEnemyBotPos(final IVector2 nearestEnemyBot)
	{
		nearestEnemyBotPos = nearestEnemyBot;
	}
}
