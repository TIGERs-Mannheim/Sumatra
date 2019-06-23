/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s):
 * Christian König
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;


/**
 * Datastorage, Sisyphus uses for RRT
 * 
 * @author Christian König 
 */
public class Node extends Vector2
{
	// ------------------------------------------------------------------------
	// --- variable(s) --------------------------------------------------------
	// ------------------------------------------------------------------------
	private static final long	serialVersionUID	= -1036999154876568257L;
	

	public Node						parent				= null;
	private final List<Node>	children				= new ArrayList<Node>();
	private Node 					successor			= null;
	
	// ------------------------------------------------------------------------
	// --- constructor(s) -----------------------------------------------------
	// ------------------------------------------------------------------------
	public Node(float x, float y)
	{
		super(x, y);
	}
	
	
	public Node(IVector2 pos)
	{
		super(pos);
	}
	

	// ------------------------------------------------------------------------
	// --- method(s) ----------------------------------------------------------
	// ------------------------------------------------------------------------
	public void addChild(Node newNode)
	{
		children.add(newNode);
		newNode.parent = this;
	}
	
	public void setSuccessor(Node suc)
	{
		this.successor = suc;
	}
	public Node getSuccessor()
	{
		return successor;
	}
	
//	public boolean equals(Node n)
//	{
//		float eps = 0.01f;
//		if( (this.x - n.x )<eps && (this.y - n.y )<eps)
//		{
//			return true;
//		}else
//		{
//			return false;
//		}
//	}
}