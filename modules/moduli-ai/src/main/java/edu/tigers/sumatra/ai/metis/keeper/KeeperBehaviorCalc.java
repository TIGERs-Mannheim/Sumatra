/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.keeper;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballinterception.BallInterception;
import edu.tigers.sumatra.ai.metis.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.pass.rating.PassInterceptionMovingRobotRater;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.DrawableShapeBoundary;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.intersections.ISingleIntersection;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;


@RequiredArgsConstructor
public class KeeperBehaviorCalc extends ACalculator
{
	private static final IVector2 DEFAULT_POS = Geometry.getPenaltyAreaOur().getRectangle().center();
	@Configurable(comment = "[s] Time offset that we think the opponent needs to react to rambo", defValue = "0.3")
	private static double ramboTimeOffset = 0.3;
	@Configurable(comment = "[mm] Minimum distance between pen area boundary and ball to try chipping", defValue = "200")
	private static double minMarginToChip = 200;
	@Configurable(comment = "[mm] How much space we assume to need for ball handling", defValue = "50")
	private static double ballHandlingExtraMargin = 50;

	@Configurable(comment = "[s] Interception slack time considered safe", defValue = "-0.25")
	private static double slackTimeConsideredSafe = -0.25;
	@Configurable(comment = "[s] Interception slack time considered safe", defValue = "0.4")
	private static double corridorWidthConsideredSafe = 0.4;
	@Configurable(comment = "[m/s] Max speed of ball in PenArea to consider it less dangerous.", defValue = "0.2")
	private static double maxBallSpeedToAllowSlowApproach = 0.2;
	@Configurable(comment = "[m/s] Lower min speed of ball necessary for ball interception", defValue = "0.1")
	private static double minBallSpeedForInterceptLower = 0.1;
	@Configurable(comment = "[m/s] Upper min speed of ball necessary for ball interception", defValue = "1.0")
	private static double minBallSpeedForInterceptUpper = 1.0;
	@Configurable(comment = "[0...1] A higher value will increase the uncertainty applied to the ball prediction", defValue = "0.15")
	private static double factorVelocityToBallStopUncertainty = 0.15;
	@Configurable(comment = "Opponent ball dist to chill", defValue = "1000.0")
	private static double opponentToBallDistToChill = 1000;

	@Configurable(comment = "[0-1] Required pass intercept-ability rating to consider ball will not be intercepted, the higher the safer we need to be", defValue = "0.95")
	private static double minPassRatingForUndisturbedBall = 0.95;


	private final Supplier<Double> keeperRamboDistance;
	private final Supplier<BotDistance> opponentClosestToBall;
	private final Supplier<RatedBallInterception> keeperBallInterception;

	@NonNull
	@Getter
	private EKeeperActionType selectedBehavior = EKeeperActionType.DEFEND;

	@NonNull
	@Getter
	private IVector2 interceptPos = DEFAULT_POS;


	/**
	 * The ball must be chipped from within this margin to ensure we chip over the opponent
	 * If the ball is stopped outside, HANDLE_BALL must be active to place the ball correctly
	 */
	public static IKeeperPenAreaMarginProvider ballMarginLower()
	{
		return new IKeeperPenAreaMarginProvider()
		{
			@Override
			public double atBorder()
			{
				return -minMarginToChip;
			}


			@Override
			public double atGoal()
			{
				return 0;
			}
		};
	}


	/**
	 * Every State shall try to get Ball Contact within this margin to ensure we have space for ball handling
	 */
	public static IKeeperPenAreaMarginProvider ballMarginMiddle()
	{
		return ballMarginLower().withExtraMargin(-ballHandlingExtraMargin);
	}


	/**
	 * Target distance for ball placement
	 */
	public static IKeeperPenAreaMarginProvider ballMarginUpper()
	{
		return ballMarginMiddle().withExtraMargin(-ballHandlingExtraMargin);
	}


	@Override
	protected void doCalc()
	{
		var interceptPrior = interceptPos;
		interceptPos = DEFAULT_POS;
		if (getBot() == null)
		{
			selectedBehavior = EKeeperActionType.DEFEND;
		} else if (getAiFrame().getGameState().isStoppedGame())
		{
			selectedBehavior = EKeeperActionType.STOP;
		} else if (getAiFrame().getGameState().isPreparePenaltyForThem())
		{
			selectedBehavior = EKeeperActionType.PREP_PENALTY;
		} else if (canGoRambo())
		{
			selectedBehavior = EKeeperActionType.RAMBO;
		} else if (needToMoveToPenArea())
		{
			selectedBehavior = EKeeperActionType.MOVE_TO_PEN_AREA;
		} else if (isBallBetweenKeeperAndGoal())
		{
			selectedBehavior = EKeeperActionType.MOVE_BETWEEN_GOAL_AND_BALL;
		} else if (canInterceptSafely())
		{
			selectedBehavior = EKeeperActionType.INTERCEPT_PASS;
			interceptPos = Optional.ofNullable(keeperBallInterception.get())
					.map(RatedBallInterception::getBallInterception)
					.map(BallInterception::getPos)
					.orElse(interceptPrior);
		} else if (needToFocusOnDefend())
		{
			selectedBehavior = EKeeperActionType.IMPORTANT_DEFEND;
		} else if (canHandleBall())
		{
			selectedBehavior = EKeeperActionType.HANDLE_BALL;
		} else
		{
			selectedBehavior = EKeeperActionType.DEFEND;
		}

		drawShapes();
	}


