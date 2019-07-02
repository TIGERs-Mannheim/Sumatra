/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ProtectBallSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public class DelayedAttackRole extends ARole
{
	@Configurable(comment = "Use old waiting behaviour", defValue = "false")
	private static boolean useOld = false;
	
	@Configurable(comment = "Distance from Ball when circling it", defValue = "100.0")
	private static double ballDist = 100.0;
	
	
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
		@Override
		public void doEntryActions()
		{
			setNewSkill(AMoveToSkill.createMoveToSkill());
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getPos() != null && !useOld)
			{
				IVector2 closeToBall = calcSafeDistPos();
				getCurrentSkill().getMoveCon().updateDestination(closeToBall);
				if (VectorMath.distancePP(getPos(), calcSafeDistPos()) < 50)
				{
					triggerEvent(EEvent.CLOSE_TO_BALL);
				}
			} else if (getPos() != null)
			{
				IVector2 movePos = calcSafeDistPos();
				getCurrentSkill().getMoveCon().updateDestination(movePos);
			}
			getCurrentSkill().getMoveCon().updateLookAtTarget(getBall());
		}
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
	
	private class MoveAroundBall extends AState
	{
		private DynamicPosition foes;
		
		
		private ProtectBallSkill protectBallSkill;


		@Override
		public void doEntryActions()
		{
			foes = new DynamicPosition(getPos());
			protectBallSkill = new ProtectBallSkill(foes);
			protectBallSkill.setFinalDist2Ball(ballDist);
			setNewSkill(protectBallSkill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getPos() != null)
			{
				ICircle movement = Circle.createCircle(getBall().getPos(), Geometry.getBotRadius() * 2 + ballDist);
				IVector2 dir = Vector2.fromPoints(getBall().getPos(), getPos()).turn(45).scaleTo(500);
				ILine dirLine = Line.fromDirection(getBall().getPos(), dir);
				IVector2 newFoe = dirLine.getEnd();
				
				protectBallSkill.setFinalDist2Ball(ballDist);
				boolean circlingPossible = Geometry.getField().isCircleInShape(movement)
						&& !Geometry.getPenaltyAreaOur().withMargin(movement.radius())
								.isPointInShapeOrBehind(movement.center())
						&& !Geometry.getPenaltyAreaTheir().withMargin(movement.radius())
								.isPointInShapeOrBehind(movement.center());
				
				if (circlingPossible)
				{
					foes.setPos(newFoe);
				}
			}
		}
	}
}
