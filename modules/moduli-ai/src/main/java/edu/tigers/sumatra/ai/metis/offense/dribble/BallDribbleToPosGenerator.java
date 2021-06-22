/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.dribble;


import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableQuadrilateral;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.quadrilateral.IQuadrilateral;
import edu.tigers.sumatra.math.quadrilateral.Quadrilateral;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.NonNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Calculate a DribbleToPos for a given tigerBot while regarding the closest opponent bot to the ball
 */
public class BallDribbleToPosGenerator
{

	@Configurable(comment = "[mm] Any dribbling distance above this value is considered a violation", defValue = "1000.0")
	private static double maxDribblingLength = 1000.0;

	static
	{
		ConfigRegistration.registerClass("metis", BallDribbleToPosGenerator.class);
	}

	public DribbleToPos getDribbleToPos(WorldFrame wFrame,
			@NonNull BotID closestEnemyBotId,
			ITrackedBot tigerBot, DribblingInformation dribblingInformation,
			List<IDrawableShape> shapes)
	{
		if (tigerBot.getBotId().isUninitializedID())
		{
			// default values, this cannot happen in game.
			return new DribbleToPos(Geometry.getGoalTheir().getCenter(), null, EDribblingCondition.DEFAULT);
		}

		IVector2 dribbleToDestination = null;
		EDribblingCondition condition = EDribblingCondition.DEFAULT;
		if (dribblingInformation.isDribblingInProgress())
		{
			shapes.add(new DrawableCircle(dribblingInformation.getStartPos(), maxDribblingLength, Color.darkGray));

			ICircle dribbleCircle = Circle.createCircle(dribblingInformation.getStartPos(), maxDribblingLength);
			var opponents = wFrame.getOpponentBots().values().stream()
					.filter(e -> dribbleCircle.isPointInShape(e.getPos()))
					.collect(Collectors.toList());

			List<IQuadrilateral> blockingQuads = new ArrayList<>();
			for (var opponent : opponents)
			{
				getBlockingQuad(tigerBot, maxDribblingLength, dribbleCircle, opponent)
						.ifPresent(e -> {
							blockingQuads.add(e);
							shapes.add(new DrawableQuadrilateral(e, new Color(255, 0, 0, 136)).setFill(true));
						});
			}

			var line = Lines.segmentFromPoints(tigerBot.getPos(), dribblingInformation.getStartPos());
			boolean isBlocked = blockingQuads.stream().map(e -> e.lineIntersections(line)).anyMatch(e -> !e.isEmpty());
			shapes.add(
					new DrawableAnnotation(dribblingInformation.getStartPos(), isBlocked ? "is blocked" : "not blocked"));

			if (!isBlocked)
			{
				dribbleToDestination = dribblingInformation.getStartPos();
			}

			if (tigerBot.getPos().distanceTo(dribblingInformation.getStartPos()) > 800)
			{
				condition = EDribblingCondition.REPOSITION;
			}

		}

		IVector2 protectTarget = getProtectTarget(wFrame, closestEnemyBotId);
		return new DribbleToPos(protectTarget, dribbleToDestination, condition);
	}


	private Optional<IQuadrilateral> getBlockingQuad(ITrackedBot tigerBot, double dribblingCircleRadius,
			ICircle dribbleCircle,
			ITrackedBot opponent)
	{
		var toOpponent = opponent.getPos().subtractNew(tigerBot.getPos());
		var offset = toOpponent.getNormalVector().scaleToNew(Geometry.getBotRadius() * 2.0);
		double distTowardsTiger = Math.min(Geometry.getBotRadius() * 3 + 10, toOpponent.getLength2());

		var p1 = opponent.getPos().addNew(offset).addNew(toOpponent.scaleToNew(-distTowardsTiger));
		var p2 = opponent.getPos().subtractNew(offset).addNew(toOpponent.scaleToNew(-distTowardsTiger));

		var base1 = opponent.getPos().addNew(offset.multiplyNew(2));
		var toBase1 = base1.subtractNew(tigerBot.getPos().addNew(offset));
		var intersections = Circle.createCircle(dribbleCircle.center(), dribblingCircleRadius * 2.0)
				.lineIntersections(Lines.halfLineFromDirection(
						base1, toBase1.scaleToNew(dribblingCircleRadius * 4.2)));
		if (intersections.size() != 1)
		{
			return Optional.empty();
		}
		var p3 = intersections.get(0);

		var base2 = opponent.getPos().addNew(offset.multiplyNew(-2));
		var toBase2 = base2.subtractNew(tigerBot.getPos().subtractNew(offset));
		intersections = Circle.createCircle(dribbleCircle.center(), dribblingCircleRadius * 2.0)
				.lineIntersections(Lines.halfLineFromDirection(
						base2, toBase2.scaleToNew(dribblingCircleRadius * 4.2)));
		if (intersections.size() != 1)
		{
			return Optional.empty();
		}
		var p4 = intersections.get(0);

		var quad = Quadrilateral.fromCorners(p1, p2, p3, p4);
		return Optional.of(quad);
	}


	private IVector2 getProtectTarget(WorldFrame wFrame, @NonNull BotID closestEnemyBotId)
	{
		IVector2 protectTarget = Geometry.getGoalTheir().getCenter();
		if (!closestEnemyBotId.isUninitializedID())
		{
			return wFrame.getOpponentBot(closestEnemyBotId).getPos();
		}
		return protectTarget;
	}
}
