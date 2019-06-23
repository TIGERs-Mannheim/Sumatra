/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util.penarea;

import org.junit.Before;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class RectifiedPenaltyAreaTest extends APenAreaTest
{
	
	@Before
	public void init()
	{
		penaltyArea = new RectifiedPenaltyArea(goalCenter, depth, length);
	}
}
