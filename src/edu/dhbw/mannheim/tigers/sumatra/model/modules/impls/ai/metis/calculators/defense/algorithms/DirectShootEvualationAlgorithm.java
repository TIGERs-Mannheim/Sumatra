/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.03.2014
 * Author(s): PP-Fotos
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.algorithms;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint.EThreatKind;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * TODO PP-Fotos, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author PP-Fotos
 */
public class DirectShootEvualationAlgorithm extends AEvaluationAlgorithm
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final EThreatKind	kindOfThread	= EThreatKind.DIRECT;
	private float					directShotDistance;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param directShotDistance
	 */
	public DirectShootEvualationAlgorithm(final float directShotDistance)
	{
		this.directShotDistance = directShotDistance;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void calculateThreatValue(final DefensePoint defensePoint, final BaseAiFrame baseAiFrame)
	{
		final float ballPassingBotDistance = GeoMath.distancePP(defensePoint.getProtectAgainst().getPos(),
				baseAiFrame.getWorldFrame().ball.getPos());
		if ((ballPassingBotDistance < (directShotDistance)) && !isPointProtected(defensePoint, baseAiFrame))
		{
			calculateDirectShootThreatValue(defensePoint, baseAiFrame);
		}
		
	}
	
	
	/**
	 * Calculates the threat value for a defensePoint which can be attacked.
	 * 
	 * @param defensePoint
	 * @param baseAiFrame
	 */
	private void calculateDirectShootThreatValue(final DefensePoint defensePoint, final BaseAiFrame baseAiFrame)
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
