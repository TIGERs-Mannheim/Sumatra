/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ProtectBallSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public class DelayedAttackRole extends ARole
{
	@Configurable(comment = "Use old waiting behaviour", defValue = "true")
	private static boolean useOld = true;

	@Configurable(comment = "Distance from Ball when circling it", defValue = "200")
	private static double ballDist = 200;


	public DelayedAttackRole()
	{
		super(ERole.DELAYED_ATTACK);
		MoveToBall moveToBall = new MoveToBall();
		MoveAroundBall moveAroundBall = new MoveAroundBall();
		setInitialState(moveToBall);
		addTransition(moveToBall, EEvent.CLOSE_TO_BALL, moveAroundBall);
	}


	private enum EEvent implements IEvent
	{
		CLOSE_TO_BALL
	}

	private class MoveToBall extends AState
	{
		private MoveToSkill skill;


		@Override
		public void doEntryActions()
		{
			skill = MoveToSkill.createMoveToSkill();
			setNewSkill(skill);
			skill.getMoveCon().physicalObstaclesOnly();
		}


		@Override
		public void doUpdate()
		{
			if (getPos() != null && !useOld)
			{
				IVector2 closeToBall = calcSafeDistPos();
				skill.updateDestination(closeToBall);
				if (VectorMath.distancePP(getPos(), calcSafeDistPos()) < 50)
				{
					triggerEvent(EEvent.CLOSE_TO_BALL);
				}
			} else if (getPos() != null)
			{
				IVector2 movePos = calcSafeDistPos();
				skill.updateDestination(movePos);
			}
			skill.updateLookAtTarget(getBall());
		}


		private IVector2 calcSafeDistPos()
		{
			IVector2 dir = Geometry.getGoalTheir().getCenter().subtractNew(getBall().getPos()).scaleToNew(-250);
			if (dir.isZeroVector())
			{
				dir = Vector2.fromXY(-300, 0);
			}
			return getBall().getPos().addNew(dir);
		}
	}

	private class MoveAroundBall extends RoleState<ProtectBallSkill>
	{

		MoveAroundBall()
		{
			super(ProtectBallSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			ICircle movement = Circle.createCircle(getBall().getPos(), Geometry.getBotRadius() * 2 + ballDist);
			IVector2 dir = Vector2.fromPoints(getBall().getPos(), getPos()).turn(45).scaleTo(500);
			IVector2 newOpponent = getBall().getPos().addNew(dir);

			skill.setFinalDist2Ball(ballDist);
			boolean circlingPossible = Geometry.getField().isCircleInShape(movement)
					&& !Geometry.getPenaltyAreaOur().withMargin(movement.radius())
					.isPointInShapeOrBehind(movement.center())
					&& !Geometry.getPenaltyAreaTheir().withMargin(movement.radius())
					.isPointInShapeOrBehind(movement.center());

			if (circlingPossible)
			{
				skill.setProtectionTarget(new DynamicPosition(newOpponent));
			} else
			{
				skill.setProtectionTarget(new DynamicPosition(Geometry.getGoalTheir().getCenter()));
			}
		}
	}
}
