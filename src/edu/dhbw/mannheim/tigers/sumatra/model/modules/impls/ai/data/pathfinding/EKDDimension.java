/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.12.2010
 * Author(s): König
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding;

/**
 * KD-Tree needs enums to know the dimension. for now, i only use x and y axis
 * (i won't use z-axis, but maybe introduce other dimensions later
 * 
 * @author König
 * 
 */
public enum EKDDimension
{
	X_PLANE,
	Y_PLANE; 
}
