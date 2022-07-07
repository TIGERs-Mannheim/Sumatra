/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.math.vector.IVector2;

import java.awt.Color;


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
	 * Transform a global angle to a GUI angle.
	 *
	 * @param globalAngle
	 * @param invert
	 * @return
	 */
	double transformToGuiAngle(double globalAngle, boolean invert);

	/**
	 * Scales a global length to a gui length.
	 *
	 * @param length length on field
	 * @return length in gui
	 */
	int scaleGlobalToGui(double length);


	/**
	 * @return field background color
	 */
	Color getFieldColor();
}
