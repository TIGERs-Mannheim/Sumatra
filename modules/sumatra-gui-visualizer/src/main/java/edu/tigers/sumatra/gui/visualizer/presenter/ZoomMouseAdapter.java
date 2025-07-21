/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.presenter;

import edu.tigers.sumatra.gui.visualizer.presenter.callbacks.FieldScaler;
import edu.tigers.sumatra.math.SumatraMath;
import lombok.RequiredArgsConstructor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;


@RequiredArgsConstructor
public class ZoomMouseAdapter extends MouseAdapter
{
	private static final double SCROLL_FACTOR = 0.1;

	/**
	 * Set a max cap on the acceleration of the mouse wheel.
	 * Mice like Logitech MX Master 3S report higher steps when scrolling fast.
	 * But this does not feel good when zooming, as it zooms in/out too quickly.
	 */
	private static final double SCROLL_MAX = 3;

	private final FieldScaler fieldScaler;


	@Override
	public void mouseWheelMoved(final MouseWheelEvent e)
	{
		double wheelRotation = SumatraMath.capMagnitude(e.getPreciseWheelRotation(), 0, SCROLL_MAX);
		double scroll = -wheelRotation * SCROLL_FACTOR;
		fieldScaler.scale(e.getPoint(), scroll);
	}
}
