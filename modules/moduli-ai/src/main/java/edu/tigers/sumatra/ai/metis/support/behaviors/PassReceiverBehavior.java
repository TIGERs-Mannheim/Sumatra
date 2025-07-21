/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors;

import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class PassReceiverBehavior extends ASupportBehavior
{

	private final Supplier<Map<BotID, Pass>> supportPassReceivers;


	@Override
	public SupportBehaviorPosition calculatePositionForRobot(BotID botID)
	{
		if (supportPassReceivers.get().containsKey(botID))
		{
			var pass = supportPassReceivers.get().get(botID);
			IVector2 passTarget = pass.getKick().getTarget();
			IVector2 passSource = pass.getKick().getSource();
			var moveToTarget = passTarget.addNew(
					passTarget.subtractNew(passSource).scaleToNew(getWFrame().getBot(botID).getCenter2DribblerDist()));
			return SupportBehaviorPosition.fromDestinationAndRotationTarget(moveToTarget, passSource, 1.0);
		}
		return SupportBehaviorPosition.notAvailable();
	}


	@Override
	public boolean isEnabled()
	{
		return true;
	}
}
