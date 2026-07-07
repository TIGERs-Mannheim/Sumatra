package edu.tigers.sumatra.pathfinder.subdestgen;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.finder.PathFinderInput;

import java.util.Iterator;


public interface SubDestGenerator
{
	Iterator<IVector2> subDestIterator(PathFinderInput input);
}
