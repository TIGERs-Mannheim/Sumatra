/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.ERotationDirection;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.VectorDistanceComparator;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.DefendingKeeper.ECriticalKeeperStates;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * Skill Implementation for Time critical keeper skills. Used by CriticalKeeperState
 */
public class CriticalKeeperSkill extends AMoveToSkill
{
	@Configurable(comment = "The radius to try intercepting the chip-kicked ball within", defValue = "500.0")
	private static double maxChipInterceptDist = 500.0;

	@Configurable(comment = "Max. Acceleration in Catch Skill", defValue = "2.5")
	private static double maxAcc = 2.5;

	@Configurable(comment = "Max acceleration of Keeper", defValue = "2.5")
	private static double keeperAcc = 2.5;

	@Configurable(comment = "Scaling Factor for Dist to GoalCenter in NormalBlockState", defValue = "1.0")
	private static double distanceToGoalCenterScalingFactor = 1.0;

	@Configurable(comment = "Keeper goes out if redirecting bot is behind this margin added to the penalty area", defValue = "-2022.5")
	private static double goOutWhileRedirectMargin = Geometry.getGoalOur().getCenter().x() / 2.0;

	@Configurable(comment = "Keeper's normal movement is circular", defValue = "true")
	private static boolean isKeepersNormalMovementCircular = true;

	@Configurable(comment = "Use MoveConstraints PrimaryDirection during intercept", defValue = "true")
	private static boolean usePrimaryDirectionsIntercept = true;
	@Configurable(comment = "Use MoveConstraints PrimaryDirection while going out or catching redirects", defValue = "true")
	private static boolean usePrimaryDirectionOuterBlockingStates = true;
	@Configurable(comment = "[mm] distance between samples to find optimal intercept destination", defValue = "10")
	private static int samplesInterceptDestinationGranularity = 10;

	private ECriticalKeeperStates currentState = ECriticalKeeperStates.NORMAL;
	private DefendingKeeper defendingKeeper = new DefendingKeeper(this);


	private double getDistanceToGoalCenter()
	{
		return Math.min(Geometry.getGoalOur().getWidth() / 2.0 - Geometry.getBotRadius(),
				Geometry.getPenaltyAreaDepth() - Geometry.getBotRadius() * 2.0) * distanceToGoalCenterScalingFactor;
	}


	public CriticalKeeperSkill()
	{
		var blockState = new NormalBlock();
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

		calcBestBisectorDefensivePositionDeflectionAdapted(getBall().getPos(), getDistanceToGoalCenter());
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


	private class InterceptBall extends AState
	{
		private double targetAngle = 0;


		@Override
		public void doEntryActions()
		{
			targetAngle = getAngle();
			prepareMoveCon();
			prepareMovementConstraints();
			getMoveConstraints().setAccMax(maxAcc);
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
			if (usePrimaryDirectionsIntercept)
			{
				getMoveConstraints().setPrimaryDirection(getBall().getVel());
			} else
			{
				getMoveConstraints().setPrimaryDirection(Vector2f.ZERO_VECTOR);
			}

			var destination = calcDestination();
			targetAngle = calcTargetAngle(destination);

			updateDestination(destination);
			updateTargetAngle(targetAngle);

			super.doUpdate();
			draw();
		}


		private void draw()
		{
			getBot().getCurrentTrajectory().ifPresent(botTraj -> getShapes().get(ESkillShapesLayer.PATH_DEBUG)
					.add(new DrawableLine(
							Line.fromDirection(getPos(), botTraj.getAcceleration(0).getXYVector().multiplyNew(1000)),
							Color.BLACK)));
		}


		private double calcTargetAngle(final IVector2 destination)
		{
			if (VectorMath.distancePP(destination, getTBot().getPos()) < (Geometry.getBotRadius() / 2))
			{
				return getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle();
			}
			return targetAngle;
		}


		private IVector2 findBestDestinationWithinPenArea(IVector2 fallbackPoint)
		{
			var penAreaWithMargin = Geometry.getPenaltyAreaOur().withMargin(-100);
			var ballPos = getBall().getPos();
			var ballInsidePenArea = penAreaWithMargin.isPointInShape(ballPos);
			var ballLine = Lines.lineFromDirection(ballPos, getBall().getVel());
			var pointACandidates = penAreaWithMargin.lineIntersections(ballLine);
			var pointBCandidate = Geometry.getGoalOur().getLine().intersectLine(ballLine);
			if (pointACandidates.size() != 1 || pointBCandidate.isEmpty())
			{
				return fallbackPoint;
			}

			final IVector2 pointA = pointACandidates.get(0);
			final IVector2 pointB = pointBCandidate.get();

			var searchDist = pointA.distanceTo(pointB);
			var numSamples = (int) (searchDist / samplesInterceptDestinationGranularity);
			var distBallToA = ((ballInsidePenArea) ? -1 : 1) * ballPos.distanceTo(pointA);

			var bestPoint = SumatraMath.evenDistribution1D(0, searchDist, numSamples).stream()
					.map(dist -> buildDestWithTrajectory(getBall().getVel().scaleToNew(dist), pointA, distBallToA))
					.max(DestWithTrajectory::compareTo)
					.map(DestWithTrajectory::dest)
					.orElse(fallbackPoint);

			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableLine(Lines.segmentFromPoints(pointA, pointB), Color.WHITE));
			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableCircle(Circle.createCircle(bestPoint, 35), Color.GREEN));
			return bestPoint;
		}


