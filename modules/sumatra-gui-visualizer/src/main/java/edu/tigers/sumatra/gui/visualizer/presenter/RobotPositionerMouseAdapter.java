/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.presenter;

import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.gui.visualizer.presenter.callbacks.MousePointTransformer;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.RequiredArgsConstructor;

import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;


@RequiredArgsConstructor
public class RobotPositionerMouseAdapter extends MouseAdapter
{
	private final MousePointTransformer mousePointTransformer;
	private final Consumer<DrawableLine> drawableLineConsumer;
	private final Consumer<ILineSegment> lineConsumer;
	private IVector2 dragPointStart;

	@Override
	public void mousePressed(final MouseEvent e)
	{
		dragPointStart = mousePointTransformer.toGlobal(e.getX(), e.getY());
	}


	@Override
	public void mouseDragged(final MouseEvent e)
	{
		if (SwingUtilities.isRightMouseButton(e) && (e.isControlDown()) && dragPointStart != null)
		{
			IVector2 dragPointEnd = mousePointTransformer.toGlobal(e.getX(), e.getY());
			ILineSegment line = Lines.segmentFromPoints(dragPointStart, dragPointEnd);
			DrawableLine drawableLine = new DrawableLine(line);
			drawableLineConsumer.accept(drawableLine);
			lineConsumer.accept(line);
		}
	}


	@Override
	public void mouseReleased(final MouseEvent e)
	{
		dragPointStart = null;
		drawableLineConsumer.accept(null);
		lineConsumer.accept(null);
	}
}
