/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 5, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;


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
		super(ESkill.PENALTY_KEEPER);
		this.dynShooterPos = dynShooterPos;
		
		getMoveCon().getMoveConstraints().setAccMax(6);
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
		
		double pufferToGoalPost = Geometry.getBotRadius() + 50;
		try
		{
			IVector2 goalLineIntersect = GeoMath.intersectionPoint(getWorldFrame().getBall().getPos(), direction,
					Geometry.getGoalOur().getGoalCenter(), AVector2.Y_AXIS);
			if (goalLineIntersect.y() < (Geometry.getGoalOur().getGoalPostRight().y() + pufferToGoalPost))
			{
				goalLineIntersect = Geometry.getGoalOur().getGoalPostRight()
						.subtractNew(AVector2.Y_AXIS.scaleToNew(-pufferToGoalPost));
			}
			if (goalLineIntersect.y() > (Geometry.getGoalOur().getGoalPostLeft().y() - pufferToGoalPost))
			{
				goalLineIntersect = Geometry.getGoalOur().getGoalPostLeft()
						.subtractNew(AVector2.Y_AXIS.scaleToNew(pufferToGoalPost));
			}
			goalLineIntersect = goalLineIntersect.addNew(AVector2.X_AXIS.scaleToNew(Geometry
					.getBotRadius() * (3.f / 4.f)));
			return goalLineIntersect;
			
		} catch (MathException err)
		{
			log.warn("Math exception: shooting line parallel to goal line?", err);
		}
		return getPos();
	}
	
	
	@Override
	protected double calcDefendingOrientation()
	{
		double turnAngle = 0;
		if (moveSidewards)
		{
			turnAngle = AngleMath.PI_HALF;
		} else
		{
			// turnAngle = getWorldFrame().getBall().getPos().subtractNew(getPos())
			// .turn(turnAngle).getAngle();
			turnAngle = 0;
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
