/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.XMLConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.data.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamFieldGeometry;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Circlef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Linef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Rectanglef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;


/**
 * Configuration object for geometry parameters.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class Geometry
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final String			nodePath	= "geometry.";
	
	/** [mm] */
	private final float			ballRadius;
	/** [mm] */
	private final float			botRadius;
	/** x-axis [mm] */
	private final float			fieldLength;
	/** y-axis [mm] */
	private final float			fieldWidth;
	/** Represents the field as a rectangle */
	private final Rectanglef	field;
	/** Distance (goal line - penalty mark)[mm] */
	private final float			distanceToPenaltyMark;
	/** radius of the two, small quarter circles at the sides of the penalty area. */
	private final float			distanceToPenaltyArea;
	/** the length of the short line of the penalty area, that is parallel to the goal line */
	private final float			lengthOfPenaltyAreaFrontLine;
	/** [mm] */
	private final float			goalSize;
	/** Our Goal */
	private final Goal			goalOur;
	/** Their Goal */
	private final Goal			goalTheir;
	/** "Mittellinie" */
	private final Linef			median;
	/** Tigers goal line */
	private final Linef			goalLineOur;
	/** Opponent goal line */
	private final Linef			goalLineTheir;
	/** Our Penalty Area ("Strafraum") */
	private final PenaltyArea	penaltyAreaOur;
	/** Their Penalty Area ("Strafraum") */
	private final PenaltyArea	penaltyAreaTheir;
	/** Our penalty mark */
	private final Vector2f		penaltyMarkOur;
	/** Their penalty mark */
	private final Vector2f		penaltyMarkTheir;
	/** The center of the field */
	private final Vector2f		center;
	/** The radius of the center circle ("Mittelkreis") [mm] */
	private final float			centerCircleRadius;
	/** The center circle ("Mittelkreis") */
	private final Circlef		centerCircle;
	
	private final Vector2f		maintenancePosition;
	
	private final Rectanglef	ourHalf;
	
	private final Circlef fakeOurPenArea;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	Geometry(XMLConfiguration configFile)
	{
		ballRadius = configFile.getFloat(nodePath + "ballRadius");
		botRadius = configFile.getFloat(nodePath + "botRadius");
		
		fieldLength = configFile.getFloat(nodePath + "field.length");
		fieldWidth = configFile.getFloat(nodePath + "field.width");
		
		goalSize = configFile.getFloat(nodePath + "field.goalSize");
		distanceToPenaltyMark = configFile.getFloat(nodePath + "field.distanceToPenaltyMark");
		distanceToPenaltyArea = configFile.getFloat(nodePath + "field.distanceToPenaltyArea");
		lengthOfPenaltyAreaFrontLine = configFile.getFloat(nodePath + "field.lengthOfPenaltyAreaFrontLine");
		
		center = getVector(configFile, nodePath + "field.center");
		centerCircleRadius = configFile.getFloat(nodePath + "field.centerCircleRadius");
		
		field = calcField(center, fieldLength, fieldWidth);
		goalOur = calcOurGoal(goalSize, fieldLength);
		goalTheir = calcTheirGoal(goalSize, fieldLength);
		goalLineOur = calcGoalLine(goalOur.getGoalCenter(), AVector2.Y_AXIS);
		goalLineTheir = calcGoalLine(goalTheir.getGoalCenter(), AVector2.Y_AXIS);
		penaltyAreaOur = new PenaltyArea(ETeam.TIGERS, goalOur.getGoalCenter(), distanceToPenaltyArea,
				lengthOfPenaltyAreaFrontLine);
		penaltyAreaTheir = new PenaltyArea(ETeam.OPPONENTS, goalTheir.getGoalCenter(), distanceToPenaltyArea,
				lengthOfPenaltyAreaFrontLine);
		
		penaltyMarkOur = calcOurPenalityMark(fieldLength, distanceToPenaltyMark);
		penaltyMarkTheir = calcTheirPenalityMark(fieldLength, distanceToPenaltyMark);
		centerCircle = calcCenterCircle(center, centerCircleRadius);
		
		median = new Linef(AVector2.ZERO_VECTOR, AVector2.Y_AXIS);
		maintenancePosition = getVector(configFile, nodePath + "maintenancePosition");
		
		ourHalf = new Rectanglef(field.topLeft(), field.xExtend() / 2, field.yExtend());
		fakeOurPenArea = new Circlef(goalOur.getGoalCenter().addNew(new Vector2(-150,0)),850);
	
	}
	

	/**
	 * Translates a new, incoming {@link CamFieldGeometry} and the old {@link Geometry} loaded from XML into the new
	 * {@link Geometry}
	 * 
	 * TODO Gero: This is dirty!!! Think of another solution (maybe move FieldGeometry into the worldFrame??) (Gero)
	 * 
	 * @param camGeom
	 * @param oldGeom
	 */
	Geometry(CamFieldGeometry camGeom, Geometry oldGeom)
	{
		ballRadius = oldGeom.ballRadius;
		botRadius = oldGeom.botRadius;
		
		fieldLength = camGeom.fieldLength;
		fieldWidth = camGeom.fieldWidth;
		
		goalSize = camGeom.goalWidth;
		distanceToPenaltyMark = camGeom.penaltySpotFromFieldLineDist;
		
		lengthOfPenaltyAreaFrontLine = camGeom.defenseStretch;
		distanceToPenaltyArea = camGeom.defenseRadius;
		
		center = oldGeom.center;
		centerCircleRadius = camGeom.centerCircleRadius;
		
		field = calcField(center, fieldLength, fieldWidth);
		goalOur = calcOurGoal(goalSize, fieldLength);
		goalTheir = calcTheirGoal(goalSize, fieldLength);
		goalLineOur = calcGoalLine(goalOur.getGoalCenter(), AVector2.Y_AXIS);
		goalLineTheir = calcGoalLine(goalTheir.getGoalCenter(), AVector2.Y_AXIS);
		penaltyAreaOur = oldGeom.penaltyAreaOur;
		penaltyAreaTheir = oldGeom.penaltyAreaTheir;
		penaltyMarkOur = calcOurPenalityMark(fieldLength, distanceToPenaltyMark);
		penaltyMarkTheir = calcTheirPenalityMark(fieldLength, distanceToPenaltyMark);
		centerCircle = calcCenterCircle(center, centerCircleRadius);
		
		median = oldGeom.median;
		maintenancePosition = oldGeom.maintenancePosition;
		
		ourHalf = oldGeom.ourHalf;
		fakeOurPenArea  = oldGeom.fakeOurPenArea;
		
		// TODO Other parameters from CamFieldGeometry???
	}
	

	private Rectanglef calcField(IVector2 center, float fieldLength, float fieldWidth)
	{
		return new Rectanglef(center.addNew(new Vector2f(-fieldLength / 2, fieldWidth / 2)), fieldLength, fieldWidth);
	}
	

	private Goal calcOurGoal(float goalSize, float fieldLength)
	{
		return new Goal(goalSize, new Vector2f(-fieldLength / 2, 0));
	}
	

	private Goal calcTheirGoal(float goalSize, float fieldLength)
	{
		return new Goal(goalSize, new Vector2f(fieldLength / 2, 0));
	}
	

	private Linef calcGoalLine(IVector2 goalCenter, IVector2 dir)
	{
		return new Linef(goalCenter, dir);
	}
	

	private Circlef calcCenterCircle(IVector2 center, float radius)
	{
		return new Circlef(center, radius);
	}
	

	private Vector2f calcOurPenalityMark(float fieldLength, float distanceToPenaltyMark)
	{
		return new Vector2f(-fieldLength / 2 + distanceToPenaltyMark, 0);
	}
	

	private Vector2f calcTheirPenalityMark(float fieldLength, float distanceToPenaltyMark)
	{
		return new Vector2f(fieldLength / 2 - distanceToPenaltyMark, 0);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 * Returns our goal.
	 * 
	 * @return goal object
	 */
	public Goal getGoalOur()
	{
		return goalOur;
	}
	

	/**
	 * 
	 * Returns their goal.
	 * 
	 * @return goal object
	 */
	public Goal getGoalTheir()
	{
		return goalTheir;
	}
	

	private Vector2f getVector(XMLConfiguration config, String value)
	{
		Vector2 vec = new Vector2();
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
	 * @return the botRadius [mm]
	 */
	public float getBotRadius()
	{
		return botRadius;
	}
	

	/**
	 * 
	 * Returns the field length [mm].
	 * 
	 * @return returns field length (x-axis).
	 */
	public float getFieldLength()
	{
		return fieldLength;
	}
	

	/**
	 * 
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
	public Rectanglef getField()
	{
		return field;
	}
	

	/**
	 * @return the maintenancePosition
	 */
	public Vector2f getMaintenancePosition()
	{
		return maintenancePosition;
	}
	

	public Linef getMedian()
	{
		return median;
	}
	

	public Linef getGoalLineOur()
	{
		return goalLineOur;
	}
	

	public Linef getGoalLineTheir()
	{
		return goalLineTheir;
	}
	

	public float getDistanceToPenaltyMark()
	{
		return distanceToPenaltyMark;
	}
	

	public Vector2f getPenaltyMarkOur()
	{
		return penaltyMarkOur;
	}
	

	public Vector2f getPenaltyMarkTheir()
	{
		return penaltyMarkTheir;
	}
	

	public Vector2f getCenter()
	{
		return center;
	}
	

	public float getGoalSize()
	{
		return goalSize;
	}
	

	public float getCenterCircleRadius()
	{
		return centerCircleRadius;
	}
	

	public Circlef getCenterCircle()
	{
		return centerCircle;
	}
	

	public float getDistanceToPenaltyArea()
	{
		return distanceToPenaltyArea;
	}
	

	public float getLengthOfPenaltyAreaFrontLine()
	{
		return lengthOfPenaltyAreaFrontLine;
	}
	

	public PenaltyArea getPenaltyAreaOur()
	{
		return penaltyAreaOur;
	}
	

	public PenaltyArea getPenaltyAreaTheir()
	{
		return penaltyAreaTheir;
	}
	

	/**
	 * @return the ourHalf
	 */
	public Rectanglef getOurHalf()
	{
		return ourHalf;
	}


	public Circlef getFakeOurPenArea()
	{
		return fakeOurPenArea;
	}
}
