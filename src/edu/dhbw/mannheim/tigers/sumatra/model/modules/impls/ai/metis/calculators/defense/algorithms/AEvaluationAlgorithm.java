/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.03.2014
 * Author(s): PP-Fotos
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;


/**
 * A bascie abstract class to represent a evaluation for a defense situation
 * 
 * @author PhilippP Ph.Posovszky@gmail.com
 */
public abstract class AEvaluationAlgorithm
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public AEvaluationAlgorithm()
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Evaluate the actual situation on the given algorhitm of the class
	 * 
	 * @param baseAiFrame
	 * @param pointsToEvualate
	 */
	public void evaluateSituation(final BaseAiFrame baseAiFrame, final List<DefensePoint> pointsToEvualate)
	{
		
		for (DefensePoint defensePoint : pointsToEvualate)
		{
			calculateThreatValue(defensePoint, baseAiFrame);
		}
		
	}
	
	
	/**
	 * This function estimates if a goal point is protected by some other role than the defenseroles.
	 * 
	 * @param defensePoint - the point on the goalLine
	 * @return true when goalPoint is protected by a bot outside the {@link PenaltyArea} and is not the {@link ARole}
	 *         PENALTy_KEEPER, DEFENDER or KEEPER
	 */
	protected boolean isPointProtected(final DefensePoint defensePoint, final BaseAiFrame baseAiFrame)
	{
		final List<BotID> ignorList = new ArrayList<BotID>();
		final BotIDMap<ARole> roles = baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles();
		for (Entry<BotID, ARole> roleEntry : roles)
		{
			ARole role = roleEntry.getValue();
			if ((role.getType() == ERole.DEFENDER) || (role.getType() == ERole.KEEPER))
			{
				ignorList.add(role.getBotID());
			}
		}
		return !GeoMath.p2pVisibility(baseAiFrame.getWorldFrame(), defensePoint.getProtectAgainst().getPos(),
				defensePoint, 50f,
				ignorList);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Calculate the threadValue for a single {@link DefensePoint} add the calculated Value and some other settings to
	 * the {@link DefensePoint}
	 * 
	 * @param defensePoint - single point to calculate
	 * @param baseAiFrame - the {@link BaseAiFrame}
	 */
	protected abstract void calculateThreatValue(DefensePoint defensePoint, BaseAiFrame baseAiFrame);
	
}
