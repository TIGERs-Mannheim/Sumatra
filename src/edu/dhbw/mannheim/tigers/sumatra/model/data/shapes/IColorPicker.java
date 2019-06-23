/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 7, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes;

import java.awt.Color;
import java.awt.Graphics2D;


/**
 * Choose a color w.r.t. to a value
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IColorPicker
{
	/**
	 * @param g
	 * @param relValue A value between 0 and 1
	 * @return
	 */
	Color applyColor(Graphics2D g, float relValue);
	
	
	/**
	 * @param relValue
	 * @return
	 */
	Color getColor(float relValue);
}
