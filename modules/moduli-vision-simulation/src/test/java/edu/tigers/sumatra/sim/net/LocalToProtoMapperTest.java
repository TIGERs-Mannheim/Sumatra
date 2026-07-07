package edu.tigers.sumatra.sim.net;

import edu.tigers.sumatra.bot.MoveConstraints;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class LocalToProtoMapperTest
{
	@Test
	void driveLimitsRoundTrip()
	{
		MoveConstraints original = MoveConstraints.ZERO
				.withVelMax(1.5)
				.withAccMax(1.0)
				.withBrkMax(2.0)
				.withVelMaxW(20.0)
				.withAccMaxW(50.0);

		SimBotActionOuterClass.DriveLimits proto = LocalToProtoMapper.mapDriveLimits(original);
		MoveConstraints roundTripped = ProtoToLocalMapper.mapDriveLimits(proto);

		assertThat(roundTripped.getVelMax()).isEqualTo(1.5);
		assertThat(roundTripped.getAccMax()).isEqualTo(1.0);
		assertThat(roundTripped.getBrkMax()).isEqualTo(2.0);
		assertThat(roundTripped.getVelMaxW()).isEqualTo(20.0);
		assertThat(roundTripped.getAccMaxW()).isEqualTo(50.0);
	}
}
