/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ballanalyzer.presenter;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import edu.tigers.sumatra.data.collector.ITimeSeriesDataCollectorObserver;
import edu.tigers.sumatra.data.collector.TimeSeriesDataCollector;
import edu.tigers.sumatra.gui.ballanalyzer.view.IBallAnalyserPanelObserver;
import edu.tigers.sumatra.gui.ballanalyzer.view.VisionAnalyserPanel;
import edu.tigers.sumatra.matlab.MatlabConnection;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import edu.tigers.sumatra.wp.util.TimeSeriesDataCollectorFactory;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import javax.swing.JOptionPane;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Presenter for vision analyser.
 */
@Log4j2
public class VisionAnalyserPresenter implements ISumatraViewPresenter
{
	@Getter
	private final VisionAnalyserPanel viewPanel = new VisionAnalyserPanel();

	private TimeSeriesDataCollector dataCollector;


	public VisionAnalyserPresenter()
	{
		viewPanel.addObserver(new PanelObserver());
	}


	private class PanelObserver implements IBallAnalyserPanelObserver, ClipboardOwner
	{
		private static final String DESCRIPTION = "description";
		private static final String LEARNING = "addpath('learning')";


		@Override
		public void onSave(final String filename)
		{
			File infoFile = new File(getBaseDir(filename) + "/info.json");
			if (infoFile.exists())
			{
				try (FileReader fr = new FileReader(infoFile))
				{
					JsonObject map = (JsonObject) Jsoner.deserialize(fr);
					map.put(DESCRIPTION, viewPanel.getDescription());
					JsonObject jo = new JsonObject(map);
					Files.write(Paths.get(infoFile.getAbsolutePath()), jo.toJson().getBytes());
				} catch (IOException | JsonException err)
				{
					log.error("Could not parse JSON.", err);
				} catch (ClassCastException err)
				{
					log.error("Could not cast a type. Wrong format?!", err);
				}
			}
			viewPanel.markDirty(false);
		}


