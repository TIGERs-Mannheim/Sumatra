/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.10.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Graphics2D;


/**
 * This layer paints the offensive points stored in
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField} of the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame}.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class OffensivePointsLayer extends AValuePointLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public OffensivePointsLayer()
	{
		super(EFieldLayer.OFFENSIVE_POINTS);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void paintLayer(Graphics2D g)
	{
		drawValuePoints(g, getAiFrame().getTacticalInfo().getOffCarrierPoints());
		drawValuePoints(g, getAiFrame().getTacticalInfo().getOffLeftReceiverPoints());
		drawValuePoints(g, getAiFrame().getTacticalInfo().getOffRightReceiverPoints());
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
