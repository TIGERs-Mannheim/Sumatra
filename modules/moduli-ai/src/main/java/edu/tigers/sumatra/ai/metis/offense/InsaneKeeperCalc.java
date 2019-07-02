package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Determine if the keeper should be insane.
 */
public class InsaneKeeperCalc extends ACalculator
{
	@Configurable(comment = "Should keeper be allowed to leave the penalty area", defValue = "false")
	private static boolean enableInsanityMode = false;


	@Override
	protected void doCalc()
	{
		getNewTacticalField().setInsaneKeeper(isKeeperInsane());
	}


	private boolean isKeeperInsane()
	{
		return enableInsanityMode
				&& getWFrame().getTigerBotsAvailable().containsKey(getAiFrame().getKeeperId())
				&& standardForUs()
				&& cornerKick();
	}


	private boolean cornerKick()
	{
		IVector2 ballPos = getAiFrame().getGamestate().getBallPlacementPositionForUs();
		if (ballPos == null)
		{
			ballPos = getBall().getPos();
		}
		return ballPos.x() > (Geometry.getFieldLength() / 2) - 500;
	}


	private boolean standardForUs()
	{
		return getAiFrame().getGamestate().isStandardSituationForUs()
				|| getAiFrame().getGamestate().isNextStandardSituationForUs();
	}
}
