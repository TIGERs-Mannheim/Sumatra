/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 16, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.sumatra.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.Serializable;

import javax.swing.border.Border;


/**
 * This border implementation draws a rectangular border with rounded corners around its component.
 * If you want to fill the interior part (inside the border) with a different color you can simply set the background
 * color on the component itself.
 * 
 * @author "Lukas Magel"
 */
public class RoundedCornerBorder implements Border, Serializable
{
	
	/**  */
	private static final long	serialVersionUID	= 713546174759345308L;
	
	private final int				radii;
	private final int				thickness;
	private final Color			borderColor;
	
	
	/**
	 * @param radii the radius of the rounded corners
	 * @param thickness the thickness of the stroke
	 * @param borderColor the color of the border
	 */
	public RoundedCornerBorder(final int radii, final int thickness, final Color borderColor)
	{
		this.radii = radii;
		this.thickness = thickness;
		this.borderColor = borderColor;
	}
	
	
	@Override
	public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width,
			final int height)
	{
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		
		Area borderArea = calcBorderRectangle(x, y, width, height);
		Area outerPaintArea = calcOuterPaintArea(x, y, width, height);
		
		/*
		 * Paint the area outside the border with the background color of the parent
		 */
		Component parent = c.getParent();
		if (parent != null)
		{
			g2.setColor(parent.getBackground());
			g2.fill(outerPaintArea);
		}
		
		/*
		 * Draw the border
		 */
		g2.setStroke(new BasicStroke(thickness));
		g2.setColor(borderColor);
		g2.draw(borderArea);
	}
	
	
	private Area calcOuterPaintArea(final int x, final int y, final int width, final int height)
	{
		Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
		Area paintArea = new Area(rect);
		paintArea.subtract(calcBorderRectangle(x, y, width, height));
		return paintArea;
	}
	
	
	private Area calcBorderRectangle(final int x, final int y, final int width, final int height)
	{
		double posOffset = thickness / 2.0f;
		double sizeOffset = thickness;
		
		double xr = x + posOffset;
		double yr = y + posOffset;
		double wr = width - sizeOffset;
		double hr = height - sizeOffset;
		double diameter = radii * 2;
		
		RoundRectangle2D rect = new RoundRectangle2D.Double(xr, yr, wr, hr, diameter, diameter);
		return new Area(rect);
	}
	
	
	@Override
	public Insets getBorderInsets(final Component c)
	{
		int inset = radii + thickness;
		return new Insets(inset, inset, inset, inset);
	}
	
	
	@Override
	public boolean isBorderOpaque()
	{
		return true;
	}
}
