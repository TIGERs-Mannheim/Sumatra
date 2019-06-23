/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.07.2011
 * Author(s): Malte
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.ACalculator;

/**
 * This calculater class is a container for really small calculations regarding 
 * frequently calculated defense problems.
 * 
 * @author Malte
 * 
 */
public class DefenseHelper extends ACalculator
{
	
	@Override
	public Object calculate(AIInfoFrame curFrame)
	{
		// TODO Auto-generated method stub
		return null;
	}
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * Calculates wheter the ball is in our penalty area
	 * 
	 * @return
	 */
	public boolean isBallInOurPenaltyArea(AIInfoFrame frame)
	{
		return AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(frame.worldFrame.ball.pos);
	
	}
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
