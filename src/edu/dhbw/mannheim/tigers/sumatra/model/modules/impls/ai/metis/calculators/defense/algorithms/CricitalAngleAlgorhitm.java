/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.04.2014
 * Author(s): PhilippP
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.algorithms;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint.EThreatKind;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;


/**
 * Check the shot angle of the robot to determine the dangerous of this point.
 * 
 * @author PhilippP
 */
public class CricitalAngleAlgorhitm extends AEvaluationAlgorithm
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final EThreatKind	kindOfThread	= EThreatKind.CRITICAL_ANGLE;
	private float					criticalAngle;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param criticalAngle
	 */
	public CricitalAngleAlgorhitm(
			final float criticalAngle)
	{
		this.criticalAngle = criticalAngle;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void calculateThreatValue(final DefensePoint defensePoint, final BaseAiFrame baseAiFrame)
	{
		if (isCiricalAngle(defensePoint, baseAiFrame) && !isPointProtected(defensePoint, baseAiFrame)
				&& isNearGoal(defensePoint, baseAiFrame))
		{
			calculateCricialAngleThreatValue(defensePoint, baseAiFrame);
		}
	}
	
	
	/**
	 * Check the if the bot is in our half of the field.
	 * 
	 * @param defensePoint
	 * @param baseAiFrame
	 * @return
	 */
	private boolean isNearGoal(final DefensePoint defensePoint, final BaseAiFrame baseAiFrame)
	{
		if (defensePoint.getProtectAgainst().getPos().x() < 0)
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * TODO PhilippP, add comment!
	 * 
	 * @param defensePoint
	 * @param baseAiFrame
	 */
	private void calculateCricialAngleThreatValue(final DefensePoint defensePoint, final BaseAiFrame baseAiFrame)
	{
		IVector2 threatPosition = defensePoint.getProtectAgainst().getPos();
		final float passerGoalDistance = GeoMath.distancePP(threatPosition, defensePoint);
		final float ballPassingBotDistance = GeoMath
				.distancePP(threatPosition, baseAiFrame.getWorldFrame().ball.getPos());
		defensePoint.addThreatKind(kindOfThread, (1 / ((passerGoalDistance + ballPassingBotDistance))) * 1000f);
	}
	
	
	/**
	 * Check if the line form {@link TigerBot} to {@link DefensePoint} is critical
	 * 
	 * @param defensePoint
	 * @param baseAiFrame
	 */
	private boolean isCiricalAngle(final DefensePoint defensePoint, final BaseAiFrame baseAiFrame)
	{
		IVector2 kickerPos = AiMath.getBotKickerPos(defensePoint.getProtectAgainst());
		IVector2 shootDir = defensePoint.subtractNew(kickerPos);
		
		if (Math.abs(shootDir.getAngle()) > criticalAngle)
		{
			return true;
		}
		return false;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
