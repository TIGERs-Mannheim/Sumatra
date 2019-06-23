/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 15, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes;

import java.awt.Graphics2D;

import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * This interface makes a shape drawable
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IDrawableShape
{
	/**
	 * Paint your shape
	 * 
	 * @param g
	 * @param fieldPanel
	 * @param invert
	 */
	void paintShape(Graphics2D g, IFieldPanel fieldPanel, boolean invert);
}
