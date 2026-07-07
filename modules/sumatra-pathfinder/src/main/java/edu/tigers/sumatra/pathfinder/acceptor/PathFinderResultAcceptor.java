package edu.tigers.sumatra.pathfinder.acceptor;

import edu.tigers.sumatra.pathfinder.finder.PathFinderResult;


public interface PathFinderResultAcceptor
{
	boolean accept(PathFinderResult result);
}
