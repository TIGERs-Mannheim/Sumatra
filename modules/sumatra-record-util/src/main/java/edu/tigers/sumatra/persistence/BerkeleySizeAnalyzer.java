/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import com.google.common.collect.Sets;
import com.joey.utils.IObjectProfileNode;
import com.joey.utils.ObjectProfileFilters;
import com.joey.utils.ObjectProfileVisitors;
import com.joey.utils.ObjectProfiler;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.sumatra.ai.BerkeleyAiFrame;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.action.situation.OffensiveActionTreePath;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveAnalysedFrame;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ai.metis.statistics.MatchStats;
import edu.tigers.sumatra.drawable.IShapeLayer;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.loganalysis.ELogAnalysisShapesLayer;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.log.BerkeleyLogEvent;
import edu.tigers.sumatra.sim.SimKickEvent;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.trees.OffensiveActionTree;
import edu.tigers.sumatra.vision.data.EVisionFilterShapesLayer;
import edu.tigers.sumatra.wp.BerkeleyShapeMapFrame;
import edu.tigers.sumatra.wp.data.BerkeleyCamDetectionFrame;
import edu.tigers.sumatra.wp.vis.EWpShapesLayer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;

import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * Analyze the size of a replay
 *
 * @author nicolai.ommer
 */
@SuppressWarnings("squid:S106")
public class BerkeleySizeAnalyzer
{
	static
	{
		// init logging
		SumatraModel.changeLogLevel(Level.INFO);
	}
	
