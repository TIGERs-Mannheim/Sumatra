/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.05.2012
 * Author(s): Paul
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.visible.TargetVisibleCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ECriterion;


/**
 * Criteria if direct shot is good.
 * 
 * @author Paul
 * 
 */
public class DirectShotCrit extends ACriterion
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final TargetVisibleCon	goalVisible;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public DirectShotCrit()
	{
		super(ECriterion.DIRECT_SHOT);
		
		goalVisible = new TargetVisibleCon();
		goalVisible.updateTarget(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public float doCheckCriterion(AIInfoFrame currentFrame)
	{
		if (currentFrame.tacticalInfo.getBallPossession().getEBallPossession() == EBallPossession.WE)
		{
			BotID botAtBall = currentFrame.tacticalInfo.getBallPossession().getTigersId();
			
			if (goalVisible.checkCondition(currentFrame.worldFrame, botAtBall) == EConditionState.FULFILLED)
			{
				return 1.0f;
			}
		}
		return 0;
		// return penaltyFactor;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
