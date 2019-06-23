package edu.dhbw.mannheim.tigers.sumatra.view.log.internals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Load File System into a Tree.
 * @author MichaelS
 */
class FileSystemTree extends DefaultMutableTreeNode 
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor to set Root Folder
	 * @param file rootFolder
	 */
    public FileSystemTree(File file) 
    {
        setUserObject(file);
    }

    /**
     * get Number of Childs from Node
     * @return int numberOfChilds
     */
    public int getChildCount() 
    {
        return addFiles((File) getUserObject()).size();
    }

    /**
     * get Child by Number
     * @param index Number of Child
     * @return FileSystemTree
     */
    public FileSystemTree getChildAt(int index) 
    {
        return new FileSystemTree((File) addFiles(
                (File) getUserObject()).get(index));
    }

    /**
     * check if Element is a Leaf
     * @return boolean isLeaf
     */
    public boolean isLeaf() 
    {
        return !((File) getUserObject()).isDirectory();
    }
  
    /**
     * get Filename
     * @return String fileName
     */
    public String toString()
    {
        return ((File) getUserObject()).getName();
    }

    /**
     * add all Files & Folders from Root Directory
     * @param file rootFolder
     * @return List fileList
     */
    private List<Object> addFiles(File file)
    {
   	 List<Object> fileList = new ArrayList<Object>();
   	 File[] files = file.listFiles();
   	 
   	 for (int i = 0; i < files.length; i++)
   	 {
   		 if (!(files[i].getName().contains(".svn"))) // filter svn-data
   		 {
   			 fileList.add(files[i]);
   		 }
   	 }
   	 
   	 return fileList;
    }
}
