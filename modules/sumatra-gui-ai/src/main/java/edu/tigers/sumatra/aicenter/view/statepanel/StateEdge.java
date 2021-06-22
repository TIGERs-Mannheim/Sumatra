/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.aicenter.view.statepanel;

import lombok.AllArgsConstructor;
import lombok.Value;


@Value
@AllArgsConstructor
public class StateEdge
{
	String fromVertice;
	String toVertice;
	String eventText;
}

