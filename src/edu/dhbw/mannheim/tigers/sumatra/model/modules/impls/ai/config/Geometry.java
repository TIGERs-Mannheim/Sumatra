/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.Configuration;

import edu.dhbw.mannheim.tigers.sumatra.model.data.area.FreekickArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.area.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;


/**
 * Configuration object for geometry parameters.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class Geometry
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** [mm] */
	private final float			ballRadius;
	/** [mm] */
	private final float			botRadius;
	/** [mm] */
	private final float			botCenterToDribblerDist;
	/** [m/s] */
	private final float			stopSpeed;
	/** x-axis [mm] */
	private final float			fieldLength;
	/** y-axis [mm] */
	private final float			fieldWidth;
	/** boundaryWidth [mm] */
	private final float			boundaryWidth;
	/** boundaryLength [mm] */
	private final float			boundaryLength;
	/** judgesWidth [mm] */
	private final float			judgesBorderWidth;
	/** judgesLength [mm] */
	private final float			judgesBorderLength;
	/** Represents the field as a rectangle */
	private final Rectangle		field;
	/** Represents the field WITH margin as a rectangle */
	private final Rectangle		fieldWBorders;
	/** Represents the field with margin and referee area */
	private final Rectangle		fieldWReferee;
	/** Distance (goal line - penalty mark)[mm] */
	private final float			distanceToPenaltyMark;
	/** radius of the two, small quarter circles at the sides of the penalty area. */
	private final float			distanceToPenaltyArea;
	/**  */
	private final float			distancePenaltyMarkToPenaltyLine;
	/** the length of the short line of the penalty area, that is parallel to the goal line */
	private final float			lengthOfPenaltyAreaFrontLine;
	/** [mm] */
	private final float			goalSize;
	/** Our Goal */
	private final Goal			goalOur;
	/** Their Goal */
	private final Goal			goalTheir;
	/** "Mittellinie" */
	private final Line			median;
	/** Tigers goal line */
	private final Line			goalLineOur;
	/** Opponent goal line */
	private final Line			goalLineTheir;
	/** Our Penalty Area ("Strafraum") */
	private final PenaltyArea	penaltyAreaOur;
	/** Their Penalty Area ("Strafraum") */
	private final PenaltyArea	penaltyAreaTheir;
	/** Our penalty mark */
	private final Vector2f		penaltyMarkOur;
	/** Their penalty mark */
	private final Vector2f		penaltyMarkTheir;
	/** penalty line on our side (bots must be behind this line when a penalty kick is executed) */
	private final Vector2f		penaltyLineOur;
	/** penalty line on their side (bots must be behind this line when a penalty kick is executed) */
	private final Vector2f		penaltyLineTheir;
	/** Our Freekick Area ("erweiterter Strafraum bei free kick") */
	private final FreekickArea	freekickAreaOur;
	/** Their Freekick Area ("erweiterter Strafraum bei free kick") */
	private final FreekickArea	freekickAreaTheir;
	/** The center of the field */
	private final Vector2f		center;
	/** The radius of the center circle ("Mittelkreis") [mm] */
	private final float			centerCircleRadius;
	/** The center circle ("Mittelkreis") */
	private final Circle			centerCircle;
	
	private final Vector2f		maintenancePosition;
	
	private final Rectangle		ourHalf;
	
	private final float			botToBallDistanceStop;
	private float					goalDepth;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	Geometry(final Configuration config)
	{
		ballRadius = config.getFloat("ballRadius");
		botRadius = config.getFloat("botRadius");
		botCenterToDribblerDist = config.getFloat("botCenterToDribblerDist");
		stopSpeed = config.getFloat("stopSpeed");
		
		fieldLength = config.getFloat("field.length");
		fieldWidth = config.getFloat("field.width");
		boundaryWidth = config.getFloat("field.border.widthW");
		boundaryLength = config.getFloat("field.border.widthL");
		judgesBorderWidth = config.getFloat("field.judgesBorder.widthW");
		judgesBorderLength = config.getFloat("field.judgesBorder.widthL");
		
		goalSize = config.getFloat("field.goal.innerWidth");
		goalDepth = config.getFloat("field.goal.innerDepth");
		distanceToPenaltyMark = config.getFloat("field.distanceToPenaltyMark");
		distanceToPenaltyArea = config.getFloat("field.distanceToPenaltyArea");
		distancePenaltyMarkToPenaltyLine = config.getFloat("field.distanceToPenaltyLine");
		lengthOfPenaltyAreaFrontLine = config.getFloat("field.lengthOfPenaltyAreaFrontLine");
		
		center = getVector(config, "field.center");
		centerCircleRadius = config.getFloat("field.centerCircleRadius");
		
		field = calcField(center, fieldLength, fieldWidth);
		fieldWBorders = calcField(center, fieldLength + (boundaryLength * 2), fieldWidth + (boundaryWidth * 2));
		fieldWReferee = calcField(center, fieldLength + (boundaryLength * 2) + (judgesBorderLength * 2), fieldWidth
				+ (boundaryWidth * 2) + (judgesBorderWidth * 2));
		goalOur = calcOurGoal(goalSize, fieldLength);
		goalTheir = calcTheirGoal(goalSize, fieldLength);
		goalLineOur = calcGoalLine(goalOur.getGoalCenter(), AVector2.Y_AXIS);
		goalLineTheir = calcGoalLine(goalTheir.getGoalCenter(), AVector2.Y_AXIS);
		penaltyAreaOur = new PenaltyArea(ETeam.TIGERS, config);
		penaltyAreaTheir = new PenaltyArea(ETeam.OPPONENTS, config);
		freekickAreaOur = new FreekickArea(ETeam.TIGERS, config);
		freekickAreaTheir = new FreekickArea(ETeam.OPPONENTS, config);
		
		penaltyMarkOur = calcOurPenalityMark(fieldLength, distanceToPenaltyMark);
		penaltyMarkTheir = calcTheirPenalityMark(fieldLength, distanceToPenaltyMark);
		centerCircle = calcCenterCircle(center, centerCircleRadius);
		penaltyLineOur = calcOurPenalityLine(fieldLength, distanceToPenaltyMark, distancePenaltyMarkToPenaltyLine);
		penaltyLineTheir = calcTheirPenalityLine(fieldLength, distanceToPenaltyMark, distancePenaltyMarkToPenaltyLine);
		
		median = new Line(AVector2.ZERO_VECTOR, AVector2.Y_AXIS);
		maintenancePosition = getVector(config, "field.maintenancePosition");
		
		ourHalf = new Rectangle(field.topLeft(), field.xExtend() / 2, field.yExtend());
		
		botToBallDistanceStop = config.getFloat("field.botToBallDistanceStop");
	}
	
	
	/**
	 * @return the goalDepth
	 */
	public float getGoalDepth()
	{
		return goalDepth;
	}
	
	
	private Rectangle calcField(final IVector2 center, final float fieldLength, final float fieldWidth)
	{
		return new Rectangle(center.addNew(new Vector2f(-fieldLength / 2, fieldWidth / 2)), fieldLength, fieldWidth);
	}
	
	
	private Goal calcOurGoal(final float goalSize, final float fieldLength)
	{
		return new Goal(goalSize, new Vector2f(-fieldLength / 2, 0), -getGoalDepth());
	}
	
	
	private Goal calcTheirGoal(final float goalSize, final float fieldLength)
	{
		return new Goal(goalSize, new Vector2f(fieldLength / 2, 0), getGoalDepth());
	}
	
	
	private Line calcGoalLine(final IVector2 goalCenter, final IVector2 dir)
	{
		return new Line(goalCenter, dir);
	}
	
	
	private Circle calcCenterCircle(final IVector2 center, final float radius)
	{
		return new Circle(center, radius);
	}
	
	
	private Vector2f calcOurPenalityMark(final float fieldLength, final float distanceToPenaltyMark)
	{
		return new Vector2f((-fieldLength / 2) + distanceToPenaltyMark, 0);
	}
	
	
	private Vector2f calcTheirPenalityMark(final float fieldLength, final float distanceToPenaltyMark)
	{
		return new Vector2f((fieldLength / 2) - distanceToPenaltyMark, 0);
	}
	
	
	private Vector2f calcTheirPenalityLine(final float fieldLength, final float distanceToPenaltyMark,
			final float distanceToPenaltyLine)
	{
		return new Vector2f((fieldLength / 2) - distanceToPenaltyMark - distanceToPenaltyLine, 0);
	}
	
	
	private Vector2f calcOurPenalityLine(final float fieldLength, final float distanceToPenaltyMark,
			final float distanceTopenaltyLine)
	{
		return new Vector2f((-fieldLength / 2) + distanceToPenaltyMark + distanceTopenaltyLine, 0);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Returns our goal.
	 * 
	 * @return goal object
	 */
	public Goal getGoalOur()
	{
		return goalOur;
	}
	
	
	/**
	 * Returns their goal.
	 * 
	 * @return goal object
	 */
	public Goal getGoalTheir()
	{
		return goalTheir;
	}
	
	
	private Vector2f getVector(final Configuration config, final String value)
	{
		final Vector2 vec = new Vector2();
		vec.setSavedString(config.getString(value));
		return new Vector2f(vec);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the ballRadius [mm]
	 */
	public float getBallRadius()
	{
		return ballRadius;
	}
	
	
	/**
	 * @return the stopSpeed
	 */
	public final float getStopSpeed()
	{
		return stopSpeed;
	}
	
	
	/**
	 * @return the botRadius [mm]
	 */
	public float getBotRadius()
	{
		return botRadius;
	}
	
	
	/**
	 * Returns the field length [mm].
	 * 
	 * @return returns field length (x-axis).
	 */
	public float getFieldLength()
	{
		return fieldLength;
	}
	
	
	/**
	 * Returns the field width [mm].
	 * 
	 * @return returns field length (y-axis).
	 */
	public float getFieldWidth()
	{
		return fieldWidth;
	}
	
	
	/**
	 * @return the field
	 */
	public Rectangle getField()
	{
		return field;
	}
	
	
	/**
	 * Field with border margin, but without referee area
	 * 
	 * @return the fieldWBorders
	 */
	public Rectangle getFieldWBorders()
	{
		return fieldWBorders;
	}
	
	
	/**
	 * Field including referee area
	 * 
	 * @return the fieldWBorders
	 */
	public Rectangle getFieldWReferee()
	{
		return fieldWReferee;
	}
	
	
	/**
	 * @return the maintenancePosition
	 */
	public Vector2f getMaintenancePosition()
	{
		return maintenancePosition;
	}
	
	
	/**
	 * @return "Mittellinie"
	 */
	public Line getMedian()
	{
		return median;
	}
	
	
	/**
	 * @return
	 */
	public Line getGoalLineOur()
	{
		return goalLineOur;
	}
	
	
	/**
	 * @return
	 */
	public Line getGoalLineTheir()
	{
		return goalLineTheir;
	}
	
	
	/**
	 * @return distance from goal line to penalty mark
	 */
	public float getDistanceToPenaltyMark()
	{
		return distanceToPenaltyMark;
	}
	
	
	/**
	 * @return Vector pointing to our penalty mark
	 */
	public Vector2f getPenaltyMarkOur()
	{
		return penaltyMarkOur;
	}
	
	
	/**
	 * @return Vector pointing to their penalty mark
	 */
	public Vector2f getPenaltyMarkTheir()
	{
		return penaltyMarkTheir;
	}
	
	
	/**
	 * @return
	 */
	public Vector2f getCenter()
	{
		return center;
	}
	
	
	/**
	 * @return
	 */
	public float getGoalSize()
	{
		return goalSize;
	}
	
	
	/**
	 * @return
	 */
	public float getCenterCircleRadius()
	{
		return centerCircleRadius;
	}
	
	
	/**
	 * @return
	 */
	public Circle getCenterCircle()
	{
		return centerCircle;
	}
	
	
	/**
	 * @return distance from goal line to penalty area
	 */
	public float getDistanceToPenaltyArea()
	{
		return distanceToPenaltyArea;
	}
	
	
	/**
	 * @return
	 */
	public float getLengthOfPenaltyAreaFrontLine()
	{
		return lengthOfPenaltyAreaFrontLine;
	}
	
	
	/**
	 * @return
	 */
	public PenaltyArea getPenaltyAreaOur()
	{
		return penaltyAreaOur;
	}
	
	
	/**
	 * @return
	 */
	public PenaltyArea getPenaltyAreaTheir()
	{
		return penaltyAreaTheir;
	}
	
	
	/**
	 * @return the ourHalf
	 */
	public Rectangle getOurHalf()
	{
		return ourHalf;
	}
	
	
	/**
	 * penalty line on our side (bots must be behind this line when a penalty kick is executed)
	 * 
	 * @return vector pointing to the center of the line
	 */
	public Vector2f getPenaltyLineOur()
	{
		return penaltyLineOur;
	}
	
	
	/**
	 * penalty line their side(bots must be behind this line when a penalty kick is executed)
	 * 
	 * @return vector pointing to the center of the line
	 */
	public Vector2f getPenaltyLineTheir()
	{
		return penaltyLineTheir;
	}
	
	
	/**
	 * @return The width of the border around the field
	 */
	public float getBoundaryWidth()
	{
		return boundaryWidth;
	}
	
	
	/**
	 * @return distance from penalty mark to penalty line
	 */
	public float getDistancePenaltyMarkToPenaltyLine()
	{
		return distancePenaltyMarkToPenaltyLine;
	}
	
	
	/**
	 * distance between ball and bot required during stop signal (without ball and bot radius!)
	 * 
	 * @return distance
	 */
	public float getBotToBallDistanceStop()
	{
		return botToBallDistanceStop;
	}
	
	
	/**
	 * @return the boundaryLength
	 */
	public final float getBoundaryLength()
	{
		return boundaryLength;
	}
	
	
	/**
	 * @return the judgesBorderWidth
	 */
	public final float getJudgesBorderWidth()
	{
		return judgesBorderWidth;
	}
	
	
	/**
	 * @return the judgesBorderLength
	 */
	public final float getJudgesBorderLength()
	{
		return judgesBorderLength;
	}
	
	
	/**
	 * @return the botCenterToDribblerDist
	 */
	public final float getBotCenterToDribblerDist()
	{
		return botCenterToDribblerDist;
	}
	
	
	/**
	 * @return the freekickarea of us
	 */
	public FreekickArea getFreekickAreaOur()
	{
		return freekickAreaOur;
	}
	
	
	/**
	 * @return the freekick area of them
	 */
	public FreekickArea getFreekickAreaTheir()
	{
		return freekickAreaTheir;
	}
}
