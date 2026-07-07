package edu.tigers.sumatra.ai.metis.ballinterception;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class InterceptionIteration
{
	double ballTravelTime;
	double slackTime;
	double includedSlackTimeBonus;
	boolean isOvershooting;
}
