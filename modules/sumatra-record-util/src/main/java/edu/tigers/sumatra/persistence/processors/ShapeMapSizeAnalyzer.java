/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.processors;

import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.persistence.util.ObjectSizeAnalyzer;
import edu.tigers.sumatra.wp.BerkeleyShapeMapFrame;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Log4j2
public class ShapeMapSizeAnalyzer implements ISizeAnalyzer<BerkeleyShapeMapFrame>
{
	private final ObjectSizeAnalyzer objectSizeAnalyzer = new ObjectSizeAnalyzer();
	private final Set<String> allKeys = new HashSet<>();
	private final List<Map<String, Long>> allMeasures = new ArrayList<>();
	private final CSVExporter exporter = new CSVExporter(
			System.getProperty("java.io.tmpdir"),
			"shapeMap",
			CSVExporter.EMode.AUTO_INCREMENT_FILE_NAME
	);


	private String key(IShapeLayerIdentifier shapeLayer)
	{
		return String.join(",", shapeLayer.getCategories()) + ":" + shapeLayer.getLayerName();
	}


	@Override
	public void process(BerkeleyShapeMapFrame frame)
	{
		for (ShapeMap shapeMap : frame.getShapeMaps().values())
		{
			Map<String, Long> measures = new LinkedHashMap<>();
			for (IShapeLayerIdentifier shapeLayer : shapeMap.getAllShapeLayersIdentifiers())
			{
				long size = shapeMap.get(shapeLayer).parallelStream()
						.mapToLong(objectSizeAnalyzer::getTotalBytes)
						.sum();
				String key = key(shapeLayer);
				measures.put(key, size);
				allKeys.add(key);
			}
			allMeasures.add(measures);
		}
	}


	@Override
	public void save(Path outputFolder)
	{
		List<String> sortedKeys = new ArrayList<>(allKeys);
		Collections.sort(sortedKeys);
		exporter.setHeader(sortedKeys);
		for (var measure : allMeasures)
		{
			Map<String, Long> measures = new LinkedHashMap<>();
			for (var key : sortedKeys)
			{
				measures.put(key, measure.getOrDefault(key, 0L));
			}
			exporter.addValues(measures.values());
		}

		exporter.close();
		try
		{
			Files.move(Paths.get(exporter.getAbsoluteFileName()), outputFolder.resolve(exporter.getFileName()));
		} catch (IOException e)
		{
			log.error("Failed to copy result file", e);
		}
	}
}
