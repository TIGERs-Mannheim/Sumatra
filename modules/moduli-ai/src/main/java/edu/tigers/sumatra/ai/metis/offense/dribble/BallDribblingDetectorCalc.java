/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.dribble;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Set;
import java.util.function.Supplier;


/**
 * This calculator calculates a dribblingInformation that contains the dribbling startPos and dribbling robot.
 */
@RequiredArgsConstructor
public class BallDribblingDetectorCalc extends ACalculator
{
	@Configurable(comment = "[mm] Any dribbling distance above this value is considered a violation. Includes safety margin", defValue = "900.0")
	private static double maxDribblingLength = 900.0;

	private final Supplier<Set<BotID>> currentlyTouchingBots;

	@Getter
	private DribblingInformation dribblingInformation;


	@Override
	public void doCalc()
	{
		dribblingInformation = calculateDribblingInformation();
	}


	private DribblingInformation calculateDribblingInformation()
	{
		if (dribblingInformation.isDribblingInProgress())
		{
			// dribbling was active last frame
			var touchingTigers = currentlyTouchingBots.get().stream()
					.filter(e -> e.getTeamColor() == getWFrame().getTeamColor())
					.filter(e -> e == dribblingInformation.getDribblingBot())
					.toList();
			if (!touchingTigers.contains(dribblingInformation.getDribblingBot()))
			{
				// dribbling not active anymore
				return new DribblingInformation(getWFrame().getBall().getPos(), false, BotID.noBot(),
						Circle.createCircle(getBall().getPos(), maxDribblingLength), null, false);
			}

			ITrackedBot bot = getWFrame().getBot(dribblingInformation.getDribblingBot());
			IVector2 intersection = getIntersectionWithDribblingCircle(bot, dribblingInformation.getDribblingCircle());
			boolean violationImminent = intersection == null || isViolationImminent(bot, intersection);
			return DribblingInformation.update(dribblingInformation, intersection, violationImminent);
		}

		double ballToBotMinDistance = Geometry.getBotRadius() + Geometry.getBallRadius() + 10;
		return currentlyTouchingBots.get().stream()
				.filter(e -> e.getTeamColor() == getWFrame().getTeamColor())
				// bots are considered touching ball quite early, so only accept them here if ball is close
				// This is fine here, because we do not need to consider ball reflection
				.filter(bp -> getWFrame().getBot(bp).getPos().distanceTo(getBall().getPos()) < ballToBotMinDistance)
				.findFirst()
				.map(this::getDribblingInformationFromBotID)
				.orElse(new DribblingInformation(getBall().getPos(), false, BotID.noBot(),
						Circle.createCircle(getBall().getPos(), maxDribblingLength), null, false));
	}


	private DribblingInformation getDribblingInformationFromBotID(BotID botID)
	{
		ITrackedBot bot = getWFrame().getBot(botID);
		ICircle dribblingCircle = Circle.createCircle(getBall().getPos(), maxDribblingLength);
		IVector2 intersection = getIntersectionWithDribblingCircle(bot, dribblingCircle);
		boolean violationImminent = intersection != null && isViolationImminent(bot, intersection);
		return new DribblingInformation(getBall().getPos(), true, botID, dribblingCircle, intersection,
				violationImminent);
	}


	private boolean isViolationImminent(ITrackedBot bot, IVector2 intersectionPoint)
	{
		double v0 = bot.getVel().getLength();
		if (v0 < 1e-3)
		{
			return false;
		}

		getShapes(EAiShapesLayer.OFFENSE_FINISHER)
				.add((new DrawableCircle(Circle.createCircle(intersectionPoint, 50))).setColor(Color.BLACK)
						.setFill(true));

		double s = intersectionPoint.distanceTo(bot.getPos()) / 1000.0;
		double t = s / v0;
		return t < 0.1;
	}


	private IVector2 getIntersectionWithDribblingCircle(ITrackedBot bot, ICircle dribblingCircle)
	{
		if (!dribblingCircle.isPointInShape(bot.getPos()))
			return null;
		var halfLine = Lines.halfLineFromDirection(bot.getPos(), getCurrentMoveToDestination(bot, dribblingCircle));
		var intersection = halfLine.intersect(dribblingCircle);
		// must have exactly one intersection, since half line starts inside the circle!
		assert intersection.size() == 1;
		return intersection.asList().getFirst();
	}


	private Vector2 getCurrentMoveToDestination(ITrackedBot bot, ICircle dribblingCircle)
	{
		return bot.getPos().addNew(bot.getVel().scaleToNew(dribblingCircle.radius() * 2.5));
	}


	@Override
	protected void reset()
	{
		dribblingInformation = new DribblingInformation(null, false, BotID.noBot(), null, null, false);
	}

}
