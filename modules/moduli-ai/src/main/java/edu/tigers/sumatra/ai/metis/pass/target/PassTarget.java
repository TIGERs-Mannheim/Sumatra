/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.target;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


/**
 * An implementation of a pass target.
 */
@Persistent
@Data
@RequiredArgsConstructor
public class PassTarget implements IPassTarget
{
	@NonNull
	private final IVector2 pos;
	@NonNull
	private final BotID botId;


	protected PassTarget()
	{
		pos = Vector2.zero();
		botId = BotID.noBot();
	}
}
