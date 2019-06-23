/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.swing.MigLayout;


/**
 * Bot tree with fancy icons.
 * 
 * @author AndreR
 * 
 */
public class BotTreePanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long					serialVersionUID		= 5912473356244041156L;
	private final List<IBotTreeObserver>	observers				= new ArrayList<IBotTreeObserver>();
	
	private JTree									tree						= null;
	private TreeModel								treeModel				= null;
	
	private ImageIcon								rootIcon					= null;
	private ImageIcon								botIcon					= null;
	private ImageIcon								graphIcon				= null;
	private ImageIcon								lampIcon					= null;
	private ImageIcon								kickIcon					= null;
	private ImageIcon								lightningIcon			= null;
	private ImageIcon								motorIcon				= null;
	private ImageIcon								apIcon					= null;
	private ImageIcon								consoleIcon				= null;
	private ImageIcon								gearIcon					= null;
	private ImageIcon								splineIcon				= null;
	
	private final Point							lastMouseRightClick	= new Point();
	private BotCenterTreeNode					lastSelectedNode		= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param root
	 */
	public BotTreePanel(BotCenterTreeNode root)
	{
		// load icons
		rootIcon = new ImageIcon(ClassLoader.getSystemResource("book.gif"));
		botIcon = new ImageIcon(ClassLoader.getSystemResource("bot.png"));
		graphIcon = new ImageIcon(ClassLoader.getSystemResource("graph.png"));
		lampIcon = new ImageIcon(ClassLoader.getSystemResource("lamp.gif"));
		kickIcon = new ImageIcon(ClassLoader.getSystemResource("kick.png"));
		lightningIcon = new ImageIcon(ClassLoader.getSystemResource("lightning.png"));
		motorIcon = new ImageIcon(ClassLoader.getSystemResource("motor.png"));
		apIcon = new ImageIcon(ClassLoader.getSystemResource("ap.png"));
		consoleIcon = new ImageIcon(ClassLoader.getSystemResource("console.png"));
		gearIcon = new ImageIcon(ClassLoader.getSystemResource("gear.png"));
		splineIcon = new ImageIcon(ClassLoader.getSystemResource("join_spline_icon.jpg"));
		
		
		// make the layout
		setLayout(new MigLayout("fill", "", ""));
		
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new ItemSelected());
		tree.setCellRenderer(new CustomRenderer());
		tree.addMouseListener(new MouseContext());
		
		final JScrollPane treeScrollPane = new JScrollPane(tree);
		treeScrollPane.setMinimumSize(new Dimension(150, 0));
		
		add(treeScrollPane, "grow, push, w 150");
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param o
	 */
	public void addObserver(IBotTreeObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(IBotTreeObserver o)
	{
		observers.remove(o);
	}
	
	
	/**
	 * @return
	 */
	public TreeModel getTreeModel()
	{
		return treeModel;
	}
	
	
	/**
	 */
	public void showAddRemoveContextMenu()
	{
		final JPopupMenu context = new JPopupMenu();
		
		final JMenuItem addBot = new JMenuItem("Add");
		final JMenuItem removeBot = new JMenuItem("Remove");
		
		addBot.addActionListener(new AddBot());
		removeBot.addActionListener(new RemoveBot());
		
		context.add(addBot);
		context.add(removeBot);
		
		context.show(this, lastMouseRightClick.x, lastMouseRightClick.y);
	}
	
	
	/**
	 */
	public void showAddContextMenu()
	{
		final JPopupMenu context = new JPopupMenu();
		
		final JMenuItem addBot = new JMenuItem("Add");
		
		addBot.addActionListener(new AddBot());
		
		context.add(addBot);
		
		context.show(this, lastMouseRightClick.x, lastMouseRightClick.y);
	}
	
	
	private void notifyItemSelected(BotCenterTreeNode node)
	{
		synchronized (observers)
		{
			for (final IBotTreeObserver observer : observers)
			{
				observer.onItemSelected(node);
			}
		}
	}
	
	
	private void notifyNodeRightClicked(BotCenterTreeNode node)
	{
		synchronized (observers)
		{
			for (final IBotTreeObserver observer : observers)
			{
				observer.onNodeRightClicked(node);
			}
		}
	}
	
	
	private void notifyAddBot()
	{
		synchronized (observers)
		{
			for (final IBotTreeObserver observer : observers)
			{
				observer.onAddBot();
			}
		}
	}
	
	
	private void notifyRemoveBot(BotCenterTreeNode node)
	{
		synchronized (observers)
		{
			for (final IBotTreeObserver observer : observers)
			{
				observer.onRemoveBot(node);
			}
		}
	}
	
	protected class ItemSelected implements TreeSelectionListener
	{
		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			final BotCenterTreeNode node = (BotCenterTreeNode) tree.getLastSelectedPathComponent();
			
			if (node == null)
			{
				return;
			}
			
			notifyItemSelected(node);
		}
	}
	
	protected class CustomRenderer extends DefaultTreeCellRenderer
	{
		private static final long	serialVersionUID	= 2116635664278328553L;
		
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus)
		{
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
			final BotCenterTreeNode node = (BotCenterTreeNode) value;
			
			setForeground(node.getColor());
			setText(node.getTitle());
			
			switch (node.getIconType())
			{
				case ROOT:
					setIcon(rootIcon);
					break;
				case BOT:
					setIcon(botIcon);
					break;
				case GRAPH:
					setIcon(graphIcon);
					break;
				case LAMP:
					setIcon(lampIcon);
					break;
				case KICK:
					setIcon(kickIcon);
					break;
				case LIGHTNING:
					setIcon(lightningIcon);
					break;
				case MOTOR:
					setIcon(motorIcon);
					break;
				case AP:
					setIcon(apIcon);
					break;
				case CONSOLE:
					setIcon(consoleIcon);
					break;
				case GEAR:
					setIcon(gearIcon);
					break;
				case SPLINE:
					setIcon(splineIcon);
					break;
			}
			
			return this;
		}
	}
	
	protected class MouseContext extends MouseAdapter
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			final TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
			// right or middle click
			if ((e.getButton() == MouseEvent.BUTTON2) || (e.getButton() == MouseEvent.BUTTON3))
			{
				BotCenterTreeNode node = null;
				lastSelectedNode = null;
				
				if (selPath != null)
				{
					tree.setSelectionPath(selPath);
					
					node = (BotCenterTreeNode) selPath.getLastPathComponent();
					
					lastSelectedNode = node;
				}
				
				lastMouseRightClick.x = e.getX();
				lastMouseRightClick.y = e.getY();
				
				notifyNodeRightClicked(node);
			}
		}
	}
	
	protected class AddBot implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyAddBot();
		}
	}
	
	protected class RemoveBot implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyRemoveBot(lastSelectedNode);
		}
	}
}
