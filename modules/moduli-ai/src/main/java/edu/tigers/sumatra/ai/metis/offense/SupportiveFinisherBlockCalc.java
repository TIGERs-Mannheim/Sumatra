/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribblingInformation;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.penaltyarea.IPenaltyArea;
import edu.tigers.sumatra.math.triangle.ITriangle;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Calculates a position near the opponents penalty area where a supportive attacker can block the opponents defense
 * from moving on the extended penArea path.
 */
@RequiredArgsConstructor
public class SupportiveFinisherBlockCalc extends ACalculator
{
	@Configurable(defValue = "1500.0")
	private static double marginToPenAreaToConsiderBlock = 1500;

	@Configurable(defValue = "800.0")
	private static double considerOpponentsWithingPenAreaMargin = 800;

	@Configurable(defValue = "80.0")
	private static double moveBorderPointsInsideDist = 80;

	@Configurable(defValue = "20.0", comment = "[mm] moves block point inside opponent pos. Higher number, more aggressive")
	private static double aggressiveness = 20;

	private final Supplier<DribblingInformation> dribblingInformation;

	private final Supplier<List<BotID>> ballHandlingBots;

	private final PointChecker pointChecker = new PointChecker().checkPointFreeOfBotsNoLookahead();

	@Getter
	private IVector2 blockerPos;


	@Override
	public void doCalc()
	{
		blockerPos = null;

		var ballPos = getBall().getPos();
		if (!Geometry.getPenaltyAreaTheir().withMargin(marginToPenAreaToConsiderBlock).isPointInShape(ballPos)
				|| Geometry.getPenaltyAreaTheir().withMargin(5).isPointInShape(ballPos)
				|| getBall().getVel().getLength() > 3.0)
		{
			// blocking just doesnt make a lot of sense
			return;
		}

		ITriangle checkerTriangle;
		try
		{
			checkerTriangle = Triangle.fromCorners(ballPos, Geometry.getGoalTheir().getLeftPost(),
					Geometry.getGoalTheir().getRightPost()).withMargin(Geometry.getBotRadius() * 2);
		} catch (IllegalStateException e)
		{
			return;
		}

		var penTheir = Geometry.getPenaltyAreaTheir().withMargin(considerOpponentsWithingPenAreaMargin);
		List<ITrackedBot> opponentDefenders = getWFrame().getOpponentBots().values().stream()
				.filter(e -> checkerTriangle.isPointInShape(e.getPos()))
				.filter(e -> !Geometry.getPenaltyAreaTheir().isPointInShape(e.getPos()))
				.filter(e -> penTheir.isPointInShape(e.getPos())).toList();

		getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_BLOCK).add(
				new DrawableTriangle(checkerTriangle).setFill(true).setColor(new Color(0, 196, 255, 17)));

		getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_BLOCK).add(new DrawableRectangle(penTheir.getRectangle())
				.setFill(true).setColor(new Color(0, 196, 255, 26)));

		opponentDefenders.forEach(e ->
				getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_BLOCK).add(
						new DrawableCircle(Circle.createCircle(e.getPos(), 140)).setFill(true)
								.setColor(new Color(122, 218, 20, 154))
				));

		if (opponentDefenders.isEmpty())
		{
			// there are no opponents to block.
			return;
		}

		IPenaltyArea defendingPenArea = getDefenderExtendedPenaltyArea(opponentDefenders);
		getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_BLOCK).add(new DrawableRectangle(defendingPenArea.getRectangle())
				.setColor(new Color(255, 196, 255, 137)));


		BotID previousSupportiveAttacker = getAiFrame().getPrevFrame().getPlayStrategy()
				.getActiveRoles(ERole.SUPPORTIVE_ATTACKER)
				.stream().map(ARole::getBotID)
				.findFirst().orElse(null);

		var actionOpt = getAiFrame().getPrevFrame().getTacticalField().getOffensiveActions()
				.entrySet().stream().filter(e -> ballHandlingBots.get().contains(e.getKey())).findFirst();
		if (actionOpt.isEmpty() || actionOpt.get().getValue().getMove() != EOffensiveActionMove.FINISHER)
		{
			// we do not want to do a Finisher move. Thus, blocking is not needed
			return;
		}
		var action = actionOpt.get().getValue().getAction();

		var dribblingCircle = dribblingInformation.get().getDribblingCircle();
		var finisherShape = action.getDribbleToPos().getDribbleKickData().getShape();

		List<IVector2> intersections = finisherShape.intersectCircle(dribblingCircle);

		intersections.forEach(e ->
				getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_BLOCK).add(
						new DrawableCircle(Circle.createCircle(e, 110)).setFill(true)
								.setColor(new Color(222, 118, 200, 224))
				));

		if (intersections.size() != 2)
		{
			// very rare edge case that also means that blocking is not required
			return;
		}

		var p1 = intersections.get(0);
		var p2 = intersections.get(1);

		double p1v = finisherShape.getStepPositionOnShape(p1);
		double p2v = finisherShape.getStepPositionOnShape(p2);

		double lowerP = Math.min(p1v, p2v);
		double ballP = finisherShape.getStepPositionOnShape(ballPos);
		double upperP = Math.max(p2v, p1v);

		var lowerOuterPoint = finisherShape.stepOnShape(lowerP + moveBorderPointsInsideDist);
		var upperOuterPoint = finisherShape.stepOnShape(upperP - moveBorderPointsInsideDist);

		List.of(lowerOuterPoint, upperOuterPoint).forEach(e ->
				getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_BLOCK).add(
						new DrawableCircle(Circle.createCircle(e, 70)).setFill(true)
								.setColor(new Color(222, 218, 200, 255))
				));

		var isSupportiveBlockPointBlockingOurShotCheckerLower = Triangle.fromCorners(lowerOuterPoint,
				Geometry.getGoalTheir().getLeftPost(), Geometry.getGoalTheir().getRightPost());
		var isSupportiveBlockPointBlockingOurShotCheckerUpper = Triangle.fromCorners(upperOuterPoint,
				Geometry.getGoalTheir().getLeftPost(), Geometry.getGoalTheir().getRightPost());

		getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_BLOCK).add(
				new DrawableCircle(dribblingCircle).setColor(Color.YELLOW));

		List.of(isSupportiveBlockPointBlockingOurShotCheckerLower, isSupportiveBlockPointBlockingOurShotCheckerUpper)
				.forEach(e ->
						getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_BLOCK).add(
								new DrawableTriangle(e).setColor(Color.BLACK)
						));

		var midPointIntersection = defendingPenArea.intersectPerimeterPath(
						Lines.segmentFromPoints(ballPos, Geometry.getGoalTheir().getCenter()))
				.stream().min(Comparator.comparingDouble(e -> e.distanceTo(ballPos)));
		if (midPointIntersection.isEmpty())
		{
			return;
		}

		double midPointDistToStart = defendingPenArea.getShapeBoundary().distanceFromStart(midPointIntersection.get());

		double dir = Math.abs(ballP - lowerP) > Math.abs(ballP - upperP) ? -1 : 1;
		Optional<IVector2> blockPointCandidate = calcBlockPointCandidate(defendingPenArea, midPointDistToStart,
				previousSupportiveAttacker, dir, opponentDefenders);
		if (blockPointCandidate.isEmpty())
		{
			return;
		}

		var posFreeChecker = dir < 0 ?
				isSupportiveBlockPointBlockingOurShotCheckerLower :
				isSupportiveBlockPointBlockingOurShotCheckerUpper;

		getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_BLOCK).add(
				new DrawableTriangle(posFreeChecker).setFill(true).setColor(new Color(255, 253, 138, 176)));

		var blockPosCircle = Circle.createCircle(blockPointCandidate.get(), Geometry.getBotRadius());
		if (!posFreeChecker.intersectShape(blockPosCircle).isEmpty())
		{
			// we would block our own kick. So rather not block.
			getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_BLOCK).add(new DrawableCircle(
					Circle.createCircle(blockPointCandidate.get(), Geometry.getBotRadius()))
					.setFill(true).setColor(Color.RED));
			return;
		}

		getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_BLOCK).add(new DrawableCircle(
				Circle.createCircle(blockPointCandidate.get(), Geometry.getBotRadius()))
				.setFill(true).setColor(Color.BLACK));

		blockerPos = blockPointCandidate.get();
	}


	private static IPenaltyArea getDefenderExtendedPenaltyArea(List<ITrackedBot> opponentDefenders)
	{
		double averageDistToPenArea = opponentDefenders.stream()
				.mapToDouble(e ->
				{
					var closestPoint = Geometry.getPenaltyAreaTheir().getShapeBoundary().closestPoint(e.getPos());
					return Math.max(Math.abs(closestPoint.x() - e.getPos().x()),
							Math.abs(closestPoint.y() - e.getPos().y()));
				})
				.average().orElse(0.0);

		return Geometry.getPenaltyAreaTheir().withMargin(averageDistToPenArea);
	}


	private Optional<IVector2> calcBlockPointCandidate(
			IPenaltyArea defendingPenArea, double distFromStart,
			BotID previousSupportiveAttacker, double dir, List<ITrackedBot> opponentDefenders)
	{
		Optional<IVector2> blockPointCandidate;
		double stepSize = 15 * dir;
		double step = 0;
		do
		{
			step += stepSize;
			blockPointCandidate = defendingPenArea.getShapeBoundary().stepAlongBoundary(distFromStart + step);
			if (blockPointCandidate.isEmpty())
			{
				break;
			}
		} while (!pointChecker.allMatch(getAiFrame(), blockPointCandidate.get(), previousSupportiveAttacker));

		if (blockPointCandidate.isEmpty())
		{
			return Optional.empty();
		}

		var blockPoint = blockPointCandidate.get();
		var closestOpponentToBlockPoint = opponentDefenders.stream()
				.min(Comparator.comparingDouble(e -> e.getPos().distanceTo(blockPoint)));

		if (closestOpponentToBlockPoint.isEmpty())
		{
			return Optional.empty();
		}

		var opponentPos = closestOpponentToBlockPoint.get().getPos();
		var opponentToBlockPoint = blockPoint.subtractNew(opponentPos)
				.scaleTo(Geometry.getBotRadius() * 2 - aggressiveness);

		return Optional.of(opponentPos.addNew(opponentToBlockPoint));
	}
}
