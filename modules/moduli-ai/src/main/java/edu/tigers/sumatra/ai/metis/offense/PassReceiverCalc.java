/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


/**
 * Add a pass receiver if required
 */
@RequiredArgsConstructor
public class PassReceiverCalc extends ACalculator
{
	private final Supplier<Map<BotID, OffensiveAction>> offensiveActions;
	private final Supplier<Pass> keeperPass;

	@Getter
	private List<BotID> passReceiver;


	@Override
	public void doCalc()
	{
		var newPassReceivers = new ArrayList<BotID>();
		if (keeperPass.get() != null)
		{
			newPassReceivers.add(keeperPass.get().getReceiver());
		}
		offensiveActions.get().values().stream()
				.filter(this::isPassAction)
				.map(OffensiveAction::getPass)
				.map(Pass::getReceiver)
				.filter(AObjectID::isBot)
				.forEach(newPassReceivers::add);

		passReceiver = Collections.unmodifiableList(newPassReceivers);
	}


	private boolean isPassAction(OffensiveAction a)
	{
		return a.getPass() != null;
	}
}
