/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.kick.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Stores all kick and ball samples.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@Getter
public class KickDatabase
{
	private List<KickModelSample> kickSamples = Collections.synchronizedList(new ArrayList<>());
	private List<BallModelSample> ballSamples = Collections.synchronizedList(new ArrayList<>());
	private List<RedirectModelSample> redirectSamples = Collections.synchronizedList(new ArrayList<>());
}
