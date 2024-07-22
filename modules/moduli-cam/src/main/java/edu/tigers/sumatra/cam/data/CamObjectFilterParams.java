/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.cam.data;

import edu.tigers.sumatra.math.rectangle.IRectangle;

import java.util.List;


public record CamObjectFilterParams(
		IRectangle exclusionRectangle,
		List<Integer> excludedCamIds
)
{
}
