/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.test;

import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Setter;


/**
 * Test Play for testing ball placement
 */
public class DribblingTestPlay extends APlay
{
	@Setter
	private IVector2 practiceLocation;


	public DribblingTestPlay()
	{
		super(EPlay.TEST_DRIBBLING);
	}


	@Override
	protected void doUpdateBeforeRoles()
	{
		// do nothing for now
	}
}
