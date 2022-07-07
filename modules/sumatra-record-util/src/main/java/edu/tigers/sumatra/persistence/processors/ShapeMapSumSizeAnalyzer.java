/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.processors;

import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.persistence.util.ObjectSizeAnalyzer;
import edu.tigers.sumatra.wp.BerkeleyShapeMapFrame;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ShapeMapSumSizeAnalyzer implements ISizeAnalyzer<BerkeleyShapeMapFrame>
{
	private final ObjectSizeAnalyzer objectSizeAnalyzer = new ObjectSizeAnalyzer();
	private final Map<String, Long> sizes = new LinkedHashMap<>();


	private String key(IShapeLayerIdentifier shapeLayer)
	{
		return String.join(",", shapeLayer.getCategories()) + ":" + shapeLayer.getLayerName();
	}


	@Override
	public void process(BerkeleyShapeMapFrame frame)
	{
		frame.getShapeMaps().values().stream()
				.map(ShapeMap::getAllShapeLayers)
				.flatMap(Collection::stream)
				.forEach(l -> {
					long sum = calcSize(frame, l.getIdentifier());
					sizes.compute(key(l.getIdentifier()), (key, value) -> (value == null ? 0L : value) + sum);
				});
	}


	private long calcSize(BerkeleyShapeMapFrame frame, IShapeLayerIdentifier shapeLayer)
	{
		return frame.getShapeMaps().values().stream()
				.map(shapeMap -> shapeMap.get(shapeLayer))
				.flatMap(Collection::parallelStream)
				.mapToLong(objectSizeAnalyzer::getTotalBytes)
				.sum();
	}


	@Override
	public void save(Path outputFolder)
	{
		try (CSVExporter exporter = new CSVExporter(outputFolder, "shapeMapSum",
				CSVExporter.EMode.AUTO_INCREMENT_FILE_NAME))
		{
			exporter.setHeader(List.of("shapeLayer", "size"));
			sizes.forEach((type, size) -> exporter.addValues(List.of(
					type,
					size
			)));
		}
	}
}
