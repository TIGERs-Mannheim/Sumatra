/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector2;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class SimilarityCheckerTest
{

	@Test
	public void initAllGameEvents()
	{
		SimilarityChecker checker = new SimilarityChecker().initAllGameEvents();

		var aimlessKick1 = new AimlessKick(BotID.createBotId(1, ETeamColor.YELLOW), Vector2.fromXY(1, 2),
				Vector2.fromXY(3, 4));
		var aimlessKick2 = new AimlessKick(BotID.createBotId(1, ETeamColor.YELLOW), Vector2.fromXY(1, 2),
				Vector2.fromXY(3, 4));
		var aimlessKick3 = new AimlessKick(BotID.createBotId(2, ETeamColor.YELLOW), Vector2.fromXY(1, 2),
				Vector2.fromXY(3, 4));
		assertThat(checker.isSimilar(aimlessKick1, aimlessKick1)).isTrue();
		assertThat(checker.isSimilar(aimlessKick1, aimlessKick2)).isTrue();
		assertThat(checker.isSimilar(aimlessKick1, aimlessKick3)).isFalse();
	}
}