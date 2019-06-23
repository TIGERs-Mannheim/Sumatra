/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;


/**
 * Store values spread over field
 */
public class ValuedField implements IDrawableShape
{
	private final double[] data;
	private final int numX;
	private final int numY;
	private final int offset;
	
	private double minValue = 0;
	private double maxValue = 1;
	
	private boolean drawDebug = false;
	
	private transient IColorPicker colorPicker = ColorPickerFactory.scaledSingleBlack(0, 0, 0, 1.0, 2);
	private transient Font font = new Font("", Font.PLAIN, 15);
	
	
	@SuppressWarnings("unused")
	private ValuedField()
	{
		numX = 0;
		numY = 0;
		offset = 0;
		data = new double[numY * numX];
		minValue = 0;
		maxValue = 1;
	}
	
	
	public ValuedField(final double[] data, final int numX, final int numY, final int offset)
	{
		this.data = data;
		this.numX = numX;
		this.numY = numY;
		this.offset = offset;
	}
	
	
	public double getValue(final int x, final int y)
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
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		double sizeY = tool.getFieldHeight() / (numX - 1.0);
		double sizeX = tool.getFieldWidth() / (numY - 1.0);
		
		int guiY = (int) (tool.getFieldMargin() - (sizeY / 2.0));
		
		tool.turnField(tool.getFieldTurn(), -AngleMath.PI_HALF, g);
		
		if (drawDebug)
		{
			g.setFont(font);
		}
		
		for (int x = 0; x < numX; x += 1)
		{
			int guiX = (int) (tool.getFieldMargin() - (sizeX / 2.0));
			int nextY = tool.getFieldMargin() + (int) Math.round((x * sizeY) + (sizeY / 2.0));
			for (int y = 0; y < (numY); y += 1)
			{
				int nextX = tool.getFieldMargin() + (int) Math.round((y * sizeX) + (sizeX / 2.0));
				
				double value;
				if (invert)
				{
					value = getValue(numX - x - 1, numY - y - 1);
				} else
				{
					value = getValue(x, y);
				}
				
				double relValue = SumatraMath.relative(value, minValue, maxValue);
				colorPicker.applyColor(g, relValue);
				g.fillRect(guiX - 1, guiY - 1, (nextX - guiX) + 2, (nextY - guiY) + 2);
				
				if (drawDebug)
				{
					g.setColor(Color.black);
					String str = String.format("%f", value);
					str = str.substring(0, Math.min(4, str.length()));
					Rectangle2D strRect = g.getFontMetrics().getStringBounds(str, g);
					float strX = (float) (guiX + (strRect.getWidth() / 2.0));
					float strY = (float) (guiY + (strRect.getHeight()));
					g.drawString(str, strX, strY);
				}
				
				guiX = nextX;
			}
			guiY = nextY;
		}
		tool.turnField(tool.getFieldTurn(), AngleMath.PI_HALF, g);
	}
	
	
	public void setDrawDebug(final boolean drawDebug)
	{
		this.drawDebug = drawDebug;
	}
	
	
	/**
	 * @param colorPicker the colorPicker to set
	 */
	public final void setColorPicker(final IColorPicker colorPicker)
	{
		this.colorPicker = colorPicker;
	}
	
	
	public void setMinValue(final double minValue)
	{
		this.minValue = minValue;
	}
	
	
	public void setMaxValue(final double maxValue)
	{
		this.maxValue = maxValue;
	}
}
