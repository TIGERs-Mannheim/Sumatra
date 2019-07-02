/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorDistanceComparator;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.DefendingKeeper.ECriticalKeeperStates;
import edu.tigers.sumatra.trajectory.BangBangTrajectoryMath;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Skill Implementation for Time critical keeper skills. Used by CriticalKeeperState
 */
public class CriticalKeeperSkill extends AMoveSkill
{
	@Configurable(comment = "The radius to try intercepting the chip-kicked ball within", defValue = "500.0")
	private static double maxChipInterceptDist = 500.0;

	@Configurable(comment = "Max. Acceleration in Catch Skill", defValue = "3.0")
	private static double maxAcc = 3.0;

	@Configurable(comment = "Over Acceleration", defValue = "false")
	private static boolean isOverAccelerationActive = false;

	@Configurable(comment = "Max acceleration of Keeper", defValue = "4.5")
	private static double keeperAcc = 4.5;

	@Configurable(comment = "Dist [mm] to GoalCenter in NormalBlockState", defValue = "500.0")
	private static double distToGoalCenter = 500.0;

	@Configurable(comment = "Keeper goes out if redirecting bot is behind this margin added to the penaulty area", defValue = "-2022.5")
	private static double goOutWhileRedirectMargin = Geometry.getGoalOur().getCenter().x() / 2.0;

	@Configurable(comment = "Keeper's normal movement is circular", defValue = "true")
	private static boolean isKeepersNormalMovementCircular = true;

	@Configurable(comment = "Angle of Keeper towards the ball in NormalBlockState", defValue = "0.0")
	private static double turnAngleOfKeeper = 0.0;


	private ECriticalKeeperStates currentState = ECriticalKeeperStates.NORMAL;
	private DefendingKeeper defendingKeeper;


	public CriticalKeeperSkill(BotID keeperID)
	{
		super(ESkill.CRITICAL_KEEPER);
		defendingKeeper = new DefendingKeeper(keeperID, this);

		NormalBlock blockState = new NormalBlock();
		setInitialState(blockState);

		addTransition(ECriticalKeeperStates.INTERCEPT_BALL, new InterceptBall());
		addTransition(ECriticalKeeperStates.DEFEND_REDIRECT, new CatchRedirect());
		addTransition(ECriticalKeeperStates.GO_OUT, new GoOut());
		addTransition(ECriticalKeeperStates.NORMAL, blockState);
	}


	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		defendingKeeper.update(getWorldFrame());

