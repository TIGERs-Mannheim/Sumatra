/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.throwin;

import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.common.AwayFromBallMover;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.PullBallSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;


/**
 * @author MarkG
 */
public abstract class ABallPlacementRole extends ARole
{
	@Configurable(comment = "max dist where ball can be pushed. if dist > this, then shoot", defValue = "3000.0")
	private static double pushBallVsPassDistance = 3000;

	@Configurable(defValue = "2000.0")
	private static double maxPullDistance = 2000;

	@Configurable(defValue = "50.0")
	protected static double hysteresisMargin = 50;


	protected final AwayFromBallMover awayFromBallMover = new AwayFromBallMover(
			new PointChecker()
					.checkBallDistances()
					.checkInsideField()
					.checkNotInPenaltyAreas());
	private IVector2 placementPos;
	private boolean hasCompanion = false;
	private IVector2 relPlacementPos;


	/**
	 * @param role
	 */
	public ABallPlacementRole(final ERole role)
	{
		super(role);
		awayFromBallMover.getPointChecker().checkCustom(this::outsideOtherRobots);
	}


	public static double getPushBallVsPassDistance()
	{
		return pushBallVsPassDistance;
	}


	@Override
	protected void beforeUpdate()
	{
		super.beforeUpdate();
		if (getAiFrame().getGamestate().isBallPlacementForUs())
		{
			setPlacementPos(getAiFrame().getGamestate().getBallPlacementPositionForUs());
		}
		IVector2 rel = getPlacementPos().subtractNew(getBall().getPos());
		if (relPlacementPos == null || rel.getLength2() > 30)
		{
			relPlacementPos = rel;
		}
	}


	protected boolean pullIsRequired()
	{
		return !ballIsInsideField(hysteresisMargin) || ballIsInsideGoal(hysteresisMargin);
	}


	private boolean ballIsInsideField(double margin)
	{
		return Geometry.getField().withMargin(margin).isPointInShape(getBall().getPos());
	}


	private boolean ballIsInsideGoal(double margin)
	{
		double fullMargin = Geometry.getBotRadius() * 2 - margin;
		return Geometry.getGoalOur().getRectangle().withMargin(fullMargin).isPointInShape(getBall().getPos())
				|| Geometry.getGoalTheir().getRectangle().withMargin(fullMargin).isPointInShape(getBall().getPos());
	}


	protected boolean isBallAtTarget()
	{
		return getBall().getPos().distanceTo(getPlacementPos()) < RuleConstraints.getBallPlacementTolerance() - 20;
	}


	protected void prepareMoveCon(MovementCon moveCon)
	{
		moveCon.setPenaltyAreaAllowedTheir(true);
		moveCon.setPenaltyAreaAllowedOur(true);
		moveCon.setGoalPostObstacle(true);
		moveCon.setIgnoreGameStateObstacles(true);
	}


	public IVector2 getPlacementPos()
	{
		return placementPos;
	}


	protected void setPlacementPos(final IVector2 placementPos)
	{
		this.placementPos = placementPos;
	}


	protected boolean isBallInsidePushRadius()
	{
		return getAiFrame().getTacticalField().isBallInPushRadius();
	}


	protected boolean hasCompanion()
	{
		return hasCompanion;
	}


	private boolean outsideOtherRobots(IVector2 pos)
	{
		return getWFrame().getBots().values().stream()
				.filter(bot -> !bot.getBotId().equals(getBotID()))
				.noneMatch(bot -> bot.getPosByTime(1).distanceTo(pos) < Geometry.getBotRadius() * 2);
	}


	protected boolean isSecondaryRoleNeeded()
	{
		return !isBallInsidePushRadius();
	}


	protected boolean ballIsLying()
	{
		return getBall().getVel().getLength2() < 0.1;
	}


	public void setHasCompanion(final boolean hasCompanion)
	{
		this.hasCompanion = hasCompanion;
	}


	/**
	 * Check if it is more clever to pull the ball instead of pushing it.
	 * Reasons for pulling are:
	 * - ball out of field
	 * - bot is between ball and placement pos, in push radius and the bot has to drive less than PI/6 around the ball
	 *
	 * @return true if the ball should be pulled
	 */
	protected boolean favorPullingTheBall()
	{
		boolean outOfField = !Geometry.getField().isPointInShape(getBall().getPos());
		IVector2 ballPos = getBall().getPos();
		IVector2 relBotPos = getPos().subtractNew(ballPos);

		boolean inPushRadius = relPlacementPos.getLength() < 800;
		Optional<Double> angleBotBallGoal = relBotPos.angleToAbs(relPlacementPos);
		boolean botBetweenBallAndGoal = angleBotBallGoal.isPresent()
				&& ((angleBotBallGoal.get() < AngleMath.PI / 6) && inPushRadius);
		return outOfField || botBetweenBallAndGoal;
	}

