/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.IPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.TuneableParameter;


/**
 * Default path finder implementation. Path goes straight to the destination
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class StubPathFinder implements IPathFinder
{
	@Override
	public IPath calcPath(final PathFinderInput pathFinderInput)
	{
		List<IVector2> nodes = new ArrayList<IVector2>(1);
		nodes.add(pathFinderInput.getDestination());
		Path path = new Path(nodes, pathFinderInput.getDstOrient());
		return path;
	}
	
	
	@Override
	public TuneableParameter getAdjustableParams()
	{
		// TODO dirk: Auto-generated method stub
		return null;
	}
}
