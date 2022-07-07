/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import com.sleepycat.persist.evolve.Deleter;
import com.sleepycat.persist.evolve.Mutations;
import edu.tigers.sumatra.ai.BerkeleyAiFrame;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.log.BerkeleyLogEvent;
import edu.tigers.sumatra.persistence.processors.AiFrameSizeAnalyzer;
import edu.tigers.sumatra.persistence.processors.FrameClassSizeAnalyzer;
import edu.tigers.sumatra.persistence.processors.ShapeMapSizeAnalyzer;
import edu.tigers.sumatra.persistence.processors.ShapeMapSumSizeAnalyzer;
import edu.tigers.sumatra.wp.BerkeleyShapeMapFrame;
import edu.tigers.sumatra.wp.data.BerkeleyCamDetectionFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Consumer;


/**
 * Analyze the size of a recording.
 */
@Log4j2
public class BerkeleySizeAnalyzer implements AutoCloseable
{
	private static final Path OUTPUT_FOLDER = Paths.get("");

	private final BerkeleyDb db;


	private BerkeleySizeAnalyzer(String dbPath)
	{
		db = BerkeleyDb.withCustomLocation(Paths.get(dbPath));
		db.add(BerkeleyAiFrame.class, new BerkeleyAccessor<>(BerkeleyAiFrame.class, true));
		db.add(BerkeleyCamDetectionFrame.class, new BerkeleyAccessor<>(BerkeleyCamDetectionFrame.class, true));
		db.add(BerkeleyShapeMapFrame.class, new BerkeleyAccessor<>(BerkeleyShapeMapFrame.class, true));
		db.add(WorldFrameWrapper.class, new BerkeleyAccessor<>(WorldFrameWrapper.class, true));
		db.add(BerkeleyLogEvent.class, new BerkeleyAccessor<>(BerkeleyLogEvent.class, false));

		Mutations mutations = new Mutations();
		mutations.addDeleter(new Deleter(
				"edu.tigers.sumatra.ai.metis.offense.ballinterception.BallInterceptionInformation",
				2,
				"interceptionTargetTimeFallback"
		));
		db.getEnv().getStoreConfig().setMutations(mutations);

		db.open();
	}


	/**
	 * @param args ...
	 */
	public static void main(final String[] args)
	{
		SumatraModel.changeLogLevel(Level.INFO);
		if (args.length != 2)
		{
			log.error("Expected two arguments, got: {}", Arrays.toString(args));
			System.exit(1);
		}

		String path = args[0];
		String type = args[1];
		try (BerkeleySizeAnalyzer sa = new BerkeleySizeAnalyzer(path))
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
				processAllFrames(db, BerkeleyShapeMapFrame.class, processor::process);
				processor.save(OUTPUT_FOLDER);
			}
			case "shapeMapSize" -> {
				var processor = new ShapeMapSizeAnalyzer();
				processAllFrames(db, BerkeleyShapeMapFrame.class, processor::process, 100_000_000);
				processor.save(OUTPUT_FOLDER);
			}
			case "frameSizeByClass" -> {
				var processor = new FrameClassSizeAnalyzer(db.getAccessorTypes());
				db.getAccessorTypes().forEach(clazz -> {
					log.info("Processing {}", clazz);
					processAllFrames(db, clazz, processor::process);
				});
				processor.save(OUTPUT_FOLDER);
			}
			case "aiFrameSize" -> {
				var processor = new AiFrameSizeAnalyzer();
				processAllFrames(db, BerkeleyAiFrame.class, processor::process);
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


	private <T> void processAllFrames(BerkeleyDb db, Class<T> clazz, Consumer<T> processor, long increment)
	{
		long first = db.getFirstKey();
		long last = db.getLastKey();
		for (long key = first; key < last; key += increment)
		{
			T frame = db.get(clazz, key);
			if (frame == null)
			{
				return;
			}
			processor.accept(frame);
		}
	}


	private <T> void processAllFrames(BerkeleyDb db, Class<T> clazz, Consumer<T> processor)
	{
		db.forEach(clazz, processor);
	}
}
