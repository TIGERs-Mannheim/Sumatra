package edu.tigers.sumatra.statemachine;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.function.BooleanSupplier;


@Value
@Builder
public class Transition
{
	@Builder.Default
	String name = "";
	@NonNull
	BooleanSupplier evaluation;
	@NonNull
	IState nextState;
}
