package edu.tigers.sumatra.ai.metis.ballinterception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;


@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class BallInterceptionInformation
{
	@NonNull
	List<InterceptionIteration> initialIterations;
	double interceptionTargetTime;
	InterceptionIteration oldInterception;
}
