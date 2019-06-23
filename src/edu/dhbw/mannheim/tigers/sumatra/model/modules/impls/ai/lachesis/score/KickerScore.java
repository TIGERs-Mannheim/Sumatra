/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 21, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;


/**
 * Check if kicker is loaded
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickerScore extends AScore
{
	
	@Override
	protected int doCalcScore(final TrackedTigerBot tiger, final ARole role, final MetisAiFrame frame)
	{
		if (role.getNeededFeatures().contains(EFeature.STRAIGHT_KICKER) ||
				role.getNeededFeatures().contains(EFeature.CHIP_KICKER))
		{
			if (tiger.getBot().getKickerLevel() < 50)
			{
				return 10000000;
			}
		}
		return 0;
	}
	
	
	@Override
	protected int doCalcScoreOnPos(final IVector2 position, final ARole role, final AthenaAiFrame frame)
	{
		return 0;
	}
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
