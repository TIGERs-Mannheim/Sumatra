/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.dribble;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AllArgsConstructor;
import lombok.Value;


@Value
@Persistent
@AllArgsConstructor
public class DribblingInformation
{
	IVector2 startPos;
	boolean dribblingInProgress;
	BotID dribblingBot;
}
