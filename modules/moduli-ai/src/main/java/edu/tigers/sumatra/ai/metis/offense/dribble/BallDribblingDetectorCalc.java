/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.dribble;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
						Circle.createCircle(getBall().getPos(), maxDribblingLength));
			}

			return dribblingInformation;
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
						Circle.createCircle(getBall().getPos(), maxDribblingLength)));
	}


	private DribblingInformation getDribblingInformationFromBotID(BotID botID)
	{
		return new DribblingInformation(getBall().getPos(), true, botID,
				Circle.createCircle(getBall().getPos(), maxDribblingLength));
	}


	@Override
	protected void reset()
	{
		dribblingInformation = new DribblingInformation(null, false, BotID.noBot(), null);
	}

}
