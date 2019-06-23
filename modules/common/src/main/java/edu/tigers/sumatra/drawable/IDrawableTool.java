/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 18, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.drawable;

import java.awt.Graphics2D;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IDrawableTool
{
	
	/**
	 * Transforms a gui position into a global(field)position.
	 * 
	 * @param guiPosition
	 * @return globalPosition
	 */
	IVector2 transformToGlobalCoordinates(IVector2 guiPosition);
	
	
	/**
	 * Transform a gui position into a global position with respect to the team color
	 * 
	 * @param globalPosition
	 * @param invert
	 * @return
	 */
	IVector2 transformToGlobalCoordinates(IVector2 globalPosition, boolean invert);
	
	
	/**
	 * Transform a global point to a GUI point. The color is important to mirror points correctly for the blue AI
	 * 
	 * @param globalPosition
	 * @param invert
	 * @return
	 */
	IVector2 transformToGuiCoordinates(IVector2 globalPosition, boolean invert);
	
	
	/**
	 * Transforms a global(field)position into a gui position.
	 * 
	 * @param globalPosition
	 * @return guiPosition
	 */
	IVector2 transformToGuiCoordinates(IVector2 globalPosition);
	
	
	/**
	 * Scales a global x length to a gui x length.
	 * 
	 * @param length length on field
	 * @return length in gui
	 */
	int scaleXLength(double length);
	
	
	/**
	 * Scales a global y length to a gui y length.
	 * 
	 * @param length length on field
	 * @return length in gui
	 */
	int scaleYLength(double length);
	
	
	/**
	 * width with margins
	 * 
	 * @return
	 */
	int getFieldTotalWidth();
	
	
	/**
	 * height width margins
	 * 
	 * @return
	 */
	int getFieldTotalHeight();
	
	
	/**
	 * Turn the field in desired angle
	 * 
	 * @param fieldTurn
	 * @param angle [rad]
	 * @param g2
	 */
	void turnField(EFieldTurn fieldTurn, double angle, Graphics2D g2);
	
	
	/**
	 * @return
	 */
	EFieldTurn getFieldTurn();
	
	
	/**
	 * @return
	 */
	int getFieldHeight();
	
	
	/**
	 * @return
	 */
	int getFieldWidth();
	
	
	/**
	 * @return the scaleFactor
	 */
	double getScaleFactor();
	
	
	/**
	 * @return the fieldOriginY
	 */
	double getFieldOriginY();
	
	
	/**
	 * @return the fieldOriginX
	 */
	double getFieldOriginX();
	
	
	/**
	 * @return
	 */
	int getWidth();
	
	
	/**
	 * @return
	 */
	int getHeight();
	
	
	/**
	 * @return
	 */
	int getFieldMargin();
}
