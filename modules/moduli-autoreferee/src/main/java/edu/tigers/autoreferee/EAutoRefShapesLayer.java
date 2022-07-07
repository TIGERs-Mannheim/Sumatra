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
			F.layer("Engine").category(AUTO_REFEREE).visibleByDefault(true));
	public static final IShapeLayerIdentifier LAST_BALL_CONTACT = F.create(
			F.layer("Ball Contact").category(AUTO_REFEREE).visibleByDefault(true));
	public static final IShapeLayerIdentifier BALL_LEFT_FIELD = F.create(
			F.layer("Ball Left Field").category(AUTO_REFEREE).visibleByDefault(true));
	public static final IShapeLayerIdentifier ALLOWED_DISTANCES = F.create(
			F.layer("Allowed Distances").category(AUTO_REFEREE).visibleByDefault(true));
	public static final IShapeLayerIdentifier ALLOWED_DRIBBLING_DISTANCE = F.create(
			F.layer("Allowed Dribbling Distances").category(AUTO_REFEREE).visibleByDefault(true));
	public static final IShapeLayerIdentifier VIOLATED_DISTANCES = F.create(
			F.layer("Violated Distances").category(AUTO_REFEREE).visibleByDefault(true));
	public static final IShapeLayerIdentifier MODE = F.create(
			F.layer("AutoRef Mode").category(AUTO_REFEREE).visibleByDefault(true));
	public static final IShapeLayerIdentifier PUSHING = F.create(
			F.layer("Pushing Detector").category(AUTO_REFEREE).visibleByDefault(true));
	public static final IShapeLayerIdentifier PASS_DETECTION = F.create(
			F.layer("Pass Detection").category(AUTO_REFEREE).visibleByDefault(true));
}
