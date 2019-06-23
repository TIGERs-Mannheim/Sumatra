/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s):
 * Oliver Steinbrecher
 * Daniel Waigand
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.PlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;


/**
 * This class is a simple container for all information the AI gathers during its processes for one {@link WorldFrame}
 * 
 * @author Oliver Steinbrecher, Daniel Waigand
 * 
 */
public class AIInfoFrame implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long					serialVersionUID	= 1035987436338077764L;
	
	// general data
	public final WorldFrame						worldFrame;
	public final RefereeMsg						refereeMsg;
	
	// from Metis
	/** stores all tactical information added by metis' calculators. */
	public final TacticalField					tacticalInfo;
	
	// from Athena
	public final PlayStrategy					playStrategy;
	

	// from Lachesis
	/** Represents the mapping between a BotID (int), and the {@link ARole} the bot had been assigned to */
	public final HashMap<Integer, ARole>	assignedRoles		= new HashMap<Integer, ARole>();
	
	
	// // debug
	// /** stores debug points for everyday use */
	// public final List<Vector2> debugPoints;
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param worldFrame
	 * @param refereeMsg
	 * @param geometryFrame
	 */
	public AIInfoFrame(WorldFrame worldFrame, RefereeMsg refereeMsg)
	{
		this.worldFrame = worldFrame;
		this.refereeMsg = refereeMsg;
		

		this.tacticalInfo = new TacticalField(worldFrame);
		this.playStrategy = new PlayStrategy();
		
		// this.debugPoints = tacticalInfo.getDebugPoints();
	}
	

	/**
	 * Providing a <strong>shallow</strong> copy of original (Thus collections are created, but filled with the same
	 * values
	 * @param original
	 */
	public AIInfoFrame(AIInfoFrame original)
	{
		this.worldFrame = new WorldFrame(original.worldFrame);
		this.refereeMsg = original.refereeMsg;
		
		this.tacticalInfo = new TacticalField(original.tacticalInfo);
		this.playStrategy = new PlayStrategy(original.playStrategy);
		
		this.assignedRoles.putAll(original.assignedRoles);
	}
	

	public void addDebugPoint(IVector2 debugPoint)
	{
		tacticalInfo.getDebugPoints().add(debugPoint);
	}
	

	public List<IVector2> getDebugPoints()
	{
		return tacticalInfo.getDebugPoints();
	}
}
