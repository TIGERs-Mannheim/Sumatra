/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.sumatra.MessagesRobocupSslGeometry;
import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.cam.data.CamFieldArc;
import edu.tigers.sumatra.cam.data.CamFieldLine;
import edu.tigers.sumatra.cam.data.CamFieldSize;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.v2.ILine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * Configuration object for geometry parameters.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class Geometry
{
	private static final Logger log = Logger.getLogger(Geometry.class.getName());
	
	/**
	 * Field Geometry
	 */
	@Configurable(spezis = { "NICOLAI", "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE" })
	private static double fieldLength = 12000;
	@Configurable(spezis = { "NICOLAI", "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE" })
	private static double fieldWidth = 9000;
	@Configurable(spezis = { "NICOLAI", "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE" })
	private static double boundaryWidth = 350;
	@Configurable(spezis = { "NICOLAI", "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE" })
	private static double judgesBorderWidth = 425;
	@Configurable(spezis = { "NICOLAI", "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE" })
	private static double centerCircleRadius = 500;
	@Configurable(spezis = { "NICOLAI", "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE" })
	private static double goalWidth = 1200;
	@Configurable(spezis = { "NICOLAI", "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE" })
	private static double goalDepth = 180;
	@Configurable(spezis = { "NICOLAI", "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE" })
	private static double goalHeight = 155;
	@Configurable(comment = "Name of the CenterCircle arc defined in SSL-Vision", defValue = "CenterCircle")
	private static String centerCircleName = "CenterCircle";
	
	@Configurable(comment = "The team on the negative half", defValue = "BLUE")
	private static ETeamColor negativeHalfTeam = ETeamColor.BLUE;
	
	/**
	 * Penalty Area
	 */
	@Configurable(spezis = { "NICOLAI", "SUMATRA", "LAB", "TISCH",
			"ROBOCUP", "ANDRE" }, comment = "Distance (goal line - penalty mark)")
	private static double distanceToPenaltyMark = 1200;
	@Configurable(spezis = { "NICOLAI", "SUMATRA", "LAB", "TISCH",
			"ROBOCUP", "ANDRE" }, comment = "depth of the penalty area.")
	private static double penaltyAreaDepth = 1200;
	@Configurable(spezis = { "NICOLAI", "SUMATRA", "LAB", "TISCH", "ROBOCUP",
			"ANDRE" }, comment = "the length of the short line of the penalty area, that is parallel to the goal line")
	private static double penaltyAreaFrontLineLength = 2400;
	@Configurable(defValue = "LeftPenaltyStretch", comment = "Name of the LeftPenaltyStretch line defined in SSL-Vision")
	private static String leftPenaltyStretchName = "LeftPenaltyStretch";
	@Configurable(defValue = "RightPenaltyStretch", comment = "Name of the RightPenaltyStretch line defined in SSL-Vision")
	private static String rightPenaltyStretchName = "RightPenaltyStretch";
	@Configurable(defValue = "LeftFieldRightPenaltyArc")
	private static String leftFieldRightPenaltyArcName = "LeftFieldRightPenaltyArc";
	@Configurable(defValue = "false")
	private static boolean legacyPenArea = false;
	
	/**
	 * Object Geometry
	 */
	@Configurable
	private static double ballRadius = 21.5;
	@Configurable
	private static double botRadius = 90;
	@Configurable
	private static double opponentCenter2DribblerDist = 85;
	@Configurable
	private static double goalWallThickness = 20;
	@Configurable
	private static double lineWidth = 10;
	
	/**
	 * TIGERs internal
	 */
	@Configurable(defValue = "10.0")
	private static double penaltyAreaMargin = 10.0;
	
	@Configurable(spezis = { "NICOLAI", "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE" })
	private static double boundaryOffset = 0;
	
	
	private final IRectangle field;
	private final IRectangle fieldWBorders;
	private final IRectangle fieldWReferee;
	private final Goal goalOur;
	private final Goal goalTheir;
	private final IPenaltyArea penaltyAreaOur;
	private final IPenaltyArea penaltyAreaTheir;
	private final ICircle centerCircle;
	private final IRectangle ourHalf;
	private final IRectangle theirHalf;
	private final ILine halfLine;
	private final List<ILineSegment> touchLines;
	
	private CamGeometry lastCamGeometry;
	
	private static BallParameters ballParameters = new BallParameters();
	
	
	private static class ConfigCallback implements IConfigObserver
	{
		@Override
		public void afterApply(final IConfigClient configClient)
		{
			refresh();
		}
	}
	
	private static Geometry instance;
	
	static
	{
		ConfigRegistration.registerClass("geom", Geometry.class);
		ConfigRegistration.registerConfigurableCallback("geom", new ConfigCallback());
		refresh();
	}
	
	
	/**
	 */
	private Geometry()
	{
		// noinspection SuspiciousNameCombination
		field = Rectangle.fromCenter(Vector2f.ZERO_VECTOR, fieldLength, fieldWidth);
		fieldWBorders = field.withMargin(boundaryOffset + boundaryWidth);
		fieldWReferee = fieldWBorders.withMargin(judgesBorderWidth);
		goalOur = calcOurGoal(goalWidth, goalDepth, fieldLength);
		goalTheir = calcTheirGoal(goalWidth, goalDepth, fieldLength);
		
		if (legacyPenArea)
		{
			penaltyAreaOur = new LegacyPenArea(penaltyAreaDepth, penaltyAreaFrontLineLength, -1);
			penaltyAreaTheir = new LegacyPenArea(penaltyAreaDepth, penaltyAreaFrontLineLength, 1);
		} else
		{
			penaltyAreaOur = new PenaltyArea(goalOur.getCenter(), penaltyAreaDepth, penaltyAreaFrontLineLength);
			penaltyAreaTheir = new PenaltyArea(goalTheir.getCenter(), penaltyAreaDepth, penaltyAreaFrontLineLength);
		}
		
		centerCircle = calcCenterCircle(Vector2f.ZERO_VECTOR, centerCircleRadius);
		
		ourHalf = Rectangle.fromCenter(field.center().subtractNew(Vector2.fromXY(field.xExtent() / 4, 0)),
				field.xExtent() / 2,
				field.yExtent());
		
		theirHalf = Rectangle.fromCenter(field.center().addNew(Vector2.fromXY(field.xExtent() / 4, 0)),
				field.xExtent() / 2,
				field.yExtent());
		
		halfLine = calcHalfLine();
		touchLines = new ArrayList<>();
		touchLines.addAll(calcTouchLines());
		
		lastCamGeometry = new CamGeometry(new HashMap<Integer, CamCalibration>(),
				new CamFieldSize(MessagesRobocupSslGeometry.SSL_GeometryFieldSize.getDefaultInstance()));
	}
	
	
	private Set<ILineSegment> calcTouchLines()
	{
		Set<ILineSegment> lines = new HashSet<>();
		lines.add(Lines.segmentFromPoints(Vector2.fromXY(-fieldLength / 2, fieldWidth / 2),
				Vector2.fromXY(fieldLength / 2, fieldWidth / 2)));
		lines.add(Lines.segmentFromPoints(Vector2.fromXY(-fieldLength / 2, -fieldWidth / 2),
				Vector2.fromXY(fieldLength / 2, -fieldWidth / 2)));
		return lines;
	}
	
	
	private ILine calcHalfLine()
	{
		return Lines.lineFromPoints(Vector2.fromY(-fieldWidth / 2), Vector2.fromY(fieldWidth / 2));
	}
	
	
	private static synchronized void update()
	{
		instance = new Geometry();
		
		String env = SumatraModel.getInstance().getEnvironment();
		ConfigRegistration.applySpezis(ballParameters, "geom", "");
		ConfigRegistration.applySpezis(ballParameters, "geom", env);
	}
	
	
	/**
	 * @param geometry
	 */
	public static void setCamDetection(final CamGeometry geometry)
	{
		CamGeometry saveCamGeometry = getLastCamGeometry();
		
		boundaryWidth = geometry.getField().getBoundaryWidth();
		fieldLength = geometry.getField().getFieldLength();
		fieldWidth = geometry.getField().getFieldWidth();
		goalWidth = geometry.getField().getGoalWidth();
		goalDepth = geometry.getField().getGoalDepth();
		
		extractGeometryFromFieldMarkings(geometry);
		
		update();
		
		instance.lastCamGeometry = saveCamGeometry;
		
		instance.lastCamGeometry.update(geometry);
	}
	
	
	private static void extractGeometryFromFieldMarkings(final CamGeometry geometry)
	{
		List<CamFieldLine> lines = geometry.getField().getFieldLines();
		List<CamFieldArc> arcs = geometry.getField().getFieldArcs();
		
		legacyPenArea = arcs.stream().anyMatch(l -> l.getName().equalsIgnoreCase(leftFieldRightPenaltyArcName));
		
		Optional<CamFieldLine> leftPenaltyStretch = lines.stream()
				.filter(l -> l.getName().equals(leftPenaltyStretchName))
				.findFirst();
		
		Optional<CamFieldLine> rightPenaltyStretch = lines.stream()
				.filter(l -> l.getName().equals(rightPenaltyStretchName))
				.findFirst();
		
		if (leftPenaltyStretch.isPresent() && rightPenaltyStretch.isPresent())
		{
			// calculate length of penalty area front line
			double leftLength = leftPenaltyStretch.get().directionVector().getLength2();
			double rightLength = rightPenaltyStretch.get().directionVector().getLength2();
			
			if (!SumatraMath.isEqual(leftLength, rightLength))
			{
				log.warn("Left and right penalty strech lines are different by " + Math.abs(leftLength - rightLength)
						+ "mm. Check vision calibration!");
			}
			
			penaltyAreaFrontLineLength = leftLength;
			
			// calculate penalty area radius/distance to front line/distance to penalty mark
			double leftDist = Math.abs(leftPenaltyStretch.get().supportVector().x());
			double rightDist = Math.abs(rightPenaltyStretch.get().supportVector().x());
			double fieldHalfLength = geometry.getField().getFieldLength() / 2.0;
			
			if (!SumatraMath.isEqual(leftDist, rightDist))
			{
				log.warn("Left and right distances to penalty area are different by " + Math.abs(leftDist - rightDist)
						+ "mm. Check vision calibration!");
			}
			
			if ((fieldHalfLength < leftDist) || (fieldHalfLength < rightDist))
			{
				log.error("Penalty area is beyond field!");
			} else
			{
				penaltyAreaDepth = fieldHalfLength - leftDist;
				distanceToPenaltyMark = penaltyAreaDepth;
			}
		} else
		{
			log.warn("PenaltyStretch line not found in vision geometry. Using defaults.");
		}
		
		Optional<CamFieldArc> centerArc = arcs.stream()
				.filter(a -> a.getName().equals(centerCircleName))
				.findFirst();
		
		if (centerArc.isPresent())
		{
			// use radius + half thickness to reflect outer radius of field marking
			centerCircleRadius = centerArc.get().radius() + (centerArc.get().getThickness() / 2.0);
		} else
		{
			log.warn("CenterCircle arc not found in vision geometry. Using defaults.");
		}
	}
	
	
	/**
	 * Update parameters according to environment
	 */
	public static void refresh()
	{
		String env = SumatraModel.getInstance().getEnvironment();
		ConfigRegistration.applySpezi("geom", env);
		update();
	}
	
	
	private Goal calcOurGoal(final double goalSize, final double goalDepth, final double fieldLength)
	{
		return new Goal(goalSize, Vector2f.fromXY(-fieldLength / 2, 0), goalDepth, goalWallThickness);
	}
	
	
	private Goal calcTheirGoal(final double goalSize, final double goalDepth, final double fieldLength)
	{
		return new Goal(goalSize, Vector2f.fromXY(fieldLength / 2, 0), goalDepth, goalWallThickness);
	}
	
	
	private ICircle calcCenterCircle(final IVector2 center, final double radius)
	{
		return Circle.createCircle(center, radius);
	}
	
	
	/**
	 * @return depth of the penaltyArea
	 */
	public static double getPenaltyAreaDepth()
	{
		return penaltyAreaDepth;
	}
	
	
	/**
	 * @return length of the penaltyArea front line
	 */
	public static double getPenaltyAreaFrontLineLength()
	{
		return penaltyAreaFrontLineLength;
	}
	
	
	/**
	 * Returns our goal.
	 * 
	 * @return goal object
	 */
	public static Goal getGoalOur()
	{
		return instance.goalOur;
	}
	
	
	/**
	 * Returns their goal.
	 * 
	 * @return goal object
	 */
	public static Goal getGoalTheir()
	{
		return instance.goalTheir;
	}
	
	
	/**
	 * Return both goals
	 * 
	 * @return list of goals
	 */
	public static List<Goal> getGoals()
	{
		return Arrays.asList(instance.goalOur, instance.goalTheir);
	}
	
	
	/**
	 * @return [mm] height from field bottom where goal bar starts
	 */
	public static double getGoalHeight()
	{
		return goalHeight;
	}
	
	
	/**
	 * @return the ballRadius [mm]
	 */
	public static double getBallRadius()
	{
		return ballRadius;
	}
	
	
	/**
	 * @return the botRadius [mm]
	 */
	public static double getBotRadius()
	{
		return botRadius;
	}
	
	
	/**
	 * OPPONENT center to dribbler distance. Distance for own bots can be found in tracked bot.
	 * 
	 * @return
	 */
	public static double getOpponentCenter2DribblerDist()
	{
		return opponentCenter2DribblerDist;
	}
	
	
	/**
	 * Returns the field length [mm].
	 * 
	 * @return returns field length (x-axis).
	 */
	public static double getFieldLength()
	{
		return fieldLength;
	}
	
	
	/**
	 * Returns the field xExtent [mm].
	 * 
	 * @return returns field length (y-axis).
	 */
	public static double getFieldWidth()
	{
		return fieldWidth;
	}
	
	
	/**
	 * @return the field
	 */
	public static IRectangle getField()
	{
		return instance.field;
	}
	
	
	/**
	 * Field with border margin, but without referee area
	 * 
	 * @return the fieldWBorders
	 */
	public static IRectangle getFieldWBorders()
	{
		return instance.fieldWBorders;
	}
	
	
	/**
	 * Field including referee area
	 * 
	 * @return the fieldWBorders
	 */
	public static IRectangle getFieldWReferee()
	{
		return instance.fieldWReferee;
	}
	
	
	/**
	 * @return
	 */
	public static IVector2 getCenter()
	{
		return Vector2f.ZERO_VECTOR;
	}
	
	
	/**
	 * @return
	 */
	public static ICircle getCenterCircle()
	{
		return instance.centerCircle;
	}
	
	
	/**
	 * @return
	 */
	public static IPenaltyArea getPenaltyAreaOur()
	{
		return instance.penaltyAreaOur;
	}
	
	
	/**
	 * @return
	 */
	public static IPenaltyArea getPenaltyAreaTheir()
	{
		return instance.penaltyAreaTheir;
	}
	
	
	/**
	 * @return the ourHalf
	 */
	public static IRectangle getFieldHalfOur()
	{
		return instance.ourHalf;
	}
	
	
	/**
	 * @return their half of the field
	 */
	public static IRectangle getFieldHalfTheir()
	{
		return instance.theirHalf;
	}
	
	
	/**
	 * @return the penaltyMark from us
	 */
	public static IVector2 getPenaltyMarkOur()
	{
		return Vector2f.fromXY((-fieldLength / 2) + distanceToPenaltyMark, 0.0);
	}
	
	
	/**
	 * @return the penaltyMark from their
	 */
	public static IVector2 getPenaltyMarkTheir()
	{
		return Vector2f.fromXY((fieldLength / 2) - distanceToPenaltyMark, 0.0);
	}
	
	
	/**
	 * @return The xExtent of the border around the field
	 */
	public static double getBoundaryWidth()
	{
		return boundaryWidth + boundaryOffset;
	}
	
	
	/**
	 * @return the boundaryLength
	 */
	public static double getBoundaryLength()
	{
		return boundaryWidth + boundaryOffset;
	}
	
	
	/**
	 * The default penalty Area margin. Use this is your base margin.
	 * You may want to set a relative margin to this one.
	 * 
	 * @return
	 */
	public static double getPenaltyAreaMargin()
	{
		return penaltyAreaMargin;
	}
	
	
	/**
	 * Returns the last received camera geometry frame.
	 *
	 * @return cam geometry
	 */
	public static CamGeometry getLastCamGeometry()
	{
		return instance.lastCamGeometry;
	}
	
	
	public static BallParameters getBallParameters()
	{
		return ballParameters;
	}
	
	
	public static ILine getHalfLine()
	{
		return instance.halfLine;
	}
	
	
	public static List<ILineSegment> getTouchLines()
	{
		return instance.touchLines;
	}
	
	
	public static ETeamColor getNegativeHalfTeam()
	{
		return negativeHalfTeam;
	}
	
	
	public static void setNegativeHalfTeam(final ETeamColor negativeHalfTeam)
	{
		Geometry.negativeHalfTeam = negativeHalfTeam;
	}
	
	
	public static double getLineWidth()
	{
		return lineWidth;
	}
}
