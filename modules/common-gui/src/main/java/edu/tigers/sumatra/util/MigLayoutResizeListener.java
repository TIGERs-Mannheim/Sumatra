/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.util;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import javax.swing.JPanel;

import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;


/**
 * Change the number of panels per row in a MigLayout depending on how many columns will fit next to each other.
 *
 * @author David Risch <DavidR@tigers-mannheim.de>
 */
public class MigLayoutResizeListener extends ComponentAdapter
{
	private static final int GAP_WIDTH = 10;
	
	private final JPanel outerPanel;
	private final JPanel migPanel;
	private final MigLayout migLayout;
	private int maxColumns;
	
	
	/**
	 * @param outerPanel JPanel which surrounds the MigLayout JPanel
	 * @param migLayoutPanel JPanel with the MigLayout as it's layout
	 * @param maxColumns Maximum number of columns to set (-1 for infinity)
	 */
	public MigLayoutResizeListener(JPanel outerPanel, JPanel migLayoutPanel, int maxColumns)
	{
		this.outerPanel = outerPanel;
		this.migPanel = migLayoutPanel;
		this.migLayout = ((MigLayout) migPanel.getLayout());
		this.maxColumns = maxColumns;
		
		migPanel.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(final ComponentEvent componentEvent)
			{
				adapt();
			}
		});
		
		migPanel.addContainerListener(new ContainerListener()
		{
			@Override
			public void componentAdded(final ContainerEvent containerEvent)
			{
				containerEvent.getChild().addComponentListener(new ComponentAdapter()
				{
					@Override
					public void componentResized(final ComponentEvent componentEvent)
					{
						adapt();
					}
				});
				adapt();
			}
			
			
			@Override
			public void componentRemoved(final ContainerEvent containerEvent)
			{
				adapt();
			}
		});
	}
	
	
	private int getMaxColumnCount()
	{
		if (maxColumns == -1)
			return migPanel.getComponentCount();
		
		return maxColumns;
	}
	
	
	private int calculateWithOfColumn(int column, int wrapAfter)
	{
		int columnWidth = 0; // = width of the widest component that would be in this column
		for (int row = 0; column + row * wrapAfter < migPanel.getComponentCount(); row++)
		{
			int componentWidth = migPanel.getComponent(column + row * wrapAfter).getMinimumSize().width;
			
			if (componentWidth > columnWidth)
				columnWidth = componentWidth;
		}
		return columnWidth;
	}
	
	
	private int calculateNumberOfColumnsThatFit()
	{
		int numberColumnsThatFit = getMaxColumnCount();
		
		while (numberColumnsThatFit > 1)
		{
			int width = GAP_WIDTH;
			for (int column = 0; column < numberColumnsThatFit && column < migPanel.getComponentCount(); column++)
			{
				width += calculateWithOfColumn(column, numberColumnsThatFit) + GAP_WIDTH;
			}
			
			if (width < outerPanel.getWidth())
				break;
			
			numberColumnsThatFit--;
		}
		
		return numberColumnsThatFit;
	}
	
	
	// called when the MigPanel or one of its components is resized, a component is added or removed
	private void adapt()
	{
		if (migPanel.getComponentCount() <= 1)
		{
			return; // wrap is irrelevant if there is only one panel (or none)
		}
		
		Object layoutConstraintsObject = migLayout.getLayoutConstraints();
		
		// layoutConstraintsObject can be a String or a net.miginfocom.layout.LC
		if (layoutConstraintsObject instanceof String)
		{
			String layoutConstraints = (String) layoutConstraintsObject;
			
			layoutConstraints = layoutConstraints.replaceAll(
					"wrap [0-9]+", "wrap " + calculateNumberOfColumnsThatFit());
			
			migLayout.setLayoutConstraints(layoutConstraints);
		} else
		{
			LC layoutConstraints = (LC) layoutConstraintsObject;
			
			layoutConstraints.setWrapAfter(calculateNumberOfColumnsThatFit());
			
			migLayout.setLayoutConstraints(layoutConstraints);
		}
		migPanel.updateUI();
	}
}
