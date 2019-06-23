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
	private static final long	serialVersionUID	= 1110743948837617965L;

	private String title;
	private ETreeIconType icon;
	private Component userComponent;	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public BotCenterTreeNode(String title, ETreeIconType type, Component component)
	{
		super(title);
		
		this.title = title;
		this.icon = type;
		this.userComponent =  component;
	}	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public String toString()
	{
		return title;
	}
	
	public ETreeIconType getIconType()
	{
		return icon;
	}
	
	public Component getUserComponent()
	{
		return userComponent;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
}
