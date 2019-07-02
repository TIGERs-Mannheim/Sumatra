/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes;

import com.github.g3force.instanceables.InstanceableClass;
import edu.tigers.sumatra.loganalysis.eventtypes.ballpossession.BallPossessionDetection;
import edu.tigers.sumatra.loganalysis.eventtypes.dribbling.DribblingDetection;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.ShotDetection;


public enum EEventType
{
	BALL_POSSESSION(new InstanceableClass(BallPossessionDetection.class)),
	DRIBBLING(new InstanceableClass(DribblingDetection.class)),
	SHOT(new InstanceableClass(ShotDetection.class)),
	;
	
	private final InstanceableClass clazz;
	
	
	EEventType(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}
	
	
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
}
