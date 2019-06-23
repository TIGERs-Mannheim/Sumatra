/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s):
 * Christian K�nig
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Embeddable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;


/**
 * Datastorage, Sisyphus uses for RRT
 * 
 * @author Christian K�nig, DirkK
 */
@Embeddable
public class Node extends Vector2
{
	// ------------------------------------------------------------------------
	// --- variable(s) --------------------------------------------------------
	// ------------------------------------------------------------------------
	private static final long	serialVersionUID	= -1036999154876568257L;
	
	// root node
	private Node					parent				= null;
	
	// all children
	private final List<Node>	children				= new LinkedList<Node>();
	
	// the children which points to the children which leads to the goal
	// parent + successor are a double linked list
	private Node					successor			= null;
	
	
	// ------------------------------------------------------------------------
	// --- constructor(s) -----------------------------------------------------
	// ------------------------------------------------------------------------
	/**
	 * @param x
	 * @param y
	 */
	public Node(float x, float y)
	{
		super(x, y);
	}
	
	
	/**
	 * @param pos
	 */
	public Node(IVector2 pos)
	{
		super(pos);
	}
	
	
	// ------------------------------------------------------------------------
	// --- method(s) ----------------------------------------------------------
	// ------------------------------------------------------------------------
	/**
	 * @param newNode
	 */
	public void addChild(Node newNode)
	{
		children.add(newNode);
		newNode.parent = this;
	}
	
	
	/**
	 * @param suc
	 */
	public void setSuccessor(Node suc)
	{
		successor = suc;
	}
	
	
	/**
	 * @return
	 */
	public Node getSuccessor()
	{
		return successor;
	}
	
	
	/**
	 * @return
	 */
	public List<Node> getChildren()
	{
		return children;
	}
	
	
	/**
	 * get all nodes recusively
	 * @return a list of all childeren and children of the children and so on
	 */
	public List<Node> getChildrenRecursive()
	{
		final List<Node> listOfAll = new LinkedList<Node>();
		listOfAll.addAll(children);
		for (final Node child : children)
		{
			listOfAll.addAll(child.getChildrenRecursive());
		}
		return listOfAll;
	}
	
	
	/**
	 * @return
	 */
	public Node copy()
	{
		return new Node(new Vector2(x, y));
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public Node getParent()
	{
		return parent;
	}
	
	
	/**
	 * @param parent
	 */
	public void setParent(Node parent)
	{
		this.parent = parent;
	}
	
	
}