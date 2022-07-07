/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botcenter.view.bots;

import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyHighestValues;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.Range;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Displays TigerSystemMatchFeedback content.
 *
 * @author AndreR
 */
public class SystemMatchFeedbackPanel extends JPanel
{
	private static final long serialVersionUID = 2431222411313088184L;

	private final JProgressBar kickerLevel = new JProgressBar(0, 1000);
	private final JTextField dribblerSpeed = new JTextField();
	private final JTextField dribblerTemp = new JTextField();
	private final JProgressBar batteryLevel = new JProgressBar(0, 1000);
	private final JTextField robotMode = new JTextField();
	private final JTextField kickCounter = new JTextField();
	private final JTextField hardwareId = new JTextField();
	private final JCheckBox barrierInterrupted = new JCheckBox("Interrupted");
	private final Map<EFeature, JCheckBox> features = new EnumMap<>(EFeature.class);
	private final JTextField[] pos = new JTextField[3];
	private final JTextField[] vel = new JTextField[3];
	private final JTextField[] ball = new JTextField[3];

	private static final int DATA_SIZE = 400;

	private final ITrace2D posXTrace = new Trace2DLtd(DATA_SIZE);
	private final ITrace2D posYTrace = new Trace2DLtd(DATA_SIZE);
	private final ITrace2D posWTrace = new Trace2DLtd(DATA_SIZE);
	private final ITrace2D velXTrace = new Trace2DLtd(DATA_SIZE);
	private final ITrace2D velYTrace = new Trace2DLtd(DATA_SIZE);
	private final ITrace2D velWTrace = new Trace2DLtd(DATA_SIZE);

	private long timeOffset;

	private final JToggleButton btnCapture = new JToggleButton("Capture");


	private final List<ISystemMatchFeedbackPanelObserver> observers = new CopyOnWriteArrayList<>();


	@SuppressWarnings("squid:S1192") // String constants
	public SystemMatchFeedbackPanel()
	{
		setLayout(new MigLayout("wrap 2"));

		robotMode.setBackground(ERobotMode.IDLE.getColor());
		kickerLevel.setStringPainted(true);
		batteryLevel.setStringPainted(true);
		btnCapture.addActionListener(new CaptureActionListener());

		final Chart2D posChart = new Chart2D();
		final List<Chart2D> chartList = new ArrayList<>(6);
		chartList.add(posChart);
		final Chart2D velChart = new Chart2D();
		chartList.add(velChart);

		posChart.setName("pos");
		velChart.setName("vel");

		// Chart setup
		posXTrace.setColor(Color.RED);
		posXTrace.setName("pX");
		posYTrace.setColor(Color.GREEN);
		posYTrace.setName("pY");
		posWTrace.setColor(Color.BLUE);
		posWTrace.setName("pW");
		velXTrace.setColor(Color.RED);
		velXTrace.setName("vX");
		velYTrace.setColor(Color.GREEN);
		velYTrace.setName("vY");
		velWTrace.setColor(Color.BLUE);
		velWTrace.setName("vW");

		for (Chart2D chart : chartList)
		{
			chart.getAxisY().setRangePolicy(new RangePolicyMinimumViewport(new Range(-1, 1)));
			chart.getAxisX().setRangePolicy(new RangePolicyHighestValues(20));
			chart.getAxisX().setMajorTickSpacing(10);
			chart.getAxisX().setMinorTickSpacing(10);
			chart.getAxisX().setAxisTitle(new AxisTitle("t [s]"));
			chart.setBackground(getBackground());
		}

		posChart.addTrace(posXTrace);
		posChart.addTrace(posYTrace);
		posChart.addTrace(posWTrace);

		velChart.addTrace(velXTrace);
		velChart.addTrace(velYTrace);
		velChart.addTrace(velWTrace);

		for (int i = 0; i < 3; i++)
		{
			pos[i] = new JTextField();
			vel[i] = new JTextField();
			ball[i] = new JTextField();
		}

		final JPanel statePanel = new JPanel(new MigLayout("fill, wrap 3", "[15]10[70,fill]10[80,fill]"));
		statePanel.add(new JLabel("Position"), "skip");
		statePanel.add(new JLabel("Velocity"));
		statePanel.add(new JLabel("X:"));
		statePanel.add(pos[0]);
		statePanel.add(vel[0]);
		statePanel.add(new JLabel("Y:"));
		statePanel.add(pos[1]);
		statePanel.add(vel[1]);
		statePanel.add(new JLabel("W:"));
		statePanel.add(pos[2]);
		statePanel.add(vel[2]);
		statePanel.setBorder(BorderFactory.createTitledBorder("Estimated State"));

		final JPanel ballPanel = new JPanel(new MigLayout("fill, wrap 2", "[20]10[80,fill]"));
		ballPanel.add(new JLabel("X:"));
		ballPanel.add(ball[0]);
		ballPanel.add(new JLabel("Y:"));
		ballPanel.add(ball[1]);
		ballPanel.add(new JLabel("t:"));
		ballPanel.add(ball[2]);
		ballPanel.setBorder(BorderFactory.createTitledBorder("Ball Position"));

		final JPanel systemPanel = new JPanel(new MigLayout("fill, wrap 2", "[50]10[100,fill]"));
		systemPanel.add(new JLabel("Mode:"));
		systemPanel.add(robotMode);
		systemPanel.add(new JLabel("Battery:"));
		systemPanel.add(batteryLevel);
		systemPanel.add(new JLabel("Kicker:"));
		systemPanel.add(kickerLevel);
		systemPanel.add(new JLabel("Dribbler:"));
		systemPanel.add(dribblerSpeed);
		systemPanel.add(new JLabel("Dribbler Temp:"));
		systemPanel.add(dribblerTemp);
		systemPanel.add(new JLabel("Barrier:"));
		systemPanel.add(barrierInterrupted);
		systemPanel.add(new JLabel("Kick Counter:"));
		systemPanel.add(kickCounter);
		systemPanel.add(new JLabel("Hardware ID:"));
		systemPanel.add(hardwareId);
		systemPanel.setBorder(BorderFactory.createTitledBorder("System"));

		final JPanel featurePanel = new JPanel(new MigLayout("fill, wrap 2", "[75]10[75]", "[20]0"));
		for (EFeature f : EFeature.values())
		{
			JCheckBox box = new JCheckBox(f.getName());
			featurePanel.add(box);
			features.put(f, box);
		}
		featurePanel.setBorder(BorderFactory.createTitledBorder("Features"));

		final JPanel chkBoxPanel = new JPanel(new MigLayout("fill, wrap 2", "[50]10[100,fill]"));
		for (Chart2D chart : chartList)
		{
			chkBoxPanel.add(createChartCheckBox(chart));
		}

		final JPanel infoPanel = new JPanel(new MigLayout("fill, wrap 1"));

		infoPanel.add(systemPanel);
		infoPanel.add(featurePanel);
		infoPanel.add(statePanel);
		infoPanel.add(ballPanel);
		infoPanel.add(chkBoxPanel);
		infoPanel.add(btnCapture);

		add(infoPanel, "spany 8, aligny top");
		add(posChart, "push, grow");
		add(velChart, "push, grow");

		timeOffset = System.nanoTime();
	}