		private DestWithTrajectory buildDestWithTrajectory(IVector2 offset, IVector2 pointA, double distBallToA)
		{
			var dest = pointA.addNew(offset);
			var ballDist = distBallToA + offset.getLength();
			var tt = ballDist > 0 ? getBall().getTrajectory().getTimeByDist(ballDist) : 0.0;
			var virtualDest = TrajectoryGenerator.generateVirtualPositionToReachPointInTime(getTBot(),
					getMoveConstraints(), dest, tt);
			var trajectory = TrajectoryGenerator.generatePositionTrajectory(getTBot(), virtualDest,
					getMoveConstraints());
			var posAtTt = trajectory.getPositionMM(tt);
			var distAtTt = posAtTt.distanceTo(dest);
			var velAtTt = trajectory.getVelocity(tt).getLength();
			var timeLeftAtTt = tt - trajectory.getTotalTime();
			var canReach = distAtTt < Geometry.getBotRadius();
			var canReachCompletely = velAtTt < 1e-3;
			var isCloseToGoalLine = Geometry.getGoalOur().getLine().distanceTo(dest) < Geometry.getBotRadius() * 3;
			return new DestWithTrajectory(
					dest,
					trajectory,
					distAtTt,
					velAtTt,
					timeLeftAtTt,
					canReach,
					canReachCompletely,
					isCloseToGoalLine
			);
		}


		private IVector2 calcDestination()
		{
			var fallBackPoint = findPointOnBallTraj();
			var bestPoint = findBestDestinationWithinPenArea(fallBackPoint);
			bestPoint = adaptDestinationToChipKick(bestPoint);
			bestPoint = moveInterceptDestinationInsideField(bestPoint);
			return calcOverAcceleration(bestPoint);
		}


		private IVector2 adaptDestinationToChipKick(final IVector2 destination)
		{
			final var goalLine = Geometry.getGoalOur().getLine();
			return getWorldFrame().getBall().getTrajectory().getTouchdownLocations().stream()
					.filter(td -> td.x() > Geometry.getGoalOur().getCenter().x())
					.filter(td -> td.distanceTo(getPos()) < maxChipInterceptDist)
					.min(Comparator.comparingDouble(goalLine::distanceToSqr))
					.orElse(destination);
		}


		private IVector2 moveInterceptDestinationInsideField(IVector2 destination)
		{
			return Geometry.getField().withMargin(Geometry.getBotRadius())
					.nearestPointInside(destination, getBall().getPos());
		}


