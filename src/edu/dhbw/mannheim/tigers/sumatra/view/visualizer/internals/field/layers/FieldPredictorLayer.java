/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 25, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictionInformation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.WorldFramePrediction;


/**
 * Visualize FieldPredictor results for debugging
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class FieldPredictorLayer extends AFieldLayer
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public FieldPredictorLayer()
	{
		super(EFieldLayer.FIELD_PREDICTOR);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void paintLayerSwf(Graphics2D g, SimpleWorldFrame frame)
	{
		WorldFramePrediction wfp = frame.getWorldFramePrediction();
		int size = wfp.getBots().size() + 1;
		Collection<FieldPredictionInformation> fpis = new ArrayList<FieldPredictionInformation>(size);
		fpis.addAll(wfp.getBots().values());
		fpis.add(wfp.getBall());
		
		g.setColor(Color.black);
		
		for (FieldPredictionInformation fpi : fpis)
		{
			for (float t = 0; t < 5; t += 0.5f)
			{
				IVector2 pos = fpi.getPosAt(t);
				IVector2 gPos = getFieldPanel().transformToGuiCoordinates(pos);
				g.fillOval((int) gPos.x(), (int) gPos.y(), 2, 2);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
