/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.presenter;

import edu.tigers.sumatra.gui.visualizer.presenter.callbacks.MousePointTransformer;
import edu.tigers.sumatra.gui.visualizer.presenter.drawables.DrawableRuler;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.RequiredArgsConstructor;

import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;


@RequiredArgsConstructor
public class RulerMouseAdapter extends MouseAdapter
{
	private final MousePointTransformer mousePointTransformer;
	private final Consumer<DrawableRuler> rulerConsumer;
	private IVector2 dragPointStart;

	@Override
	public void mousePressed(final MouseEvent e)
	{
		dragPointStart = mousePointTransformer.toGlobal(e.getX(), e.getY());
	}


	@Override
	public void mouseDragged(final MouseEvent e)
	{
		if (SwingUtilities.isLeftMouseButton(e) && (e.isAltDown()) && dragPointStart != null)
		{
			IVector2 dragPointEnd = mousePointTransformer.toGlobal(e.getX(), e.getY());
			DrawableRuler ruler = new DrawableRuler(dragPointStart, dragPointEnd);
			rulerConsumer.accept(ruler);
		}
	}


	@Override
	public void mouseReleased(final MouseEvent e)
	{
		dragPointStart = null;
		rulerConsumer.accept(null);
	}
}
