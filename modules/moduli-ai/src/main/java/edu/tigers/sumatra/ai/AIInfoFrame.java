/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai;

import edu.tigers.sumatra.ai.ares.AresData;
import edu.tigers.sumatra.ai.ares.IAresData;
import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.athena.IPlayStrategy;
import edu.tigers.sumatra.ai.athena.PlayStrategy;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Delegate;


/**
 * This class is a simple container for all information the AI gathers during its processes for one {@link WorldFrame}
 */
@Value
@Builder
public class AIInfoFrame
{
	@Delegate
	BaseAiFrame baseAiFrame;
	AthenaAiFrame athenaAiFrame;
	IAresData aresData;


	/**
	 * Create a frame based on the given baseAiFrame, filling the rest with dummy data
	 *
	 * @param baseAiFrame
	 * @return
	 */
	public static AIInfoFrame fromBaseAiFrame(BaseAiFrame baseAiFrame)
	{
		MetisAiFrame metisAiFrame = MetisAiFrame.builder()
				.baseAiFrame(baseAiFrame)
				.tacticalField(TacticalField.empty())
				.build();
		AthenaAiFrame athenaAiFrame = AthenaAiFrame.builder()
				.baseAiFrame(baseAiFrame)
				.metisAiFrame(metisAiFrame)
				.playStrategy(PlayStrategy.builder().build())
				.build();
		return AIInfoFrame.builder()
				.baseAiFrame(baseAiFrame)
				.athenaAiFrame(athenaAiFrame)
				.aresData(new AresData())
				.build();
	}


	public TacticalField getTacticalField()
	{
		return athenaAiFrame.getMetisAiFrame().getTacticalField();
	}


	public IPlayStrategy getPlayStrategy()
	{
		return athenaAiFrame.getPlayStrategy();
	}
}
