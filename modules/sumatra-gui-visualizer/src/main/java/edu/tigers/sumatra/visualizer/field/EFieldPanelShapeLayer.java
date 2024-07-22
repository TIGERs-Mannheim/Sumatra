/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field;

import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeLayerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EFieldPanelShapeLayer
{
	private static final ShapeLayerFactory F = new ShapeLayerFactory(EFieldPanelShapeLayer.class, 50);
	private static final String PANEL = "Panel";

	public static final IShapeLayerIdentifier FPS = F.create(
			F.category(PANEL).layerName("FPS").visibleByDefault(true));
	public static final IShapeLayerIdentifier COORDINATES = F.create(
			F.category(PANEL).layerName("Coordinates").visibleByDefault(true));
	public static final IShapeLayerIdentifier RULER = F.create(
			F.category(PANEL).layerName("Ruler").visibleByDefault(true));
	public static final IShapeLayerIdentifier RECORDING = F.create(
			F.category(PANEL).layerName("Recording").visibleByDefault(true));
}