	private Writer sizesWriter;
	private final BerkeleyDb db;
	
	
	private BerkeleySizeAnalyzer(String dbPath)
	{
		db = BerkeleyDb.withCustomLocation(Paths.get(dbPath));
		db.add(BerkeleyAiFrame.class, new BerkeleyAccessor<>(BerkeleyAiFrame.class, true));
		db.add(BerkeleyCamDetectionFrame.class, new BerkeleyAccessor<>(BerkeleyCamDetectionFrame.class, true));
		db.add(BerkeleyShapeMapFrame.class, new BerkeleyAccessor<>(BerkeleyShapeMapFrame.class, true));
		db.add(BerkeleyLogEvent.class, new BerkeleyAccessor<>(BerkeleyLogEvent.class, false));
		db.open();
	}
	
	
	/**
	 * @param args ...
	 */
	public static void main(final String[] args)
	{
		if (args.length != 2)
		{
			System.exit(1);
		}
		
		BerkeleySizeAnalyzer sa = new BerkeleySizeAnalyzer(args[0]);
		
		String type = args[1];
		
		if ("frameSizeByClass".equalsIgnoreCase(type))
		{
			sa.frameSizeByClass();
		} else if ("aiFrameSize".equalsIgnoreCase(type))
		{
			sa.frameSizeAi();
		} else if ("shapeMapSize".equalsIgnoreCase(type))
		{
			sa.frameSizeShapeMap();
		} else if ("frameSizeAnalysis".equalsIgnoreCase(type))
		{
			sa.frameSizeAnalysis();
		}
		sa.close();
	}
	
	
	private void close()
	{
		db.close();
	}
	
	
	private int getSizeInBytes(Object object)
	{
		return ObjectProfiler.profile(object).size();
	}
	
	
	private Map<String, Integer> calcSizeInBytes(Map<String, Object> input)
	{
		return input.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() == null ? 0 : getSizeInBytes(e.getValue())));
	}
	
	
	private void frameSizeShapeMap()
	{
		sizesWriter = new Writer("/tmp/shapeMap");
		processAllFrames(db, BerkeleyShapeMapFrame.class, BerkeleySizeAnalyzer.this::processShapeMapFrame, 100_000_000);
		sizesWriter.exporter.close();
	}
	
	
	private void frameSizeAi()
	{
		sizesWriter = new Writer("/tmp/aiFrame");
		processAllFrames(db, BerkeleyAiFrame.class, BerkeleySizeAnalyzer.this::processRecordFrame);
		sizesWriter.exporter.close();
	}
	
	
	private void frameSizeByClass()
	{
		Set<Class<?>> frameClasses = Sets.newHashSet(
				BerkeleyAiFrame.class,
				BerkeleyCamDetectionFrame.class,
				BerkeleyShapeMapFrame.class,
				BerkeleyLogEvent.class,
				VisualizationFrame.class,
				MatchStats.class,
				OffensiveStrategy.class,
				OffensiveAnalysedFrame.class,
				OffensiveActionTreePath.class,
				OffensiveActionTree.class,
				SimKickEvent.class);
		for (Class<?> clazz : frameClasses)
		{
			Summer summer = new Summer();
			processAllFrames(db, clazz, f -> processObject(f, summer));
			System.out.printf("\n%30s: %10d %10d %10d\n", clazz.getName(), summer.sum, summer.count,
					summer.count > 0 ? summer.sum / summer.count : 0);
		}
	}
	
	
	private void frameSizeAnalysis()
	{
		Set<Class<?>> frameClasses = Sets.newHashSet(BerkeleyAiFrame.class, BerkeleyCamDetectionFrame.class,
				BerkeleyShapeMapFrame.class, BerkeleyLogEvent.class);
		for (Class<?> clazz : frameClasses)
		{
			Object o = db.get(clazz, db.getFirstKey());
			processObject(o);
		}
	}
	
	
	private void processRecordFrame(BerkeleyAiFrame berkeleyAiFrame)
	{
		VisualizationFrame visFrameY = berkeleyAiFrame.getVisFrame(ETeamColor.YELLOW);
		VisualizationFrame visFrameB = berkeleyAiFrame.getVisFrame(ETeamColor.BLUE);
		
		Map<String, Object> measures = new LinkedHashMap<>();
		measures.put("berkeleyAiFrame", berkeleyAiFrame);
		measures.put("visFrameY", visFrameY);
		measures.put("visFrameB", visFrameB);
		
		Map<String, Integer> sizes = calcSizeInBytes(measures);
		sizesWriter.write(sizes);
	}
	
	
	private void processShapeMapFrame(BerkeleyShapeMapFrame frame)
	{
		for (ShapeMap shapeMap : frame.getShapeMaps().values())
		{
			processShapeMap(shapeMap);
		}
	}
	
	
	private void processShapeMap(ShapeMap shapeMap)
	{
		Map<String, Integer> measures = new LinkedHashMap<>();
		for (IShapeLayer shapeLayer : getAllShapeLayers())
		{
			int size = shapeMap.get(shapeLayer).parallelStream()
					.mapToInt(this::getSizeInBytes).sum();
			measures.put(shapeLayer.getCategory() + ":" + shapeLayer.getLayerName(), size);
		}
		sizesWriter.write(measures);
	}
	
	
	private Set<IShapeLayer> getAllShapeLayers()
	{
		Set<IShapeLayer> all = new LinkedHashSet<>();
		all.addAll(Arrays.asList(ESkillShapesLayer.values()));
		all.addAll(Arrays.asList(EAiShapesLayer.values()));
		all.addAll(Arrays.asList(EWpShapesLayer.values()));
		all.addAll(Arrays.asList(EVisionFilterShapesLayer.values()));
		all.addAll(Arrays.asList(EAutoRefShapesLayer.values()));
		all.addAll(Arrays.asList(ELogAnalysisShapesLayer.values()));
		return all;
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
		Long first = db.getFirstKey();
		if (first == null)
		{
			return;
		}
		long last = db.getLastKey();
		long range = last - first;
		long key = first;
		int i = 0;
		int n = 50;
		System.out.println("Process all " + clazz.getName());
		System.out.println(StringUtils.repeat("#", n));
		while (true)
		{
			Long nextKey = db.getNextKey(key);
			if (nextKey == null || nextKey == key || nextKey >= last)
			{
				break;
			}
			T frame = db.get(clazz, key);
			processor.accept(frame);
			key = nextKey;
			
			if ((key - first) > i * range / n)
			{
				System.out.print("-");
				i++;
			}
			
		}
		System.out.println();
	}
	
	
	private void processObject(Object object, Summer summer)
	{
		if (object != null)
		{
			summer.sum += getSizeInBytes(object);
			summer.count++;
		}
	}
	
	
	private static void processObject(final Object obj)
	{
		IObjectProfileNode profile = ObjectProfiler.profile(obj);
		
		System.out.println("obj size = " + profile.size() + " bytes");
		System.out.println(profile.dump());
		System.out.println();
		
		// dump the same profile, but now only show nodes that are at least
		// 25% of 'obj' size:
		
		System.out.println("size fraction filter with threshold=0.25:");
		final PrintWriter out = new PrintWriter(System.out);
		
		profile.traverse(ObjectProfileFilters.newSizeFractionFilter(0.05),
				ObjectProfileVisitors.newDefaultNodePrinter(out, null, null, true));
	}
	
	private static class Writer
	{
		CSVExporter exporter;
		boolean first = true;
		
		
		Writer(String filename)
		{
			exporter = new CSVExporter(filename, true, false);
		}
		
		
		void write(Map<String, ? extends Number> dataSet)
		{
			if (first)
			{
				exporter.setHeader(dataSet.keySet().toArray(new String[dataSet.size()]));
				first = false;
			}
			exporter.addValues(dataSet.values());
		}
	}
	
	private static class Summer
	{
		long sum = 0;
		long count = 0;
	}
}
