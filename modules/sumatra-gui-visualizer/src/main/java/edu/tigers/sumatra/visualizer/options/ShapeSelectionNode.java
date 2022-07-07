/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.options;

import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeMapSource;

import javax.swing.tree.DefaultMutableTreeNode;


public class ShapeSelectionNode extends DefaultMutableTreeNode
{
	public ShapeSelectionNode(Object userObject)
	{
		super(userObject);
	}


	public ShapeSelectionNode(Object userObject, boolean allowsChildren)
	{
		super(userObject, allowsChildren);
	}


	@Override
	public String toString()
	{
		if (userObject != null)
		{
			if (userObject instanceof IShapeLayerIdentifier shapeLayer)
			{
				return shapeLayer.getLayerName();
			} else if (userObject instanceof ShapeCategoryId categoryId)
			{
				return categoryId.name();
			} else if (userObject instanceof ShapeMapSource sourceId)
			{
				return sourceId.getName();
			}
		}
		return super.toString();
	}
}
