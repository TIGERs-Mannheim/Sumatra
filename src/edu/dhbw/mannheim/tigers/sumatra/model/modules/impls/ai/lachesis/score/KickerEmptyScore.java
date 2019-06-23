/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 21, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.ScoreResult.EUsefulness;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;


/**
 * Check if kicker is loaded
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickerEmptyScore extends AScore
{
	@Override
	protected ScoreResult doCalcScore(final TrackedTigerBot tiger, final ARole role, final MetisAiFrame frame)
	{
		if (role.getNeededFeatures().contains(EFeature.STRAIGHT_KICKER) ||
				role.getNeededFeatures().contains(EFeature.CHIP_KICKER))
		{
			float lvl = tiger.getBot().getKickerLevel();
			// float cap = tiger.getBot().getKickerMaxCap();
			// float rel = lvl / cap;
			if (lvl < 15)
			{
				// kicker almost empty, probably not charging
				return new ScoreResult(EUsefulness.BAD);
			}
			// kicker can be charged fast enough now
			// if (rel > 0.85f)
			// {
			// return new ScoreResult(EUsefulness.NEUTRAL);
			// }
			return new ScoreResult(EUsefulness.LIMITED);
		}
		return ScoreResult.defaultResult();
	}
}
