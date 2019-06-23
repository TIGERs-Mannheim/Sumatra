/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 1, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.vis;

import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IWpCalc
{
	/**
	 * @param wfw
	 */
	void process(WorldFrameWrapper wfw);
}