	private void drawShapes()
	{
		var shapes = getShapes(EAiShapesLayer.KEEPER_BEHAVIOR);
		if (getBot() != null)
		{
			shapes.add(new DrawableAnnotation(getPos().addNew(Vector2.fromY(150)), selectedBehavior.toString(), true));
		}

		var debug = getShapes(EAiShapesLayer.KEEPER_BEHAVIOR_DEBUG);
		Stream.of(ballMarginLower().atBorder(), ballMarginMiddle().atBorder(), ballMarginUpper().atBorder(),
						ballHandlingAreaMargin())
				.map(margin -> Geometry.getPenaltyAreaOur().withMargin(margin))
				.map(I2DShape::getShapeBoundary)
				.map(boundary -> new DrawableShapeBoundary(boundary, Color.GRAY))
				.forEach(debug::add);
		Stream.of(ballMarginLower().atGoal(), ballMarginMiddle().atGoal(), ballMarginUpper().atGoal())
				.map(Math::abs)
				.map(margin -> Rectangle.aroundLine(Geometry.getGoalOur().getLineSegment(), margin))
				.map(rect -> new DrawableRectangle(rect, Color.GRAY))
				.forEach(debug::add);
	}


	private boolean canGoRambo()
	{
		ITrackedBot opponent = getWFrame().getBot(opponentClosestToBall.get().getBotId());
		if (!getAiFrame().getGameState().isPenalty()
				|| isBallInPenaltyArea(0)
				|| isGoalKick()
				|| opponent == null)
		{
			return false;
		}
		if (selectedBehavior == EKeeperActionType.RAMBO)
		{
			// Avoid toggling -> Once Rambo always rambo in the current penalty
			return true;
		}

		boolean isKeeperOnLine;
		if (getBall().getVel().getLength2() > 0.3)
		{
			ILine line = Lines.lineFromDirection(getBall().getPos(), getBall().getVel());
			isKeeperOnLine = line.distanceTo(getPos()) < Geometry.getBotRadius() / 2.;
		} else
		{
			isKeeperOnLine = false;
		}

		double timeOpponent = TrajectoryGenerator.generatePositionTrajectory(opponent, getBall().getPos()).getTotalTime();
		double timeKeeper = TrajectoryGenerator.generatePositionTrajectory(getBot(), getBall().getPos()).getTotalTime();

		boolean isBallCloseToGoal = isBallInPenaltyArea(keeperRamboDistance.get());
		boolean isKeeperFaster = timeOpponent + ramboTimeOffset > timeKeeper;
		boolean isBallInFrontOfKeeper = getPos().x() < getWFrame().getBall().getPos().x();
		boolean isBallGoingTowardsGoal = Math.abs(getBall().getVel().getAngle()) > AngleMath.PI_HALF;

		return ((isBallCloseToGoal && isKeeperOnLine) || isKeeperFaster)
				&& isBallInFrontOfKeeper
				&& isBallGoingTowardsGoal;
	}


	private boolean needToMoveToPenArea()
	{
		if (selectedBehavior == EKeeperActionType.MOVE_TO_PEN_AREA)
		{
			return !Geometry.getPenaltyAreaOur().withMargin(ballMarginLower().atBorder()).isPointInShape(getPos());
		}
		var shapeWithMargin = Geometry.getPenaltyAreaOur().withMargin(1000);
		return !shapeWithMargin.isPointInShapeOrBehind(getPos());
	}


	private boolean isBallBetweenKeeperAndGoal()
	{
		var ballOnGoal = Geometry.getGoalOur().getLineSegment().closestPointOnPath(getBall().getPos());
		var ballToGoal = Vector2.fromPoints(getBall().getPos(), ballOnGoal);
		var ballToBot = Vector2.fromPoints(getBall().getPos(), getPos());
		var isBehindKeeper = AngleMath.diffAbs(ballToBot.getAngle(), ballToGoal.getAngle()) > 0.75 * AngleMath.PI;
		return isBallInPenaltyArea(ballMarginLower().atBorder()) && isBallStopped() && isBehindKeeper;
	}


