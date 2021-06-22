/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.shot.states;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;
import edu.tigers.sumatra.statemachine.IEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum EPassingDetectionState implements IInstanceableEnum, IEvent
{
	NO_PASS(new InstanceableClass<>(NoPassState.class,
			new InstanceableParameter(EPassingDetectionState.class, EPassingDetectionState.PARAM_DESC, "NO_PASS")), false),
	PASS(new InstanceableClass<>(PassState.class,
			new InstanceableParameter(EPassingDetectionState.class, EPassingDetectionState.PARAM_DESC, "PASS")), true),
	PASS_DETECTION(new InstanceableClass<>(PassDetectionPhaseState.class,
			new InstanceableParameter(EPassingDetectionState.class, EPassingDetectionState.PARAM_DESC, "PASS_DETECTION")),
			false),
	CHIP(new InstanceableClass<>(ChipState.class,
			new InstanceableParameter(EPassingDetectionState.class, EPassingDetectionState.PARAM_DESC, "CHIP")), true),
	WAITING_CHIP(new InstanceableClass<>(WaitingChipState.class,
			new InstanceableParameter(EPassingDetectionState.class, EPassingDetectionState.PARAM_DESC, "WAITING_CHIP")),
			true);

	private static final String PARAM_DESC = "stateId";


	private final InstanceableClass<?> instanceableClass;
	private final boolean passActive;


	@Override
	public IInstanceableEnum parse(String value)
	{
		return valueOf(value);
	}
}
