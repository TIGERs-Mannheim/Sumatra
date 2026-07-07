package edu.tigers.sumatra.botparams;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


class BotParamsDatabaseTest
{
	@Test
	void roundTripsWholeDatabase(@TempDir Path tmpDir) throws Exception
	{
		// Seed a config file as it would exist on disk, then exercise the real load/save logic
		// from BotParamsManager (including its ObjectMapper configuration) for a full round trip.
		File source = tmpDir.resolve("source.json").toFile();
		try (InputStream in = Objects.requireNonNull(
				getClass().getResourceAsStream("botParamsDatabase.json"),
				"test resource botParamsDatabase.json not found"
		))
		{
			Files.copy(in, source.toPath());
		}

		BotParamsDatabase db = BotParamsManager.loadDatabase(source);

		File roundTripped = tmpDir.resolve("roundTripped.json").toFile();
		BotParamsManager.saveDatabase(roundTripped, db);
		BotParamsDatabase reread = BotParamsManager.loadDatabase(roundTripped);

		// Recursive comparison covers every entry, label and nested spec, since BotParams
		// and BotDimensions do not implement equals.
		assertThat(reread).usingRecursiveComparison().isEqualTo(db);
	}
}
