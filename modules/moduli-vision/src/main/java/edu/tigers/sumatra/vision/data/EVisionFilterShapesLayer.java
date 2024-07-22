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
	private static final String VISION_FILTER = "Vision Filter";

	public static final IShapeLayerIdentifier QUALITY_SHAPES = F.create(
			F.category(VISION_FILTER).layerName("Quality Inspector"));
	public static final IShapeLayerIdentifier CAM_INFO_SHAPES = F.create(
			F.category(VISION_FILTER).layerName("Cam Info"));
	public static final IShapeLayerIdentifier VIEWPORT_SHAPES = F.create(
			F.category(VISION_FILTER).layerName("Viewports"));
	public static final IShapeLayerIdentifier ROBOT_TRACKER_SHAPES = F.create(
			F.category(VISION_FILTER).layerName("Robot Trackers")
					.persistenceType(EShapeLayerPersistenceType.ALWAYS_PERSIST));
	public static final IShapeLayerIdentifier ROBOT_QUALITY_INSPECTOR = F.create(
			F.category(VISION_FILTER).layerName("Robot Quality Inspector"));
	public static final IShapeLayerIdentifier BALL_TRACKER_SHAPES_IMPORTANT = F.create(
			F.category(VISION_FILTER).layerName("Ball Trackers"));
	public static final IShapeLayerIdentifier BALL_TRACKER_SHAPES = F.create(
			F.category(VISION_FILTER).layerName("Ball Trackers Debug")
					.persistenceType(EShapeLayerPersistenceType.NEVER_PERSIST));
	public static final IShapeLayerIdentifier VIRTUAL_BALL_SHAPES = F.create(
			F.category(VISION_FILTER).layerName("Virtual Balls"));
}
