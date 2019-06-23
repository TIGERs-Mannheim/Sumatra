/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 27, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.ScoreResult.EUsefulness;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeatureState;


/**
 * Scores the requested and available features of a bot
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class FeatureScore extends AScore
{
	private static final Logger	log	= Logger.getLogger(FeatureScore.class.getName());
	
	
	@Override
	protected ScoreResult doCalcScore(final TrackedTigerBot tiger, final ARole role, final MetisAiFrame frame)
	{
		List<EFeature> features = role.getNeededFeatures();
		Map<EFeature, EFeatureState> featureStates = tiger.getBot().getBotFeatures();
		
		int kaput = 0;
		for (EFeature feature : features)
		{
			EFeatureState state = featureStates.get(feature);
			if (state == null)
			{
				log.warn("Feature " + feature.name() + " has no state for bot " + tiger.getId());
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
