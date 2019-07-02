/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.util.Optional;
import java.util.Random;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PenaltyKeeperSkill extends AMoveSkill
{
	private static final Logger log = Logger.getLogger(PenaltyKeeperSkill.class.getName());
	
	@Configurable(comment = "Move sidewards, because it is faster?", defValue = "false")
	private static boolean moveSidewards = false;
	
	@Configurable(comment = "Selects how the keeper should select its target during penalty", defValue = "LEGACY")
	private static ETargetSelectionMode penaltyKeeperMode = ETargetSelectionMode.LEGACY;
	
	@Configurable(comment = "How much space should there be between the both positions and the goal posts. (Only with PERIODIC/RANDOM", defValue = "90.0")
	private static double distanceToGoalPosts = 90;
	
	@Configurable(comment = "How long should the RANDOM keeper wait until he chooses a new side [s]", defValue = "0.15")
	private static double randomTimeDelay = 0.15;
	
	private DynamicPosition dynShooterPos;
	private IVector2 dest = null;
	
	private TimestampTimer timer = new TimestampTimer(1);
	
	/**
	 * @param dynShooterPos
	 */
	public PenaltyKeeperSkill(final DynamicPosition dynShooterPos)
	{
		super(ESkill.PENALTY_KEEPER);
		this.dynShooterPos = dynShooterPos;
		
		getMoveCon().getMoveConstraints().setAccMax(6);
		timer.setDuration(randomTimeDelay);
	}
	
	@Override
	protected void beforeStateUpdate()
	{
		IVector2 nextDest = calcDefendingDestination();
		double targetAngle = calcDefendingOrientation();
		if (nextDest != null)
		{
			setTargetPose(nextDest, targetAngle, getMoveCon().getMoveConstraints());
			dest = nextDest;
		}
		
	}
	
	
	private IVector2 calcDefendingDestination()
	{
		if (penaltyKeeperMode == ETargetSelectionMode.LEGACY)
		{
			return calcLegacyPosition();
		} else if (penaltyKeeperMode == ETargetSelectionMode.PERIODIC)
		{
			return calcPeriodicPosition();
		} else if (penaltyKeeperMode == ETargetSelectionMode.RANDOM)
		{
			return calcRandomPosition();
		}
		return null;
	}
	
	
	private IVector2 calcPeriodicPosition()
	{
		IVector2 posLeft = Geometry.getGoalOur().getLeftPost().subtractNew(Vector2f.fromXY(0, distanceToGoalPosts));
		IVector2 posRight = Geometry.getGoalOur().getRightPost().addNew(Vector2f.fromXY(0, distanceToGoalPosts));
		
		if (getTBot().getPos().distanceTo(posLeft) < Geometry.getBotRadius())
		{
			return posRight;
		} else if (getTBot().getPos().distanceTo(posRight) < Geometry.getBotRadius())
		{
			return posLeft;
		} else if (dest == null)
		{
			return posRight;
		}
		return null;
	}
	
	
	private IVector2 calcRandomPosition()
	{
		IVector2 posLeft = Geometry.getGoalOur().getLeftPost().subtractNew(Vector2f.fromXY(0, distanceToGoalPosts));
		IVector2 posRight = Geometry.getGoalOur().getRightPost().addNew(Vector2f.fromXY(0, distanceToGoalPosts));
		
		long ts = getWorldFrame().getTimestamp();
		timer.update(ts);
		if (timer.isTimeUp(ts))
		{
			timer.reset();
			timer.setDuration(randomTimeDelay);
			double distToLeft = getTBot().getPos().distanceTo(posLeft);
			double pLeft = distToLeft / posLeft.distanceTo(posRight);
			Random rand = new Random(ts);
			if (rand.nextDouble() <= pLeft)
			{
				return posLeft;
			} else
			{
				return posRight;
			}
		}
		
		return null;
	}
	
	
	private IVector2 calcLegacyPosition()
	{
		dynShooterPos.update(getWorldFrame());
		
		Vector2 direction;
		IVector2 ballPos = getWorldFrame().getBall().getPos();
		if (dynShooterPos != null)
		{
			direction = Vector2.copy(ballPos.subtractNew(dynShooterPos.getPos()));
		} else
		{
			// in case no enemy bot exists
			direction = Vector2.fromXY(1, 0);
		}
		
		double pufferToGoalPost = Geometry.getBotRadius() + 50;
		Optional<IVector2> possibleGoalLineIntersect = LineMath.intersectionPoint(
				Line.fromDirection(ballPos, direction),
				Line.fromDirection(Geometry.getGoalOur().getCenter(), Vector2f.Y_AXIS));
		
		if (possibleGoalLineIntersect.isPresent())
		{
			IVector2 goalLineIntersect = possibleGoalLineIntersect.get();
			if (goalLineIntersect.y() < (Geometry.getGoalOur().getRightPost().y() + pufferToGoalPost))
			{
				goalLineIntersect = Geometry.getGoalOur().getRightPost()
						.subtractNew(Vector2f.Y_AXIS.scaleToNew(-pufferToGoalPost));
			}
			if (goalLineIntersect.y() > (Geometry.getGoalOur().getLeftPost().y() - pufferToGoalPost))
			{
				goalLineIntersect = Geometry.getGoalOur().getLeftPost()
						.subtractNew(Vector2f.Y_AXIS.scaleToNew(pufferToGoalPost));
			}
			goalLineIntersect = goalLineIntersect.addNew(Vector2f.X_AXIS.scaleToNew(Geometry
					.getBotRadius() * (3.f / 4.f)));
			return goalLineIntersect;
		}
		
		log.warn("No line intersection found. Shooting line parallel to goal line?");
		return getPos();
	}
	
	
	private enum ETargetSelectionMode
	{
		LEGACY,
		PERIODIC,
		RANDOM
	}
	
	
	private double calcDefendingOrientation()
	{
		double turnAngle;
		if (moveSidewards)
		{
			turnAngle = AngleMath.PI_HALF;
		} else
		{
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
