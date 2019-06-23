/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.03.2014
 * Author(s):PhilippP
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.algorithms;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint.EThreatKind;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * @authorPhilippP
 */
public class IndirectShootEvualationAlgorithm extends AEvaluationAlgorithm
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final EThreatKind	kindOfThread	= EThreatKind.INDIRECT;
	private float					indirectShotDistance;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param indirectShotDistance
	 */
	public IndirectShootEvualationAlgorithm(final float indirectShotDistance)
	{
		this.indirectShotDistance = indirectShotDistance;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void calculateThreatValue(final DefensePoint defensePoint, final BaseAiFrame baseAiFrame)
	{
		final float ballPassingBotDistance = GeoMath.distancePP(defensePoint.getProtectAgainst().getPos(),
				baseAiFrame.getWorldFrame().ball.getPos());
		
		// Check if the point is a indirectDefensPoint else do nothing with the point
		if ((ballPassingBotDistance > (indirectShotDistance)) && !isPointProtected(defensePoint, baseAiFrame))
		{
			calculateIndirectShootThreatValue(defensePoint, baseAiFrame);
		}
		
	}
	
	
	/**
	 * Calculates the threat value for a defensePoint which can be attacked.
	 * 
	 * @param defensePoint
	 * @param baseAiFrame
	 */
	private void calculateIndirectShootThreatValue(final DefensePoint defensePoint, final BaseAiFrame baseAiFrame)
	{
		IVector2 threatPosition = defensePoint.getProtectAgainst().getPos();
		final float passerGoalDistance = GeoMath.distancePP(threatPosition, defensePoint);
		final float ballPassingBotDistance = GeoMath
				.distancePP(threatPosition, baseAiFrame.getWorldFrame().ball.getPos());
		
		defensePoint.addThreatKind(kindOfThread, (1 / ((passerGoalDistance + ballPassingBotDistance))) * 1000f);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
