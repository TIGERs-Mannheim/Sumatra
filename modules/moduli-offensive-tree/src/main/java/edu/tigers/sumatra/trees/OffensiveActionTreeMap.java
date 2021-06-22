/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trees;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sleepycat.persist.model.Persistent;


/**
 * This class stores the offensive moves that have been executed in the past. The moves are weighted and can be analysed
 * for better decision makings later.
 */
@Persistent
public class OffensiveActionTreeMap
{
	private static final Logger log = LogManager.getLogger(OffensiveActionTreeMap.class);

	private Map<EOffensiveSituation, OffensiveActionTree> actionTrees = new EnumMap<>(EOffensiveSituation.class);


	public OffensiveActionTreeMap()
	{
		for (EOffensiveSituation situation : EOffensiveSituation.values())
		{
			actionTrees.put(situation, new OffensiveActionTree());
		}
	}


	public OffensiveActionTreeMap(final Map<EOffensiveSituation, OffensiveActionTree> actionTreesCopy)
	{
		this.actionTrees = actionTreesCopy;
	}


	public Map<EOffensiveSituation, OffensiveActionTree> getActionTrees()
	{
		return actionTrees;
	}


	public void setActionTrees(final Map<EOffensiveSituation, OffensiveActionTree> actionTrees)
	{
		this.actionTrees = actionTrees;
	}


	public static Optional<OffensiveActionTreeMap> loadTreeDataFromFile(String path)
	{
		ObjectMapper mapper = new ObjectMapper();
		try
		{
			OffensiveActionTreeMap node = mapper.readValue(new File(path), OffensiveActionTreeMap.class);
			// now rebuild the parent structure that got not persisted (because of infinite recursion)
			for (OffensiveActionTree tree : node.getActionTrees().values())
			{
				fillParentData(tree.getHead());
			}
			return Optional.of(node);
		} catch (IOException e)
		{
			log.warn("Could not load edu.tigers.sumatra.trees.OffensiveActionTree Data", e);
		}
		return Optional.empty();
	}


	@JsonIgnore
	public void saveTreeDataToFile(String path)
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try
		{
			mapper.writeValue(new File(path), this);
		} catch (IOException e)
		{
			log.warn("Could not write edu.tigers.sumatra.trees.OffensiveActionTree Data", e);
		}

	}


	private static void fillParentData(OffensiveActionTreeNode node)
	{
		for (OffensiveActionTreeNode child : node.getChildren().values())
		{
			child.setParent(node);
			fillParentData(child);
		}
	}
}
