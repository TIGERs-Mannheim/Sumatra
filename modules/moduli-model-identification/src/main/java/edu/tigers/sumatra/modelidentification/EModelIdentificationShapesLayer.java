package edu.tigers.sumatra.modelidentification;

import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeLayerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EModelIdentificationShapesLayer
{
	private static final ShapeLayerFactory F = new ShapeLayerFactory(EModelIdentificationShapesLayer.class, 0);
	private static final String MOVEMENT_OBSERVER = "Movement Observer";

	public static final IShapeLayerIdentifier BALL_OBSERVER = F.create(
			F.category(MOVEMENT_OBSERVER).layerName("Ball observer"));
	public static final IShapeLayerIdentifier BOT_OBSERVER = F.create(
			F.category(MOVEMENT_OBSERVER).layerName("Bot observer"));
}
