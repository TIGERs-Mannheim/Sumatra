/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.shot.states;

import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;
import edu.tigers.sumatra.statemachine.IEvent;


public enum EPassingDetectionState implements IEvent
{
	NO_PASS(new InstanceableClass(NoPassState.class,
			new InstanceableParameter(EPassingDetectionState.class, EPassingDetectionState.PARAM_DESC, "NO_PASS")), false),
	PASS(new InstanceableClass(PassState.class,
			new InstanceableParameter(EPassingDetectionState.class, EPassingDetectionState.PARAM_DESC, "PASS")), true),
	PASS_DETECTION(new InstanceableClass(PassDetectionPhaseState.class,
			new InstanceableParameter(EPassingDetectionState.class, EPassingDetectionState.PARAM_DESC, "PASS_DETECTION")),
			false),
	CHIP(new InstanceableClass(ChipState.class,
			new InstanceableParameter(EPassingDetectionState.class, EPassingDetectionState.PARAM_DESC, "CHIP")), true),
	WAITING_CHIP(new InstanceableClass(WaitingChipState.class,
			new InstanceableParameter(EPassingDetectionState.class, EPassingDetectionState.PARAM_DESC, "WAITING_CHIP")),
			true);
	
	private static final String PARAM_DESC = "stateId";
	
	
	private final InstanceableClass clazz;
	
	private final boolean passActive;
	
	
	EPassingDetectionState(final InstanceableClass clazz, final boolean passActive)
	{
		this.clazz = clazz;
		this.passActive = passActive;
	}
	
	
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
	
	
	public final boolean isPassActive()
	{
		return passActive;
	}
}
