/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.IPath;


/**
 * Data holder for drawable paths, basically for splines and stuff
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawablePath
{
	private IPath								path					= null;
	private final List<IDrawableShape>	pathShapes			= new ArrayList<>();
	private final List<IDrawableShape>	pathDebugShapes	= new ArrayList<>();
	
	
	/**
	  * 
	  */
	public DrawablePath()
	{
	}
	
	
	/**
	 * @return the pathShapes
	 */
	public List<IDrawableShape> getPathShapes()
	{
		return pathShapes;
	}
	
	
	/**
	 * @return the pathDebugShapes
	 */
	public List<IDrawableShape> getPathDebugShapes()
	{
		return pathDebugShapes;
	}
	
	
	/**
	 * @return the path or null
	 */
	public IPath getPath()
	{
		return path;
	}
	
	
	/**
	 * @param path the path to set
	 */
	public final void setPath(final IPath path)
	{
		this.path = path;
	}
}
