/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.VectorDistanceComparator;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.DefendingKeeper.ECriticalKeeperStates;
import edu.tigers.sumatra.skillsystem.skills.keeper.GoOutDestinationCalculator;
import edu.tigers.sumatra.skillsystem.skills.keeper.IKeeperDestinationCalculator;
import edu.tigers.sumatra.skillsystem.skills.keeper.InterceptBallCurveDestinationCalculator;
import edu.tigers.sumatra.skillsystem.skills.keeper.InterceptBallLineDestinationCalculator;
import edu.tigers.sumatra.skillsystem.skills.keeper.NormalBlockDestinationCalculator;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.NonNull;
import lombok.Setter;

import java.awt.Color;


/**
 * Skill Implementation for Time critical keeper skills. Used by CriticalKeeperState
 */
public class CriticalKeeperSkill extends AMoveToSkill
{
	@Configurable(comment = "Use MoveConstraints PrimaryDirection while going out or catching redirects", defValue = "true")
	private static boolean usePrimaryDirectionOuterBlockingStates = true;

	@Configurable(comment = "Use ball curve for intercepts", defValue = "false")
	private static boolean useBallCurveForIntercept = false;
	@Configurable(comment = "Stay in normal blocking in the intercept if spare time is great enough", defValue = "true")
	private static boolean useSpareTimeInIntercept = true;

	@Configurable(comment = "[s] Necessary spare time to delay the movement of the keeper", defValue = "0.3")
	private static double necessarySpareTimeToDelayMovement = 0.3;

	@Configurable(comment = "[s] Time we project the ball position into the future", defValue = "0.3")
	private static double ballLookaheadTime = 0.3;

	private ECriticalKeeperStates currentState = ECriticalKeeperStates.NORMAL;
	private DefendingKeeper defendingKeeper = new DefendingKeeper(this);

	@Setter
	@NonNull
	private KickParams ballHandlingKickParams = KickParams.disarm();


	public CriticalKeeperSkill()
	{
		var blockState = new NormalBlock();
		setInitialState(blockState);

		addTransition(ECriticalKeeperStates.HAS_CONTACT, new ComeToAStop());
		addTransition(ECriticalKeeperStates.INTERCEPT_BALL, new InterceptBall());
		addTransition(ECriticalKeeperStates.DEFEND_REDIRECT, new CatchRedirect());
		addTransition(ECriticalKeeperStates.GO_OUT, new GoOut());
		addTransition(ECriticalKeeperStates.NORMAL, blockState);
	}


	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		defendingKeeper.update(getWorldFrame(), getBot().getBotId());

