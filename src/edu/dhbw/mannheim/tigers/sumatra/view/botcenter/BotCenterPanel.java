/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.08.2010
 * Author(s): rYan
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotTreePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.IBotTreeObserver;

/**
 * Bot center main panel.
 * Contains tree on the left and a panel on the right.
 * 
 * @author AndreR
 * 
 */
public class BotCenterPanel extends JPanel implements IBotTreeObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 2686191777355388548L;
	
	private BotTreePanel botTree = null;
	private JSplitPane splitPane = null;

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public BotCenterPanel(BotCenterTreeNode rootNode)
	{
		setLayout(new MigLayout("fill", "", ""));
		
		botTree = new BotTreePanel(rootNode);
		
		botTree.addObserver(this);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, botTree, rootNode.getUserComponent());
		
		add(splitPane, "grow, push");
	}	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public BotTreePanel getTreePanel()
	{
		return botTree;
	}
	
	@Override
	public void onItemSelected(BotCenterTreeNode node)
	{
		final Component userComponent =  node.getUserComponent();
		
		if(userComponent != null)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					splitPane.setRightComponent(userComponent);
				}
			});
		}
	}

	@Override
	public void onNodeRightClicked(BotCenterTreeNode node)
	{
	}

	@Override
	public void onAddBot()
	{
	}

	@Override
	public void onRemoveBot(BotCenterTreeNode node)
	{
	}
}
