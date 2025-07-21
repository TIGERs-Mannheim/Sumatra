/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.processors;

import edu.tigers.sumatra.ai.PersistenceAiFrame;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.persistence.util.ObjectSizeAnalyzer;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class AiFrameSizeAnalyzer implements IPersistenceDbAnalyzer<PersistenceAiFrame>
{
	private final ObjectSizeAnalyzer objectSizeAnalyzer = new ObjectSizeAnalyzer();
	private final Map<String, Long> sizes = new LinkedHashMap<>();


	@Override
	public void process(PersistenceAiFrame frame)
	{
		Map<String, Object> measures = new LinkedHashMap<>();
		measures.put("berkeleyAiFrame", frame);
		for (ETeamColor teamColor : ETeamColor.yellowBlueValues())
		{
			VisualizationFrame visFrame = frame.getVisFrame(teamColor);
			if (visFrame != null)
			{
				String qualifier = ":" + teamColor.name();
				measures.put("visFrame" + qualifier, visFrame);
				measures.put("aiInfos" + qualifier, visFrame.getAiInfos());
				measures.put("matchStats" + qualifier, visFrame.getMatchStats());
				measures.put("offensiveStrategy" + qualifier, visFrame.getOffensiveStrategy());
				measures.put("offensiveActions" + qualifier, visFrame.getOffensiveActions());
				measures.put("ballInterceptions" + qualifier, visFrame.getBallInterceptionInformationMap());
				measures.put("activeSupportBehaviors" + qualifier, visFrame.getActiveSupportBehaviors());
				measures.put("supportBehaviorAssignment" + qualifier, visFrame.getSupportBehaviorAssignment());
				measures.put("supportBehaviorViabilities" + qualifier, visFrame.getSupportBehaviorViabilities());
			}
		}

		measures.forEach((key, value) -> sizes.compute(key, (k, v) -> v == null
				? 0
				: v + objectSizeAnalyzer.getTotalBytes(value)));
	}


	@Override
	public void save(Path outputFolder)
	{
		try (CSVExporter exporter = new CSVExporter(outputFolder, "aiFrameSizes",
				CSVExporter.EMode.AUTO_INCREMENT_FILE_NAME))
		{
			exporter.setHeader(List.of("type", "size"));
			sizes.forEach((type, size) -> exporter.addValues(List.of(
					type,
					size
			)));
		}
	}
}
