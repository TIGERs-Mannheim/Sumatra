/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.situation.zone;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.awt.Color;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class OffensiveZones
{
	private final Map<EOffensiveZone, OffensiveZone> zones = new EnumMap<>(EOffensiveZone.class);

	@Configurable(defValue = "800.0")
	private static double penAreaAttackerSidesMargin = 800.0;

	@Configurable(defValue = "1300.0")
	private static double centerAttackerZoneWidth = 1300.0;

	@Configurable(defValue = "3300.0")
	private static double closeMidfieldCenterWidth = 3300.0;

	@Configurable(defValue = "1800.0")
	private static double closeMidfieldCenterLength = 1800.0;

	@Configurable(defValue = "3000.0")
	private static double midfieldLength = 3000.0;

	static
	{
		ConfigRegistration.registerClass("metis", OffensiveZones.class);
	}

	private OffensiveZones()
	{
	}

	@Value
	@EqualsAndHashCode
	public static class OffensiveZoneGeometry
	{
		double fieldWidth;
		double fieldLength;
		IVector2 penAreaTheirPosCorner;
		IVector2 penAreaTheirNegCorner;
	}


	private IRectangle getMirroredRectangle(IRectangle rect)
	{
		var p1 = rect.getCorner(IRectangle.ECorner.BOTTOM_LEFT);
		var p2 = rect.getCorner(IRectangle.ECorner.TOP_RIGHT);
		return Rectangle.fromPoints(
				Vector2.fromXY(p1.x(), -p1.y()),
				Vector2.fromXY(p2.x(), -p2.y()));
	}


	public List<IDrawableShape> getZoneShapes(Color color)
	{
		return zones
				.values()
				.stream()
				.map(e -> new DrawableRectangle(e.getRect(), color).setStrokeWidth(23.0))
				.map(IDrawableShape.class::cast)
				.toList();
	}


	public List<IDrawableShape> getZoneAnnotations(Color color)
	{
		return zones
				.values()
				.stream()
				.map(e -> new DrawableAnnotation(e.getRect().center(), e.getZoneName().name(), color).withFontHeight(100)
						.withCenterHorizontally(true))
				.map(IDrawableShape.class::cast)
				.toList();
	}


	public Optional<OffensiveZone> getZoneByPoint(IVector2 point)
	{
		return zones
				.values()
				.stream()
				.filter(e -> e.getRect().isPointInShape(point))
				.findFirst();
	}


	public OffensiveZone getZone(EOffensiveZone zone)
	{
		return zones.get(zone);
	}


	public static OffensiveZones generateDefaultOffensiveZones(OffensiveZoneGeometry geometry)
	{
		OffensiveZones offensiveZones = new OffensiveZones();
		IVector2 posCorner = geometry.getPenAreaTheirPosCorner();
		IVector2 positivePenZoneCorner = Vector2.fromXY(posCorner.x(), posCorner.y() + penAreaAttackerSidesMargin);
		IVector2 negativePenZoneCorner = Vector2.fromXY(geometry.fieldLength / 2.0,
				-posCorner.y() - penAreaAttackerSidesMargin);
		Rectangle rect = Rectangle.fromPoints(
				geometry.penAreaTheirNegCorner,
				negativePenZoneCorner);
		offensiveZones.zones.put(EOffensiveZone.PEN_AREA_ATTACKER_SIDE,
				new OffensiveZone(EOffensiveZone.PEN_AREA_ATTACKER_SIDE, rect));
		offensiveZones.zones.put(EOffensiveZone.PEN_AREA_ATTACKER_SIDE_MIRROR,
				new OffensiveZone(EOffensiveZone.PEN_AREA_ATTACKER_SIDE_MIRROR, offensiveZones.getMirroredRectangle(rect)));

		IVector2 positiveCenterAttackerZoneCorner = positivePenZoneCorner.addNew(Vector2.fromX(-centerAttackerZoneWidth));
		offensiveZones.zones.put(EOffensiveZone.PEN_AREA_ATTACKER_CENTER,
				new OffensiveZone(EOffensiveZone.PEN_AREA_ATTACKER_CENTER,
						Rectangle.fromPoints(positiveCenterAttackerZoneCorner,
								Vector2.fromXY(positivePenZoneCorner.x(), -positivePenZoneCorner.y()))));

		rect = Rectangle.fromPoints(positiveCenterAttackerZoneCorner,
				Vector2.fromXY(geometry.fieldLength / 2.0, geometry.fieldWidth / 2.0));
		offensiveZones.zones.put(EOffensiveZone.OPPONENT_CORNER, new OffensiveZone(EOffensiveZone.OPPONENT_CORNER, rect));
		offensiveZones.zones.put(EOffensiveZone.OPPONENT_CORNER_MIRROR,
				new OffensiveZone(EOffensiveZone.OPPONENT_CORNER_MIRROR, offensiveZones.getMirroredRectangle(rect)));

		IVector2 positiveCloseMidfieldCenterCorner = Vector2.fromXY(
				positiveCenterAttackerZoneCorner.x() - closeMidfieldCenterLength,
				closeMidfieldCenterWidth / 2.0);
		offensiveZones.zones.put(EOffensiveZone.CLOSE_MIDFIELD_CENTER,
				new OffensiveZone(EOffensiveZone.CLOSE_MIDFIELD_CENTER, Rectangle.fromPoints(
						positiveCloseMidfieldCenterCorner,
						Vector2.fromXY(positiveCenterAttackerZoneCorner.x(), -positiveCloseMidfieldCenterCorner.y()))));

		rect = Rectangle.fromPoints(
				Vector2.fromXY(positiveCenterAttackerZoneCorner.x(), positiveCloseMidfieldCenterCorner.y()),
				Vector2.fromXY(positiveCloseMidfieldCenterCorner.x(), geometry.fieldWidth / 2.0));
		offensiveZones.zones.put(EOffensiveZone.CLOSE_MIDFIELD_SIDE,
				new OffensiveZone(EOffensiveZone.CLOSE_MIDFIELD_SIDE, rect));
		offensiveZones.zones.put(EOffensiveZone.CLOSE_MIDFIELD_SIDE_MIRROR,
				new OffensiveZone(EOffensiveZone.CLOSE_MIDFIELD_SIDE_MIRROR, offensiveZones.getMirroredRectangle(rect)));

		double midfieldPosX = positiveCloseMidfieldCenterCorner.x() - midfieldLength;
		offensiveZones.zones.put(EOffensiveZone.MIDFIELD, new OffensiveZone(EOffensiveZone.MIDFIELD,
				Rectangle.fromPoints(
						Vector2.fromXY(positiveCloseMidfieldCenterCorner.x(), geometry.fieldWidth / 2.0),
						Vector2.fromXY(midfieldPosX, -geometry.fieldWidth / 2.0))));

		offensiveZones.zones.put(EOffensiveZone.BACKFIELD, new OffensiveZone(EOffensiveZone.BACKFIELD,
				Rectangle.fromPoints(
						Vector2.fromXY(midfieldPosX, geometry.fieldWidth / 2.0),
						Vector2.fromXY(-geometry.fieldLength / 2.0, -geometry.fieldWidth / 2.0)
				)));
		return offensiveZones;
	}
}


