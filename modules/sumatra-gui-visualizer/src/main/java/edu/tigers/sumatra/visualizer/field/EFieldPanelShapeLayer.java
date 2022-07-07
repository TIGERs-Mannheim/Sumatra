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
	private static final String CATEGORY = "Panel";

	public static final IShapeLayerIdentifier FPS = F.create(F.layer("FPS")
			.category(CATEGORY)
			.visibleByDefault(true));
	public static final IShapeLayerIdentifier COORDINATES = F.create(F.layer("Coordinates")
			.category(CATEGORY)
			.visibleByDefault(true));
	public static final IShapeLayerIdentifier RULER = F.create(F.layer("Ruler")
			.category(CATEGORY)
			.visibleByDefault(true));
	public static final IShapeLayerIdentifier RECORDING = F.create(F.layer("Recording")
			.category(CATEGORY)
			.visibleByDefault(true));
}
