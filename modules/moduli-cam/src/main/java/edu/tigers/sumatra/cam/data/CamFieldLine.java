/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_FieldShapeType;
import edu.tigers.sumatra.math.line.ILine;
import lombok.Value;


/**
 * SSL vision field line
 */
@Value
public class CamFieldLine
{
	String name;
	SSL_FieldShapeType type;
	double thickness;
	ILine line;
}
