/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 27, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;


/**
 * Scores the requested and available features of a bot
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class FeatureScore extends AScore
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log	= Logger.getLogger(FeatureScore.class.getName());
	
	private final int					maxDistScore;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public FeatureScore()
	{
		int tmpMaxDistScore = (int) (AIConfig.getGeometry().getFieldLength() + AIConfig.getGeometry().getFieldWidth());
		maxDistScore = tmpMaxDistScore * tmpMaxDistScore;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected int doCalcScore(TrackedTigerBot tiger, ARole role, MetisAiFrame frame)
	{
		int featureScore = 0;
		List<EFeature> features = role.getNeededFeatures();
		Map<EFeature, EFeatureState> featureStates = tiger.getBot().getBotFeatures();
		
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
				case LIMITED:
					featureScore += maxDistScore / 2;
					break;
				case KAPUT:
					featureScore += maxDistScore;
					break;
				case WORKING:
					break;
				case UNKNOWN:
					break;
			}
		}
		return featureScore;
	}
	
	
	@Override
	protected int doCalcScoreOnPos(IVector2 position, ARole role, AthenaAiFrame frame)
	{
		return 0;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
