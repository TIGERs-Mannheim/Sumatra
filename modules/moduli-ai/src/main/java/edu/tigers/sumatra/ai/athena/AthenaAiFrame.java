package edu.tigers.sumatra.ai.athena;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ids.BotID;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.Delegate;

import java.util.Map;


/**
 * Ai frame for athena data, based on {@link MetisAiFrame}
 */
@Value
@Builder
public class AthenaAiFrame
{
	@Delegate
	BaseAiFrame baseAiFrame;
	MetisAiFrame metisAiFrame;
	IPlayStrategy playStrategy;
	@Singular("botAiInfo")
	Map<BotID, BotAiInformation> botAiInformation;


	public TacticalField getTacticalField()
	{
		return metisAiFrame.getTacticalField();
	}
}
