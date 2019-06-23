/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.filter;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.IPath;


/**
 * A Path filter may decide if a path is accepted, given the current path, or not.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IPathFilter
{
	/**
	 * If the path is accepted, it will be send to the skill, if not, the current path will be kept.
	 * 
	 * @param pathFinderInput
	 * @param newPath
	 * @param currentPath
	 * @return
	 */
	boolean accept(PathFinderInput pathFinderInput, IPath newPath, IPath currentPath);
	
	
	/**
	 * You can add shapes here if you like
	 * 
	 * @param shapes
	 */
	void getDrawableShapes(List<IDrawableShape> shapes);
}
