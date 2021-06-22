/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.dribble;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ids.BotID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * This calculator calculates a dribblingInformation that contains the dribbling startPos and dribbling robot.
 */
@RequiredArgsConstructor
public class BallDribblingDetectorCalc extends ACalculator
{
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
					.collect(Collectors.toList());
			if (!touchingTigers.contains(dribblingInformation.getDribblingBot()))
			{
				// dribbling not active anymore
				return new DribblingInformation(null, false, BotID.noBot());
			}

			return dribblingInformation;
		}

		return currentlyTouchingBots.get().stream()
				.filter(e -> e.getTeamColor() == getWFrame().getTeamColor())
				.findFirst()
				.map(this::getDribblingInformationFromBotID)
				.orElse(new DribblingInformation(null, false, BotID.noBot()));

	}


	private DribblingInformation getDribblingInformationFromBotID(BotID botID)
	{
		return new DribblingInformation(getBall().getPos(), true, botID);
	}


	@Override
	protected void reset()
	{
		dribblingInformation = new DribblingInformation(null, false, BotID.noBot());
	}

}