		@Override
		public void onRecord(final boolean doRecord, final boolean stopAutomatically)
		{
			if (doRecord)
			{
				if (dataCollector != null)
				{
					log.warn("Last ball watcher was not removed?!");
					dataCollector.stopExport();
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
				String fileName = "manual/" + sdf.format(new Date());
				dataCollector = TimeSeriesDataCollectorFactory.createFullCollector(fileName);
				dataCollector.addObserver(new TimeSeriesDataCollectorObserver());
				dataCollector.setStopAutomatically(stopAutomatically);
				boolean started = dataCollector.start();
				viewPanel.setRecording(started);
				if (!started)
				{
					dataCollector = null;
				}
			} else
			{
				dataCollector.stopExport();
			}
		}


		@Override
		public void onDelete(final List<String> filenames)
		{
			StringBuilder sb = new StringBuilder();
			for (String s : filenames)
			{
				sb.append(s);
				sb.append('\n');
			}

			int answer = JOptionPane.showConfirmDialog(viewPanel,
					"Do you really want to delete following files? \n" + sb, "Delete?",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (answer == JOptionPane.YES_OPTION)
			{
				for (String filename : filenames)
				{
					processYesDeleteAnswerForFile(filename);
				}
				viewPanel.updateFiles();
			}
		}


		private void processYesDeleteAnswerForFile(final String filename)
		{
			File file = new File(filename);
			if (file.isDirectory())
			{
				File[] files = file.listFiles();
				deleteFiles(file, files);
			}
			if (!file.delete())
			{
				log.error("Could not delete file: {}", filename);
			}
		}


		private void deleteFiles(final File originalFile, final File[] files)
		{
			if (files != null)
			{
				for (File f : files)
				{
					if (f.isDirectory())
					{
						log.warn("Recursive deletion not supported for safety reasons! Skip {}", f.getAbsolutePath());
						continue;
					}
					if (!f.delete())
					{
						log.warn("Could not delete file: {}", originalFile.getAbsolutePath());
					}
				}
			}
		}


		@Override
		public void onPlot(final List<String> filenames)
		{
			Thread t = new Thread(() -> plotBallData(filenames));
			t.start();
		}


		private void plotBallData(final List<String> filenames)
		{
			for (String filename : filenames)
			{
				String basePath = getBaseDir(filename);
				File f = new File(basePath);
				if (!f.exists())
				{
					continue;
				}
				callMatlab("plotVisionData", basePath);
			}
		}


		private void callMatlab(final String function, final Object... params)
		{
			MatlabProxy mp;
			try
			{
				mp = MatlabConnection.getMatlabProxy();
				mp.feval(function, params);
			} catch (MatlabConnectionException err)
			{
				log.error(err.getMessage(), err);
			} catch (MatlabInvocationException err)
			{
				log.error("Error evaluating matlab function", err);
			}
		}


		@Override
		public void onNewSelectedFile(final List<String> filenames)
		{
			if (filenames.isEmpty())
			{
				return;
			}
			File infoFile = new File(getBaseDir(filenames.getFirst()) + "/info.json");
			if (infoFile.exists())
			{
				viewPanel.setValidFileSelected(true);
				viewPanel.clearKeyValue();
				try (FileReader fr = new FileReader(infoFile))
				{
					JsonObject jo = (JsonObject) Jsoner.deserialize(fr);
					String description = (String) jo.get(DESCRIPTION);
					Long numSamples = (Long) jo.get("numSamples");
					viewPanel.setDescription(Objects.requireNonNullElse(description, ""));
					if (numSamples != null)
					{
						viewPanel.setNumSamples((int) (long) numSamples);
					} else
					{
						viewPanel.setNumSamples(-1);
					}

					processJsonbjects(jo);
				} catch (IOException | JsonException err)
				{
					log.error("Could not parse JSON.", err);
				} catch (ClassCastException err)
				{
					log.error("Could not cast a type. Wrong format?!", err);
				}
			} else
			{
				viewPanel.setValidFileSelected(false);
				viewPanel.setDescription("");
				viewPanel.setNumSamples(0);
				viewPanel.clearKeyValue();
			}
		}


		void processJsonbjects(final Map<String, Object> jsonObjects)
		{
			for (Map.Entry<String, Object> entry : jsonObjects.entrySet())
			{
				if (DESCRIPTION.equals(entry.getKey()) || "numSamples".equals(entry.getKey()))
				{
					continue;
				}
				viewPanel.setKeyValue(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}


		private String getBaseDir(final String filename)
		{
			File f = new File(filename);
			final String basePath;
			if (f.isDirectory())
			{
				basePath = f.getAbsolutePath();
			} else
			{
				basePath = f.getParent();
			}
			return basePath;
		}


		@Override
		public void onCopy(final List<String> filenames)
		{
			StringBuilder sb = new StringBuilder();
			for (String s : filenames)
			{
				sb.append(s);
				sb.append('\n');
			}
			if (!sb.isEmpty())
			{
				sb.deleteCharAt(sb.length() - 1);
			}
			StringSelection stringSelection = new StringSelection(sb.toString());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, this);
		}


		@Override
		public void lostOwnership(final Clipboard clipboard, final Transferable contents)
		{
			// nothing to do here
		}


		@Override
		public void onPlotCrookedKick(final List<String> filenames)
		{
			try
			{
				MatlabProxy mp = MatlabConnection.getMatlabProxy();
				mp.eval(LEARNING);
				mp.returningFeval("crookedKick", 1,
						filenames.toArray(new Object[0]));
			} catch (MatlabConnectionException err)
			{
				log.error(err.getMessage(), err);
			} catch (MatlabInvocationException err)
			{
				log.error("Error evaluating matlab function", err);
			}
		}
	}

	private class TimeSeriesDataCollectorObserver implements ITimeSeriesDataCollectorObserver
	{
		@Override
		public void postProcessing(final String fileName)
		{
			viewPanel.updateFiles();
			viewPanel.setRecording(false);
			dataCollector = null;
		}
	}
}
