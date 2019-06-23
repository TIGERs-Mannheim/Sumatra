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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotTreePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.IBotTreeObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.commons.ConfigControlMenu;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;


/**
 * Bot center main panel.
 * Contains tree on the left and a panel on the right.
 * 
 * @author AndreR
 * 
 */
public class BotCenterPanel extends JPanel implements IBotTreeObserver, ISumatraView
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long			serialVersionUID	= 2686191777355388548L;
	
	private static final String		TITLE					= "Bot Center";
	private static final int			ID						= 2;
	
	private BotTreePanel					botTree				= null;
	private JSplitPane					splitPane			= null;
	
	private final ConfigControlMenu	configMenu			= new ConfigControlMenu("Botmanager",
																				ABotManager.KEY_BOTMANAGER_CONFIG);
	
	private boolean						active				= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param rootNode
	 */
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
	/**
	 * @return
	 */
	public BotTreePanel getTreePanel()
	{
		return botTree;
	}
	
	
	@Override
	public void onItemSelected(BotCenterTreeNode node)
	{
		final Component userComponent = node.getUserComponent();
		
		if (userComponent != null)
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
	
	
	@Override
	public int getId()
	{
		return ID;
	}
	
	
	@Override
	public String getTitle()
	{
		return TITLE;
	}
	
	
	@Override
	public Component getViewComponent()
	{
		return this;
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		final List<JMenu> menus = new ArrayList<JMenu>();
		
		menus.add(configMenu.getConfigMenu());
		
		return menus;
	}
	
	
	@Override
	public void onShown()
	{
		active = true;
	}
	
	
	@Override
	public void onHidden()
	{
		active = false;
	}
	
	
	@Override
	public void onFocused()
	{
		active = true;
	}
	
	
	@Override
	public void onFocusLost()
	{
	}
	
	
	/**
	 * @return the active
	 */
	public final boolean isActive()
	{
		return active;
	}
	
	
}
