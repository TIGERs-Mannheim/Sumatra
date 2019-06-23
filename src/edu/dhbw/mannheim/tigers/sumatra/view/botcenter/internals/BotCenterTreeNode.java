/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * The only node type used by the bot tree.
 * 
 * @author AndreR
 * 
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
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param title
	 * @param type
	 * @param component
	 */
	public BotCenterTreeNode(String title, ETreeIconType type, Component component)
	{
		super(title);
		
		this.title = title;
		icon = type;
		userComponent = new JScrollPane(component);
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
	public void setTitle(String title)
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
}
