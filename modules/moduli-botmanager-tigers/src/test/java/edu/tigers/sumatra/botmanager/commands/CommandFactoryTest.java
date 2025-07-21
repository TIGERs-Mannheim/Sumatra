package edu.tigers.sumatra.botmanager.commands;

import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchCtrl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class CommandFactoryTest
{
	@Test
	void parserTest()
	{
		var command = new TigerSystemMatchCtrl();
		var data = CommandFactory.getInstance().encode(command);
		var decoded = CommandFactory.getInstance().decode(data);
		assertThat(decoded).isNotNull();
		assertThat(decoded.getType()).isEqualTo(command.getType());

		var length = CommandFactory.getInstance().getLength(command);
		assertThat(length).isPositive();
	}
}