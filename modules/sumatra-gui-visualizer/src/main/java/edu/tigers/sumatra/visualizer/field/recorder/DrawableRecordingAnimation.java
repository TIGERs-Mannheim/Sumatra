/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field.recorder;

import edu.tigers.sumatra.drawable.IDrawableShape;

import java.awt.Color;
import java.awt.Graphics2D;


public class DrawableRecordingAnimation implements IDrawableShape
{
	private long recordingAnimation;


	@Override
	public void paintBorder(Graphics2D g, int width, int height)
	{
		g.setColor(Color.red);
		int recordingRadius = 15 + (int) ((Math.sin(recordingAnimation / 7.0)) * 10);
		int recX = width - 50 - recordingRadius / 2;
		int recY = 15 - recordingRadius / 2;
		g.fillOval(recX, recY, recordingRadius, recordingRadius);

		g.drawString("REC", width - 50 + 17, 15 + 4);
		recordingAnimation++;
	}
}
