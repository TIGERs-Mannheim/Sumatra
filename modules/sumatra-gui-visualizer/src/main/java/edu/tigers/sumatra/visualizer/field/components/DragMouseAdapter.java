/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field.components;

import lombok.RequiredArgsConstructor;

import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


@RequiredArgsConstructor
public class DragMouseAdapter extends MouseAdapter
{
	private final DragMouseHandler dragMouseHandler;
	private int mousePressedY = 0;
	private int mousePressedX = 0;


	@Override
	public void mousePressed(final MouseEvent e)
	{
		mousePressedY = e.getY();
		mousePressedX = e.getX();
	}


	@Override
	public void mouseDragged(final MouseEvent e)
	{
		if (SwingUtilities.isLeftMouseButton(e) && !e.isControlDown() && !e.isAltDown())
		{
			final int dy = e.getY() - mousePressedY;
			final int dx = e.getX() - mousePressedX;
			dragMouseHandler.drag(dx, dy);
			mousePressedY += dy;
			mousePressedX += dx;
		}
	}


	@FunctionalInterface
	public interface DragMouseHandler
	{
		void drag(int dx, int dy);
	}
}
