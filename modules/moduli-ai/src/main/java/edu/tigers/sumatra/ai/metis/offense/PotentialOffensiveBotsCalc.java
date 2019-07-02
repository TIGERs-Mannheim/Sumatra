package edu.tigers.sumatra.ai.metis.offense;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotID;


public class PotentialOffensiveBotsCalc extends ACalculator
{
	@Override
	protected void doCalc()
	{
		getNewTacticalField().setPotentialOffensiveBots(getPotentialOffensiveBotMap());
	}


	private Set<BotID> getPotentialOffensiveBotMap()
	{
		if (getNewTacticalField().isBallLeavingFieldGood())
		{
			return Collections.emptySet();
		}

		Set<BotID> bots = new HashSet<>(getAiFrame().getWorldFrame().getTigerBotsAvailable().keySet());
		bots.removeAll(getNewTacticalField().getCrucialDefender());
		bots.removeAll(getNewTacticalField().getBotInterchange().getDesiredInterchangeBots());

		if (!getNewTacticalField().isInsaneKeeper()
				&& getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(ERole.ATTACKER)
						.stream().map(ARole::getBotID)
						.noneMatch(b -> b.equals(getAiFrame().getKeeperId())))
		{
			// if insane keeper has performed a pass, allow it to stay attacker until pass is performed
			bots.remove(getAiFrame().getKeeperId());
		}

		return bots;
	}
}
