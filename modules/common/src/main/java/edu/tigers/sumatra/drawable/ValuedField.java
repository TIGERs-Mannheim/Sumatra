/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Store values spread over field
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ValuedField implements IDrawableShape
{
	private final double[]				data;
	private final int						numX;
	private final int						numY;
	private final int						offset;
	
	private final double					fieldLength;
	private final double					fieldWidth;
	
	private boolean						drawDebug	= false;
	
	private transient IColorPicker	colorPicker	= ColorPickerFactory.scaledSingleBlack(0, 0, 0, 1.0, 2);
	private transient Font				font			= new Font("", Font.PLAIN, 3);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@SuppressWarnings("unused")
	private ValuedField()
	{
		numX = 0;
		numY = 0;
		offset = 0;
		fieldLength = 0;
		fieldWidth = 0;
		data = new double[numY * numX];
	}
	
	
	/**
	 * @param data
	 * @param numX
	 * @param numY
	 * @param offset
	 * @param fieldLength
	 * @param fieldWidth
	 */
	public ValuedField(final double[] data, final int numX, final int numY, final int offset,
			final double fieldLength, final double fieldWidth)
	{
		this.data = data;
		this.numX = numX;
		this.numY = numY;
		this.offset = offset;
		this.fieldLength = fieldLength;
		this.fieldWidth = fieldWidth;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param x
	 * @param y
	 * @return
	 */
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
		double sizeY = tool.getFieldHeight() / (getNumX() - 1.0);
		double sizeX = tool.getFieldWidth() / (getNumY() - 1.0);
		
		int guiY = (int) (tool.getFieldMargin() - (sizeY / 2.0));
		
		tool.turnField(tool.getFieldTurn(), -AngleMath.PI_HALF, g);
		
		if (drawDebug)
		{
			g.setFont(font);
		}
		
		for (int x = 0; x < getNumX(); x += 1)
		{
			int guiX = (int) (tool.getFieldMargin() - (sizeX / 2.0));
			int nextY = tool.getFieldMargin() + (int) Math.round((x * sizeY) + (sizeY / 2.0));
			for (int y = 0; y < (getNumY()); y += 1)
			{
				int nextX = tool.getFieldMargin() + (int) Math.round((y * sizeX) + (sizeX / 2.0));
				
				double relValue;
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
		return Vector2.fromXY((((x + 0.5) / numX) * fieldLength)
				- (fieldLength / 2.0), (((y + 0.5) / numY) * fieldWidth)
						- (fieldWidth / 2.0));
	}
	
	
	/**
	 * Value for a vector in field coordinates
	 * 
	 * @param point
	 * @return
	 */
	public double getValueForPoint(final IVector2 point)
	{
		double x = ((numX / fieldLength) * (point.x() + (fieldLength / 2.0))) - 0.5;
		double y = ((numY / fieldWidth) * (point.y() + (fieldWidth / 2.0))) - 0.5;
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
