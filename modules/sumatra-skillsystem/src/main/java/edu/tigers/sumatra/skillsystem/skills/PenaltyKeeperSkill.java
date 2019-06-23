/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.util.Optional;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.wp.data.DynamicPosition;


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
		IVector2 ballPos = getWorldFrame().getBall().getPos();
		if (dynShooterPos != null)
		{
			direction = Vector2.copy(ballPos.subtractNew(dynShooterPos));
		} else
		{
			// in case no enemy bot exists
			direction = Vector2.fromXY(1, 0);
		}
		
		double pufferToGoalPost = Geometry.getBotRadius() + 50;
		Optional<IVector2> possibleGoalLineIntersect = LineMath.intersectionPoint(
				Line.fromDirection(ballPos, direction),
				Line.fromDirection(Geometry.getGoalOur().getCenter(), AVector2.Y_AXIS));
		
		if (possibleGoalLineIntersect.isPresent())
		{
			IVector2 goalLineIntersect = possibleGoalLineIntersect.get();
			if (goalLineIntersect.y() < (Geometry.getGoalOur().getRightPost().y() + pufferToGoalPost))
			{
				goalLineIntersect = Geometry.getGoalOur().getRightPost()
						.subtractNew(AVector2.Y_AXIS.scaleToNew(-pufferToGoalPost));
			}
			if (goalLineIntersect.y() > (Geometry.getGoalOur().getLeftPost().y() - pufferToGoalPost))
			{
				goalLineIntersect = Geometry.getGoalOur().getLeftPost()
						.subtractNew(AVector2.Y_AXIS.scaleToNew(pufferToGoalPost));
			}
			goalLineIntersect = goalLineIntersect.addNew(AVector2.X_AXIS.scaleToNew(Geometry
					.getBotRadius() * (3.f / 4.f)));
			return goalLineIntersect;
		}
		
		log.warn("No line intersection found. Shooting line parallel to goal line?");
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
			// .turn(turnAngle).getOrientation();
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
