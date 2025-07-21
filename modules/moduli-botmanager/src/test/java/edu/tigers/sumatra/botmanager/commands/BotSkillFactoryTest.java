package edu.tigers.sumatra.botmanager.commands;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.botskills.BotSkillFactory;
import edu.tigers.sumatra.botmanager.botskills.BotSkillGlobalPosition;
import edu.tigers.sumatra.math.vector.Vector2;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BotSkillFactoryTest
{
	@Test
	void parserTest()
	{
		var skill = new BotSkillGlobalPosition(Vector2.zero(), 0, new MoveConstraints());
		var data = BotSkillFactory.getInstance().encode(skill);
		var decoded = BotSkillFactory.getInstance().decode(data, skill.getType().getId());
		assertThat(decoded).isNotNull();
		assertThat(decoded.getType()).isEqualTo(skill.getType());

		var length = BotSkillFactory.getInstance().getLength(skill);
		assertThat(length).isPositive();
	}
}