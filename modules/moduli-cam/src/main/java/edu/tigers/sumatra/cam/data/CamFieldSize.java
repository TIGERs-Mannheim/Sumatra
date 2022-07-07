/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import lombok.Builder;
import lombok.Value;

import java.util.List;


/**
 * SSL vision field dimensions
 */
@Value
@Builder
public class CamFieldSize
{
	double fieldLength;
	double fieldWidth;
	double goalWidth;
	double goalDepth;
	double boundaryWidth;
	List<CamFieldLine> fieldLines;
	List<CamFieldArc> fieldArcs;
	double penaltyAreaDepth;
	double penaltyAreaWidth;
	double centerCircleRadius;
	double lineThickness;
	double goalCenterToPenaltyMark;
	double goalHeight;
	double ballRadius;
	double robotRadius;
}
