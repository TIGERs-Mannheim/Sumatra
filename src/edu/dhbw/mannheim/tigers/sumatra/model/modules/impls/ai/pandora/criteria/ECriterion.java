/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.03.2011
 * Author(s):
 * FlorianS
 * ChristianK
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria;

/**
 * Enum for specification of ACriterion type
 * 
 * @author FlorianS
 * 
 */
public enum ECriterion
{
	// basic
	BALL_POSSESSION,
	DYNAMIC_FIELD_RASTER,
	OBJECT_POSITION,
	TEAM_CLOSEST_TO_BALL,
	
	// related to our team
	TIGERS_PASS_RECEIVER,
	TIGERS_SCORING_CHANCE,
	TIGERS_APPROXIMATE_SCORING_CHANCE,
	
	// related to our opponents
	OPPONENT_PASS_RECEIVER,
	OPPONENT_SCORING_CHANCE,
	OPPONENT_APPROXIMATE_SCORING_CHANCE
}
