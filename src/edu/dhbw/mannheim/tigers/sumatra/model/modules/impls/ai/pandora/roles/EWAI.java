/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s):
 * Daniel Waigand, Christian K�nig
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles;


/**
 * Enum, roles can use to determine, which type they are.
 * 
 * For example, a defence-blocker can be left, right or in the middle. This he should know to act according to it
 * "Enum Who Am I"
 * 
 * @author Christian
 * 
 */
public enum EWAI
{
	/** */
	ONLY,
	/** */
	FIRST,
	/** */
	SECOND,
	
	/** */
	MIDDLE,
	/** */
	LEFT,
	/** */
	RIGHT,
	
	/** */
	GOALIE,
	/** */
	DEFENDER,
	/**
	 * 
	 */
	OTHER;
	
	
}
