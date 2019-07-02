/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.offensive.view;


import edu.tigers.sumatra.trees.OffensiveActionTree;
import edu.tigers.sumatra.trees.OffensiveActionTreeNode;
import edu.tigers.sumatra.ai.metis.offense.action.situation.OffensiveActionTreePath;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;


public class TreeDrawerPanel extends JPanel implements MouseWheelListener, MouseMotionListener, MouseListener
{
	
	private int cellHeight = 55;
	private int margin = 10;
	
	private double zoom = 1.0;
	
	private transient OffensiveActionTree tree;
	private int cellWidth = 150;
	
	private Point lastCursorPos;
	private Point origin = new Point(10, 10);
	
	private transient OffensiveActionTreePath currentPath = null;
	
	
	public TreeDrawerPanel()
	{
		setLayout(new MigLayout("fill", "", ""));
		setMinimumSize(new Dimension(400, 200));
		this.addMouseWheelListener(this);
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.setBorder(BorderFactory.createLoweredBevelBorder());
	}
	
	
	private void drawStringLimited(Graphics g, String s, int x, int y, int limit)
	{
		FontMetrics metrics = g.getFontMetrics();
		int line = metrics.getHeight();
		
		int curX = x;
		int curY = y;
		
		String[] elements = s.split("[_ ]");
		
		for (int i = 0; i < elements.length; i++)
		{
			String e = elements[i];
			e += " ";
			int width = metrics.stringWidth(e);
			
			
			if (curX + width >= x + limit && i != 0) // Never limit the first word
			{
				curY += line;
				curX = x;
			}
			
			g.drawString(e, curX, curY);
			curX += width;
		}
	}
	
	
	private boolean nodeIsActive(OffensiveActionTreeNode node)
	{
		if (node == null)
			return false;
		
		List<String> lookUpList = new ArrayList<>();
		
		OffensiveActionTreeNode ptr = node;
		while (ptr != null)
		{
			if (ptr.getParent() != null)
				lookUpList.add(0, ptr.getType());
			ptr = ptr.getParent();
		}
		
		List<EOffensiveActionMove> current = currentPath.getCurrentPath();
		
		if (current.size() < lookUpList.size() || current.isEmpty())
			return false;
		
		for (int x = 0; x < lookUpList.size(); x++)
		{
			if (!lookUpList.get(x).equals(currentPath.getCurrentPath().get(x).name()))
				return false;
		}
		
		return true;
	}
	
	
	private void paintTreeGroup(Graphics g, int x, int y, OffensiveActionTreeNode node)
	{
		Color old = g.getColor();
		double w = node.getWeight();
		String type = "HEAD";
		if (node.getType() != null)
			type = node.getType() + String.format("_(%.3f)", node.getWeight()) + " - " + node.getNumOfUpdates();
		if (w > 1)
		{
			g.setColor(new Color(0, (int) Math.min((w - 1) * 2000, 255), 0, 50));
		} else
		{
			g.setColor(new Color((int) Math.min((1 - w) * 2000, 255), 0, 0, 50));
		}
		g.fillRect(x, y, cellWidth, cellHeight);
		
		g.setColor(old);
		if (nodeIsActive(node))
			g.setColor(Color.CYAN);
		
		g.drawRect(x, y, cellWidth, cellHeight);
		g.setColor(old);
		
		drawStringLimited(g, type, x + margin, y + cellHeight / 3, cellWidth - 2 * margin);
		
		List<OffensiveActionTreeNode> nodes = new ArrayList<>(node.getChildren().values());
		int lastX = x;
		int lastWidth = 0;
		for (OffensiveActionTreeNode node1 : nodes)
		{
			int newX = lastX + lastWidth * (cellWidth + margin);
			int newY = y + 2 * cellHeight;
			
			int oldXCenter = x + cellWidth / 2;
			int oldYCenter = y + cellHeight;
			
			int newXCenter = newX + cellWidth / 2;
			int newYCenter = newY;
			
			double weight = node1.getWeight();
			g.setColor(old);
			
			lastX = newX;
			lastWidth = getTreeWidth(node1);
			
			if (weight > 1)
			{
				g.setColor(new Color(0, (int) Math.min((weight - 1) * 2000, 255), 0, 100));
			} else
			{
				g.setColor(new Color((int) Math.min((1 - weight) * 2000, 255), 0, 0, 100));
			}
			
			int offset = (int) ((double) cellWidth / 2 / nodes.size() * nodes.indexOf(node1));
			
			drawConnector(g, oldXCenter + offset, oldYCenter, newXCenter, newYCenter, nodeIsActive(node1));
			g.setColor(old);
			paintTreeGroup(g, newX, newY, node1);
		}
	}
	
	
	private void drawConnector(final Graphics g, final int oldXCenter, final int oldYCenter, final int newXCenter,
			final int newYCenter, boolean current)
	{
		int bonus = cellWidth / 32;
		int[] x = { oldXCenter - bonus / 2, oldXCenter + bonus / 2, newXCenter + bonus, newXCenter - bonus };
		int[] y = { oldYCenter, oldYCenter, newYCenter, newYCenter };
		g.fillPolygon(x, y, 4);
		if (current)
			g.setColor(Color.CYAN);
		else
			g.setColor(Color.BLACK);
		g.drawPolygon(x, y, 4);
		
		g.setColor(Color.BLACK);
	}
	
	
	private int getTreeWidth(OffensiveActionTreeNode node)
	{
		if (node.getChildren().values().isEmpty())
		{
			return 1;
		} else
		{
			int r = 0;
			for (OffensiveActionTreeNode n : node.getChildren().values())
			{
				r += getTreeWidth(n);
			}
			return r;
		}
	}
	
	
	private int getTreeHeight(OffensiveActionTreeNode node)
	{
		if (node.getChildren().values().isEmpty())
		{
			return 1;
		} else
		{
			int max = 1;
			for (OffensiveActionTreeNode n : node.getChildren().values())
			{
				int h = getTreeHeight(n);
				if (h > max)
					max = h;
			}
			
			return max + 1;
		}
	}
	
	
	public void setTree(OffensiveActionTree tree)
	{
		this.tree = tree;
		this.revalidate();
		this.repaint();
	}
	
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		Color old = g.getColor();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(old);
		
