/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import edu.tigers.sumatra.ai.PersistenceAiFrame;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.log.PersistenceLogEvent;
import edu.tigers.sumatra.persistence.processors.AiFrameSizeAnalyzer;
import edu.tigers.sumatra.persistence.processors.BotFeedbackAnalyzer;
import edu.tigers.sumatra.persistence.processors.FrameClassSizeAnalyzer;
import edu.tigers.sumatra.persistence.processors.ShapeMapSizeAnalyzer;
import edu.tigers.sumatra.persistence.processors.ShapeMapSumSizeAnalyzer;
import edu.tigers.sumatra.wp.PersistenceShapeMapFrame;
import edu.tigers.sumatra.wp.data.PersistenceCamDetectionFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;


/**
 * Analyze the size of a recording.
 */
@Log4j2
public class PersistenceDbAnalyzer implements AutoCloseable
{
	private static final Path OUTPUT_FOLDER = Paths.get("");

	private final PersistenceDb db;


	private PersistenceDbAnalyzer(String dbPath)
	{
		db = PersistenceDb.withCustomLocation(Paths.get(dbPath));
		db.add(PersistenceAiFrame.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
		db.add(PersistenceCamDetectionFrame.class, EPersistenceKeyType.ARBITRARY);
		db.add(PersistenceShapeMapFrame.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
		db.add(WorldFrameWrapper.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
		db.add(PersistenceLogEvent.class, EPersistenceKeyType.ARBITRARY);
	}


	/**
	 * @param args ...
	 */
	public static void main(final String[] args)
	{
		SumatraModel.changeLogLevel(Level.INFO);
		String path = "data/record/2025-05-05_11-02-50-ELIMINATION_PHASE-NORMAL_FIRST_HALF-KIKS-vs-TIGERs_Mannheim";
		String type = "botFeedback";
		try (PersistenceDbAnalyzer sa = new PersistenceDbAnalyzer(path))
		{
			sa.process(type);
		}
	}


	public void process(String type)
	{
		switch (type)
		{
			case "shapeMapSizeSum" -> {
				var processor = new ShapeMapSumSizeAnalyzer();
				processAllFrames(db, PersistenceShapeMapFrame.class, processor::process);
				processor.save(OUTPUT_FOLDER);
			}
			case "shapeMapSize" -> {
				var processor = new ShapeMapSizeAnalyzer();
				processAllFrames(db, PersistenceShapeMapFrame.class, processor::process, 100_000_000);
				processor.save(OUTPUT_FOLDER);
			}
			case "frameSizeByClass" -> {
				var processor = new FrameClassSizeAnalyzer(db.getTableTypes());
				db.forEachTable(table -> {
					log.info("Processing {}", table);
					table.forEach(processor::process);
				});
				processor.save(OUTPUT_FOLDER);
			}
			case "aiFrameSize" -> {
				var processor = new AiFrameSizeAnalyzer();
				processAllFrames(db, PersistenceAiFrame.class, processor::process);
				processor.save(OUTPUT_FOLDER);
			}
			case "botFeedback" -> {
				var processor = new BotFeedbackAnalyzer();
				processAllFrames(db, PersistenceAiFrame.class, processor::process);
				processor.save(OUTPUT_FOLDER);
			}
			case "all" -> {
				process("shapeMapSizeSum");
				process("shapeMapSize");
				process("frameSizeByClass");
				process("aiFrameSize");
			}

			default -> throw new IllegalArgumentException("Unknown type: " + type);
		}
	}


	@Override
	public void close()
	{
		db.close();
	}


	private <T extends PersistenceTable.IEntry<T>> void processAllFrames(PersistenceDb db, Class<T> clazz,
			Consumer<T> processor, long increment)
	{
		PersistenceTable<T> table = db.getTable(clazz);
		long first = db.getFirstKey();
		long last = db.getLastKey();
		for (long key = first; key < last; key += increment)
		{
			T frame = table.get(key);
			if (frame == null)
			{
				return;
			}
			processor.accept(frame);
		}
	}


	private <T extends PersistenceTable.IEntry<T>> void processAllFrames(PersistenceDb db, Class<T> clazz,
			Consumer<T> processor)
	{
		db.getTable(clazz).forEach(processor);
	}
}