		ECriticalKeeperStates nextState = defendingKeeper.calcNextKeeperState();
		if (nextState != currentState)
		{
			currentState = nextState;
			triggerEvent(nextState);
		}
	}


	private void prepareMoveCon()
	{
		getMoveCon().setPenaltyAreaAllowedOur(true);
		getMoveCon().setBotsObstacle(false);
		getMoveCon().setBallObstacle(false);
		getMoveCon().setGoalPostObstacle(false);
	}


	private class InterceptBall extends MoveToState
	{
		private double targetAngle = 0;


		private InterceptBall()
		{
			super(CriticalKeeperSkill.this);
		}


		@Override
		public void doEntryActions()
		{
			targetAngle = getAngle();
			prepareMoveCon();
			getMoveCon().getMoveConstraints().setAccMax(maxAcc);
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

			final IVector2 destination = getDestination();

			updateTargetAngle(destination);

			getMoveCon().updateDestination(destination);
			getMoveCon().updateTargetAngle(targetAngle);

			super.doUpdate();
			draw();
		}


		private void draw()
		{
			if (getBot().getCurrentTrajectory().isPresent())
			{
				getShapes().get(ESkillShapesLayer.PATH_DEBUG).add(new DrawableLine(Line.fromDirection(getPos(),
						getBot().getCurrentTrajectory().get().getAcceleration(0).getXYVector().multiplyNew(1000)),
						Color.BLACK));
			}
		}


		private void updateTargetAngle(final IVector2 destination)
		{
			if (VectorMath.distancePP(destination, getTBot().getPos()) < (Geometry.getBotRadius() / 2))
			{
				targetAngle = getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle();
			}
		}


		private IVector2 getDestination()
		{
			IVector2 leadPoint = findPointOnBallTraj();

			IVector2 destination = leadPoint;

			if (!Geometry.getField().isPointInShape(destination))
			{
				List<IVector2> intersections = Geometry.getField()
						.lineIntersections(Line.fromPoints(getBall().getPos(), leadPoint));
				for (IVector2 intersection : intersections)
				{
					IVector2 vec = intersection.subtractNew(getBall().getPos());
					if (Math.abs(vec.getAngle() - getBall().getVel().getAngle()) < 0.1)
					{
						destination = intersection;
						break;
					}
				}
			}
			if (isOverAccelerationActive)
			{
				destination = calcAcceleration(destination);
			}
			return destination;
		}


		private IVector2 findPointOnBallTraj()
		{
			IVector2 leadPoint = LineMath.leadPointOnLine(Line.fromDirection(getBall().getPos(), getBall().getVel()),
					getPos());
			if (getBall().isChipped())
			{
				IVector2 nearestTouchdown = getNearestChipTouchdown();
				if ((nearestTouchdown != null) && (nearestTouchdown.distanceTo(getPos()) < maxChipInterceptDist))
				{
					// Get some distance between touchdown and bot
					double distance = Geometry.getBotRadius() + Geometry.getBallRadius();
					IVector2 direction = nearestTouchdown.subtractNew(getBall().getPos()).scaleTo(distance);

					leadPoint = nearestTouchdown.addNew(direction);
				}
			}
			return leadPoint;
		}


		private IVector2 getNearestChipTouchdown()
		{

			ITrackedBall ball = getWorldFrame().getBall();
			List<IVector2> touchdowns = ball.getTrajectory().getTouchdownLocations();

			IVector2 nearestPoint = null;
			double min = -1;
			for (IVector2 td : touchdowns)
			{
				double dist = td.distanceTo(getPos());

				if ((min < 0) || ((dist < min) && (td.x() > Geometry.getGoalOur().getCenter().x())))
				{
					min = dist;
					nearestPoint = td;
				}
			}

			return nearestPoint;
		}


		private IVector2 calcAcceleration(final IVector2 destination)
		{
			double acc = getMoveCon().getMoveConstraints().getAccMax();
			double vel = getMoveCon().getMoveConstraints().getVelMax();
			return BangBangTrajectoryMath.getVirtualDestinationToReachPositionInTime(getPos(), getVel(),
					destination, acc, vel,
					getWorldFrame().getBall().getTrajectory().getTimeByPos(
							LineMath.stepAlongLine(destination, getWorldFrame().getBall().getPos(),
									Geometry.getBotRadius() / 2)),
					Geometry.getBotRadius());
		}

	}

	private class CatchRedirect extends MoveToState
	{
		private boolean stayInGoOut = false;


		private CatchRedirect()
		{
			super(CriticalKeeperSkill.this);
		}


		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			getMoveCon().setDestinationOutsideFieldAllowed(true);
			getMoveCon().getMoveConstraints().setAccMax(keeperAcc);
			stayInGoOut = false;

			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			IVector2 redirectBot = getRedirectBotPosition();
			IVector2 destination = LineMath.stepAlongLine(Geometry.getGoalOur().getCenter(), redirectBot,
					distToGoalCenter);

			if (VectorMath.distancePP(getPos(), destination) < (Geometry.getBotRadius() / 2))
			{
				getMoveCon().updateLookAtTarget(redirectBot);
			}
			if ((isKeeperBetweenRedirectAndGoalCenter(redirectBot) || stayInGoOut) && isGoOutUseful(redirectBot))
			{
				stayInGoOut = true;
				destination = calcBestDefensivePositionInPE(redirectBot);
				getMoveCon().updateLookAtTarget(redirectBot);
			}
			getMoveCon().updateDestination(destination);
			super.doUpdate();
		}


		private IVector2 getRedirectBotPosition()
		{
			IVector2 redirectBot;
			BotID redirectBotId = defendingKeeper.getBestRedirector(getWorldFrame().getFoeBots());
			if (redirectBotId.isBot())
			{
				redirectBot = getWorldFrame().getFoeBot(redirectBotId).getBotKickerPos();
			} else
			{
				// .getEnemyClosestToBall from tactical field
				redirectBot = getWorldFrame().getFoeBots().values().stream().map(ITrackedBot::getBotKickerPos)
						.min(new VectorDistanceComparator(getWorldFrame().getBall().getPos())).orElse(Vector2.fromXY(0, 0));
			}
			return redirectBot;
		}


		private boolean isGoOutUseful(IVector2 redirectBot)
		{
			return redirectBot.x() < goOutWhileRedirectMargin;
		}


		private boolean isKeeperBetweenRedirectAndGoalCenter(IVector2 redirectBot)
		{
			return LineMath.distancePL(getPos(),
					Line.fromPoints(Geometry.getGoalOur().getCenter(),
							redirectBot)) < Geometry.getBotRadius();
		}

	}

	private class GoOut extends MoveToState
	{
		private GoOut()
		{
			super(CriticalKeeperSkill.this);
		}


		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			getMoveCon().getMoveConstraints().setAccMax(keeperAcc);
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			IVector2 targetPosition;
			if (isBallInsidePenaltyArea())
			{
				targetPosition = calcPositionBehindBall();
			} else
			{
				targetPosition = calcBestDefensivePositionInPE(getWorldFrame().getBall().getPos());
			}
			getMoveCon().updateDestination(targetPosition);
			getMoveCon().updateTargetAngle(getPos().subtractNew(Geometry.getGoalOur().getCenter()).getAngle());

			super.doUpdate();

			drawKeeperShapes(targetPosition);
		}


		private boolean isBallInsidePenaltyArea()
		{
			return Geometry.getPenaltyAreaOur().isPointInShape(getWorldFrame().getBall().getPos(),
					-Geometry.getBotRadius() * 2);
		}


		private IVector2 calcPositionBehindBall()
		{
			IVector2 ballPos = getWorldFrame().getBall().getPos();
			IVector2 goalCenter = Geometry.getGoalOur().getCenter();
			double distanceToBall = goalCenter.distanceTo(ballPos);
			return LineMath.stepAlongLine(goalCenter, ballPos, distanceToBall - Geometry.getBotRadius());
		}


		private void drawKeeperShapes(IVector2 targetPose)
		{
			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableLine(Line.fromPoints(getWorldFrame().getBall().getPos(),
							Geometry.getGoalOur().getLeftPost())));
			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableLine(Line.fromPoints(getWorldFrame().getBall().getPos(),
							Geometry.getGoalOur().getRightPost())));
			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableCircle(targetPose, 30, Color.green));
		}


	}

	private class NormalBlock extends MoveToState
	{
		private NormalBlock()
		{
			super(CriticalKeeperSkill.this);
		}


		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			getMoveCon().getMoveConstraints().setAccMax(keeperAcc);
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			if (isKeepersNormalMovementCircular &&
					getBall().getPos().distanceTo(Geometry.getGoalOur().getCenter()) > Geometry.getGoalOur().getWidth() / 2
							+ 100)
			{
				blockCircular();
			} else
			{
				blockOnLine();
			}

			super.doUpdate();
		}


		private void blockOnLine()
		{

			IVector2 newLeftPost = Geometry.getGoalOur().getLeftPost().addNew(Vector2.fromX(Geometry.getBotRadius()));
			IVector2 newRightPost = Geometry.getGoalOur().getRightPost().addNew(Vector2.fromX(Geometry.getBotRadius()));
			ILine newGoalLine = Line.fromPoints(newLeftPost, newRightPost);
			ILine ballGoalcenterLine = Line.fromPoints(getWorldFrame().getBall().getPos(),
					Geometry.getGoalOur().getCenter());

			Optional<IVector2> intersection = newGoalLine.intersectionWith(ballGoalcenterLine);

			IVector2 destination = Geometry.getGoalOur().getCenter();

			if (intersection.isPresent())
			{

				destination = checkPosts(intersection.get());
			}
			getMoveCon().updateDestination(destination);
			getMoveCon().updateLookAtTarget(getWorldFrame().getBall().getPos());
		}


		private IVector2 checkPosts(IVector2 destination)
		{
			IVector2 finalDestination = destination;
			boolean isBallBehindGoalline = getWorldFrame().getBall().getPos().x() < Geometry.getGoalOur().getCenter().x();
			boolean isDestinationLeftFromLeftPost = destination.y() > Geometry.getGoalOur().getLeftPost().y();
			boolean isDestinationRightFromRightPost = destination.y() < Geometry.getGoalOur().getRightPost().y();
			boolean isBallLeftFromGoal = getWorldFrame().getBall().getPos().y() > 0;

			if ((isDestinationLeftFromLeftPost && !isBallBehindGoalline) || (isBallBehindGoalline && isBallLeftFromGoal))
			{
				finalDestination = Vector2.fromXY(destination.x(), Geometry.getGoalOur().getLeftPost().y());
			} else if (isDestinationRightFromRightPost || isBallBehindGoalline)
			{
				finalDestination = Vector2.fromXY(destination.x(), Geometry.getGoalOur().getRightPost().y());
			}
			return finalDestination;
		}


		private void blockCircular()
		{
			IVector2 ballPos = getWorldFrame().getBall().getTrajectory().getPosByTime(0.1).getXYVector();

			IVector2 bisector = TriangleMath.bisector(ballPos, Geometry.getGoalOur().getLeftPost(), Geometry
					.getGoalOur().getRightPost());

			IVector2 destination = bisector
					.addNew(ballPos.subtractNew(bisector).scaleToNew(distToGoalCenter));

			destination = setDestinationOutsideGoalPosts(destination);


			getMoveCon().updateDestination(destination);
			getMoveCon().updateTargetAngle(calcDefendingOrientation());
		}


		protected double calcDefendingOrientation()
		{
			return getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle()
					+ turnAngleOfKeeper;
		}


		protected IVector2 setDestinationOutsideGoalPosts(final IVector2 destination)
		{
			IVector2 newDestination = destination;
			// Is Bot able to hit Goalpost?
			IVector2 leftPost = Geometry.getGoalOur().getLeftPost();
			IVector2 rightPost = Geometry.getGoalOur().getRightPost();

			if (isDestinationIn(leftPost, destination))
			{
				newDestination = moveDestinationOutsideGoalpost(leftPost, destination);
			} else if (isDestinationIn(rightPost, destination))
			{
				newDestination = moveDestinationOutsideGoalpost(rightPost, destination);
			}
			if (isDestinationBehindGoalLine(newDestination)
					|| !isDestinationBetweenGoalposts(newDestination))
			{
				newDestination = moveDestinationInsideField(newDestination);
			}
			return newDestination;
		}


		private boolean isDestinationIn(IVector2 goalpost, IVector2 destination)
		{
			return VectorMath.distancePP(destination,
					goalpost) <= (Geometry.getBotRadius() + Geometry.getBallRadius());
		}


		private boolean isDestinationBehindGoalLine(IVector2 destination)
		{
			return destination.x() < Geometry.getGoalOur().getCenter().x();
		}


		private boolean isDestinationBetweenGoalposts(IVector2 destination)
		{
			return Math.abs(destination.y()) < Math.abs(Geometry.getGoalOur().getLeftPost().y() + Geometry.getBotRadius());
		}


		private IVector2 moveDestinationOutsideGoalpost(IVector2 goalpost, IVector2 destination)
		{
			return LineMath.stepAlongLine(goalpost, destination,
					Geometry.getBotRadius() + Geometry.getBallRadius());
		}


		private IVector2 moveDestinationInsideField(IVector2 destination)
		{
			return Vector2.fromXY(
					Geometry.getGoalOur().getCenter().x() + Geometry.getBotRadius() + Geometry.getBallRadius() / 2,
					destination.y());
		}


	}


	/**
	 * Calc the position within
	 *
	 * @param posToCover
	 * @return the position
	 */
	private IVector2 calcBestDefensivePositionInPE(IVector2 posToCover)
	{
		Optional<IVector2> poleCoveringPosition = calcPositionAtPoleCoveringWholeGoal(posToCover);
		IVector2 targetPosition;
		if (poleCoveringPosition.isPresent())
		{
			targetPosition = poleCoveringPosition.get();
		} else
		{
			IVector2 posCoveringWholeGoal = calcPositionCoveringWholeGoal(posToCover);
			if (isPositionInPenaltyArea(posCoveringWholeGoal))
			{
				targetPosition = posCoveringWholeGoal;
			} else if (isTargetPositionOutsideOfField(posCoveringWholeGoal))
			{
				targetPosition = calcNearestGoalPostPosition(posToCover);
			} else
			{
				targetPosition = calcPositionBehindPenaltyArea(posToCover);
			}
		}
		return targetPosition;
	}


	private IVector2 calcPositionBehindPenaltyArea(IVector2 posToCover)
	{
		IVector2 goalCenter = Geometry.getGoalOur().getCenter();
		ILine ballGoalCenter = Line.fromPoints(goalCenter, posToCover);

		List<IVector2> intersectionsList = Geometry.getPenaltyAreaOur().lineIntersections(ballGoalCenter);

		IVector2 intersection = intersectionsList.stream()
				.max((a, b) -> VectorMath.distancePP(a, posToCover) < VectorMath.distancePP(b, posToCover) ? 1 : -1)
				.orElse(posToCover);

		double stepSize = VectorMath.distancePP(goalCenter,
				intersection) - ((2 * Geometry.getBallRadius()) + Geometry.getBotRadius());
		return LineMath.stepAlongLine(goalCenter, intersection, stepSize);
	}


	private IVector2 calcNearestGoalPostPosition(IVector2 posToCover)
	{
		IVector2 leftPole = Geometry.getGoalOur().getLeftPost();
		IVector2 rightPole = Geometry.getGoalOur().getRightPost();

		IVector2 nearestPole;
		if (posToCover.y() < 0)
		{
			nearestPole = rightPole;
		} else
		{
			nearestPole = leftPole;
		}
		return LineMath.stepAlongLine(nearestPole, Geometry.getCenter(),
				Geometry.getBotRadius() + Geometry.getBallRadius());
	}


	private boolean isTargetPositionOutsideOfField(IVector2 targetPos)
	{
		return !Geometry.getField().isPointInShape(targetPos);
	}


	private boolean isPositionInPenaltyArea(IVector2 targetPos)
	{
		return Geometry.getPenaltyAreaOur().isPointInShape(targetPos,
				-((2 * Geometry.getBallRadius()) + Geometry.getBotRadius()));
	}


	private IVector2 calcPositionCoveringWholeGoal(IVector2 posToCover)
	{
		IVector2 goalCenter = Geometry.getGoalOur().getCenter();
		IVector2 leftPole = Geometry.getGoalOur().getLeftPost();
		IVector2 rightPole = Geometry.getGoalOur().getRightPost();

		IVector2 ballGoalOrthoDirection = Line.fromPoints(posToCover, goalCenter).directionVector().getNormalVector();
		ILine ballGoalLineOrtho = Line.fromDirection(goalCenter, ballGoalOrthoDirection);
		ILine ballLeftPose = Line.fromPoints(posToCover, leftPole);
		ILine ballRightPose = Line.fromPoints(posToCover, rightPole);

		double distLM = VectorMath.distancePP(goalCenter,
				LineMath.intersectionPoint(ballGoalLineOrtho, ballLeftPose).orElseThrow(IllegalStateException::new));

		double distRM = VectorMath.distancePP(goalCenter,
				LineMath.intersectionPoint(ballGoalLineOrtho, ballRightPose).orElseThrow(IllegalStateException::new));

		double relativeRadius = (2 * Geometry.getBotRadius() * distLM) / (distLM + distRM);

		double alpha = ballLeftPose.directionVector().angleToAbs(ballGoalOrthoDirection).orElse(0.0);
		// angle should be less than 90° = pi/2
		if ((alpha > (AngleMath.PI / 2)) && (alpha < AngleMath.PI))
		{
			alpha = AngleMath.PI - alpha;
		}

		IVector2 optimalDistanceToBallPosDirectedToGoal = LineMath.stepAlongLine(posToCover,
				goalCenter, relativeRadius * SumatraMath.tan(alpha));

		IVector2 optimalDistanceToBallPosDirectedToRightPose = LineMath.intersectionPoint(ballRightPose,
				Line.fromDirection(optimalDistanceToBallPosDirectedToGoal, ballGoalOrthoDirection))
				.orElseThrow(IllegalStateException::new);

		return LineMath.stepAlongLine(optimalDistanceToBallPosDirectedToRightPose, optimalDistanceToBallPosDirectedToGoal,
				Geometry.getBotRadius());
	}


	private Optional<IVector2> calcPositionAtPoleCoveringWholeGoal(IVector2 posToCover)
	{
		IVector2 leftPole = Geometry.getGoalOur().getLeftPost();
		IVector2 rightPole = Geometry.getGoalOur().getRightPost();

		double distanceRightPole = Line.fromPoints(posToCover, leftPole).distanceTo(rightPole);
		double distanceLeftPole = Line.fromPoints(posToCover, rightPole).distanceTo(leftPole);

		boolean isPositionAtLeftPoleCoverGoal = distanceLeftPole <= Geometry.getBotRadius() * 2 && posToCover.y() > 0;
		boolean isPositionAtRightPoleCoverGoal = distanceRightPole <= Geometry.getBotRadius() * 2 && posToCover.y() < 0;

		Optional<IVector2> pole = Optional.empty();

		if (isPositionAtLeftPoleCoverGoal)
		{
			pole = Optional.of(leftPole);
		} else if (isPositionAtRightPoleCoverGoal)
		{
			pole = Optional.of(rightPole);
		}

		Optional<IVector2> coveringPosition = Optional.empty();
		if (pole.isPresent())
		{
			coveringPosition = Optional.of(LineMath.stepAlongLine(pole.get(), Geometry.getCenter(),
					Geometry.getBotRadius() + Geometry.getBallRadius()));
		}
		return coveringPosition;
	}


	public static double getKeeperAcc()
	{
		return keeperAcc;
	}


	public static double getTurnAngleOfKeeper()
	{
		return turnAngleOfKeeper;
	}
}
