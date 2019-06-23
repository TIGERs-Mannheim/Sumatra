/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.11.2014
 * Author(s): KaiE
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.SyncedCamFrameBufferV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.SyncedCamFrameBufferV2.MathematicalQuadrants;


/**
 * Layer to display the configurable option from the SyncedCamFrameBufferV2
 * 
 * @author KaiE
 */
public class CamIntersectionLayer extends AFieldLayer
{
	/**
	 * 
	 */
	public CamIntersectionLayer()
	{
		super(EFieldLayer.INTERSECTION, false);
	}
	
	
	@Override
	protected void paintLayerSwf(final Graphics2D g, final SimpleWorldFrame frame)
	{
		g.setStroke(new BasicStroke());
		Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] { 5.0f,
				10.0f }, 0.0f);
		Color color = Color.BLACK;
		
		float maxY = AIConfig.getGeometry().getFieldWidth() / 2;
		float maxX = AIConfig.getGeometry().getFieldLength() / 2;
		float itsepsl = (float) SyncedCamFrameBufferV2.getIntersectionEpsilon();
		DrawableLine xAxis1 = new DrawableLine(Line.newLine(new Vector2(-maxX, itsepsl), new Vector2(maxX, itsepsl)),
				color);
		DrawableLine xAxis2 = new DrawableLine(Line.newLine(new Vector2(-maxX, -itsepsl), new Vector2(maxX, -itsepsl)),
				color);
		DrawableLine yAxis1 = new DrawableLine(Line.newLine(new Vector2(itsepsl, -maxY), new Vector2(itsepsl, maxY)),
				color);
		DrawableLine yAxis2 = new DrawableLine(Line.newLine(new Vector2(-itsepsl, -maxY), new Vector2(-itsepsl, maxY)),
				color);
		
		DrawablePoint q1 = new DrawablePoint(new Vector2(maxX / 2, maxY / 2));
		DrawablePoint q2 = new DrawablePoint(new Vector2(-maxX / 2, maxY / 2));
		DrawablePoint q3 = new DrawablePoint(new Vector2(-maxX / 2, -maxY / 2));
		DrawablePoint q4 = new DrawablePoint(new Vector2(+maxX / 2, -maxY / 2));
		
		final MathematicalQuadrants[] quads = SyncedCamFrameBufferV2.getCamToQuadrantAssociation();
		
		
		List<Integer> cams1 = new ArrayList<Integer>();
		List<Integer> cams2 = new ArrayList<Integer>();
		List<Integer> cams3 = new ArrayList<Integer>();
		List<Integer> cams4 = new ArrayList<Integer>();
		
		for (int i = 0; i < SyncedCamFrameBufferV2.getNumCams(); ++i)
		{
			MathematicalQuadrants q = MathematicalQuadrants.Q1234;
			if (i < quads.length)
			{
				q = quads[i];
			}
			
			switch (q)
			{
				case Q1234:
					cams3.add(i);
					cams4.add(i);
				case Q12:
					cams1.add(i);
				case Q2:
					cams2.add(i);
					break;
				case Q14:
					cams4.add(i);
				case Q1:
					cams1.add(i);
					break;
				case Q23:
					cams2.add(i);
				case Q3:
					cams3.add(i);
					break;
				case Q34:
					cams3.add(i);
				case Q4:
					cams4.add(i);
					break;
			}
			
		}
		q1.setText("Q1:" + cams1);
		q2.setText("Q2:" + cams2);
		q3.setText("Q3:" + cams3);
		q4.setText("Q4:" + cams4);
		
		xAxis1.setDrawArrowHead(false);
		xAxis2.setDrawArrowHead(false);
		yAxis1.setDrawArrowHead(false);
		yAxis2.setDrawArrowHead(false);
		xAxis1.setStroke(stroke);
		xAxis2.setStroke(stroke);
		yAxis1.setStroke(stroke);
		yAxis2.setStroke(stroke);
		xAxis1.paintShape(g, getFieldPanel(), false);
		yAxis1.paintShape(g, getFieldPanel(), false);
		xAxis2.paintShape(g, getFieldPanel(), false);
		yAxis2.paintShape(g, getFieldPanel(), false);
		q1.paintShape(g, getFieldPanel(), false);
		q2.paintShape(g, getFieldPanel(), false);
		q3.paintShape(g, getFieldPanel(), false);
		q4.paintShape(g, getFieldPanel(), false);
		
		
	}
}
