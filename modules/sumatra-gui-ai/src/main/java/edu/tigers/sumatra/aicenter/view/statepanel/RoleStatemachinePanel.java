/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.aicenter.view.statepanel;

import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class RoleStatemachinePanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener
{
	private static final int SCROLL_SPEED = 20;
	private static final double SCROLL_FACTOR = 250.0;
	private transient Map<IEvent, Map<IState, IState>> graph;
	private transient List<BotID> bots = new ArrayList<>();
	private transient BotID selectedBot = null;
	private transient AIInfoFrame lastAiFrame = null;

	private JComboBox<BotID> botBox = new JComboBox<>();

	private double circleRadius = 120;
	private double angleOffset = 0;
	private double fanciness = 0;
	private int xPos = 0;
	private int yPos = 0;
	private double scale = 1.0;
	private int startDragX = 0;
	private int startDragY = 0;
	private boolean mousePressed = false;
	private boolean showSkillGraph = false;
	private boolean addAllState = true;


	public RoleStatemachinePanel()
	{
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);

		setLayout(new BorderLayout());
		final CanvasPanel visArea = new CanvasPanel();
		add(visArea, BorderLayout.CENTER);
		visArea.setBorder(BorderFactory.createTitledBorder("Visualization Area"));

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.add(Box.createVerticalStrut(10));

		controlPanel.add(leftify(new JLabel("Selected Bot")));
		botBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		controlPanel.add(botBox);
		botBox.addItemListener(e ->
		{
			selectedBot = (BotID) e.getItem();
			repaint();
		});
		controlPanel.add(Box.createVerticalStrut(20));

		JPanel buttonWrapper = new JPanel();
		buttonWrapper.setLayout(new BoxLayout(buttonWrapper, BoxLayout.Y_AXIS));
		buttonWrapper.setBorder(BorderFactory.createTitledBorder("statemachine selection"));
		JRadioButton showRole = new JRadioButton("show role statemachine");
		showRole.setSelected(true);
		JPanel showRoleLeft = leftify(showRole);

		JRadioButton showSkill = new JRadioButton("show skill statemachine");
		showSkill.setSelected(false);
		JPanel showSkillLeft = leftify(showSkill);

		showRole.addActionListener(e -> {
			JRadioButton source = (JRadioButton) e.getSource();
			showSkill.setSelected(!source.isSelected());
			showSkillGraph = !source.isSelected();
			repaint();
		});

		showSkill.addActionListener(e -> {
			JRadioButton source = (JRadioButton) e.getSource();
			showRole.setSelected(!source.isSelected());
			showSkillGraph = source.isSelected();
			repaint();
		});

		buttonWrapper.add(showRoleLeft);
		buttonWrapper.add(showSkillLeft);
		controlPanel.add(buttonWrapper);

		controlPanel.add(Box.createGlue());

		JCheckBox allVertice = new JCheckBox("add \"all\" state");
		allVertice.setSelected(true);
		allVertice.addActionListener(e -> {
			JCheckBox source = (JCheckBox) e.getSource();
			addAllState = source.isSelected();
			repaint();
		});

		JPanel allVerticeLeft = leftify(allVertice);
		controlPanel.add(allVerticeLeft);
		controlPanel.add(Box.createVerticalStrut(20));

		JCheckBox eventNames = new JCheckBox("show event names");
		JPanel eventNamesLeft = leftify(eventNames);
		eventNames.setSelected(true);
		controlPanel.add(eventNamesLeft);
		controlPanel.add(Box.createVerticalStrut(20));

		controlPanel.add(leftify(new JLabel("circle radius")));
		JSlider slider = new JSlider(100, 850);
		slider.setValue((int) circleRadius);
		slider.addChangeListener(changeEvent -> {
			JSlider source = (JSlider) changeEvent.getSource();
			circleRadius = source.getValue();
			repaint();
		});
		controlPanel.add(slider);

		controlPanel.add(Box.createVerticalStrut(20));
		controlPanel.add(leftify(new JLabel("angle offset")));
		JSlider angleOffsetSlider = new JSlider(0, (int) (Math.PI * 100 * 2));
		angleOffsetSlider.setValue(0);
		angleOffsetSlider.addChangeListener(changeEvent -> {
			JSlider source = (JSlider) changeEvent.getSource();
			angleOffset = source.getValue();
			repaint();
		});
		controlPanel.add(angleOffsetSlider);

		controlPanel.add(Box.createVerticalStrut(20));
		controlPanel.add(leftify(new JLabel("fanciness")));
		JSlider fancinessSlider = new JSlider(0, 100);
		fancinessSlider.setValue(0);
		fancinessSlider.addChangeListener(changeEvent -> {
			JSlider source = (JSlider) changeEvent.getSource();
			fanciness = (source.getValue()) / 100.0;
			repaint();
		});
		controlPanel.add(fancinessSlider);

		controlPanel.setBorder(BorderFactory.createTitledBorder("Control Panel"));

		add(controlPanel, BorderLayout.EAST);
	}


	private JPanel leftify(final Component showRole)
	{
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
		wrapper.add(showRole);
		wrapper.add(Box.createHorizontalGlue());
		return wrapper;
	}


	class CanvasPanel extends JPanel
	{

		@Override
		public void paint(final Graphics g)
		{
			super.paint(g);

			if (lastAiFrame == null || selectedBot == null)
			{
				return;
			}

			if (showSkillGraph)
			{
				if (lastAiFrame.getTacticalField().getSkillStatemachineGraphBotMap().containsKey(selectedBot))
				{
					graph = lastAiFrame.getTacticalField().getSkillStatemachineGraphBotMap().get(selectedBot);
				}
			} else
			{
				if (lastAiFrame.getTacticalField().getRoleStatemachineGraphBotMap().containsKey(selectedBot))
				{
					graph = lastAiFrame.getTacticalField().getRoleStatemachineGraphBotMap().get(selectedBot);
				}
			}

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if (graph == null)
			{
				g2.drawString("No graph available", getWidth() / 2 - 30, getHeight() / 2);
				return;
			} else if (graph.isEmpty())
			{
				g2.drawString("Graph does not have transitions", getWidth() / 2 - 50, getHeight() / 2);
				return;
			}

			g.translate(getWidth() / 2, getHeight() / 2);
			g2.scale(1.0 / scale, 1.0 / scale);
			g.translate(-getWidth() / 2, -getHeight() / 2);

			g.translate(xPos, yPos);
			List<IState> listVertices = getListOfVertices();

			// create drawable Vertices
			IVector2 center = Vector2.fromXY(getWidth() / 2.0, getHeight() / 2.0);
			IVector2 vec = center.addNew(Vector2.fromX(circleRadius));
			double stepSize = (Math.PI * 2) / listVertices.size();
			double angle = -Math.PI + angleOffset / 100.0;
			Map<String, StateVertice> drawableVertices = new HashMap<>();
			createInitialDrawableVertices(g, listVertices, center, vec, stepSize, angle, drawableVertices);

			Map<String, List<StateEdge>> outgoingEdges = generateDrawableEdges(listVertices);

			drawableVertices.values().forEach(e -> e.draw(g2));
			drawableVertices.forEach((key, value) -> value.drawEdges(g2, outgoingEdges.get(key), drawableVertices));
		}


		private Map<String, List<StateEdge>> generateDrawableEdges(List<IState> listVertices)
		{
			Map<String, List<StateEdge>> outgoingEdges = new HashMap<>();
			for (var edges : graph.entrySet())
			{
				mapTransitionsToOutgoingEdges(listVertices, outgoingEdges, edges);
			}
			return outgoingEdges;
		}


		private void mapTransitionsToOutgoingEdges(List<IState> listVertices,
				Map<String, List<StateEdge>> outgoingEdges,
				Map.Entry<IEvent, Map<IState, IState>> edges)
		{
			for (var edge : edges.getValue().entrySet())
			{
				var originState = edge.getKey();
				var targetState = edge.getValue();

				String originStateString;
				if (originState == null)
				{
					if (addAllState)
					{
						originStateString = "All Transition";
					} else
					{
						addEdgeToMatchingVertices(listVertices, outgoingEdges, edges, targetState);
						continue;
					}
				} else
				{
					originStateString = originState.toString();
				}

				outgoingEdges.computeIfAbsent(originStateString, k -> new ArrayList<>());
				outgoingEdges.get(originStateString)
						.add(new StateEdge(originStateString, targetState.toString(),
								edges.getKey().toString()));
			}
		}


		private void addEdgeToMatchingVertices(List<IState> listVertices, Map<String, List<StateEdge>> outgoingEdges,
				Map.Entry<IEvent, Map<IState, IState>> edges, IState targetState)
		{
			for (var fromState : listVertices)
			{
				if (fromState != targetState)
				{
					StateEdge stateEdge = new StateEdge(
							fromState.toString(),
							targetState.toString(),
							edges.getKey().toString()
					);
					if (!outgoingEdges.containsKey(fromState.toString()))
					{
						outgoingEdges.put(fromState.toString(), new ArrayList<>());
					}
					outgoingEdges.get(fromState.toString()).add(stateEdge);
				}
			}
		}


		private void createInitialDrawableVertices(final Graphics g, final List<IState> listVertices,
				final IVector2 center, final IVector2 vec, final double stepSize, double angle,
				final Map<String, StateVertice> convertedVertices)
		{
			for (IState listVertex : listVertices)
			{
				String text = listVertex.toString();
				IVector2 pos = CircleMath.stepAlongCircle(center, vec, angle).getXYVector();
				IVector2 rcenter = center.addNew(Vector2.fromX(circleRadius));
				ILine topLine = Lines.lineFromDirection(rcenter.addNew(Vector2.fromY(circleRadius)), Vector2.fromX(1));
				ILine bottomLine = Lines.lineFromDirection(rcenter.addNew(Vector2.fromY(-circleRadius)), Vector2.fromX(1));

				double distance;
				if (center.subtractNew(pos).y() > 0)
				{
					// move towards bottom line
					distance = bottomLine.distanceTo(pos) * fanciness;
				} else
				{
					// move towards top line
					distance = -topLine.distanceTo(pos) * fanciness;
				}

				pos = pos.addNew(Vector2.fromY(-distance));
				if (center.subtractNew(pos).y() > 0)
				{
					// move towards bottom line
					distance = -distance;
				}
				pos = pos.addNew(Vector2.fromX(rcenter.subtractNew(pos).x() > 0 ? distance : -distance));

				int width = g.getFontMetrics().stringWidth(text);
				int height = g.getFontMetrics().getHeight();
				int rectX = (int) pos.x() - 3 - width / 2;
				int rectY = (int) pos.y() - height / 2;
				int rectW = width + 6;
				int rectH = height * 2;

				convertedVertices.put(text, new StateVertice(rectX, rectY, rectW, rectH, text));
				angle += stepSize;
			}
		}


		private List<IState> getListOfVertices()
		{
			Set<IState> vertices = new HashSet<>();
			// get unique vertices
			for (var entry : graph.entrySet())
			{
				for (var innerEntry : entry.getValue().entrySet())
				{
					if (innerEntry.getKey() != null)
					{
						vertices.add(innerEntry.getKey());
					}
					if (innerEntry.getValue() != null)
					{
						vertices.add(innerEntry.getValue());
					}
				}
			}

			if (addAllState)
			{
				vertices.add(new IState()
				{
					@Override
					public String getName()
					{
						return "All states";
					}
				});
			}

			return new ArrayList<>(vertices);
		}
	}


	public void onUpdate(final AIInfoFrame lastFrame)
	{
		lastAiFrame = lastFrame;
		fillComboBox(new ArrayList<>(lastFrame.getTacticalField().getRoleStatemachineGraphBotMap().keySet()));
		repaint();
	}


	@Override
	public void mouseDragged(final MouseEvent mouseEvent)
	{
		if (mousePressed)
		{
			xPos -= (startDragX - mouseEvent.getX()) * scale;
			yPos -= (startDragY - mouseEvent.getY()) * scale;
			startDragX = mouseEvent.getX();
			startDragY = mouseEvent.getY();
			repaint();
		}
	}


	private void fillComboBox(List<BotID> bots)
	{
		if (bots.stream().anyMatch(e -> !this.bots.contains(e)))
		{
			botBox.removeAllItems();
			bots.forEach(e -> botBox.addItem(e));
			this.bots = new ArrayList<>(bots);
			if (!bots.isEmpty())
			{
				repaint();
			}
			if (selectedBot != null)
			{
				botBox.setSelectedItem(selectedBot);
			}
		}
	}


	@Override
	public void mouseMoved(final MouseEvent mouseEvent)
	{
		// not needed
	}


	@Override
	public void mouseClicked(final MouseEvent mouseEvent)
	{
		// not needed
	}


	@Override
	public void mousePressed(final MouseEvent mouseEvent)
	{
		startDragX = mouseEvent.getX();
		startDragY = mouseEvent.getY();
		mousePressed = true;
	}


	@Override
	public void mouseReleased(final MouseEvent mouseEvent)
	{
		mousePressed = false;
	}


	@Override
	public void mouseEntered(final MouseEvent mouseEvent)
	{
		// not needed
	}


	@Override
	public void mouseExited(final MouseEvent mouseEvent)
	{
		// not needed
	}


	@Override
	public void mouseWheelMoved(final MouseWheelEvent e)
	{
		final int rot = e.getWheelRotation();
		final int scroll = SCROLL_SPEED * rot;
		scale = scale * (1 + (scroll / SCROLL_FACTOR));
		repaint();
	}
}
