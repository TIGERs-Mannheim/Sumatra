/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.view.options;

import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


@Log4j2
public class ShapeSelectionModel extends DefaultTreeModel
{
	private final DefaultMutableTreeNode sourcesNode = new ShapeSelectionNode("Sources");
	private final DefaultMutableTreeNode layersNode = new ShapeSelectionNode("Layers");

	@Getter
	private final Map<ShapeMapSource, DefaultMutableTreeNode> sources = new HashMap<>();
	private final Map<ShapeMapSource, DefaultMutableTreeNode> sourceCategories = new HashMap<>();
	@Getter
	private final Map<IShapeLayerIdentifier, DefaultMutableTreeNode> layers = new HashMap<>();
	@Getter
	private final Map<ShapeCategoryId, DefaultMutableTreeNode> layerCategories = new HashMap<>();


	public ShapeSelectionModel()
	{
		super(null);
		var root = new DefaultMutableTreeNode("Shapes");
		root.add(sourcesNode);
		root.add(layersNode);
		setRoot(root);
	}


	public Optional<DefaultMutableTreeNode> addShapeMapSource(ShapeMapSource shapeMapSource)
	{
		DefaultMutableTreeNode node = sourcesNode;
		for (ShapeMapSource source : shapeMapSource.getPath())
		{
			DefaultMutableTreeNode parentNode = node;
			node = sourceCategories.computeIfAbsent(
					source,
					name -> addChild(parentNode, source)
			);
		}
		if (sources.containsKey(shapeMapSource))
		{
			return Optional.empty();
		}
		DefaultMutableTreeNode parentNode = node;
		return Optional.of(sources.computeIfAbsent(
				shapeMapSource,
				name -> addLeaf(parentNode, shapeMapSource)
		));
	}


	public Optional<DefaultMutableTreeNode> addShapeLayer(IShapeLayerIdentifier shapeLayer)
	{
		DefaultMutableTreeNode node = layersNode;
		StringBuilder id = new StringBuilder();
		for (String category : shapeLayer.getCategories())
		{
			DefaultMutableTreeNode parentNode = node;
			id.append(category);
			node = layerCategories.computeIfAbsent(
					new ShapeCategoryId(id.toString()),
					name -> addChild(parentNode, new ShapeCategoryId(category))
			);
		}
		if (layers.containsKey(shapeLayer))
		{
			return Optional.empty();
		}
		DefaultMutableTreeNode parentNode = node;
		return Optional.of(layers.computeIfAbsent(
				shapeLayer,
				name -> addLeaf(parentNode, shapeLayer)
		));
	}


	private Stream<DefaultMutableTreeNode> getAllNodes()
	{
		return iterateNode((DefaultMutableTreeNode) root).stream();
	}


	public Stream<TreePath> getAllNonLeafPaths()
	{
		return getAllNonLeafNodes()
				.map(DefaultMutableTreeNode::getPath)
				.map(TreePath::new);
	}


	public Stream<DefaultMutableTreeNode> getAllNonLeafNodes()
	{
		return getAllNodes()
				.filter(node -> !node.isLeaf())
				.sorted(Comparator.comparingInt(node -> -node.getPath().length));
	}


	public Stream<DefaultMutableTreeNode> getAllLayerNodes()
	{
		return getAllNodes()
				.filter(this::isLayer);
	}


	public boolean isSource(DefaultMutableTreeNode node)
	{
		return sourcesNode.isNodeDescendant(node);
	}


	public boolean isLayer(DefaultMutableTreeNode node)
	{
		return layersNode.isNodeDescendant(node);
	}


	public Stream<DefaultMutableTreeNode> getAllLayerNonLeafNodes()
	{
		return getAllNodes()
				.filter(node -> !node.isLeaf())
				.filter(this::isLayer);
	}


	private List<DefaultMutableTreeNode> iterateNode(DefaultMutableTreeNode node)
	{
		var children = node.children();
		List<DefaultMutableTreeNode> paths = new ArrayList<>();
		paths.add(node);
		while (children.hasMoreElements())
		{
			paths.addAll(iterateNode((DefaultMutableTreeNode) children.nextElement()));
		}
		return paths;
	}


	private DefaultMutableTreeNode addLeaf(
			DefaultMutableTreeNode parent,
			Object userObject
	)
	{
		DefaultMutableTreeNode node = new ShapeSelectionNode(userObject, false);
		return addNode(node, parent);
	}


	private DefaultMutableTreeNode addChild(
			DefaultMutableTreeNode parent,
			Object userObject
	)
	{
		DefaultMutableTreeNode node = new ShapeSelectionNode(userObject, true);
		return addNode(node, parent);
	}


	private DefaultMutableTreeNode addNode(DefaultMutableTreeNode node, DefaultMutableTreeNode parent)
	{
		try
		{
			SwingUtilities.invokeAndWait(() -> {
				for (int i = 0; i < parent.getChildCount(); i++)
				{
					if (parent.getChildAt(i).toString().compareTo(node.toString()) >= 0)
					{
						insertNodeInto(node, parent, i);
						return;
					}
				}
				insertNodeInto(node, parent, parent.getChildCount());
			});
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		} catch (InvocationTargetException e)
		{
			log.warn("Failed to add node to model", e);
		}
		return node;
	}
}
