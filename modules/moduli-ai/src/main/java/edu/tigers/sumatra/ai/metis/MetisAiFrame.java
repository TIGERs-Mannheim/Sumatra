/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis;

import edu.tigers.sumatra.ai.BaseAiFrame;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.Delegate;

import java.util.Map;


/**
 * This frame extends the {@link BaseAiFrame} with information gathered from Metis module.
 */

@Value
@Builder
public class MetisAiFrame
{
	@Delegate
	@NonNull
	BaseAiFrame baseAiFrame;
	@NonNull
	TacticalField tacticalField;

	@Singular
	Map<Class<? extends ACalculator>, CalculatorExecution> calculatorExecutions;
}
