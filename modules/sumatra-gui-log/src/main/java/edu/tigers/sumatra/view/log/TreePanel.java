/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.log;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;


/**
 * Create JPanel wit JTree of Filesystem. Updates Filter by Selection.
 * 
 * @author MichaelS, AndreR
 */
public class TreePanel extends JPanel
{
	private static final long					serialVersionUID	= 1L;
	
	private final List<ITreePanelObserver>	observers			= new ArrayList<ITreePanelObserver>();
	
	private JTree									tree					= null;
	
	
	/**
	 * Create Tree Panel and add Listener for Filter Update.
	 */
	public TreePanel()
	{
		// create tree on panel
		setLayout(new BorderLayout());
		
		tree = new JTree();
		tree.setModel(new DefaultTreeModel(new FileSystemTree(new File("src/edu/dhbw/mannheim/tigers/sumatra"))));
		tree.addTreeSelectionListener(new PathSelection());
		
		add(new JScrollPane(tree), BorderLayout.CENTER);
	}
	
	
	/**
	 * @param o
	 */
	public void addObserver(final ITreePanelObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(final ITreePanelObserver o)
	{
		observers.remove(o);
	}
	
	
	/**
	 * create a List of all Classes in this Node
	 * 
	 * @param treeNode
	 */
	private void createClassList(final FileSystemTree treeNode, final List<String> list)
	{
		if (treeNode.isLeaf())
		{
			list.add(treeNode.toString().substring(0, treeNode.toString().length() - 5));
		} else
		{
			for (int i = 0; i < treeNode.getChildCount(); i++)
			{
				createClassList(treeNode.getChildAt(i), list);
			}
		}
	}
	
	protected class PathSelection implements TreeSelectionListener
	{
		@Override
		public void valueChanged(final TreeSelectionEvent e)
		{
			if (e.getNewLeadSelectionPath() == null)
			{
				return;
			}
			
			final List<String> classList = new ArrayList<String>();
			
			final TreePath[] pathes = tree.getSelectionPaths();
			
			for (final TreePath pathe : pathes)
			{
				final FileSystemTree node = (FileSystemTree) pathe.getLastPathComponent();
				createClassList(node, classList);
			}
			
			for (final ITreePanelObserver o : observers)
			{
				o.onNewClassList(classList);
			}
		}
	}
}
