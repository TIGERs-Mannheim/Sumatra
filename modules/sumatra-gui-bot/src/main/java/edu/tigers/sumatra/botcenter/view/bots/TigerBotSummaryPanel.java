/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botcenter.view.bots;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.ids.BotID;
import net.miginfocom.swing.MigLayout;


/**
 * Tiger bot summary for the overview panel.
 *
 * @author AndreR
 */
public class TigerBotSummaryPanel extends JPanel
{
	private static final long serialVersionUID = 5485796598824650963L;
	private final JTextField status;
	private final JProgressBar battery;

	private String name;
	private BotID id;
	private ERobotMode robotMode;
	private final JTextField cap;
	private JTextField broken;


	public TigerBotSummaryPanel()
	{
		setLayout(new MigLayout("fillx", "[80,fill]10[40]10[60,fill]20[30]10[60,fill]10[60,fill]10[200,fill]", "0[]0"));

		cap = new JTextField();
		cap.setHorizontalAlignment(SwingConstants.RIGHT);

		name = "Bob";
		id = BotID.noBot();
		robotMode = ERobotMode.IDLE;

		status = new JTextField();
		status.setEditable(false);
		battery = new JProgressBar(0, 1000);
		battery.setStringPainted(true);
		broken = new JTextField("N/A");

		add(status);
		add(new JLabel("Battery:"));
		add(battery, "growy");
		add(new JLabel("Kicker:"));
		add(cap);
		add(new JLabel("Broken Features:"));
		add(broken);

		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Trish (1)"));

		battery.setValue(500);
		battery.setString("14.2 V");
	}


	/**
	 * @param id
	 */
	public void setId(final BotID id)
	{
		this.id = id;
		updateTitle();
	}


	/**
	 * @param name
	 */
	public void setBotName(final String name)
	{
		this.name = name;
		updateTitle();
	}


	/**
	 * @param mode
	 */
	public void setRobotMode(final ERobotMode mode)
	{
		robotMode = mode;
		updateTitle();
	}


	/**
	 * @param feedback
	 */
	public void setMatchFeedback(final TigerSystemMatchFeedback feedback)
	{
		battery.setValue((int) (feedback.getBatteryPercentage() * 1000));
		battery.setString(String.format(Locale.ENGLISH, "%1.2f V", feedback.getBatteryLevel()));

		if (robotMode != feedback.getRobotMode())
		{
			setRobotMode(feedback.getRobotMode());
		}

		setBrokenFeatures(feedback);
		setCap(feedback.getKickerLevel());
	}


	private void setBrokenFeatures(final TigerSystemMatchFeedback feedback)
	{
		if (feedback.getRobotMode() != ERobotMode.READY)
		{
			broken.setText("N/A");
			return;
		}

		List<String> brokenList = new ArrayList<>();

		if (!feedback.isFeatureWorking(EFeature.BARRIER))
		{
			brokenList.add("Barrier");
		}
		if (!feedback.isFeatureWorking(EFeature.STRAIGHT_KICKER))
		{
			brokenList.add("Straight");
		}
		if (!feedback.isFeatureWorking(EFeature.CHIP_KICKER))
		{
			brokenList.add("Chip");
		}
		if (!feedback.isFeatureWorking(EFeature.DRIBBLER))
		{
			brokenList.add("Dribbler");
		}
		if (!feedback.isFeatureWorking(EFeature.MOVE))
		{
			brokenList.add("Move");
		}
		if (!feedback.isFeatureWorking(EFeature.CHARGE_CAPS))
		{
			brokenList.add("Charge");
		}

		broken.setText(String.join(", ", brokenList));
	}


	/**
	 * @param f
	 */
	private void setCap(final double f)
	{
		double green;
		double red;

		// increase red level => yellow
		if (f < 125)
		{
			red = f / 125.0;
		} else
		{
			red = 1;
		}

		// decrease green level => red
		if (f > 125)
		{
			green = 1 - ((f - 125) / 125.0);
		} else
		{
			green = 1;
		}

		if (green < 0)
		{
			green = 0;
		}

		if (red < 0)
		{
			red = 0;
		}

		final double g = green;
		final double r = red;

		cap.setText(String.format(Locale.ENGLISH, "%3.1fV", f));
		cap.setBackground(new Color((float) r, (float) g, 0));
	}


	private void updateTitle()
	{
		final String title = String.format("[ %d ] %s", id.getNumber(), name);
		Color borderColor = robotMode.getColor();
		String stateText = robotMode.toString();

		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColor), title));
		status.setText(stateText);
	}
}