		private IVector2 calcOverAcceleration(final IVector2 destination)
		{
			final double targetTime;
			if (Math.abs(destination.subtractNew(getBall().getPos()).getAngle()) > AngleMath.PI_HALF)
			{
				targetTime = getBall().getTrajectory().getTimeByPos(destination);
			} else
			{
				targetTime = 0.0;
			}
			return TrajectoryGenerator.generateVirtualPositionToReachPointInTime(
					getTBot(),
					getMoveConstraints(),
					destination,
					targetTime
			);
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


		record DestWithTrajectory(
				IVector2 dest,
				ITrajectory<IVector2> trajectory,
				double distAtTt,
				double velAtTt,
				double timeLeftAtTt,
				boolean canReach,
				boolean canReachCompletely,
				boolean isCloseToGoalLine
		) implements Comparable<DestWithTrajectory>
		{
			/**
			 * Compare such that the "highest" object is the best object
			 *
			 * @param other
			 * @return -1 if other is better, 1 if this is better
			 */
			@Override
			public int compareTo(DestWithTrajectory other)
			{
				if (!canReach)
				{
					if (other.canReach)
					{
						// We can't reach, but other can -> other wins
						return -1;
					} else
					{
						// Smaller distance wins
						return -Double.compare(distAtTt, other.distAtTt);
					}
				}

				// canReach == true
				if (!canReachCompletely)
				{
					if (other.canReachCompletely)
					{
						// We can't reach completely, but other can -> other wins
						return -1;
					} else if (!other.canReach)
					{
						// We can reach, but other can't -> we win
						return 1;
					} else
					{
						// Smaller velocity wins
						return -Double.compare(velAtTt, other.velAtTt);
					}
				}

				// canReachCompletely == true
				if (!other.canReachCompletely)
				{
					// We can reach completely, but other can't -> we win
					return 1;
				}
				if (isCloseToGoalLine != other.isCloseToGoalLine)
				{
					// Position not close to goal line wins
					return -Boolean.compare(isCloseToGoalLine, other.isCloseToGoalLine);
				}
				// Higher timeLeft wins
				return Double.compare(timeLeftAtTt, other.timeLeftAtTt);
			}
		}
	}


