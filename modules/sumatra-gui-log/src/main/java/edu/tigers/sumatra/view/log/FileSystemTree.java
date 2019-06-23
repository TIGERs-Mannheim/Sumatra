/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * Load File System into a Tree.
 * 
 * @author MichaelS
 */
public class FileSystemTree extends DefaultMutableTreeNode
{
	private static final long	serialVersionUID	= 1L;
	
	
	/**
	 * Constructor to set Root Folder
	 * 
	 * @param file rootFolder
	 */
	public FileSystemTree(final File file)
	{
		setUserObject(file);
	}
	
	
	/**
	 * get Number of Childs from Node
	 * 
	 * @return int numberOfChilds
	 */
	@Override
	public int getChildCount()
	{
		return addFiles((File) getUserObject()).size();
	}
	
	
	/**
	 * get Child by Number
	 * 
	 * @param index Number of Child
	 * @return FileSystemTree
	 */
	@Override
	public FileSystemTree getChildAt(final int index)
	{
		return new FileSystemTree((File) addFiles((File) getUserObject()).get(index));
	}
	
	
	/**
	 * check if Element is a Leaf
	 * 
	 * @return boolean isLeaf
	 */
	@Override
	public boolean isLeaf()
	{
		return !((File) getUserObject()).isDirectory();
	}
	
	
	/**
	 * get Filename
	 * 
	 * @return String fileName
	 */
	@Override
	public String toString()
	{
		return ((File) getUserObject()).getName();
	}
	
	
	/**
	 * add all Files & Folders from Root Directory
	 * 
	 * @param file rootFolder
	 * @return List fileList
	 */
	private List<Object> addFiles(final File file)
	{
		List<Object> fileList = new ArrayList<Object>();
		File[] files = file.listFiles();
		
		if (files != null)
		{
			for (int i = 0; i < files.length; i++)
			{
				// filter svn-data
				if (!(files[i].getName().contains(".svn")))
				{
					fileList.add(files[i]);
				}
			}
		}
		
		return fileList;
	}
}
