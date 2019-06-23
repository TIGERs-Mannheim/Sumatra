/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 14, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.wp.data.MotionContext;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IWfPostProcessor
{
	/**
	 * @param swf
	 * @return
	 */
	SimpleWorldFrame process(final SimpleWorldFrame swf);
	
	
	/**
	 * @param context
	 * @param timestamp
	 */
	void processMotionContext(MotionContext context, long timestamp);
}
