/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * Config editor main panel.
 */
public class ConfigEditorPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = -7007103316635397718L;

	private final JTabbedPane tabpane;
	private final SortedMap<String, EditorView> tabs = new TreeMap<>();
	private final ConfigEditorSearchBar searchBar = new ConfigEditorSearchBar();

	private String nameSearchTokens = "";
	private String tagSearchTokens = "";
	private EditorView currentView = null;


	public ConfigEditorPanel()
	{
		setLayout(new BorderLayout());

		tabpane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		tabpane.addChangeListener(e -> {
			Component c = tabpane.getComponentAt(tabpane.getSelectedIndex());
			EditorView ev = (EditorView) c;
			ev.initialReload();
			currentView = ev;
			refreshConfigModel(ev.getReferenceConfig());
		});

		searchBar.getTextField().getDocument().addDocumentListener(new SearchBarListener());

		add(searchBar, BorderLayout.NORTH);
		add(tabpane, BorderLayout.CENTER);

	}


	/**
	 * @param client
	 * @param observer
	 */
	public void addConfigModel(final String client, final IConfigEditorViewObserver observer)
	{
		if (tabs.containsKey(client))
		{
			return;
		}
		final EditorView newView = new EditorView(client, client, new HierarchicalConfiguration()
		);
		newView.addObserver(observer);

		String configKey = newView.getConfigKey();
		tabs.put(configKey, newView);

		int index = 0;
		for (String key : tabs.keySet())
		{
			if (key.equals(configKey))
			{
				break;
			}
			index++;
		}
		tabpane.insertTab(client, null, newView, null, index);

		if (currentView == null)
		{
			currentView = newView;
		}

		revalidate();
		this.repaint();
	}


	/**
	 * @param config
	 */
	public void refreshConfigModel(HierarchicalConfiguration config)
	{
		currentView.setReferenceConfig(config);

		var model = new ConfigXMLTreeTableModel(config);

		if (!nameSearchTokens.isEmpty() || !tagSearchTokens.isEmpty())
		{
			currentView.updateModel(filterModel(config, model), true);
		} else
		{
			currentView.updateModel(model, false);
		}
	}


	private ConfigXMLTreeTableModel filterModel(HierarchicalConfiguration config, ConfigXMLTreeTableModel model)
	{
		var filteredConfig = new HierarchicalConfiguration();
		var filteredRoot = filterConfigNode(config.getRootNode(), model, false).map(FilterResult::node);
		filteredRoot.ifPresent(filteredConfig::setRootNode);
		var filteredModel = new ConfigXMLTreeTableModel(filteredConfig);
		filteredModel.addTreeModelListener(new TreeChangeForwarder(model, filteredModel));
		return filteredModel;
	}


	private Optional<FilterResult> filterConfigNode(ConfigurationNode originalNode, ConfigXMLTreeTableModel model,
			boolean someParentIsMatching)
	{
		// Score formula might need adjustments
		int tagScore = tagSearchTokens.isEmpty() ? 100 : matchingTagScore(originalNode, model);
		int nameScore = nameSearchTokens.isEmpty() ? 100 : matchingScore(originalNode, model);

		int score = (tagScore + nameScore) / 2;

		boolean matching = someParentIsMatching || score >= 85;
		var newChildren = originalNode.getChildren().stream()
				.map(node -> filterConfigNode(node, model, someParentIsMatching || matching))
				.flatMap(Optional::stream)
				.sorted(Comparator.comparingInt(FilterResult::matchingScore).reversed())
				.toList();

		if (newChildren.isEmpty() && !matching)
		{
			return Optional.empty();
		}

		var newNode = new HierarchicalConfiguration.Node(originalNode.getName(), originalNode.getValue());
		newNode.setReference(originalNode.getReference());
		newChildren.forEach(child -> newNode.addChild(child.node));

		var newAttributes = originalNode.getAttributes().stream()
				.map(node -> filterConfigNode(node, model, true))
				.flatMap(Optional::stream)
				.toList();
		newAttributes.forEach(attrib -> newNode.addAttribute(attrib.node));
		return Optional.of(new FilterResult(score, newNode));
	}


	private int matchingScore(ConfigurationNode node, ConfigXMLTreeTableModel model)
	{
		var stringsToCheck = Stream.concat(
				Stream.of(node.getName()),
				IntStream.range(0, model.getColumnCount())
						.mapToObj(i -> model.getValueAt(node, i))
						.filter(Objects::nonNull)
						.filter(String.class::isInstance)
						.map(String.class::cast)
		);

		var description = stringsToCheck
				.map(s -> s.split(" "))
				.flatMap(Arrays::stream)
				.map(String::strip)
				.filter(s -> !s.isEmpty())
				.map(String::toLowerCase)
				.collect(Collectors.joining(" "));

		return FuzzySearch.tokenSetPartialRatio(description, nameSearchTokens);
	}


	private int matchingTagScore(ConfigurationNode node, ConfigXMLTreeTableModel model)
	{
		String tags = Stream.of(node).map(n -> model.getValueAt(n, 3))
				.filter(Objects::nonNull)
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.map(String::toLowerCase)
				.collect(Collectors.joining());

		return FuzzySearch.tokenSetPartialRatio(tags, tagSearchTokens);
	}


	private record FilterResult(int matchingScore, ConfigurationNode node)
	{
	}


	private class SearchBarListener implements DocumentListener
	{

		@Override
		public void insertUpdate(DocumentEvent documentEvent)
		{
			applySearch();
		}


		@Override
		public void removeUpdate(DocumentEvent documentEvent)
		{
			applySearch();
		}


		@Override
		public void changedUpdate(DocumentEvent documentEvent)
		{
			applySearch();
		}


		private void applySearch()
		{
			nameSearchTokens = Arrays.stream(searchBar.getTextField().getText().split(" "))
					.map(String::toLowerCase)
					.map(String::strip)
					.filter(s -> !s.isEmpty())
					.filter(s -> !s.startsWith("@"))
					.collect(Collectors.joining(" "));

			tagSearchTokens = Arrays.stream(searchBar.getTextField().getText().split(" "))
					.map(String::toLowerCase)
					.map(String::strip)
					.filter(s -> s.startsWith("@"))
					.map(s -> s.substring(1))
					.filter(s -> !s.isEmpty())
					.collect(Collectors.joining(" "));

			refreshConfigModel(currentView.getReferenceConfig());
		}

	}

	@RequiredArgsConstructor
	private static class TreeChangeForwarder implements TreeModelListener
	{
		@NonNull
		private final ConfigXMLTreeTableModel originalModel;
		@NonNull
		private final ConfigXMLTreeTableModel filteredModel;


		@Override
		public void treeNodesChanged(TreeModelEvent treeModelEvent)
		{
			var filteredObj = (ConfigurationNode) treeModelEvent.getTreePath().getLastPathComponent();
			for (var child : filteredObj.getChildren())
			{
				var originalChild = convertPath(treeModelEvent.getTreePath(), child).getLastPathComponent();
				var value = filteredModel.getValueAt(child, 1);
				originalModel.setValueAt(value, originalChild, 1);
			}
		}


		private TreePath convertPath(TreePath oldTreePath, ConfigurationNode interestingChild)
		{
			var originalPath = new ArrayList<>(oldTreePath.getPathCount());

			var originalNode = (ConfigurationNode) originalModel.getRoot();
			@SuppressWarnings("squid:S6204") // Immutable on intention and sonar is too dumb to detect the list changes
			var oldPath = Arrays.stream(oldTreePath.getPath()).collect(Collectors.toList());
			// Handle root node manually
			oldPath.removeFirst();
			oldPath.addLast(interestingChild);
			originalPath.addFirst(originalNode);


			for (var filteredNodeObj : oldPath)
			{
				var filteredNode = (ConfigurationNode) filteredNodeObj;
				for (var child : originalNode.getChildren())
				{
					if (child.getName().equals(filteredNode.getName()))
					{
						originalPath.addLast(child);
						originalNode = child;
						break;
					}
				}
			}
			return new TreePath(originalPath.toArray());
		}


		@Override
		public void treeNodesInserted(TreeModelEvent treeModelEvent)
		{
			// Unused
		}


		@Override
		public void treeNodesRemoved(TreeModelEvent treeModelEvent)
		{
			// Unused
		}


		@Override
		public void treeStructureChanged(TreeModelEvent treeModelEvent)
		{
			// Unused
		}
	}
}
