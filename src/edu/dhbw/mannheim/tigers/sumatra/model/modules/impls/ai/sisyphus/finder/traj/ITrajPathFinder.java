/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 7, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ITrajPathFinder
{
	/**
	 * @param input
	 * @return
	 */
	TrajPath calcPath(final TrajPathFinderInput input);
}
