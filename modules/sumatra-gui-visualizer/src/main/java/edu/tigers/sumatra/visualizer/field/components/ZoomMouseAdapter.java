/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field.components;

import edu.tigers.sumatra.visualizer.field.callbacks.FieldScaler;
import lombok.RequiredArgsConstructor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;


@RequiredArgsConstructor
public class ZoomMouseAdapter extends MouseAdapter
{
	private static final double SCROLL_SPEED = 12;

	private final FieldScaler fieldScaler;


	@Override
	public void mouseWheelMoved(final MouseWheelEvent e)
	{
		final double scroll = 1.0 - e.getWheelRotation() / SCROLL_SPEED;
		fieldScaler.scale(e.getPoint(), scroll);
	}
}