	/**
	 * @param observer
	 */
	public void addObserver(final ISystemMatchFeedbackPanelObserver observer)
	{
		observers.add(observer);
	}


	/**
	 * @param observer
	 */
	public void removeObserver(final ISystemMatchFeedbackPanelObserver observer)
	{
		observers.remove(observer);
	}


	private JCheckBox createChartCheckBox(final Chart2D chart)
	{
		final JCheckBox chkBox = new JCheckBox(chart.getName(), true);
		chkBox.addActionListener(e -> {
			if (chkBox.isSelected())
			{
				chart.setEnabled(true);
				add(chart, "push, grow");
			} else
			{
				chart.setEnabled(false);
				remove(chart);
			}
			invalidate();
		});
		return chkBox;
	}


	public void addTigerSystemMatchFeedback(final TigerSystemMatchFeedback status)
	{
		robotMode.setText(status.getRobotMode().toString());
		robotMode.setBackground(status.getRobotMode().getColor());
		batteryLevel.setString(String.format(Locale.ENGLISH, "%.1f V", status.getBatteryLevel()));
		batteryLevel.setValue((int) (status.getBatteryPercentage() * 1000));
		kickerLevel.setString(String.format(Locale.ENGLISH, "%d V", (int) status.getKickerLevel()));
		kickerLevel.setValue((int) (status.getKickerPercentage() * 1000));
		dribblerSpeed.setText(String.format(Locale.ENGLISH, "%d RPM (%.2fA)", (int) status.getDribblerSpeed(), status.getDribblerCurrent()));
		dribblerTemp.setText(status.getDribblerState().toString());
		kickCounter.setText(String.format("%d", status.getKickCounter()));
		hardwareId.setText(String.format("%d", status.getHardwareId()));
		barrierInterrupted.setSelected(status.isBarrierInterrupted());

		for (EFeature f : EFeature.values())
		{
			features.get(f).setSelected(status.isFeatureWorking(f));
		}

		if (status.isPositionValid())
		{
			pos[0].setText(String.format(Locale.ENGLISH, "%.3f m", status.getPosition().x()));
			pos[1].setText(String.format(Locale.ENGLISH, "%.3f m", status.getPosition().y()));
			pos[2].setText(String.format(Locale.ENGLISH, "%.3f rad", status.getOrientation()));

			posXTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, status.getPosition().x());
			posYTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, status.getPosition().y());
			posWTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, status.getOrientation());
		}

		if (status.isVelocityValid())
		{
			vel[0].setText(String.format(Locale.ENGLISH, "%.3f m/s", status.getVelocity().x()));
			vel[1].setText(String.format(Locale.ENGLISH, "%.3f m/s", status.getVelocity().y()));
			vel[2].setText(String.format(Locale.ENGLISH, "%.3f rad/s", status.getAngularVelocity()));

			velXTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, status.getVelocity().x());
			velYTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, status.getVelocity().y());
			velWTrace.addPoint((System.nanoTime() - timeOffset) / 1000000000.0, status.getAngularVelocity());
		}

		if (status.isBallPositionValid())
		{
			ball[0].setText(String.format(Locale.ENGLISH, "%.3f m", status.getBallPosition().x()));
			ball[1].setText(String.format(Locale.ENGLISH, "%.3f m", status.getBallPosition().y()));
			ball[2].setText(String.format(Locale.ENGLISH, "%.3f s", status.getBallAge()));
		}
		else
		{
			ball[0].setText("-");
			ball[1].setText("-");
			ball[2].setText("-");
		}
	}


	private class CaptureActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			notifyCapture(btnCapture.isSelected());
		}


		private void notifyCapture(final boolean enabled)
		{
			for (ISystemMatchFeedbackPanelObserver observer : observers)
			{
				observer.onCapture(enabled);
			}
		}
	}

	@FunctionalInterface
	public interface ISystemMatchFeedbackPanelObserver
	{
		/**
		 * @param enable
		 */
		void onCapture(boolean enable);
	}
}
