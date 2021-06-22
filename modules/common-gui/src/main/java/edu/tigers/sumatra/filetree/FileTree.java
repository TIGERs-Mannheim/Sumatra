/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.filetree;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Display a file system in a JTree view
 *
 * @version $Id: FileTree.java,v 1.9 2004/02/23 03:39:22 ian Exp $
 * @author Ian Darwin
 */
public class FileTree extends JPanel
{
	/**  */
	private static final long serialVersionUID = 6901756711335047357L;
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(FileTree.class.getName());

	private final List<IFileTreeObserver> observers = new CopyOnWriteArrayList<>();
	private final JTree tree;

	private final List<String> selectedPaths = new ArrayList<>();


	/**
	 * Construct a FileTree
	 *
	 * @param dir
	 */
	public FileTree(final File dir)
	{
		this(dir, null);
	}


	/**
	 * Construct a FileTree
	 *
	 * @param dir
	 * @param preFileTree
	 */
	public FileTree(final File dir, final FileTree preFileTree)
	{
		setLayout(new BorderLayout());

		// Make a tree list with all the nodes, and make it a JTree
		tree = new JTree(addNodes(null, dir));
		tree.setRootVisible(false);

		if (preFileTree != null)
		{
			setupFromPreFileTree(preFileTree);
		}

		// Add a listener
		tree.addTreeSelectionListener(new FileTreeSelectionListener());

		// Lastly, put the JTree into a JScrollPane.
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(tree);
		scrollPane.setPreferredSize(new Dimension(400, 0));
		add(BorderLayout.CENTER, scrollPane);
	}


	private boolean equalTreePaths(final TreePath tp1, final TreePath tp2)
	{
		if (tp1.getPath().length != tp2.getPath().length)
		{
			return false;
		}
		for (int i = 0; i < tp1.getPathCount(); i++)
		{
			DefaultMutableTreeNode node1 = (DefaultMutableTreeNode) tp1.getPathComponent(i);
			DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) tp2.getPathComponent(i);
			if (!node1.getUserObject().equals(node2.getUserObject()))
			{
				return false;
			}
		}
		return true;
	}


	@SuppressWarnings("squid:S134")
	private void setupFromPreFileTree(final FileTree preFileTree)
	{
		JTree preTree = preFileTree.tree;
		int nextRow = 0;
		for (int preRow = 0; preRow < preTree.getRowCount(); preRow++)
		{
			TreePath preTp = preTree.getPathForRow(preRow);
			for (int row = nextRow; row < tree.getRowCount(); row++)
			{
				TreePath tp = tree.getPathForRow(row);
				if (equalTreePaths(preTp, tp))
				{
					if (preTree.isExpanded(preRow))
					{
						tree.expandPath(tp);
					}
					nextRow = row + 1;
					break;
				}
			}
		}
	}


	/**
	 * @param numPaths
	 */
	public void expandPaths(final int numPaths)
	{
		for (int i = 0; i < tree.getRowCount(); i++)
		{
			TreePath path = tree.getPathForRow(i);
			if (path.getPathCount() < numPaths)
			{
				tree.expandPath(path);
			}
		}
	}


	/**
	 * @param observer
	 */
	public void addObserver(final IFileTreeObserver observer)
	{
		observers.add(observer);
	}


	/**
	 * @param observer
	 */
	public void removeObserver(final IFileTreeObserver observer)
	{
		observers.remove(observer);
	}


	/**
	 * Add nodes from under "dir" into curTop. Highly recursive.
	 *
	 * @param curTop
	 * @param dir
	 * @return
	 */
	private DefaultMutableTreeNode addNodes(final DefaultMutableTreeNode curTop, final File dir)
	{
		Element curPath = new Element(dir);
		DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(curPath);
		if (curTop != null)
		{ // should only be null at root
			curTop.add(curDir);
		}
		List<String> ol = new ArrayList<>();
		String[] tmp = dir.list();
		if (tmp == null)
		{
			log.info("Directory does not exist and will be created: " + dir.getAbsolutePath());
			// noinspection ResultOfMethodCallIgnored
			dir.mkdirs();
			return curDir;
		}

		ol.addAll(Arrays.asList(tmp));
		ol.sort(String.CASE_INSENSITIVE_ORDER);
		File f;
		List<File> files = new ArrayList<>();

		// Make two passes, one for Dirs and one for Files. This is #1.
		for (String thisObject : ol)
		{
			String newPath;
			if (".".equals(curPath.file.getPath()))
			{
				newPath = thisObject;
			} else
			{
				newPath = curPath.file.getPath() + File.separator + thisObject;
			}

			f = new File(newPath);
			if (f.isDirectory())
			{
				addNodes(curDir, f);
			} else
			{
				files.add(f);
			}
		}

		// Pass two: for files.
		for (File file : files)
		{
			curDir.add(new DefaultMutableTreeNode(new Element(file)));
		}

		return curDir;
	}


	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public interface IFileTreeObserver
	{
		/**
		 * @param filenames
		 */
		void onFileSelected(List<String> filenames);
	}

	private static class Element
	{
		File file;


		Element(final File file)
		{
			this.file = file;
		}


		@Override
		public String toString()
		{
			return file.getName();
		}


		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((file == null) ? 0 : file.hashCode());
			return result;
		}


		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			Element other = (Element) obj;
			if (file == null)
			{
				if (other.file != null)
				{
					return false;
				}
			} else if (!file.equals(other.file))
			{
				return false;
			}
			return true;
		}
	}

	private class FileTreeSelectionListener implements TreeSelectionListener
	{
		@Override
		public void valueChanged(final TreeSelectionEvent e)
		{
			for (TreePath tp : e.getPaths())
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
				Element el = (Element) node.getUserObject();
				String filename = el.file.getAbsolutePath();
				boolean isNew = e.isAddedPath(tp);
				if (isNew && !selectedPaths.contains(filename))
				{
					selectedPaths.add(filename);
				}
				if (!isNew)
				{
					selectedPaths.remove(filename);
				}
			}
			for (IFileTreeObserver o : observers)
			{
				o.onFileSelected(selectedPaths);
			}
		}
	}
}
