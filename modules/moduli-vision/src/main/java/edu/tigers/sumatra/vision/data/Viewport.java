/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.math.rectangle.IRectangle;
import lombok.Value;


@Value
public class Viewport
{
	int cameraId;
	IRectangle area;
}
