/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 28, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Graphics2D;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ValuedField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;


/**
 * Visualize information about Support positions and stuff
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ValuedFieldLayer extends AFieldLayer
{
	/**
	 */
	public ValuedFieldLayer()
	{
		super(EFieldLayer.VALUED_FIELD, false);
	}
	
	
	@Override
	protected void paintLayerAif(final Graphics2D g, final IRecordFrame frame)
	{
		ValuedField vf = frame.getTacticalField().getSupporterValuedField();
		if (vf != null)
		{
			vf.setDrawDebug(isDebugInformationVisible());
			vf.paintShape(g, getFieldPanel(), frame.getWorldFrame().isInverted());
		}
	}
}