	protected enum EEvent implements IEvent
	{
		PASS,
		PULL,
		CLEAR,
		RECEIVE,
		PUSH,
		PREPARE_PUSH,
		STOP_BALL,
		DONE
	}

	protected class ClearBaseState extends AState
	{
		private static final double MAX_VEL = 1;
		private static final double MAX_ACC = 1;
		private AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
		private IVector2 dest;
		private IVector2 currentDest;
		private boolean ballLying = false;


		@Override
		public void doEntryActions()
		{
			ballLying = false;
			skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().getMoveConstraints().setVelMax(MAX_VEL);
			prepareMoveCon(skill.getMoveCon());
			skill.getMoveCon().setIgnoreGameStateObstacles(false);
			setNewSkill(skill);

			dest = LineMath.stepAlongLine(getBall().getPos(), getPos(), Geometry.getBotRadius() * 3);
		}


		@Override
		public void doUpdate()
		{
			if (getBot().getVel().getLength2() <= MAX_VEL)
			{
				// reduce acc limit only after bot is slow enough
				skill.getMoveCon().getMoveConstraints().setAccMax(MAX_ACC);
			}

			currentDest = dest;
			if ((ballLying || ballIsLying()) && getPos().distanceTo(getBall().getPos()) > Geometry.getBotRadius() * 2)
			{
				// find a valid destination, after having sufficient distance to the ball
				currentDest = awayFromBallMover.findValidDest(getAiFrame(), dest);
				ballLying = true;
			}

			skill.getMoveCon().updateDestination(currentDest);
			skill.getMoveCon().updateLookAtTarget(getBall());
		}


		public boolean isDestReached()
		{
			return currentDest.distanceTo(getPos()) < hysteresisMargin;
		}
	}


	protected class PullState extends AState
	{
		private IVector2 destination;
		private PullBallSkill skill;


		@Override
		public void doEntryActions()
		{
			destination = getDestination();
			skill = new PullBallSkill(destination);
			prepareMoveCon(skill.getMoveCon());
			setNewSkill(skill);
		}


		@Override
		public void doUpdate()
		{
			if (skill.hasReleasedBall() && (pullIsDone() || ballIsNearDestination()))
			{
				triggerEvent(EEvent.DONE);
			}
		}


		private boolean pullIsDone()
		{
			return !favorPullingTheBall() && ballIsInsideField(-hysteresisMargin) && !ballIsInsideGoal(-hysteresisMargin);
		}


		private boolean ballIsNearDestination()
		{
			// Checks whether is at the placement destination. 2 times ball radius to make sure that the ball is in the
			// placement tolerance.
			return destination.distanceTo(getBall().getPos()) < RuleConstraints.getBallPlacementTolerance()
					- Geometry.getBallRadius() * 2;
		}


		private IVector2 getDestination()
		{
			if (ballIsInsideGoal(hysteresisMargin))
			{
				return getDestinationOutsideGoal();
			}

			IVector2 nearestInField = Geometry.getField().withMargin(-200).nearestPointInside(getBall().getPos());

			IVector2 ball2Field = nearestInField.subtractNew(getBall().getPos());
			IVector2 ball2Target = placementPos.subtractNew(getBall().getPos());

			IVector2 dest;
			if (ball2Field.angleToAbs(ball2Target).orElse(0.0) > AngleMath.PI_QUART
					|| placementPos.distanceTo(nearestInField) > maxPullDistance)
			{
				dest = nearestInField;
			} else
			{
				dest = placementPos;
			}
			if (nearGoal())
			{
				return dest.addNew(Vector2.fromY(Math.signum(getBall().getPos().y()) * 300));
			}
			return dest;
		}


		private boolean nearGoal()
		{
			return Math.abs(getBall().getPos().x()) > Geometry.getFieldLength() / 2
					&& Math.abs(getBall().getPos().y()) < Geometry.getGoalOur().getWidth() / 2 + 200;
		}


		private IVector2 getDestinationOutsideGoal()
		{
			double margin = Geometry.getBotRadius() * 2 + hysteresisMargin + 100;
			double y = 0;
			if (Math.abs(getBall().getPos().y()) > Geometry.getGoalTheir().getWidth() / 2)
			{
				y = Math.signum(getBall().getPos().y()) * Geometry.getGoalTheir().getWidth();
			}
			return Vector2.fromXY(Math.signum(getBall().getPos().x()) * (Geometry.getFieldLength() / 2 - margin), y);
		}
	}
}
