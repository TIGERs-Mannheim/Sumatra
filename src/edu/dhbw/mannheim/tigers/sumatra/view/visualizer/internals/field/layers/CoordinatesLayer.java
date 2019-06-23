/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 19, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine.ETextLocation;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;


/**
 * This layer shows coordinate axis
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class CoordinatesLayer extends AFieldLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final int	STEP_1				= 1000;
	private static final int	STEP_2				= 2000;
	private static final int	STEP_MARKER_LEN	= 100;
	
	private IVector2				lastMousePoint		= new Vector2();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public CoordinatesLayer()
	{
		super(EFieldLayer.COORDINATES, false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void paintLayer(Graphics2D g)
	{
		g.setStroke(new BasicStroke(2));
		
		float maxY = AIConfig.getGeometry().getFieldWidth() / 2;
		float maxX = AIConfig.getGeometry().getFieldLength() / 2;
		
		DrawableLine xAxis = new DrawableLine(new Line(new Vector2(0, 0), new Vector2(maxX, 0)), Color.red);
		xAxis.setText("x");
		xAxis.setTextLocation(ETextLocation.HEAD);
		xAxis.paintShape(g);
		
		DrawableLine yAxis = new DrawableLine(new Line(new Vector2(0, 0), new Vector2(0, maxY)), Color.red);
		yAxis.setText("y");
		yAxis.setTextLocation(ETextLocation.HEAD);
		yAxis.paintShape(g);
		
		DrawableLine xStep1 = new DrawableLine(new Line(new Vector2(STEP_1, (STEP_MARKER_LEN) / 2), new Vector2(0,
				-STEP_MARKER_LEN)), Color.red);
		xStep1.setText(String.valueOf(STEP_1));
		xStep1.setTextLocation(ETextLocation.HEAD);
		xStep1.setDrawArrowHead(false);
		xStep1.paintShape(g);
		
		DrawableLine xStep2 = new DrawableLine(new Line(new Vector2(STEP_2, (STEP_MARKER_LEN) / 2), new Vector2(0,
				-STEP_MARKER_LEN)), Color.red);
		xStep2.setText(String.valueOf(STEP_2));
		xStep2.setTextLocation(ETextLocation.HEAD);
		xStep2.setDrawArrowHead(false);
		xStep2.paintShape(g);
		
		DrawableLine yStep1 = new DrawableLine(new Line(new Vector2((STEP_MARKER_LEN) / 2, STEP_1), new Vector2(
				-STEP_MARKER_LEN, 0)), Color.red);
		yStep1.setText(String.valueOf(STEP_1));
		yStep1.setTextLocation(ETextLocation.HEAD);
		yStep1.setDrawArrowHead(false);
		yStep1.paintShape(g);
		
		g.setColor(Color.white);
		g.drawString(String.format("x:%5d y:%5d", (int) lastMousePoint.x(), (int) lastMousePoint.y()),
				(FieldPanel.getFieldTotalWidth() / 3) * 2, (FieldPanel.FIELD_MARGIN / 2) + 5);
	}
	
	
	/**
	 * Set the mouse position for coordinates text
	 * 
	 * @param location
	 */
	public void updateMouseLocation(IVector2 location)
	{
		lastMousePoint = location;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
