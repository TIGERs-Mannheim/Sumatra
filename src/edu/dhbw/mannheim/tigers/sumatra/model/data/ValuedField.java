/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ColorPickerFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IColorPicker;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;


/**
 * Store values spread over field
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class ValuedField implements IDrawableShape
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final float[]				data;
	private final int						numX;
	private final int						numY;
	private final int						offset;
	
	private boolean						drawDebug	= false;
	
	private transient IColorPicker	colorPicker	= ColorPickerFactory.scaledSingleBlack(0, 0, 0, 0.5f, 2);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@SuppressWarnings("unused")
	private ValuedField()
	{
		numX = 0;
		numY = 0;
		offset = 0;
		data = new float[numY * numX];
		colorPicker = ColorPickerFactory.scaledSingleBlack(0, 0, 0, 0.5f, 2);
	}
	
	
	/**
	 * @param data
	 * @param numX
	 * @param numY
	 * @param offset
	 */
	public ValuedField(final float[] data, final int numX, final int numY, final int offset)
	{
		this.data = data;
		this.numX = numX;
		this.numY = numY;
		this.offset = offset;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param x
	 * @param y
	 * @return
	 */
	public float getValue(final int x, final int y)
	{
		int idx = offset + (y * numX) + x;
		if (idx < data.length)
		{
			return data[idx];
		}
		return -1;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < numX; x++)
		{
			for (int y = 0; y < numY; y++)
			{
				sb.append(getValue(x, y));
				sb.append(' ');
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		final Composite originalComposite = g.getComposite();
		float sizeY = fieldPanel.getFieldHeight() / (getNumX() - 1.0f);
		float sizeX = fieldPanel.getFieldWidth() / (getNumY() - 1.0f);
		
		int guiY = (int) (FieldPanel.FIELD_MARGIN - (sizeY / 2));
		
		fieldPanel.turnField(fieldPanel.getFieldTurn(), -AngleMath.PI_HALF, g);
		
		g.setFont(new Font("", Font.PLAIN, 3));
		
		for (int x = 0; x < getNumX(); x += 1)
		{
			int guiX = (int) (FieldPanel.FIELD_MARGIN - (sizeX / 2));
			int nextY = FieldPanel.FIELD_MARGIN + Math.round((x * sizeY) + (sizeY / 2));
			for (int y = 0; y < (getNumY()); y += 1)
			{
				int nextX = FieldPanel.FIELD_MARGIN + Math.round((y * sizeX) + (sizeX / 2));
				
				float relValue;
				if (invert)
				{
					relValue = getValue(getNumX() - x - 1, getNumY() - y - 1);
				} else
				{
					relValue = getValue(x, y);
				}
				
				if (!drawDebug)
				{
					if (relValue < 0)
					{
						relValue *= -1;
					}
					if (relValue > 1)
					{
						relValue = 1;
					}
					colorPicker.applyColor(g, 1 - relValue);
					g.fillRect(guiX - 1, guiY - 1, (nextX - guiX) + 2, (nextY - guiY) + 2);
				} else
				{
					g.setColor(Color.black);
					String str = String.format("%f", relValue);
					str = str.substring(0, Math.min(4, str.length()));
					Rectangle2D strRect = g.getFontMetrics().getStringBounds(str, g);
					float strX = guiX + ((float) strRect.getWidth() / 2);
					float strY = guiY + ((float) strRect.getHeight());
					g.drawString(str, strX, strY);
				}
				
				guiX = nextX;
			}
			guiY = nextY;
		}
		g.setComposite(originalComposite);
		fieldPanel.turnField(fieldPanel.getFieldTurn(), AngleMath.PI_HALF, g);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param x
	 * @param y
	 * @return
	 */
	public IVector2 getPointOnField(final int x, final int y)
	{
		return new Vector2((((x + 0.5) / numX) * AIConfig.getGeometry().getFieldLength())
				- (AIConfig.getGeometry().getFieldLength() / 2), (((y + 0.5) / numY) * AIConfig.getGeometry()
				.getFieldWidth())
				- (AIConfig.getGeometry().getFieldWidth() / 2));
	}
	
	
	/**
	 * Value for a vector in field coordinates
	 * 
	 * @param point
	 * @return
	 */
	public float getValueForPoint(final IVector2 point)
	{
		float fieldLength = AIConfig.getGeometry().getFieldLength();
		float fieldWidth = AIConfig.getGeometry().getFieldWidth();
		float x = ((numX / fieldLength) * (point.x() + (fieldLength / 2))) - 0.5f;
		float y = ((numY / fieldWidth) * (point.y() + (fieldWidth / 2))) - 0.5f;
		return getValue((int) x, (int) y);
	}
	
	
	/**
	 * @return the numX
	 */
	public final int getNumX()
	{
		return numX;
	}
	
	
	/**
	 * @return the numY
	 */
	public final int getNumY()
	{
		return numY;
	}
	
	
	/**
	 * @return the drawDebug
	 */
	public final boolean isDrawDebug()
	{
		return drawDebug;
	}
	
	
	/**
	 * @param drawDebug the drawDebug to set
	 */
	@Override
	public final void setDrawDebug(final boolean drawDebug)
	{
		this.drawDebug = drawDebug;
	}
	
	
	/**
	 * @return the colorPicker
	 */
	public final IColorPicker getColorPicker()
	{
		return colorPicker;
	}
	
	
	/**
	 * @param colorPicker the colorPicker to set
	 */
	public final void setColorPicker(final IColorPicker colorPicker)
	{
		this.colorPicker = colorPicker;
	}
}
