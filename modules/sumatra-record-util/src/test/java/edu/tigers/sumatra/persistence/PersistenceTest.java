package edu.tigers.sumatra.persistence;

import edu.tigers.sumatra.ai.PersistenceAiFrame;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ids.ETeamColor;
import org.assertj.core.util.DoubleComparator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for persistence
 */
class PersistenceTest
{
	@Test
	void testSaveLoadRecordFrame() throws ExecutionException, InterruptedException
	{
		PersistenceDb db = PersistenceDb.withCustomLocation(Paths.get(PersistenceTestHelper.DB_NAME));
		db.add(PersistenceAiFrame.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
		PersistenceTable<PersistenceAiFrame> aiTable = db.getTable(PersistenceAiFrame.class);

		PersistenceTestHelper helper = new PersistenceTestHelper();
		List<PersistenceAiFrame> origFrames = helper.createOneRecordFrame();
		// Writes use confined arenas owned by the db's IO thread, so dispatch through the executor.
		db.getIoExecutor().submit(() -> aiTable.write(origFrames)).get();
		Long firstKey = db.getFirstKey();
		assertThat(firstKey).as("Inserted frame should be retrievable").isNotNull();
		assertFrameEquals(aiTable.get(db.getFirstKey()), origFrames.getFirst());
		db.close();

		db = PersistenceDb.withCustomLocation(Paths.get(PersistenceTestHelper.DB_NAME));
		db.add(PersistenceAiFrame.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
		var aiTable2 = db.getTable(PersistenceAiFrame.class);
		firstKey = db.getFirstKey();
		assertThat(firstKey).as("Loaded database should contain the written frame").isNotNull();
		assertFrameEquals(aiTable2.get(db.getFirstKey()), origFrames.getFirst());
		db.close();
	}


	private static void assertFrameEquals(PersistenceAiFrame actual, PersistenceAiFrame expected)
	{
		assertThat(actual.getKey()).isEqualTo(expected.getKey());
		for (ETeamColor color : List.of(ETeamColor.YELLOW, ETeamColor.BLUE))
		{
			VisualizationFrame actualFrame = actual.getVisFrame(color);
			VisualizationFrame expectedFrame = expected.getVisFrame(color);
			assertThat(actualFrame)
					.usingRecursiveComparison()
					.ignoringFields("playStrategy")
					// Doubles are persisted as floats (see MappedDataOutputStream#write(double))
					.withComparatorForType(new DoubleComparator(1e-6), Double.class)
					.isEqualTo(expectedFrame);
		}
	}


	@AfterEach
	void after()
	{
		PersistenceTestHelper.cleanup();
	}


	@BeforeEach
	void before()
	{
		PersistenceTestHelper.cleanup();
	}
}
