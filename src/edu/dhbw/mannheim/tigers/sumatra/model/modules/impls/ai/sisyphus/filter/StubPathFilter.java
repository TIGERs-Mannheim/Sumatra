/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.filter;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.IPath;


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
