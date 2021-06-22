/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_FieldShapeType;
import edu.tigers.sumatra.math.circle.IArc;
import lombok.Value;


/**
 * SSL Vision arc
 */
@Value
public class CamFieldArc
{
	String name;
	SSL_FieldShapeType type;
	double thickness;
	IArc arc;
}
