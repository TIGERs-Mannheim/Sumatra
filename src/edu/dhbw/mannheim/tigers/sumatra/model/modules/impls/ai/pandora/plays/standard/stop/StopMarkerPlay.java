/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.01.2011
 * Author(s): FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.stop;

import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.AStandardPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.ManToManMarkerRole;


/**
 * Our bot will block one opponent pass receiver.
 * @author FlorianS
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class StopMarkerPlay extends AStandardPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** distance between middle line and the line, the bot is not allowed to cross */
	private static final float			DISTANCE_TO_MEDIAN	= -200;
	/** gap between bot and target */
	private static final float			GAP						= 150;
	
	private List<ManToManMarkerRole>	markers					= new LinkedList<ManToManMarkerRole>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public StopMarkerPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		// theoretically dynamic, but we only have one opponent pass receiver, so only one role is useful
		for (int i = 0; i < getNumAssignedRoles(); i++)
		{
			ManToManMarkerRole marker = new ManToManMarkerRole(new Vector2());
			marker.setMaxLength(DISTANCE_TO_MEDIAN);
			marker.setGap(GAP);
			addAggressiveRole(marker, aiFrame.tacticalInfo.getOpponentPassReceiver().getPos());
			markers.add(marker);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		for (ManToManMarkerRole marker : markers)
		{
			marker.updateTarget(currentFrame.tacticalInfo.getOpponentPassReceiver());
			final float botBallDistanceStop = AIConfig.getGeometry().getBotToBallDistanceStop();
			marker.setForbiddenCircle(new Circle(currentFrame.worldFrame.ball.getPos(), botBallDistanceStop
					+ AIConfig.getGeometry().getBotRadius()));
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// nothing todo
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