	private boolean canInterceptSafely()
	{
		if (!isBallMovingSufficientlyForIntercept())
		{
			return false;
		}
		if (!isBallMovingTowardsKeeper() && ballToBotDist() > 2 * Geometry.getBotRadius())
		{
			return false;
		}
		if (!willBallEnterPenAreaUndisturbed())
		{
			return false;
		}
		var margin = selectedBehavior == EKeeperActionType.INTERCEPT_PASS
				? KeeperBehaviorCalc.ballMarginLower()
				: KeeperBehaviorCalc.ballMarginMiddle();
		return Optional.ofNullable(keeperBallInterception.get())
				.map(i -> isBallInterceptionSafe(i, margin))
				.orElse(false);
	}


	private boolean needToFocusOnDefend()
	{
		return isGoalKick() || !isBallPosInsideHandlingArea(getBall().getPos());
	}


	private boolean canHandleBall()
	{
		if (selectedBehavior == EKeeperActionType.HANDLE_BALL)
		{
			return isBallRangeInsideHandlingArea(ballStopRange()) ||
					(isBallPosInsideHandlingArea(getBall().getPos())
							&& getBot().getRobotInfo().getDribbleTraction().getId() >= EDribbleTractionState.LIGHT.getId());
		} else
		{
			return isBallRangeInsideHandlingArea(ballStopRange()) && isBallStopped();
		}
	}


	private boolean isBallInPenaltyArea(final double getBallPenaltyAreaMargin)
	{
		return Geometry.getPenaltyAreaOur().withMargin(getBallPenaltyAreaMargin).isPointInShape(getBall().getPos());
	}


	private boolean isBallPosSafeForControl(IVector2 point, IKeeperPenAreaMarginProvider marginProvider)
	{
		var penArea = Geometry.getPenaltyAreaOur().withMargin(marginProvider.atBorder());
		return penArea.isPointInShape(point)
				&& Geometry.getGoalOur().getLineSegment().distanceTo(point) > Math.abs(marginProvider.atGoal());
	}


	private boolean isGoalKick()
	{
		var goalSegment = Geometry.getGoalOur().getLineSegment().withMargin(4 * Geometry.getBotRadius());
		return Stream.concat(
						Stream.of(ballWithUncertainty()),
						curvedBallWithUncertainty().stream()
				)
				.map(goalSegment::intersect)
				.anyMatch(ISingleIntersection::isPresent);
	}


	private boolean isBallEndangered()
	{
		var botDist = opponentClosestToBall.get().getDist();
		if (botDist > opponentToBallDistToChill)
		{
			return false;
		}
		if (botDist < 3 * Geometry.getBotRadius())
		{
			return true;
		}

		var maxVel = 0.1 + 0.9 * SumatraMath.relative(botDist, 3 * Geometry.getBotRadius(), opponentToBallDistToChill);
		var opponentVel = getWFrame().getBot(opponentClosestToBall.get().getBotId()).getVel().getLength2();
		return maxVel >= opponentVel;
	}


	private double ballHandlingAreaMargin()
	{
		var extra = selectedBehavior == EKeeperActionType.HANDLE_BALL ? ballHandlingExtraMargin : 0;
		if (isBallEndangered())
		{
			return extra + Math.max(-2 * Geometry.getBallRadius() - ballHandlingExtraMargin, ballMarginLower().atBorder());
		} else
		{
			return extra + Geometry.getBotRadius();
		}
	}


	private boolean isBallPosInsideHandlingArea(IVector2 pos)
	{
		return Geometry.getPenaltyAreaOur().withMargin(ballHandlingAreaMargin()).isPointInShapeOrBehind(pos);
	}


	private boolean isBallRangeInsideHandlingArea(ILineSegment ballRange)
	{
		return isBallPosInsideHandlingArea(ballRange.getPathStart())
				&& isBallPosInsideHandlingArea(ballRange.getPathEnd());
	}


