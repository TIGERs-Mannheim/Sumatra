/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ColorWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * Draw a tree
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class DrawableTree implements IDrawableShape
{
	private final Node	rootNode;
	private ColorWrapper	color;
	
	
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
		this.color = new ColorWrapper(color);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		Node root = rootNode;
		for (Node children : root.getChildrenRecursive())
		{
			IVector2 childrenGUI = fieldPanel.transformToGuiCoordinates(children, invert);
			IVector2 parentGUI = fieldPanel.transformToGuiCoordinates(children.getParent(), invert);
			g.setColor(color.getColor());
			g.drawLine((int) childrenGUI.x(), (int) childrenGUI.y(), (int) parentGUI.x(), (int) parentGUI.y());
		}
	}
}
