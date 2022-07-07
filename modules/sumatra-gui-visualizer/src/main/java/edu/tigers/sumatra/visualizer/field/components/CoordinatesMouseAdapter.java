/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field.components;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.visualizer.field.callbacks.MousePointTransformer;
import lombok.RequiredArgsConstructor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;


@RequiredArgsConstructor
public class CoordinatesMouseAdapter extends MouseAdapter
{
	private final MousePointTransformer mousePointTransformer;
	private final Consumer<List<DrawableCoordinates>> coordinatesConsumer;


	@Override
	public void mouseMoved(final MouseEvent e)
	{
		IVector2 lastMousePoint = mousePointTransformer.toGlobal(e.getX(), e.getY());
		List<DrawableCoordinates> coordinates = Stream.of(ETeamColor.values())
				.map(team -> new DrawableCoordinates(lastMousePoint, team))
				.toList();
		coordinatesConsumer.accept(coordinates);
	}
}