	private boolean willBallEnterPenAreaUndisturbed()
	{
		var ballPos = getBall().getPos();
		if (isBallPosInsideHandlingArea(ballPos))
		{
			return true;
		}

		var entryPoint = ballPos.nearestToOpt(
				Geometry.getPenaltyAreaOur().intersectPerimeterPath(getBall().getTrajectory().getTravelLineSegment())
		).orElse(null);
		if (entryPoint == null)
		{
			return false;
		}


		var consideredBots = getWFrame().getBots().values().stream()
				.filter(bot -> !bot.getBotId().equals(getBotID()))
				.toList();
		var rater = new PassInterceptionMovingRobotRater(consideredBots, Collections.emptyList());
		var shapes = getShapes(EAiShapesLayer.KEEPER_BEHAVIOR_DEBUG);
		rater.setShapes(shapes);
		rater.setScoringFactorOffset(-0.3);
		rater.setRobotMovementLimitFactor(1.3);
		var rating = rater.rateRollingBall(getBall(), getBall().getTrajectory().getTimeByPos(entryPoint));
		shapes.add(new DrawableAnnotation(getBall().getPos().addNew(Vector2.fromY(50)), String.format("%.2f", rating),
				getWFrame().getTeamColor()
						.getColor()));
		return rating >= minPassRatingForUndisturbedBall;
	}


	private double ballToBotDist()
	{
		return getBall().getPos().distanceTo(getPos());
	}


	private boolean isBallMovingSufficientlyForIntercept()
	{
		double requiredSpeed;
		if (selectedBehavior == EKeeperActionType.INTERCEPT_PASS || !isBallInPenaltyArea(0))
		{
			requiredSpeed = minBallSpeedForInterceptLower;
		} else
		{
			requiredSpeed = minBallSpeedForInterceptUpper;
		}
		return getBall().getVel().getLength() > requiredSpeed;
	}


	private boolean isBallMovingTowardsKeeper()
	{
		var ballDir = getBall().getVel();
		var ballToBotDir = getPos().subtractNew(getBall().getPos());
		var angle = ballDir.angleToAbs(ballToBotDir).orElse(0.0);
		return angle < AngleMath.DEG_090_IN_RAD;
	}


	private boolean isBallInterceptionSafe(RatedBallInterception interception,
			IKeeperPenAreaMarginProvider marginProvider)
	{
		if (!isBallPosSafeForControl(interception.getBallInterception().getPos(), marginProvider))
		{
			return false;
		}
		if (isGoalKick())
		{
			return interception.getMinCorridorSlackTime() < 2 * slackTimeConsideredSafe
					|| interception.getCorridorLength() > 2 * corridorWidthConsideredSafe;
		}
		return interception.getMinCorridorSlackTime() < slackTimeConsideredSafe
				|| interception.getCorridorLength() > corridorWidthConsideredSafe;
	}


	private double calcBallStopUncertainty()
	{
		return getBall().getVel().getLength() * 1000 * factorVelocityToBallStopUncertainty;
	}


	private ILineSegment ballStopRange()
	{
		var margin = calcBallStopUncertainty();
		var finalBallPos = getBall().getTrajectory().getTravelLineSegment().getPathEnd();
		var offset = getBall().getTrajectory().getTravelLineSegment().directionVector().scaleToNew(margin);
		var stop = Lines.segmentFromPoints(finalBallPos.subtractNew(offset), finalBallPos.addNew(offset));
		getShapes(EAiShapesLayer.KEEPER_BEHAVIOR_DEBUG).add(new DrawableLine(stop, Color.RED));
		return stop;
	}


	private ILineSegment ballWithUncertainty()
	{
		var margin = calcBallStopUncertainty();
		var seg = getBall().getTrajectory().getTravelLineSegment();
		var newSeg = Lines.segmentFromPoints(
				seg.getPathStart(),
				seg.getPathEnd().addNew(seg.directionVector().scaleToNew(margin))
		);
		getShapes(EAiShapesLayer.KEEPER_BEHAVIOR_DEBUG).add(new DrawableLine(newSeg, Color.BLUE.darker()));
		return newSeg;
	}


	private List<ILineSegment> curvedBallWithUncertainty()
	{
		var margin = calcBallStopUncertainty();
		var segments = new ArrayList<>(getBall().getTrajectory().getTravelLineSegments());
		if (segments.isEmpty())
		{
			return List.of();
		}
		var last = segments.getLast();
		var newLast = Lines.segmentFromPoints(
				last.getPathStart(),
				last.getPathEnd().addNew(last.directionVector().scaleToNew(margin))
		);
		segments.set(segments.size() - 1, newLast);
		segments.stream()
				.map(seg -> new DrawableLine(seg, Color.BLUE.brighter()))
				.forEach(shape -> getShapes(EAiShapesLayer.KEEPER_BEHAVIOR_DEBUG).add(shape));
		return Collections.unmodifiableList(segments);
	}


	private boolean isBallStopped()
	{
		return getBall().getVel().getLength() < maxBallSpeedToAllowSlowApproach;
	}


	private IVector2 getPos()
	{
		return getBot().getPos();
	}


	private ITrackedBot getBot()
	{
		return getWFrame().getBot(getBotID());
	}


	private BotID getBotID()
	{
		return getAiFrame().getKeeperId();
	}
}
