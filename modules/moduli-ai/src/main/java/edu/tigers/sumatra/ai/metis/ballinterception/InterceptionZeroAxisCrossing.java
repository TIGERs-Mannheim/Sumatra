package edu.tigers.sumatra.ai.metis.ballinterception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InterceptionZeroAxisCrossing
{
	private double ballTravelTime;
	private boolean positiveCrossing;
}
