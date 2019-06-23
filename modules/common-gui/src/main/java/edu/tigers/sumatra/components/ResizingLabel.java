/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.sumatra.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JLabel;


/**
 * @author "Lukas Magel"
 */
public class ResizingLabel extends JLabel
{
	
	/**  */
	private static final long	serialVersionUID	= 3739325947916547746L;
	
	private static final float	MIN_FONT_SIZE		= 3.0f;
	private static final float	MAX_FONT_SIZE		= 100.0f;
	
	private final boolean		forceTargetHeight;
	
	private Font					targetFont;
	
	
	/**
	 * @param forceTargetHeight If set to true the label does not scale down if there is not enough vertical space
	 */
	public ResizingLabel(final boolean forceTargetHeight)
	{
		this.forceTargetHeight = forceTargetHeight;
		targetFont = getFont();
		addComponentListener(new ResizeListener());
	}
	
	
	/**
	 * @param font
	 */
	public void setTargetFont(final Font font)
	{
		targetFont = font;
	}
	
	
	/**
	 * @return
	 */
	public Font getTargetFont()
	{
		return targetFont;
	}
	
	
	@Override
	public Dimension getMinimumSize()
	{
		if (isMinimumSizeSet())
		{
			return super.getMinimumSize();
		}
		return getSizeRespectForcedHeight(getTargetFont().deriveFont(MIN_FONT_SIZE));
	}
	
	
	@Override
	public Dimension getPreferredSize()
	{
		if (isPreferredSizeSet())
		{
			return super.getPreferredSize();
		}
		return getSizeRespectForcedHeight(getTargetFont());
	}
	
	
	@Override
	public Dimension getMaximumSize()
	{
		if (isMaximumSizeSet())
		{
			return super.getMaximumSize();
		}
		return getSizeRespectForcedHeight(getTargetFont().deriveFont(MAX_FONT_SIZE));
	}
	
	
	private Dimension getSize(final Font font)
	{
		String text = getText();
		FontMetrics metrics = getFontMetrics(font);
		int width = 0;
		if (text != null)
		{
			width = metrics.stringWidth(text);
		}
		int height = metrics.getHeight();
		return new Dimension(width, height);
	}
	
	
	private Dimension getSizeRespectForcedHeight(final Font font)
	{
		Dimension size = getSize(font);
		if (forceTargetHeight)
		{
			size.height = getTargetFontHeight();
		}
		return size;
	}
	
	
	private int getTargetFontHeight()
	{
		FontMetrics metrics = getFontMetrics(targetFont);
		return metrics.getHeight();
	}
	
	
	private Font calcFontSize(final Dimension size, final Font font, final String text)
	{
		FontMetrics metrics = getFontMetrics(font);
		
		int targetWidth = size.width;
		int targetHeight = size.height;
		
		int textWidth = 0;
		if (text != null)
		{
			textWidth = metrics.stringWidth(text);
		}
		int textHeight = metrics.getHeight();
		
		float ratio = Math.min((float) targetWidth / textWidth, (float) targetHeight / textHeight);
		
		float targetSize = (int) (font.getSize() * Math.min(ratio, 1.0f));
		return font.deriveFont(targetSize);
	}
	
	private class ResizeListener extends ComponentAdapter
	{
		@Override
		public void componentResized(final ComponentEvent e)
		{
			setFont(calcFontSize(getSize(), targetFont, getText()));
		}
	}
}
