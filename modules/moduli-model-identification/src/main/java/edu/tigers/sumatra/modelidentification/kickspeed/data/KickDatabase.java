/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.modelidentification.kickspeed.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import edu.tigers.sumatra.vision.kick.estimators.EBallModelIdentType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Stores all kick and ball samples.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@Getter
public class KickDatabase
{
	private List<KickModelSample> kickSamples = Collections.synchronizedList(new ArrayList<>());
	private Map<EBallModelIdentType, List<BallModelSample>> ballSamplesByIdentType = Collections.synchronizedMap(new HashMap<>());
	private List<RedirectModelSample> redirectSamples = Collections.synchronizedList(new ArrayList<>());
}
