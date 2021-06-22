/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Data;


/**
 * @author MarkG
 */
@Data
public class SkirmishInformation
{
	private ESkirmishStrategy strategy = ESkirmishStrategy.NONE;
	private IVector2 supportiveCircleCatchPos;
	private boolean startCircleMove;
}
