/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.testplays.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.guinotifications.GuiNotificationsController;
import edu.tigers.sumatra.guinotifications.visualizer.IVisualizerObserver;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.testplays.commands.PathCommand;
import edu.tigers.sumatra.testplays.util.Point;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class PathCommandPanel extends ACommandDetailsPanel
{
	
	private static final Logger	log	= Logger
			.getLogger(PathCommandPanel.class.getName());
	
	private JTextField				xInput;
	private JTextField				yInput;
	private JList<Point>				lstPoints;
	
	private JButton					recordButton;
	
	private PointListModel			pointListModel;
	
	private PathCommand				command;
	
	
	/**
	 * Creates a new PathCommandPanel
	 * 
	 * @param command
	 */
	public PathCommandPanel(PathCommand command)
	{
		
		super(command.getCommandType());
		
		this.command = command;
		
		final JLabel lblNumOfPoints = new JLabel(String.valueOf(command.getNumberOfPoints()));
		addLine(new JLabel("Number of points:"), lblNumOfPoints);
		
		JPanel pointListPanel = new JPanel(new BorderLayout());
		lstPoints = new JList<>();
		
		pointListModel = new PointListModel();
		// noinspection unchecked
		lstPoints.setModel(pointListModel);
		lstPoints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		lstPoints.setLayoutOrientation(JList.VERTICAL);
		
		JScrollPane listScroll = new JScrollPane(lstPoints);
		pointListPanel.add(listScroll, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		
		JPanel buttonPanelFirstRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel buttonPanelSecondRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton delButton = new JButton("-");
		delButton.addActionListener(new PointRemoveListener());
		buttonPanelFirstRow.add(delButton);
		
		JButton addButton = new JButton("+");
		addButton.addActionListener(new PointAddListener());
		buttonPanelFirstRow.add(addButton);
		
		xInput = new JTextField("x", 4);
		buttonPanelFirstRow.add(xInput);
		
		yInput = new JTextField("y", 4);
		buttonPanelFirstRow.add(yInput);
		
		
		recordButton = new JButton("Record");
		recordButton.addActionListener(new RecordingHandler());
		buttonPanelSecondRow.add(recordButton);
		
		buttonPanel.add(buttonPanelFirstRow);
		buttonPanel.add(buttonPanelSecondRow);
		
		pointListPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		GridBagConstraints gbc = getGridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.5;
		
		JLabel lblPoints = new JLabel("Points:");
		lblPoints.setVerticalAlignment(JLabel.TOP);
		addLine(lblPoints, pointListPanel, gbc);
	}
	
	
	private void addPoint(Point point)
	{
		
		command.getPoints().add(point);
		pointListModel.reload();
	}
	
	
	@Override
	void onSave()
	{
		// Nothing to do...
	}
	
	class PointListModel extends AbstractListModel
	{
		
		/**
		 * Reloads the list's contents
		 */
		public void reload()
		{
			
			super.fireContentsChanged(this, 0, getSize());
		}
		
		
		@Override
		public int getSize()
		{
			
			return command.getPoints().size();
		}
		
		
		@Override
		public Object getElementAt(final int index)
		{
			return command.getPoints().get(index);
		}
		
	}
	
	class PointAddListener implements ActionListener
	{
		
		// Simplified if statement would not be readable anymore
		@SuppressWarnings("SimplifiableIfStatement")
		private boolean verifyInput()
		{
			
			if (!(StringUtils.isNumeric(xInput.getText().substring(1))
					&& (StringUtils.isNumeric(xInput.getText().substring(0, 1))
							|| xInput.getText().startsWith("-"))))
			{
				return false;
			}
			
			return StringUtils.isNumeric(yInput.getText().substring(1))
					&& (StringUtils.isNumeric(yInput.getText().substring(0, 1))
							|| yInput.getText().startsWith("-"));
			
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			
			if (!verifyInput())
			{
				return;
			}
			
			addPoint(new Point(Integer.parseInt(xInput.getText()), Integer.parseInt(yInput.getText())));
		}
	}
	
	class PointRemoveListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			
			if (lstPoints.getSelectedIndex() == -1)
			{
				return;
			}
			
			command.getPoints().remove(lstPoints.getSelectedValue());
			pointListModel.reload();
		}
	}
	
	class RecordingHandler implements ActionListener, IVisualizerObserver
	{
		
		private boolean recording = false;
		
		
		@Override
		public void actionPerformed(final ActionEvent actionEvent)
		{
			
			GuiNotificationsController notificationsController;
			try
			{
				notificationsController = SumatraModel.getInstance()
						.getModule(GuiNotificationsController.class);
			} catch (ModuleNotFoundException e)
			{
				log.error(e);
				return;
			}
			
			recording = !recording;
			
			if (recording)
			{
				notificationsController.addVisualizerObserver(this);
				recordButton.setText("Stop");
			} else
			{
				notificationsController.removeVisualizerObserver(this);
				recordButton.setText("Record");
			}
			
		}
		
		
		@Override
		public void onMoveClick(final BotID botID, final IVector2 pos)
		{
			if (!recording)
			{
				return;
			}
			
			addPoint(new Point(pos.x(), pos.y()));
		}
		
		
		@Override
		public void onRobotClick(final BotID botID)
		{
			// Nothing to do here...
		}
		
		
		@Override
		public void onHideFromRcm(final BotID botID, final boolean hide)
		{
			// Nothing to do here...
		}
	}
	
}
