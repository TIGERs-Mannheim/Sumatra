/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee;


import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeLayerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EAutoRefShapesLayer
{
	private static final ShapeLayerFactory F = new ShapeLayerFactory(EAutoRefShapesLayer.class, 50);
	private static final String AUTO_REFEREE = "AutoReferee";

	public static final IShapeLayerIdentifier ENGINE = F.create(
			F.category(AUTO_REFEREE).layerName("Engine").visibleByDefault(true));
	public static final IShapeLayerIdentifier LAST_BALL_CONTACT = F.create(
			F.category(AUTO_REFEREE).layerName("Ball Contact").visibleByDefault(true));
	public static final IShapeLayerIdentifier BALL_LEFT_FIELD = F.create(
			F.category(AUTO_REFEREE).layerName("Ball Left Field").visibleByDefault(true));
	public static final IShapeLayerIdentifier ALLOWED_DISTANCES = F.create(
			F.category(AUTO_REFEREE).layerName("Allowed Distances").visibleByDefault(true));
	public static final IShapeLayerIdentifier ALLOWED_DRIBBLING_DISTANCE = F.create(
			F.category(AUTO_REFEREE).layerName("Allowed Dribbling Distances").visibleByDefault(true));
	public static final IShapeLayerIdentifier VIOLATED_DISTANCES = F.create(
			F.category(AUTO_REFEREE).layerName("Violated Distances").visibleByDefault(true));
	public static final IShapeLayerIdentifier MODE = F.create(
			F.category(AUTO_REFEREE).layerName("AutoRef Mode").visibleByDefault(true));
	public static final IShapeLayerIdentifier PUSHING = F.create(
			F.category(AUTO_REFEREE).layerName("Pushing Detector").visibleByDefault(true));
	public static final IShapeLayerIdentifier PASS_DETECTION = F.create(
			F.category(AUTO_REFEREE).layerName("Pass Detection").visibleByDefault(true));
}