	private IVector2 getPrimaryDirectionOuterBlockingStates(final IVector2 threatPosition,
			final IVector2 defensePosition)
	{
		IVector2 finalPrimaryDirection = Vector2f.ZERO_VECTOR;
		if (usePrimaryDirectionOuterBlockingStates)
		{
			final IVector2 primaryDirection = Vector2.fromPoints(threatPosition, defensePosition);
			final var keeperPrimaryDirectionIntersection = Lines.halfLineFromDirection(getPos(), getVel())
					.intersectLine(Lines.lineFromDirection(threatPosition, primaryDirection));
			final Color primaryDirectionColor;
			if (keeperPrimaryDirectionIntersection.isPresent() && Geometry.getField()
					.isPointInShape(keeperPrimaryDirectionIntersection.get()))
			{
				finalPrimaryDirection = primaryDirection;
				primaryDirectionColor = Color.BLACK;
			} else
			{
				primaryDirectionColor = Color.GRAY;
			}
			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableLine(Line.fromDirection(getPos(), primaryDirection.scaleToNew(-1000)),
							primaryDirectionColor));
		}
		return finalPrimaryDirection;
	}


	private IVector2 calcBestBisectorDefensivePositionDeflectionAdapted(final IVector2 posToCover,
			final double distanceToGoalLine)
	{
		var shapes = getShapes().get(ESkillShapesLayer.KEEPER_DEFLECTION_ADAPTION);
		final IVector2 goalLineBisector = TriangleMath.bisector(posToCover, Geometry.getGoalOur().getLeftPost(), Geometry
				.getGoalOur().getRightPost());

		final IVector2 unadaptedPos = LineMath.stepAlongLine(goalLineBisector, posToCover, distanceToGoalLine);

		final IVector2 leftPostBisector = TriangleMath
				.bisector(unadaptedPos, Geometry.getGoalOur().getLeftPost(), posToCover);
		final IVector2 rightPosBisector = TriangleMath
				.bisector(unadaptedPos, posToCover, Geometry.getGoalOur().getRightPost());


		shapes.add(new DrawableLine(
				Line.fromDirection(unadaptedPos, Vector2.fromPoints(unadaptedPos, rightPosBisector).scaleToNew(250)),
				Color.RED).setStrokeWidth(5));
		shapes.add(new DrawableLine(
				Line.fromDirection(unadaptedPos, Vector2.fromPoints(unadaptedPos, leftPostBisector).scaleToNew(250)),
				Color.RED).setStrokeWidth(5));
		shapes.add(new DrawableLine(
				Line.fromDirection(unadaptedPos, Vector2.fromPoints(unadaptedPos, posToCover).scaleToNew(250)), Color.BLUE)
				.setStrokeWidth(5));
		shapes.add(new DrawableLine(Line.fromDirection(unadaptedPos,
				Vector2.fromPoints(unadaptedPos, Geometry.getGoalOur().getLeftPost()).scaleToNew(250)), Color.BLUE)
				.setStrokeWidth(5));
		shapes.add(new DrawableLine(Line.fromDirection(unadaptedPos,
				Vector2.fromPoints(unadaptedPos, Geometry.getGoalOur().getRightPost()).scaleToNew(250)), Color.BLUE)
				.setStrokeWidth(5));

		final double keeperLockingDirection = Vector2.fromPoints(unadaptedPos, posToCover).getAngle();
		final double leftBlockingRatio = Vector2.fromPoints(unadaptedPos, leftPostBisector).getAngle();
		final double rightBlockingRatio = Vector2.fromPoints(unadaptedPos, rightPosBisector).getAngle();

		final double leftBlockingDist =
				AngleMath.sin(AngleMath.diffAbs(keeperLockingDirection, leftBlockingRatio)) * Geometry.getBotRadius();
		final double rightBlockingDist =
				AngleMath.sin(AngleMath.diffAbs(keeperLockingDirection, rightBlockingRatio)) * Geometry.getBotRadius();

		final IVector2 leftToRightVector = Vector2.fromAngle(
				AngleMath.rotateAngle(keeperLockingDirection, AngleMath.PI_HALF, ERotationDirection.CLOCKWISE));


		final IVector2 offset = leftToRightVector.scaleToNew(leftBlockingDist - rightBlockingDist);


		shapes.add(new DrawableLine(Line.fromDirection(unadaptedPos, offset), Color.PINK));

		return unadaptedPos.addNew(offset);
	}


	private class CatchRedirect extends AState
	{
		private boolean stayInGoOut = false;


		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			prepareMovementConstraints();
			getMoveCon().setFieldBorderObstacle(false);
			getMoveConstraints().setAccMax(keeperAcc);
			stayInGoOut = false;

			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			IVector2 redirectBot = getRedirectBotPosition();
			IVector2 destination = calcBestBisectorDefensivePositionDeflectionAdapted(redirectBot,
					getDistanceToGoalCenter());

			if (VectorMath.distancePP(getPos(), destination) < (Geometry.getBotRadius() / 2))
			{
				updateLookAtTarget(redirectBot);
			}
			getMoveConstraints().setPrimaryDirection(getPrimaryDirectionOuterBlockingStates(redirectBot, destination));
			if ((isKeeperBetweenRedirectAndGoalCenter(redirectBot) || stayInGoOut) && isGoOutUseful(redirectBot))
			{
				stayInGoOut = true;
				destination = calcBestDefensivePositionInPE(redirectBot);
				updateLookAtTarget(redirectBot);
			}
			updateDestination(destination);
			super.doUpdate();
		}


		private IVector2 getRedirectBotPosition()
		{
			IVector2 redirectBot;
			var redirectBotId = defendingKeeper.getBestRedirector(getWorldFrame().getOpponentBots());
			if (redirectBotId.isBot())
			{
				redirectBot = getWorldFrame().getOpponentBot(redirectBotId).getBotKickerPos();
			} else
			{
				// .getOpponentClosestToBall from tactical field
				redirectBot = getWorldFrame().getOpponentBots().values().stream().map(ITrackedBot::getBotKickerPos)
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

	private class GoOut extends AState
	{
		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			prepareMovementConstraints();
			getMoveConstraints().setAccMax(keeperAcc);
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			IVector2 targetPosition;
			final IVector2 positionBehindBall = calcPositionBehindBall();
			if (isBallInsidePenaltyArea())
			{
				targetPosition = positionBehindBall;
			} else
			{
				targetPosition = calcBestDefensivePositionInPE(getWorldFrame().getBall().getPos());
			}
			getMoveConstraints()
					.setPrimaryDirection(getPrimaryDirectionOuterBlockingStates(getBall().getPos(), positionBehindBall));
			updateDestination(targetPosition);
			updateTargetAngle(getPos().subtractNew(Geometry.getGoalOur().getCenter()).getAngle());

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
			final double distanceToBall = TriangleMath.bisector(ballPos, Geometry.getGoalOur().getLeftPost(), Geometry
					.getGoalOur().getRightPost()).distanceTo(ballPos);
			return calcBestBisectorDefensivePositionDeflectionAdapted(ballPos, distanceToBall - Geometry.getBotRadius());
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


	private class NormalBlock extends AState
	{
		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			prepareMovementConstraints();
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
			ILine ballGoalCenterLine = Line.fromPoints(getWorldFrame().getBall().getPos(),
					Geometry.getGoalOur().getCenter());

			Optional<IVector2> intersection = newGoalLine.intersectionWith(ballGoalCenterLine);

			IVector2 destination = Geometry.getGoalOur().getCenter();

			if (intersection.isPresent())
			{

				destination = checkPosts(intersection.get());
			}
			updateDestination(destination);
			updateLookAtTarget(getWorldFrame().getBall().getPos());
		}


		private IVector2 checkPosts(IVector2 destination)
		{
			IVector2 finalDestination = destination;
			boolean isBallBehindGoalLine = getWorldFrame().getBall().getPos().x() < Geometry.getGoalOur().getCenter().x();
			boolean isDestinationLeftFromLeftPost = destination.y() > Geometry.getGoalOur().getLeftPost().y();
			boolean isDestinationRightFromRightPost = destination.y() < Geometry.getGoalOur().getRightPost().y();
			boolean isBallLeftFromGoal = getWorldFrame().getBall().getPos().y() > 0;

			if ((isDestinationLeftFromLeftPost && !isBallBehindGoalLine) || (isBallBehindGoalLine && isBallLeftFromGoal))
			{
				finalDestination = Vector2.fromXY(destination.x(), Geometry.getGoalOur().getLeftPost().y());
			} else if (isDestinationRightFromRightPost || isBallBehindGoalLine)
			{
				finalDestination = Vector2.fromXY(destination.x(), Geometry.getGoalOur().getRightPost().y());
			}
			return finalDestination;
		}


		private void blockCircular()
		{
			IVector2 ballPos = getWorldFrame().getBall().getTrajectory().getPosByTime(0.1).getXYVector();
			IVector2 destination = calcBestBisectorDefensivePositionDeflectionAdapted(ballPos, getDistanceToGoalCenter());
			destination = Geometry.getPenaltyAreaOur().withMargin(-Geometry.getBotRadius() - Geometry.getBallRadius())
					.nearestPointInside(destination);
			destination = setDestinationOutsideGoalPosts(destination);

			updateDestination(destination);
			updateLookAtTarget(getBall());
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
			double x = Math.max(Geometry.getGoalOur().getCenter().x() + Geometry.getBotRadius()
					+ Geometry.getBallRadius() / 2, destination.x());
			return Vector2.fromXY(x, destination.y());
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
				.min(new VectorDistanceComparator(posToCover))
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
}
