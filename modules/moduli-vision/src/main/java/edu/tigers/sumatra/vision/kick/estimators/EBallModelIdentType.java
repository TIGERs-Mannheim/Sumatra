/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators;

import edu.tigers.sumatra.vision.kick.estimators.chip.ChipKickSolverNonLinIdentDirect.ChipModelIdentResult;
import edu.tigers.sumatra.vision.kick.estimators.straight.FlatKickSolverNonLin3Factor;
import edu.tigers.sumatra.vision.kick.estimators.straight.StraightKickSolverNonLinIdentDirect.StraightModelIdentResult;


/**
 * @author AndreR <andre@ryll.cc>
 */
public enum EBallModelIdentType
{
	STRAIGHT_TWO_PHASE(StraightModelIdentResult.getParameterNames()),
	CHIP_FIXED_LOSS_PLUS_ROLLING(ChipModelIdentResult.getParameterNames()),
	REDIRECT(FlatKickSolverNonLin3Factor.RedirectModelIdentResult.getParameterNames()),
	;

	private final String[] parameterNames;


	EBallModelIdentType(final String[] parameterNames)
	{
		this.parameterNames = parameterNames;
	}


	/**
	 * @return the parameterNames
	 */
	public String[] getParameterNames()
	{
		return parameterNames;
	}
}
