package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.BallStabilizer;
import edu.tigers.sumatra.skillsystem.skills.util.InterceptorUtil;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public abstract class ABallArrivalSkill extends AMoveSkill
{
	@Configurable(defValue = "200.0", comment = "Distance bot pos to ball [mm] to fix the target orientation of the bot.")
	private static double distThresholdToFixOrientation = 200.0;

	@Configurable(defValue = "1000.0", comment = "If the receiving pos is further than this away from the rolling travel line, the ball can not reach the receiving pos")
	private static double maxDistanceToReceivingPosition = 1000.0;

	protected final DynamicPosition receivingPosition;

	private final Hysteresis ballSpeedHysteresis = new Hysteresis(0.3, 0.6).initiallyInUpperState();
	private final PositionValidator positionValidator = new PositionValidator();
	private ETeam consideredPenAreas = ETeam.BOTH;
	protected final BallStabilizer ballStabilizer = new BallStabilizer();
	private double marginToTheirPenArea = 0;


	protected ABallArrivalSkill(final ESkill skillName, final DynamicPosition receivingPosition)
	{
		super(skillName);
		this.receivingPosition = receivingPosition;
	}


	public void setMarginToTheirPenArea(final double marginToTheirPenArea)
	{
		this.marginToTheirPenArea = marginToTheirPenArea;
	}


	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		receivingPosition.update(getWorldFrame());
		ballSpeedHysteresis.update(getBall().getVel().getLength2());
		ballStabilizer.update(getBall(), getTBot());
		positionValidator.update(getWorldFrame(), getMoveCon());
		positionValidator.getMarginToPenArea().put(ETeam.OPPONENTS, marginToTheirPenArea);
	}


	public final void setConsideredPenAreas(final ETeam consideredPenAreas)
	{
		this.consideredPenAreas = consideredPenAreas;
	}


	protected final boolean receivingPositionIsReachableByBall(IVector2 pos)
	{
		return getBall().getTrajectory().getTravelLineRolling().distanceTo(pos) < maxDistanceToReceivingPosition;
	}


	protected final boolean ballIsMoving()
	{
		return ballSpeedHysteresis.isUpper() && !getTBot().hasBallContact();
	}


	protected final boolean ballNearKicker()
	{
		return ballStabilizer.getBallPos().distanceTo(getTBot().getBotKickerPos()) < 100;
	}


	protected boolean ballIsMovingTowardsMe()
	{
		return ballIsMoving()
				&& getPos().subtractNew(ballStabilizer.getBallPos())
						.angleToAbs(getBall().getVel())
						.map(a -> a < AngleMath.PI_HALF).orElse(false);
	}

	protected abstract class ABallArrivalState extends MoveToState
	{
		protected IVector2 currentReceivingPosition;
		protected double currentTargetAngle;


		protected ABallArrivalState()
		{
			super(ABallArrivalSkill.this);
		}


		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			getMoveCon().setBallObstacle(false);
			getMoveCon().setPenaltyAreaAllowedTheir(true);

			// init target orientation
			currentTargetAngle = getBall().getPos().subtractNew(getPos()).getAngle();
			setCurrentReceivingPosition(moveToReceivingPosition());
		}


		@Override
		public void doUpdate()
		{
			setCurrentReceivingPosition(determineReceivingPosition());

			getMoveCon().setBotsObstacle(!ballNearKicker());

			writeTargetPoseToMoveCon();

			drawShapes();

			super.doUpdate();
		}


		private IVector2 determineReceivingPosition()
		{
			IVector2 closestPointOnBallLine = moveToNearestPointOnBallLine();
			if (ballIsMoving() &&
					receivingPositionIsReachableByBall(closestPointOnBallLine) &&
					ballIsMovingTowardsBot())
			{
				return closestPointOnBallLine;
			} else if (!ballNearKicker())
			{
				return moveToReceivingPosition();
			}
			return currentReceivingPosition;
		}


		private void setCurrentReceivingPosition(final IVector2 receivingPosition)
		{
			IVector2 dest = receivingPosition;
			dest = positionValidator.movePosInFrontOfOpponent(dest);
			dest = positionValidator.movePosInsideFieldWrtBallPos(dest);
			currentReceivingPosition = dest;
		}


		private boolean ballIsMovingTowardsBot()
		{
			return getBall().getTrajectory().getTravelLine().isPointInFront(getPos());
		}


		protected IVector2 moveToReceivingPosition()
		{
			return receivingPosition.getPos();
		}


		protected IVector2 moveToNearestPointOnBallLine()
		{
			return InterceptorUtil.closestInterceptionPos(getBall().getTrajectory().getTravelLineSegment(), getTBot());
		}


		private void writeTargetPoseToMoveCon()
		{
			currentTargetAngle = calcTargetAngle(currentReceivingPosition);
			getMoveCon().updateTargetAngle(currentTargetAngle);

			IVector2 dest = BotShape.getCenterFromKickerPos(currentReceivingPosition, currentTargetAngle,
					getTBot().getCenter2DribblerDist() + Geometry.getBallRadius());

			// the bot may drive through the penArea, but it should not have a destination inside,
			// because touching the ball while being partially inside the penArea is a foul.
			dest = positionValidator.movePosOutOfPenAreaWrtBall(dest, getMarginBetweenDestAndPenArea(),
					consideredPenAreas);

			getMoveCon().updateDestination(dest);

			if (getBall().getVel().getLength2() > 0.2)
			{
				getMoveCon().getMoveConstraints().setPrimaryDirection(getBall().getVel());
			} else
			{
				getMoveCon().getMoveConstraints().setPrimaryDirection(Vector2f.ZERO_VECTOR);
			}
		}


		private double calcTargetAngle(final IVector2 kickerPos)
		{
			IVector2 ballPos = ballStabilizer.getBallPos();
			double distBallBot = ballPos.distanceTo(kickerPos);
			if (distBallBot < distThresholdToFixOrientation)
			{
				// just keep last position -> this is probably most safe to not push ball away again
				return currentTargetAngle;
			}

			return calcMyTargetAngle(kickerPos);
		}


		protected void drawShapes()
		{
			getShapes().get(ESkillShapesLayer.BALL_ARRIVAL_SKILL)
					.add(new DrawablePoint(ballStabilizer.getBallPos(), Color.green));
			getShapes().get(ESkillShapesLayer.BALL_ARRIVAL_SKILL)
					.add(new DrawableAnnotation(getPos(), ballIsMoving() ? "ballMoving" : "ballNotMoving",
							Vector2.fromX(100)));
		}


		protected abstract double calcMyTargetAngle(final IVector2 kickerPos);


		protected abstract double getMarginBetweenDestAndPenArea();
	}
}
