package edu.tigers.sumatra.wp.vis;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@FunctionalInterface
public interface IWpCalc
{
	/**
	 * @param wfw
	 * @param shapeMap
	 */
	void process(WorldFrameWrapper wfw, final ShapeMap shapeMap);
	
	
	/**
	 * 
	 */
	default void reset()
	{
	}
}
