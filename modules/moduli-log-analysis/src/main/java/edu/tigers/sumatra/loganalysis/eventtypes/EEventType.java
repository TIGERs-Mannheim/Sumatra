/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import edu.tigers.sumatra.loganalysis.eventtypes.ballpossession.BallPossessionDetection;
import edu.tigers.sumatra.loganalysis.eventtypes.dribbling.DribblingDetection;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.ShotDetection;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum EEventType implements IInstanceableEnum
{
	BALL_POSSESSION(new InstanceableClass<>(BallPossessionDetection.class)),
	DRIBBLING(new InstanceableClass<>(DribblingDetection.class)),
	SHOT(new InstanceableClass<>(ShotDetection.class)),
	;

	private final InstanceableClass<?> instanceableClass;


	@Override
	public IInstanceableEnum parse(String value)
	{
		return valueOf(value);
	}
}
