package edu.tigers.sumatra.ai.metis;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class CalculatorExecution
{
	boolean executed;
	long processingTime;
}
