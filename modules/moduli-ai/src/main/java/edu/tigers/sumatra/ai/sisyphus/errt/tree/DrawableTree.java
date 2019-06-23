/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.errt.tree;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.IVector2;


/**
 * Draw a tree
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class DrawableTree implements IDrawableShape
{
	private final Node	rootNode;
	private final Color	color;
	
	
	@SuppressWarnings("unused")
	private DrawableTree()
	{
		this(null, Color.black);
	}
	
	
	/**
	 * @param rootNode
	 * @param color
	 */
	public DrawableTree(final Node rootNode, final Color color)
	{
		this.rootNode = rootNode;
		this.color = color;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		Node root = rootNode;
		for (Node children : root.getChildrenRecursive())
		{
			IVector2 childrenGUI = tool.transformToGuiCoordinates(children, invert);
			IVector2 parentGUI = tool.transformToGuiCoordinates(children.getParent(), invert);
			g.setColor(color);
			g.drawLine((int) childrenGUI.x(), (int) childrenGUI.y(), (int) parentGUI.x(), (int) parentGUI.y());
		}
	}
}