		this.revalidate(); // Tell parent layout manager (e.g. ScrollPane) that size has changed
		if (tree == null)
		{
			g.drawString("Tree Unavailable", margin + 10, margin + 10);
			origin.setLocation(10, 10);
			return; // nothing to paint
		}
		Graphics2D g2d = (Graphics2D) g.create();
		
		g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		g2d.setStroke(new BasicStroke(2));
		
		g2d.scale(zoom, zoom);
		g2d.translate(origin.x, origin.y);
		paintTreeGroup(g2d, 0, 0, tree.getHead());
	}
	
	
	public void setZoomLevel(double zoom)
	{
		this.zoom = zoom;
		this.revalidate();
		this.repaint();
	}
	
	
	public void setCurrentPath(final OffensiveActionTreePath currentPath)
	{
		this.currentPath = currentPath;
		this.revalidate();
		this.repaint();
	}
	
	
	public void setOrigin(Point origin)
	{
		this.origin.setLocation(origin.x + 10, origin.y + 10);
	}
	
	
	@Override
	public Dimension getPreferredSize()
	{
		if (tree == null)
			return new Dimension(200, 200);
		return new Dimension((int) ((cellWidth + margin) * zoom * getTreeWidth(tree.getHead())),
				(int) (2 * cellHeight * getTreeHeight(tree.getHead()) * zoom));
	}
	
	
	@Override
	public void mouseWheelMoved(final MouseWheelEvent mouseWheelEvent)
	{
		double diff = (double) -mouseWheelEvent.getWheelRotation() / 20;
		if (zoom + diff <= 0)
			return; // do not allow negative zoom
			
		zoom += diff;
		
		int oldX = (int) (mouseWheelEvent.getX() / (zoom - diff));
		int oldY = (int) (mouseWheelEvent.getY() / (zoom - diff));
		
		int newX = (int) (mouseWheelEvent.getX() / zoom);
		int newY = (int) (mouseWheelEvent.getY() / zoom);
		
		int dx = oldX - newX;
		int dy = oldY - newY;
		
		origin.translate(-dx, -dy);
		
		repaint();
	}
	
	
	@Override
	public void mouseDragged(final MouseEvent mouseEvent)
	{
		if (SwingUtilities.isRightMouseButton(mouseEvent))
		{
			int dx = mouseEvent.getX() - lastCursorPos.x;
			int dy = mouseEvent.getY() - lastCursorPos.y;
			
			origin.setLocation(origin.x + dx / zoom, origin.y + dy / zoom);
			
			lastCursorPos = mouseEvent.getPoint();
			repaint();
		}
	}
	
	
	@Override
	public void mouseMoved(final MouseEvent mouseEvent)
	{
		// No need to track all movements
	}
	
	
	@Override
	public void mouseClicked(final MouseEvent mouseEvent)
	{
		// No need to track clicks
	}
	
	
	@Override
	public void mousePressed(final MouseEvent mouseEvent)
	{
		lastCursorPos = mouseEvent.getPoint();
	}
	
	
	@Override
	public void mouseReleased(final MouseEvent mouseEvent)
	{
		// No need to track release Events
	}
	
	
	@Override
	public void mouseEntered(final MouseEvent mouseEvent)
	{
		// No need to track MouseEnter
	}
	
	
	@Override
	public void mouseExited(final MouseEvent mouseEvent)
	{
		// No need to track MouseExit
	}
}