		ECriticalKeeperStates nextState = defendingKeeper.calcNextKeeperState();
		if (nextState != currentState)
		{
			currentState = nextState;
			triggerEvent(nextState);
		}
		if (getPos().distanceTo(getBall().getPos()) < Geometry.getBallRadius() + Geometry.getBotRadius() + 500)
		{
			setKickParams(ballHandlingKickParams);
		} else
		{
			setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.OFF));
		}
	}


	private void prepareMoveCon()
	{
		getMoveCon().setPenaltyAreaOurObstacle(false);
		getMoveCon().setBotsObstacle(false);
		getMoveCon().setBallObstacle(false);
		getMoveCon().setGoalPostsObstacle(false);
	}


	private void prepareMovementConstraints()
	{
		getMoveConstraints().setPrimaryDirection(Vector2f.ZERO_VECTOR);
	}


	private IVector2 getBallPosWithLookahead()
	{
		return getWorldFrame().getBall().getTrajectory().getPosByTime(ballLookaheadTime).getXYVector();
	}


	private IVector2 getPrimaryDirectionOuterBlockingStates(
			IVector2 threatPos,
			IVector2 normalBlockingPos,
			IVector2 wantedBlockingPos
	)
	{
		IVector2 finalPrimaryDirection = Vector2f.ZERO_VECTOR;
		if (usePrimaryDirectionOuterBlockingStates)
		{
			var breakDistance = 1000 * SumatraMath.square(getVel().getLength()) / (2 * getMoveConstraints().getAccMax());
			var enoughSpaceToBreak =
					getPos().distanceTo(wantedBlockingPos) > 1.1 * breakDistance + Geometry.getBotRadius();
			var notOvershoot = getPos().distanceTo(Geometry.getGoalOur().getCenter()) < wantedBlockingPos.distanceTo(
					Geometry.getGoalOur().getCenter());

			var primaryDirection = Vector2.fromPoints(threatPos, normalBlockingPos);
			var keeperPrimaryDirectionIntersection = Lines.halfLineFromDirection(getPos(), getVel())
					.intersect(Lines.lineFromDirection(threatPos, primaryDirection)).asOptional();
			var validIntersection = keeperPrimaryDirectionIntersection.isEmpty() || Geometry.getField()
					.isPointInShape(keeperPrimaryDirectionIntersection.get());
			final Color primaryDirectionColor;
			if (validIntersection && enoughSpaceToBreak && notOvershoot)
			{
				finalPrimaryDirection = primaryDirection;
				primaryDirectionColor = Color.BLACK;
			} else
			{
				primaryDirectionColor = Color.GRAY;
			}
			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableLine(Lines.segmentFromOffset(getPos(), primaryDirection.scaleToNew(-1000)),
							primaryDirectionColor));
		}
		return finalPrimaryDirection;
	}


	private boolean isSpareTimeLeft(IVector2 interestingPosOnBallTrajectory, IVector2 defendPosition, double extraTime)
	{
		var timeLeft = getBall().getTrajectory().getTimeByPos(interestingPosOnBallTrajectory);
		var drivingTime =
				TrajectoryGenerator.generatePositionTrajectory(getTBot(), defendPosition).getTotalTime() + extraTime;
		var spareTime = timeLeft - drivingTime;

		var shapes = getShapes().get(ESkillShapesLayer.KEEPER);
		shapes.add(new DrawableCircle(defendPosition, Geometry.getBotRadius(), Color.RED));
		shapes.add(new DrawableAnnotation(defendPosition, String.format("%.2f", spareTime), Color.RED));
		return spareTime > necessarySpareTimeToDelayMovement;
	}


	private class InterceptBall extends AState
	{
		private double targetAngle;
		private boolean spareTimeOver = false;

		private IKeeperDestinationCalculator destCalculator;
		private IKeeperDestinationCalculator normalBlockCalculator;


		@Override
		public void doEntryActions()
		{
			targetAngle = getAngle();
			spareTimeOver = false;
			prepareMoveCon();
			prepareMovementConstraints();
			if (useBallCurveForIntercept)
			{
				destCalculator = new InterceptBallCurveDestinationCalculator();
			} else
			{
				destCalculator = new InterceptBallLineDestinationCalculator();
			}
			normalBlockCalculator = new NormalBlockDestinationCalculator();
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			if (getBall().getVel().isZeroVector())
			{
				// skill is not designed for lying balls
				return;
			}
			var destination = destCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(), getMoveConstraints(),
					null);
			if (isSpareTimeLeft(destination, destination, 0.0) && !spareTimeOver && useSpareTimeInIntercept)
			{
				var normalBlockDestination = normalBlockCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(),
						getMoveConstraints(), getBallPosWithLookahead());
				updateDestination(normalBlockDestination);
			} else
			{
				updateDestination(destination);
				spareTimeOver = true;
			}
			updateTargetAngle(calcTargetAngle());

			super.doUpdate();
		}


		public double calcTargetAngle()
		{
			if (VectorMath.distancePP(getBall().getPos(), getTBot().getPos()) < (Geometry.getBotRadius() / 2))
			{
				return getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle();
			}
			return targetAngle;
		}
	}

	private class CatchRedirect extends AState
	{
		private boolean stayInGoOut = false;
		private boolean spareTimeOver = false;
		private IKeeperDestinationCalculator goOutCalculator;
		private IKeeperDestinationCalculator normalBlockCalculator;


		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			prepareMovementConstraints();
			getMoveCon().setFieldBorderObstacle(false);
			stayInGoOut = false;
			spareTimeOver = false;

			goOutCalculator = new GoOutDestinationCalculator();
			normalBlockCalculator = new NormalBlockDestinationCalculator();

			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			var redirectPos = getRedirectBotPosition();
			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableCircle(Circle.createCircle(redirectPos, 50), Color.MAGENTA));
			var normalBlockingPos = normalBlockCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(),
					getMoveConstraints(), redirectPos);
			var goOutFeasible = isRedirectGoOutFeasible(redirectPos);

			var destination = normalBlockingPos;
			double extraDriveTime = 0.0;

			if ((isKeeperBetweenRedirectAndNormalBlock(redirectPos, destination) || stayInGoOut) && goOutFeasible)
			{
				stayInGoOut = true;
				destination = goOutCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(), getMoveConstraints(),
						redirectPos);
			} else
			{
				stayInGoOut = false;
				if (goOutFeasible)
				{
					var goOutPos = goOutCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(),
							getMoveConstraints(), redirectPos);
					extraDriveTime = goOutPos.distanceTo(normalBlockingPos) / (getMoveConstraints().getVelMax() * 500);
				}
			}
			if (isSpareTimeLeft(redirectPos, destination, extraDriveTime) && !spareTimeOver)
			{
				destination = normalBlockCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(),
						getMoveConstraints(), getBallPosWithLookahead());
			} else
			{
				spareTimeOver = true;
			}
			getMoveConstraints().setPrimaryDirection(
					getPrimaryDirectionOuterBlockingStates(redirectPos, normalBlockingPos, destination));
			updateLookAtTarget(redirectPos);
			updateDestination(destination);
			super.doUpdate();
		}


		private boolean isRedirectGoOutFeasible(IVector2 redirectPos)
		{
			return defendingKeeper.isPositionCloseToPenaltyArea(redirectPos) || defendingKeeper.isGoOutFeasible();
		}


		private IVector2 getRedirectBotPosition()
		{
			IVector2 redirectBotPosition;
			var redirectBotId = defendingKeeper.getBestRedirector(getWorldFrame().getOpponentBots());
			var ballTravelLine = Lines.halfLineFromDirection(getBall().getPos(), getBall().getVel());
			if (redirectBotId.isBot())
			{
				var opponent = getWorldFrame().getOpponentBot(redirectBotId);
				redirectBotPosition = ballTravelLine.closestPointOnPath(opponent.getBotKickerPos());
			} else
			{
				redirectBotPosition = getWorldFrame().getOpponentBots().values().stream()
						.map(ITrackedBot::getBotKickerPos)
						.min(new VectorDistanceComparator(getWorldFrame().getBall().getPos()))
						.map(ballTravelLine::closestPointOnPath)
						.orElse(Vector2.fromXY(0, 0));
			}
			return redirectBotPosition;
		}


		private boolean isKeeperBetweenRedirectAndNormalBlock(IVector2 redirectPos, IVector2 normalBlockPosition)
		{
			return Lines.segmentFromPoints(redirectPos, normalBlockPosition).distanceTo(getPos())
					< 3 * Geometry.getBotRadius();
		}
	}

	private class GoOut extends AState
	{
		private IKeeperDestinationCalculator goOutCalculator;
		private IKeeperDestinationCalculator normalBlockCalculator;


		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			prepareMovementConstraints();
			goOutCalculator = new GoOutDestinationCalculator();
			normalBlockCalculator = new NormalBlockDestinationCalculator();
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			var ballPos = getBallPosWithLookahead();
			IVector2 normalBlockingPos = normalBlockCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(),
					getMoveConstraints(), ballPos);
			IVector2 targetPosition;
			if (isBallInsidePenaltyArea())
			{
				targetPosition = normalBlockingPos;
			} else
			{
				targetPosition = goOutCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(),
						getMoveConstraints(), ballPos);
			}
			getMoveConstraints().setPrimaryDirection(
					getPrimaryDirectionOuterBlockingStates(getBall().getPos(), normalBlockingPos, targetPosition));
			updateDestination(targetPosition);
			updateTargetAngle(getPos().subtractNew(Geometry.getGoalOur().getCenter()).getAngle());

			super.doUpdate();
		}


		private boolean isBallInsidePenaltyArea()
		{
			return Geometry.getPenaltyAreaOur()
					.withMargin(-2 * Geometry.getBotRadius())
					.isPointInShape(getWorldFrame().getBall().getPos());
		}
	}

	private class ComeToAStop extends AState
	{
		IVector2 destination;


		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			prepareMovementConstraints();
			var vel = getVel();
			var velLength = vel.getLength2();
			var breakDistance = 1000 * (0.5f * velLength * velLength / getMoveConstraints().getAccMax());
			destination = Geometry.getPenaltyAreaOur().getRectangle().withMargin(-Geometry.getBotRadius())
					.nearestPointInside(getPos().addNew(vel.scaleToNew(breakDistance)));
			updateDestination(destination);
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableCircle(Circle.createCircle(destination, 100), Color.MAGENTA));
			updateDestination(destination);
			super.doUpdate();
		}
	}

	private class NormalBlock extends AState
	{
		IKeeperDestinationCalculator destCalculator;


		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			prepareMovementConstraints();
			destCalculator = new NormalBlockDestinationCalculator();
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			updateDestination(destCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(), getMoveConstraints(),
					getBallPosWithLookahead()));
			updateLookAtTarget(getBall());

			super.doUpdate();
		}


	}


}
