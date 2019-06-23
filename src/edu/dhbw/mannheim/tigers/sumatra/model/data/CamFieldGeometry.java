/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import java.io.Serializable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;


/**
 * Data holder for the fields geometry information provided by SSL-Vision
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @author Gero
 * 
 */
public class CamFieldGeometry implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 8954822618638598522L;
	

	public final int				lineWidth;
	public final int				fieldLength;
	public final int				fieldWidth;
	public final int				boundaryWidth;
	public final int				refereeWidth;
	public final int				goalWidth;
	public final int				goalDepth;
	public final int				goalWallWidth;
	public final int				centerCircleRadius;
	public final int				defenseRadius;
	public final int				defenseStretch;
	public final int				freeKickFromDefenseDist;
	public final int				penaltySpotFromFieldLineDist;
	public final int				penaltyLineFromSpotDist;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * <p>
	 * <i>Implemented being aware of EJSE Item 2; but we prefer performance over readability - at least in this case.
	 * Objects are created at only one point in the system, but needs to be fast (so builder seems to be too much
	 * overhead).</i>
	 * </p>
	 * 
	 * @param lineWidth
	 * @param fieldLength
	 * @param fieldWidth
	 * @param boundaryWidth
	 * @param refereeWidth
	 * @param goalWidth
	 * @param goalDepth
	 * @param goalWallWidth
	 * @param centerCircleRadius
	 * @param defenseRadius
	 * @param defenseStretch
	 * @param freeKickFromDefenseDist
	 * @param penaltySpotFromFieldLineDist
	 * @param penaltyLineFromSpotDist
	 */
	public CamFieldGeometry(int lineWidth, int fieldLength, int fieldWidth, int boundaryWidth, int refereeWidth,
			int goalWidth, int goalDepth, int goalWallWidth, int centerCircleRadius, int defenseRadius,
			int defenseStretch, int freeKickFromDefenseDist, int penaltySpotFromFieldLineDist,
			int penaltyLineFromSpotDist)
	{
		this.lineWidth = lineWidth;
		this.fieldLength = fieldLength;
		this.fieldWidth = fieldWidth;
		this.boundaryWidth = boundaryWidth;
		this.refereeWidth = refereeWidth;
		this.goalWidth = goalWidth;
		this.goalDepth = goalDepth;
		this.goalWallWidth = goalWallWidth;
		this.centerCircleRadius = centerCircleRadius;
		this.defenseRadius = defenseRadius;
		this.defenseStretch = defenseStretch;
		this.freeKickFromDefenseDist = freeKickFromDefenseDist;
		this.penaltySpotFromFieldLineDist = penaltySpotFromFieldLineDist;
		this.penaltyLineFromSpotDist = penaltyLineFromSpotDist;
	}
	

	public CamFieldGeometry(SSL_GeometryFieldSize fs)
	{
		this.lineWidth = fs.getLineWidth();
		this.fieldLength = fs.getFieldLength();
		this.fieldWidth = fs.getFieldWidth();
		this.boundaryWidth = fs.getBoundaryWidth();
		this.refereeWidth = fs.getRefereeWidth();
		this.goalWidth = fs.getGoalWidth();
		this.goalDepth = fs.getGoalDepth();
		this.goalWallWidth = fs.getGoalWallWidth();
		this.centerCircleRadius = fs.getCenterCircleRadius();
		this.defenseRadius = fs.getDefenseRadius();
		this.defenseStretch = fs.getDefenseStretch();
		this.freeKickFromDefenseDist = fs.getFreeKickFromDefenseDist();
		this.penaltySpotFromFieldLineDist = fs.getPenaltySpotFromFieldLineDist();
		this.penaltyLineFromSpotDist = fs.getPenaltyLineFromSpotDist();
	}
	

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SSLFieldGeometry [line_width=");
		builder.append(lineWidth);
		builder.append(", field_length=");
		builder.append(fieldLength);
		builder.append(", field_width=");
		builder.append(fieldWidth);
		builder.append(", boundary_width=");
		builder.append(boundaryWidth);
		builder.append(", referee_width=");
		builder.append(refereeWidth);
		builder.append(", goal_width=");
		builder.append(goalWidth);
		builder.append(", goal_depth=");
		builder.append(goalDepth);
		builder.append(", goal_wall_width=");
		builder.append(goalWallWidth);
		builder.append(", center_circle_radius=");
		builder.append(centerCircleRadius);
		builder.append(", defense_radius=");
		builder.append(defenseRadius);
		builder.append(", defense_stretch=");
		builder.append(defenseStretch);
		builder.append(", free_kick_from_defense_dist=");
		builder.append(freeKickFromDefenseDist);
		builder.append(", penalty_spot_from_field_line_dist=");
		builder.append(penaltySpotFromFieldLineDist);
		builder.append(", penalty_line_from_spot_dist=");
		builder.append(penaltyLineFromSpotDist);
		builder.append("]");
		return builder.toString();
	}
}
