/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.vladium.utils.IObjectProfileNode;
import com.vladium.utils.ObjectProfileFilters;
import com.vladium.utils.ObjectProfileVisitors;
import com.vladium.utils.ObjectProfiler;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.wp.vis.EWpShapesLayer;


/**
 * Analyze the size of a replay
 *
 * @author nicolai.ommer
 */
@SuppressWarnings("squid:S106")
public class BerkeleySizeAnalyzer
{
	
	private static final String	SIZE_FILE	= "/tmp/sizes";
	
	private Writer						sizesWriter;
	
	
	private BerkeleySizeAnalyzer() throws FileNotFoundException
	{
		sizesWriter = new Writer(SIZE_FILE);
	}
	
	private static class Writer
	{
		CSVExporter	exporter;
		boolean		first	= true;
		
		
		Writer(String filename) throws FileNotFoundException
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
	
	
	private int getSizeInBytes(Object object)
	{
		return ObjectProfiler.profile(object).size();
	}
	
	
	private Map<String, Integer> calcSizeInBytes(Map<String, Object> input)
	{
		return input.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() == null ? 0 : getSizeInBytes(e.getValue())));
	}
	
	
	private void processRecordFrame(RecordFrame recordFrame)
	{
		VisualizationFrame visFrameY = recordFrame.getVisFrame(ETeamColor.YELLOW);
		VisualizationFrame visFrameB = recordFrame.getVisFrame(ETeamColor.BLUE);
		
		Map<String, Object> measures = new LinkedHashMap<>();
		measures.put("recordFrame", recordFrame);
		measures.put("wfw", recordFrame.getWorldFrameWrapper());
		measures.put("visFrameY", visFrameY);
		measures.put("visFrameB", visFrameB);
		measures.put("shapeMapY", visFrameY == null ? null : visFrameY.getShapes());
		measures.put("shapeMapB", visFrameB == null ? null : visFrameB.getShapes());
		
		Map<String, Integer> sizes = calcSizeInBytes(measures);
		sizesWriter.write(sizes);
	}
	
	
	private void processAllRecordFrames(AiBerkeleyPersistence db)
	{
		long key = db.getFirstKey();
		long inc = 500_000_000L;
		RecordFrame recordFrame;
		while (true)
		{
			recordFrame = db.getRecordFrame(key);
			if (recordFrame == null)
			{
				break;
			}
			processRecordFrame(recordFrame);
			// ShapeMap shapeMap = recordFrame.getVisFrames().iterator().next().getShapes();
			// processShapeMap(shapeMap);
			
			Long nextKey = db.getNextKey(key + inc);
			if (nextKey == null || nextKey == key)
			{
				break;
			}
			key = nextKey;
			System.out.println(key);
		}
	}
	
	
	private void processShapeMap(ShapeMap shapeMap)
	{
		Map<String, Integer> measures = new LinkedHashMap<>();
		for (ShapeMap.IShapeLayer shapeLayer : getAllShapeLayers())
		{
			int size = shapeMap.get(shapeLayer).parallelStream()
					.mapToInt(this::getSizeInBytes).sum();
			measures.put(shapeLayer.getCategory() + ":" + shapeLayer.getLayerName(), size);
		}
		sizesWriter.write(measures);
	}
	
	
	private Set<ShapeMap.IShapeLayer> getAllShapeLayers()
	{
		Set<ShapeMap.IShapeLayer> all = new LinkedHashSet<>();
		all.addAll(Arrays.asList(ESkillShapesLayer.values()));
		all.addAll(Arrays.asList(EAiShapesLayer.values()));
		all.addAll(Arrays.asList(EWpShapesLayer.values()));
		return all;
	}
	
	
	private void process(String recordDbPath)
	{
		AiBerkeleyPersistence db = new AiBerkeleyPersistence(recordDbPath);
		db.open();
		
		processAllRecordFrames(db);
		
		// ShapeMap shapeMap = db.getRecordFrame(db.getFirstKey()).getVisFrames().iterator().next().getShapes();
		// processShapeMap(shapeMap);
		
		db.close();
		sizesWriter.exporter.close();
	}
	
	
	/**
	 * @param args ...
	 */
	public static void main(final String[] args) throws FileNotFoundException
	{
		String recordDbPath = "data/record/2017-02-22_22-16-53";
		new BerkeleySizeAnalyzer().process(recordDbPath);
	}
	
	
	private static void stuff()
	{
		String recordDbPath = "data/record/2017-02-22_22-16-53";
		AiBerkeleyPersistence db = new AiBerkeleyPersistence(recordDbPath);
		db.open();
		
		RecordFrame recordFrame = db.getRecordFrame(db.getFirstKey());
		IObjectProfileNode recordFrameProfile = ObjectProfiler.profile(recordFrame);
		RecordCamFrame camFrame = db.getCamFrame(db.getFirstKey());
		IObjectProfileNode camFrameProfile = ObjectProfiler.profile(camFrame);
		List<BerkeleyLogEvent> logEvents = db.loadLogEvents();
		IObjectProfileNode logEventsProfile = ObjectProfiler.profile(logEvents.get(0));
		
		ShapeMap shapeMap = recordFrame.getVisFrame(ETeamColor.BLUE).getShapes();
		List<IDrawableShape> allShapes = new ArrayList<>();
		for (ShapeMap.IShapeLayer shapeLayer : shapeMap.getAllShapeLayers())
		{
			allShapes.addAll(shapeMap.get(shapeLayer));
		}
		Map<Class<?>, Integer> maxSize = new HashMap<>();
		Map<Class<?>, Integer> count = new HashMap<>();
		for (IDrawableShape shape : allShapes)
		{
			int curValue = maxSize.getOrDefault(shape.getClass(), 0);
			int size = ObjectProfiler.profile(shape).size();
			int newValue = Math.max(curValue, size);
			maxSize.put(shape.getClass(), newValue);
			count.put(shape.getClass(), count.getOrDefault(shape.getClass(), 0) + 1);
		}
		
		processObject(allShapes);
		
		maxSize.forEach((k, v) -> System.out.println(k + ": " + v + " (" + count.get(k) + ")"));
		
		Map<String, Integer> sizes = new HashMap<>();
		sizes.put("shapesBlue", ObjectProfiler.profile(recordFrame.getVisFrame(ETeamColor.BLUE).getShapes()).size());
		sizes.put("shapesYellow", ObjectProfiler.profile(recordFrame.getVisFrame(ETeamColor.YELLOW).getShapes()).size());
		sizes.put("visFrameYellow", ObjectProfiler.profile(recordFrame.getVisFrame(ETeamColor.YELLOW)).size());
		sizes.put("visFrameBlue", ObjectProfiler.profile(recordFrame.getVisFrame(ETeamColor.BLUE)).size());
		sizes.put("wfw", ObjectProfiler.profile(recordFrame.getWorldFrameWrapper()).size());
		sizes.put("offAct",
				ObjectProfiler.profile(recordFrame.getVisFrame(ETeamColor.BLUE).getOffensiveActions()).size());
		sizes.put("offStrat",
				ObjectProfiler.profile(recordFrame.getVisFrame(ETeamColor.BLUE).getOffensiveStrategy()).size());
		
		for (Map.Entry<String, Integer> entry : sizes.entrySet())
		{
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
		
		System.out.println("Number of record frames: " + db.getNumberOfRecordFrames());
		System.out.println("Number of cam frames: " + db.getNumberOfCamFrames());
		System.out.println("Number of log events: " + db.getNumberOfLogEvents());
		
		String sizeUnit = " bytes";
		System.out.println("recordFrame size = " + recordFrameProfile.size() + sizeUnit);
		System.out.println("camFrame size = " + camFrameProfile.size() + sizeUnit);
		System.out.println("logEvent size = " + logEventsProfile.size() + sizeUnit);
		
		System.out.println(
				"recordFrame total size = " + (db.getNumberOfRecordFrames() * recordFrameProfile.size()) + sizeUnit);
		System.out.println("camFrame total size = " + (db.getNumberOfCamFrames() * camFrameProfile.size()) + sizeUnit);
		System.out.println("logEvent total size = " + (db.getNumberOfLogEvents() * logEventsProfile.size()) + sizeUnit);
		
		long duration = db.getLastKey() - db.getFirstKey();
		System.out.println("duration = " + (duration / 1e9));
		
		db.close();
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
}
