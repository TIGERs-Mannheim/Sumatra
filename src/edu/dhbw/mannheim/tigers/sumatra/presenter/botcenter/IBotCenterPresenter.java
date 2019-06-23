/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * Bot center presenter interface.
 * 
 * @author AndreR
 * 
 */
public interface IBotCenterPresenter
{
	/**
	 * 
	 * @param node
	 */
	void reloadNode(DefaultMutableTreeNode node);
}