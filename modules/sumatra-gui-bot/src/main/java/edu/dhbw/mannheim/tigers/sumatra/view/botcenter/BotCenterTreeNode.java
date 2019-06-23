/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * The only node type used by the bot tree.
 * 
 * @author AndreR
 */
public class BotCenterTreeNode extends DefaultMutableTreeNode
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long		serialVersionUID	= 1110743948837617965L;
	
	private String						title;
	private final ETreeIconType	icon;
	private final Component			userComponent;
	private Color						color;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param title
	 * @param type
	 * @param component
	 * @param useScrollbar
	 */
	public BotCenterTreeNode(final String title, final ETreeIconType type, final Component component,
			final boolean useScrollbar)
	{
		this(title, type, Color.black, component, useScrollbar);
	}
	
	
	/**
	 * @param title
	 * @param type
	 * @param color
	 * @param component
	 * @param useScrollbar
	 */
	public BotCenterTreeNode(final String title, final ETreeIconType type, final Color color, final Component component,
			final boolean useScrollbar)
	{
		super(title);
		this.title = title;
		icon = type;
		if (useScrollbar)
		{
			userComponent = new JScrollPane(component);
		} else
		{
			userComponent = component;
		}
		this.color = color;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public String toString()
	{
		return title;
	}
	
	
	/**
	 * @return
	 */
	public ETreeIconType getIconType()
	{
		return icon;
	}
	
	
	/**
	 * @return
	 */
	public Component getUserComponent()
	{
		return userComponent;
	}
	
	
	/**
	 * @param title
	 */
	public void setTitle(final String title)
	{
		this.title = title;
	}
	
	
	/**
	 * @return the title
	 */
	public final String getTitle()
	{
		return title;
	}
	
	
	/**
	 * @return the color
	 */
	public final Color getColor()
	{
		return color;
	}
	
	
	/**
	 * @param color the color to set
	 */
	public final void setColor(final Color color)
	{
		this.color = color;
	}
}
