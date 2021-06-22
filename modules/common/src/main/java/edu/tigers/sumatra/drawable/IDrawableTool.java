/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.math.vector.IVector2;

import java.awt.Color;
import java.awt.Graphics2D;


/**
 * Interface for drawing to the FieldPanel.
 */
public interface IDrawableTool
{
	/**
	 * Transform a global point to a GUI point. The color is important to mirror points correctly for the blue AI
	 *
	 * @param globalPosition
	 * @param invert
	 * @return
	 */
	IVector2 transformToGuiCoordinates(IVector2 globalPosition, boolean invert);


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
	 * Turn the field in desired angle
	 *
	 * @param fieldTurn
	 * @param angle     [rad]
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
	 * @return
	 */
	int getFieldMargin();

	/**
	 * @return field background color
	 */
	Color getFieldColor();
}
