/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.loganalysis;


import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeLayerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ELogAnalysisShapesLayer
{
	private static final ShapeLayerFactory F = new ShapeLayerFactory(ELogAnalysisShapesLayer.class, 50);
	private static final String CATEGORY = "LogAnalysis";

	public static final IShapeLayerIdentifier LOG_ANALYSIS = F.create(
			F.layer("Log_Analysis").category(CATEGORY).visibleByDefault(true));
	public static final IShapeLayerIdentifier BALL_POSSESSION = F.create(
			F.layer("Ball_Possession").category(CATEGORY).visibleByDefault(true));
	public static final IShapeLayerIdentifier DRIBBLING = F.create(
			F.layer("DribblingDetection").category(CATEGORY).visibleByDefault(true));
	public static final IShapeLayerIdentifier PASSING = F.create(
			F.layer("PassingDetection").category(CATEGORY).visibleByDefault(true));
}
