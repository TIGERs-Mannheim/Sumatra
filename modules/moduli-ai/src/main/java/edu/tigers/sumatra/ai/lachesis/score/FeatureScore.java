/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 27, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.lachesis.score;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.lachesis.score.ScoreResult.EUsefulness;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Scores the requested and available features of a bot
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class FeatureScore extends AScore
{
	private static final Logger	log	= Logger.getLogger(FeatureScore.class.getName());
	
	
	@Override
	protected ScoreResult doCalcScore(final ITrackedBot tiger, final ARole role, final MetisAiFrame frame)
	{
		// FIXME get required features
		List<EFeature> features = new ArrayList<>();
		Map<EFeature, EFeatureState> featureStates = tiger.getBot().getBotFeatures();
		
		int kaput = 0;
		for (EFeature feature : features)
		{
			EFeatureState state = featureStates.get(feature);
			if (state == null)
			{
				log.warn("Feature " + feature.name() + " has no state for bot " + tiger.getBotId());
				continue;
			}
			switch (state)
			{
				case KAPUT:
					kaput++;
					break;
				case WORKING:
					break;
				case UNKNOWN:
					break;
			}
		}
		if (kaput > 0)
		{
			return new ScoreResult(EUsefulness.BAD, kaput);
		}
		return ScoreResult.defaultResult();
	}
}
