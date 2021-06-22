/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.calc;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.text.DecimalFormat;


/**
 * A pass detected by the autoRef.
 */
@Value
@Builder
class Pass
{
	int id;
	long timestamp;
	boolean valid;
	double distance;
	Double directionChange;
	BotID shooter;
	BotID receiver;
	double initialBallSpeed;
	double receivingBallSpeed;
	double direction;
	IVector2 source;
	IVector2 target;


	@Override
	public String toString()
	{
		var df = new DecimalFormat("0.0");
		var angleDf = new DecimalFormat("0.00");
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("id", id)
				.append("timestamp", timestamp)
				.append("distance", df.format(distance))
				.append("direction", angleDf.format(direction))
				.append("directionChange", directionChange == null ? null : angleDf.format(directionChange))
				.append("valid", valid)
				.append("source", df.format(source.x()) + ", " + df.format(source.y()))
				.append("target", df.format(target.x()) + ", " + df.format(target.y()))
				.append("shooter", shooter)
				.append("receiver", receiver)
				.append("initialBallSpeed", df.format(initialBallSpeed))
				.append("receivingBallSpeed", df.format(receivingBallSpeed))
				.toString();
	}
}
