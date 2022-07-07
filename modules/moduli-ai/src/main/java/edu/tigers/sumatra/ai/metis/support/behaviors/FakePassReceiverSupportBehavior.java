/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.animated.AnimatedCrosshair;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.Math.min;


/**
 * The aim of this behavior is if the supporter is within the pass line of our own team, to move out of this pass line
 * but pretend to receive the pass
 */
@RequiredArgsConstructor
public class FakePassReceiverSupportBehavior extends ASupportBehavior
{
	@Configurable(comment = "Defines whether this behavior is active of not", defValue = "true")
	private static boolean enabled = true;

	@Configurable(comment = "Min distance to both passing bots[mm]", defValue = "1300")
	private static int minDistanceToPassingBots = 1300;

	@Configurable(comment = "Distance between passline and FakePassReceiverSupportBehavior[mm]", defValue = "80")
	private static int distanceToPassLine = 80;

	@Configurable(comment = "Max time to fakePosition[sec]", defValue = "1.0")
	private static double maxTimeToDestination = 1;

	private final Supplier<Map<BotID, OffensiveAction>> offensiveActions;

	private final Supplier<Optional<OngoingPass>> ongoingPass;


	@Override
	public SupportBehaviorPosition calculatePositionForRobot(BotID botID)
	{
		return Stream.concat(
						offensiveActions.get().values().stream().map(OffensiveAction::getPass),
						ongoingPass.get().stream().map(OngoingPass::getPass))
				.filter(Objects::nonNull)
				.map(pass -> Lines.segmentFromPoints(pass.getKick().getSource(), pass.getKick().getTarget()))
				.filter(lineSegment -> isFakePassReceiverReasonableOn(lineSegment, getWFrame().getBot(botID)))
				.findFirst()
				.map(iLineSegment -> getPositionOnPassLine(iLineSegment, getWFrame().getBot(botID)))
				.orElseGet(SupportBehaviorPosition::notAvailable);
	}


	private SupportBehaviorPosition getPositionOnPassLine(ILineSegment passLine, ITrackedBot bot)
	{
		IVector2 intersectionPoint = passLine.closestPointOnLine(bot.getPos());
		IVector2 direction = passLine.toLine().getOrthogonalLine().directionVector().normalizeNew();
		if (direction.x() > 0)
		{
			direction = direction.multiplyNew(-1);
		}
		IVector2 destination = intersectionPoint
				.addNew(direction.scaleToNew(Geometry.getBotRadius() + distanceToPassLine));

		draw(passLine, destination);
		return SupportBehaviorPosition
				.fromDestinationAndRotationTarget(destination, passLine.closestPointOnLine(destination), 1.0);
	}


	private boolean isFakePassReceiverReasonableOn(ILineSegment segment, ITrackedBot bot)
	{
		IVector2 referencePoint = segment.closestPointOnLine(bot.getPos());
		boolean isBehindPassLine = referencePoint.subtractNew(bot.getPos()).x() > 0;
		boolean isInsidePenaltyArea = Geometry.getPenaltyAreaTheir().isPointInShapeOrBehind(referencePoint);
		boolean isReachableInTime = TrajectoryGenerator
				.generatePositionTrajectory(bot, referencePoint)
				.getTotalTime() <= maxTimeToDestination;
		double distToA = referencePoint.distanceTo(segment.getStart());
		double distToB = referencePoint.distanceTo(segment.getEnd());
		return min(distToA, distToB) > minDistanceToPassingBots
				&& isBehindPassLine
				&& !isInsidePenaltyArea
				&& isReachableInTime;
	}


	private void draw(ILineSegment segment, IVector2 destination)
	{
		List<IDrawableShape> shapes = getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_FAKE_PASS_RECEIVER);
		shapes.add(AnimatedCrosshair.aCrazyCrosshair(destination,
				(float) Geometry.getBotRadius(),
				(float) Geometry.getBotRadius() * 2, 500,
				Color.RED,
				Color.RED,
				new Color(255, 0, 0, 0)
		));
		shapes.add(new DrawableLine(segment, Color.RED));
	}


	@Override
	public boolean isEnabled()
	{
		return enabled;
	}
}
