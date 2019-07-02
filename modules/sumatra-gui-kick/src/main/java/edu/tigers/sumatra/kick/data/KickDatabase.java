/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.kick.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;


/**
 * Stores all kick and ball samples.
 * 
 * @author AndreR <andre@ryll.cc>
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class KickDatabase
{
	private List<KickModelSample> kickSamples = new ArrayList<>();
	private List<BallModelSample> ballSamples = new ArrayList<>();
	
	
	/**
	 * @return the kickSamples
	 */
	public List<KickModelSample> getKickSamples()
	{
		return kickSamples;
	}
	
	
	/**
	 * @return the ballSamples
	 */
	public List<BallModelSample> getBallSamples()
	{
		return ballSamples;
	}
}
