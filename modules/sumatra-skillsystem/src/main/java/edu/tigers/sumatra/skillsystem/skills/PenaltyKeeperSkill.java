/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.Random;


/**
 * Keeper skill for penalty kicks.
 */
@Log4j2
@NoArgsConstructor
public class PenaltyKeeperSkill extends AMoveSkill
{
	@Configurable(comment = "Selects how the keeper should select its target during penalty", defValue = "LEGACY")
	private static ETargetSelectionMode penaltyKeeperMode = ETargetSelectionMode.LEGACY;

	@Configurable(comment = "How much space should there be between the both positions and the goal posts. (Only with PERIODIC/RANDOM", defValue = "90.0")
	private static double distanceToGoalPosts = 90;

	@Configurable(comment = "How long should the RANDOM keeper wait until he chooses a new side [s]", defValue = "0.15")
	private static double randomTimeDelay = 0.15;

	private final TimestampTimer timer = new TimestampTimer(randomTimeDelay);

	@Setter
	private DynamicPosition shooterPos = new DynamicPosition(BallID.instance());

	private IVector2 dest = null;
	private MoveConstraints moveConstraints;


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		moveConstraints = defaultMoveConstraints();
		moveConstraints.setAccMax(6);
	}


	@Override
	protected void doUpdate()
	{
		IVector2 nextDest = calcDefendingDestination();
		double targetAngle = 0;
		if (nextDest != null)
		{
			setTargetPose(nextDest, targetAngle, moveConstraints);
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
		shooterPos = shooterPos.update(getWorldFrame());

		IVector2 ballPos = getWorldFrame().getBall().getPos();
		var direction = Vector2.copy(ballPos.subtractNew(shooterPos.getPos()));

		double pufferToGoalPost = Geometry.getBotRadius() + 50;
		var possibleGoalLineIntersect = Geometry.getGoalOur().getLine()
				.intersect(Lines.halfLineFromDirection(ballPos, direction)).asOptional();

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
}
