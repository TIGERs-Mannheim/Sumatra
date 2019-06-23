/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s): MichealS, AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.log.internals;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Create JPanel wit JTree of Filesystem. Updates Filter by Selection.
 * @author MichaelS, AndreR
 */
public class TreePanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private ArrayList<ITreePanelObserver> observers = new ArrayList<ITreePanelObserver>();
	
	private JTree tree = null;
	
	/**
	 * Create Tree Panel and add Listener for Filter Update.
	 * @param logPanel
	 */
	public TreePanel() 
	{
		// create tree on panel
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(200,200));
        
        tree = new JTree();
        tree.setModel(new DefaultTreeModel(new FileSystemTree(new File("src/edu/dhbw/mannheim/tigers/sumatra"))));
        tree.addTreeSelectionListener(new PathSelection());
        
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }
	
	public void addObserver(ITreePanelObserver o)
	{
		observers.add(o);
	}
	
	public void removeObserver(ITreePanelObserver o)
	{
		observers.remove(o);
	}
	
	/**
	 * create a List of all Classes in this Node
	 * @param treeNode
	 */
	private void createClassList(FileSystemTree treeNode, ArrayList<String> list)
	{
		if(treeNode.isLeaf())
		{
			list.add(treeNode.toString().substring(0, treeNode.toString().length()-5));
		}
		else
		{
			for(int i=0; i<treeNode.getChildCount(); i++)
			{
				createClassList(treeNode.getChildAt(i), list);
			}
		}
	}
	
	protected class PathSelection implements TreeSelectionListener
	{
		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			if (e.getNewLeadSelectionPath() == null)
			{
				return;
			}
			
			ArrayList<String> classList = new ArrayList<String>();
			
			TreePath[] pathes = tree.getSelectionPaths();
			
			for (int i = 0; i < pathes.length; i++)
			{
				FileSystemTree node = (FileSystemTree) pathes[i].getLastPathComponent();
				createClassList(node, classList);
			}
			
			for (ITreePanelObserver o : observers)
			{
				o.onNewClassList(classList);
			}
		}
	}
}
