/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder;

import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeLayerFactory;
import edu.tigers.sumatra.drawable.ShapeLayerIdentifier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.DEBUG_PERSIST;


/**
 * Shape layers for Skills
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EPathFinderShapesLayer
{
	private static final ShapeLayerFactory F = new ShapeLayerFactory(EPathFinderShapesLayer.class, 80);
	private static final String PATH_FINDER = "PathFinder";

	public static final IShapeLayerIdentifier PATH_COLLISION_AREA = F.create(
			F.layer("Path collision area").category(PATH_FINDER).persistenceType(DEBUG_PERSIST));

	public static final IShapeLayerIdentifier PATHS_CHECKED = F.create(
			F.layer("Paths checked").category(PATH_FINDER).persistenceType(DEBUG_PERSIST));

	public static final IShapeLayerIdentifier SUB_DEST_TRIED = F.create(
			F.layer("Tried sub destinations").category(PATH_FINDER).persistenceType(DEBUG_PERSIST));

	public static final IShapeLayerIdentifier COLLISION_CHECK_POINTS = F.create(
			F.layer("Collision check points").category(PATH_FINDER).persistenceType(DEBUG_PERSIST));

	public static final IShapeLayerIdentifier ALL_OBSTACLES = F.create(
			F.layer("All obstacles").category(PATH_FINDER));


	public static IShapeLayerIdentifier obstacleCheckPoints(String obstacleId)
	{
		return F.create(
				ShapeLayerIdentifier.builder()
						.layerName(obstacleId)
						.orderId(113)
						.category(PATH_FINDER)
						.category("Check point checked")
						.persistenceType(DEBUG_PERSIST)
		);
	}


	public static IShapeLayerIdentifier obstacleCheckPointsCollision(String obstacleId)
	{
		return F.create(
				ShapeLayerIdentifier.builder()
						.layerName(obstacleId)
						.orderId(112)
						.category(PATH_FINDER)
						.category("Check point w/ collision")
						.persistenceType(DEBUG_PERSIST)
		);
	}


	public static IShapeLayerIdentifier obstacleCheckPointsNoCollision(String obstacleId)
	{
		return F.create(
				ShapeLayerIdentifier.builder()
						.layerName(obstacleId)
						.orderId(111)
						.category(PATH_FINDER)
						.category("Check point w/o collision")
						.persistenceType(DEBUG_PERSIST)
		);
	}


	public static IShapeLayerIdentifier obstacle(String obstacleId)
	{
		return F.create(
				ShapeLayerIdentifier.builder()
						.layerName(obstacleId)
						.orderId(100)
						.category(PATH_FINDER)
						.category("Obstacles")
						.persistenceType(DEBUG_PERSIST)
		);
	}
}
