/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoref.view.humanref;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.visualizer.view.field.FieldPanel;
import edu.tigers.sumatra.visualizer.view.field.IFieldPanel;
import net.miginfocom.swing.MigLayout;


/**
 * @author "Lukas Magel"
 */
public abstract class AHumanRefPanel extends JPanel implements ISumatraView
{
	/**  */
	private static final long serialVersionUID = 1014751860256460541L;
	
	/** Per thumb size of the field panel in pixels for non pixelated display */
	private static final int FIELD_PANEL_WIDTH = 2000;
	
	private boolean isVertical = true;
	
	private FieldPanel fieldPanel;
	private JPanel headerPanel;
	private JPanel contentPanel;
	
	protected final Font smallFont;
	protected final Font regularFont;
	protected final Font largeFont;
	protected final Font headerFont;
	
	
	/**
	 * Default
	 */
	protected AHumanRefPanel()
	{
		Font defaultFont = getFont().deriveFont(Font.BOLD);
		smallFont = defaultFont.deriveFont(35.0f);
		regularFont = defaultFont.deriveFont(50.0f);
		largeFont = defaultFont.deriveFont(60.0f);
		headerFont = defaultFont.deriveFont(70.0f);
		
		setupGUI();
		
		addComponentListener(new ResizeListener());
	}
	
	
	protected void setupGUI()
	{
		fieldPanel = new FieldPanel(FIELD_PANEL_WIDTH);
		fieldPanel.setPaintCoordinates(false);
		fieldPanel.setFancyPainting(true);
		setLayout(isVertical);
	}
	
	
	protected final JPanel getHeaderPanel()
	{
		return headerPanel;
	}
	
	
	protected final JPanel getContentPanel()
	{
		return contentPanel;
	}
	
	
	/**
	 * @return
	 */
	public IFieldPanel getFieldPanel()
	{
		return fieldPanel;
	}
	
	
	protected final void setLayout(final boolean isVertical)
	{
		removeAll();
		headerPanel = new JPanel();
		contentPanel = new JPanel();
		
		if (isVertical)
		{
			createVerticalLayout();
		} else
		{
			createHorizontalLayout();
		}
	}
	
	
	private void createVerticalLayout()
	{
		JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		separator.setForeground(Color.BLACK);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new MigLayout("fill", "[fill]", "[][][fill]"));
		
		centerPanel.add(headerPanel, "wrap");
		centerPanel.add(separator, "wrap 1%");
		centerPanel.add(contentPanel, "pushy");
		
		setLayout(new BorderLayout());
		add(fieldPanel, BorderLayout.WEST);
		add(centerPanel, BorderLayout.CENTER);
		
		fillVerticalLayout();
	}
	
	
	private void createHorizontalLayout()
	{
		setLayout(new MigLayout("fill", "[][fill]", "[][fill]"));
		add(headerPanel, "span, growx, wrap");
		add(fieldPanel);
		add(contentPanel, "pushy");
		
		fillHorizontalLayout();
	}
	
	
	protected abstract void fillVerticalLayout();
	
	
	protected abstract void fillHorizontalLayout();
	
	
	private void resizeField()
	{
		/*
		 * This little piece of code resizes the visualizer panel to the correct aspect ratio to fit the entire field.
		 * The visualizer should however only take up half of the maximum available space.
		 */
		Insets insets = getInsets();
		int containerWidth = getWidth() - insets.left - insets.right;
		int containerHeight = getHeight() - insets.top - insets.bottom;
		
		IRectangle field = Geometry.getFieldWBorders();
		double ratio = field.yExtent() / field.xExtent();
		
		int preferredWidth;
		int preferredHeight;
		if (isVertical)
		{
			preferredWidth = (int) (containerHeight * ratio);
			preferredHeight = containerHeight;
			
			if (preferredWidth > (containerWidth / 2))
			{
				preferredWidth = containerWidth / 2;
			}
		} else
		{
			preferredWidth = (int) (containerWidth / 2.0f);
			preferredHeight = (int) (preferredWidth * ratio);
		}
		fieldPanel.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		fieldPanel.invalidate();
	}
	
	
	/**
	 * @param vertical
	 */
	public final void setFieldOrientation(final boolean vertical)
	{
		isVertical = vertical;
		resizeField();
		setLayout(vertical);
	}
	
	
	/**
	 * @return true if vertical, false if horizontal
	 */
	public boolean getFieldOrientation()
	{
		return isVertical;
	}
	
	
	/**
	 * Turns the field by 180 degrees
	 */
	public void turnField()
	{
		for (int i = 0; i < 2; i++)
		{
			fieldPanel.setFieldTurn(fieldPanel.getFieldTurn().getOpposite());
		}
	}
	
	private class ResizeListener extends ComponentAdapter
	{
		
		@Override
		public void componentResized(final ComponentEvent e)
		{
			resizeField();
		}
	}
}
