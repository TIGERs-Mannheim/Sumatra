/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.filter;

import java.util.List;

import edu.tigers.sumatra.ai.sisyphus.PathFinderInput;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.shapes.path.IPath;


/**
 * Default path filter stub. Accepts everything
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class StubPathFilter implements IPathFilter
{
	@Override
	public boolean accept(final PathFinderInput pathFinderInput, final IPath newPath, final IPath currentPath)
	{
		return true;
	}
	
	
	@Override
	public void getDrawableShapes(final List<IDrawableShape> shapes)
	{
	}
}
