/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 20, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.ellipse;

/**
 * Type of an apex ("Scheitel")
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EApexType
{
	/** main axis (longer axis) in positive direction */
	MAIN_POS,
	/** main axis (longer axis) in negative direction */
	MAIN_NEG,
	/** secondary axis (shorter axis) in positive direction */
	SEC_POS,
	/** secondary axis (shorter axis) in negative direction */
	SEC_NEG,
	/** intersecting point in western direction */
	CENTER_WEST,
	/** intersecting point in eastern direction */
	CENTER_EAST,
	/** intersecting point in southern direction */
	CENTER_SOUTH,
	/** intersecting point in northern direction */
	CENTER_NORTH,
	/** westernmost point */
	WESTERNMOST,
	/** easternmost point */
	EASTERNMOST,
	/** southernmost point */
	SOUTHERNMOST,
	/** northernmost point */
	NORTHERNMOST;
}
