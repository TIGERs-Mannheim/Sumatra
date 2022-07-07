/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.processors;

import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.persistence.util.ObjectSizeAnalyzer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Log4j2
public class FrameClassSizeAnalyzer implements ISizeAnalyzer<Object>
{
	private final ObjectSizeAnalyzer objectSizeAnalyzer = new ObjectSizeAnalyzer();
	private final Map<Class<?>, Summer> map = new LinkedHashMap<>();


	public FrameClassSizeAnalyzer(Collection<Class<?>> classes)
	{
		classes.stream().sorted(Comparator.comparing(Class::getSimpleName)).forEach(c -> map.put(c, new Summer()));
	}


	@Override
	public void process(Object frame)
	{
		Summer summer = map.get(frame.getClass());
		summer.sum += objectSizeAnalyzer.getTotalBytes(frame);
		summer.count++;
	}


	@Override
	public void save(Path outputFolder)
	{
		try (CSVExporter exporter = new CSVExporter(outputFolder, "frameClasses",
				CSVExporter.EMode.AUTO_INCREMENT_FILE_NAME))
		{
			exporter.setHeader(List.of("class", "count", "size", "sizePerFrame"));
			map.forEach((clazz, summer) -> exporter.addValues(List.of(
					clazz.getSimpleName(),
					summer.count,
					summer.sum,
					summer.sum / summer.count
			)));
		}
	}


	@Getter
	private static class Summer
	{
		private long sum = 0;
		private long count = 0;
	}
}
