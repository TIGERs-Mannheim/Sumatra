/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeLayerFactory;
import edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EVisionFilterShapesLayer
{
	private static final ShapeLayerFactory F = new ShapeLayerFactory(EVisionFilterShapesLayer.class, 10);
	private static final String CATEGORY = "Vision Filter";

	public static final IShapeLayerIdentifier QUALITY_SHAPES = F.create(
			F.layer("Quality Inspector").category(CATEGORY));
	public static final IShapeLayerIdentifier CAM_INFO_SHAPES = F.create(
			F.layer("Cam Info").category(CATEGORY));
	public static final IShapeLayerIdentifier VIEWPORT_SHAPES = F.create(
			F.layer("Viewports").category(CATEGORY));
	public static final IShapeLayerIdentifier ROBOT_TRACKER_SHAPES = F.create(
			F.layer("Robot Trackers").category(CATEGORY)
					.persistenceType(EShapeLayerPersistenceType.ALWAYS_PERSIST));
	public static final IShapeLayerIdentifier ROBOT_QUALITY_INSPECTOR = F.create(
			F.layer("Robot Quality Inspector").category(CATEGORY));
	public static final IShapeLayerIdentifier BALL_TRACKER_SHAPES_IMPORTANT = F.create(
			F.layer("Ball Trackers").category(CATEGORY));
	public static final IShapeLayerIdentifier BALL_TRACKER_SHAPES = F.create(
			F.layer("Ball Trackers Debug").category(CATEGORY)
					.persistenceType(EShapeLayerPersistenceType.NEVER_PERSIST));
}
