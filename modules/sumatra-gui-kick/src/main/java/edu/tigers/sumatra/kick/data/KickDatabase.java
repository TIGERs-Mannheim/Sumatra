/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.kick.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


/**
 * Stores all kick and ball samples.
 *
 * @author AndreR <andre@ryll.cc>
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@Getter
public class KickDatabase
{
	private List<KickModelSample> kickSamples = new ArrayList<>();
	private List<BallModelSample> ballSamples = new ArrayList<>();
	private List<RedirectModelSample> redirectSamples = new ArrayList<>();
}
