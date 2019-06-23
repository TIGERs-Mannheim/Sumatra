/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 5, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PenaltyKeeperSkill extends BlockSkill
{
	private static final Logger	log				= Logger.getLogger(PenaltyKeeperSkill.class.getName());
	private DynamicPosition			dynShooterPos;
	
	@Configurable(comment = "Move sidewards, because it is faster?")
	private static boolean			moveSidewards	= false;
	
	
	/**
	 * @param dynShooterPos
	 */
	public PenaltyKeeperSkill(final DynamicPosition dynShooterPos)
	{
		super(ESkillName.PENALTY_KEEPER);
		this.dynShooterPos = dynShooterPos;
	}
	
	
	@Override
	protected IVector2 calcDefendingDestination()
	{
		dynShooterPos.update(getWorldFrame());
		
		Vector2 direction;
		if (dynShooterPos != null)
		{
			direction = new Vector2(getWorldFrame().getBall().getPos().subtractNew(dynShooterPos));
		} else
		{
			// in case no enemy bot exists
			direction = new Vector2(1, 0);
		}
		
		float pufferToGoalPost = AIConfig.getGeometry().getBotRadius() + 50;
		try
		{
			IVector2 goalLineIntersect = GeoMath.intersectionPoint(getWorldFrame().getBall().getPos(), direction, AIConfig
					.getGeometry().getGoalOur().getGoalCenter(), AVector2.Y_AXIS);
			if (goalLineIntersect.y() < (AIConfig.getGeometry().getGoalOur().getGoalPostRight().y() + pufferToGoalPost))
			{
				goalLineIntersect = AIConfig.getGeometry().getGoalOur().getGoalPostRight()
						.subtractNew(AVector2.Y_AXIS.scaleToNew(-pufferToGoalPost));
			}
			if (goalLineIntersect.y() > (AIConfig.getGeometry().getGoalOur().getGoalPostLeft().y() - pufferToGoalPost))
			{
				goalLineIntersect = AIConfig.getGeometry().getGoalOur().getGoalPostLeft()
						.subtractNew(AVector2.Y_AXIS.scaleToNew(pufferToGoalPost));
			}
			goalLineIntersect = goalLineIntersect.addNew(AVector2.X_AXIS.scaleToNew(AIConfig.getGeometry()
					.getBotRadius() * (3.f / 4.f)));
			return goalLineIntersect;
			
		} catch (MathException err)
		{
			log.warn("Math exception: shooting line parallel to goal line?", err);
		}
		return getPos();
	}
	
	
	@Override
	protected float calcDefendingOrientation()
	{
		float turnAngle = 0;
		if (moveSidewards)
		{
			turnAngle = AngleMath.PI_HALF;
		} else
		{
			turnAngle = getWorldFrame().getBall().getPos().subtractNew(getPos())
					.turn(turnAngle).getAngle();
		}
		return turnAngle;
	}
	
	
	/**
	 * @param position the dynamic position of the penalty shooter to set
	 */
	public void setShooterPos(final DynamicPosition position)
	{
		dynShooterPos = position;
	}
	
}
