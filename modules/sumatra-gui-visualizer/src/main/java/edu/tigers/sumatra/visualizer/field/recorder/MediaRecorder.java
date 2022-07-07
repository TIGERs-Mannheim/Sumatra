/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field.recorder;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.visualizer.field.FieldPane;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


@Log4j2
@RequiredArgsConstructor
public class MediaRecorder
{
	private static final Path BASE_SCREENCAST_PATH = Path.of("data/screencast/");
	private static final int PERIOD = 40;

	@Getter
	private final FieldPane fieldPane = new FieldPane();
	private Recorder recorder;


	public void takeScreenshotFullField(List<ShapeMap.ShapeLayer> shapeLayers)
	{
		var image = paintFullField(shapeLayers);
		storeScreenshot(image);
	}


	public void takeScreenshotCurrentSelection(FieldPane currentFieldPane, List<ShapeMap.ShapeLayer> shapeLayers)
	{
		var image = paintCurrentSelection(currentFieldPane, shapeLayers);
		storeScreenshot(image);
	}


	public void startVideoRecordingFullField(
			Supplier<List<ShapeMap.ShapeLayer>> shapeLayerSupplier
	)
	{
		if (recorder != null)
		{
			log.warn("Can not start recording: Recorder already active");
		}
		recorder = new Recorder(null, shapeLayerSupplier);
		try
		{
			recorder.startCaptureFullField();
		} catch (IOException e)
		{
			recorder = null;
			log.error("Failed to capture video", e);
		}
	}


	public void startVideoRecordingCurrentSelection(
			FieldPane currentFieldPane,
			Supplier<List<ShapeMap.ShapeLayer>> shapeLayerSupplier
	)
	{
		if (recorder != null)
		{
			log.warn("Can not start recording: Recorder already active");
		}
		recorder = new Recorder(currentFieldPane, shapeLayerSupplier);
		try
		{
			recorder.startCaptureCurrentSelection();
		} catch (IOException e)
		{
			recorder = null;
			log.error("Failed to capture video", e);
		}
	}


	public void stopVideoRecording()
	{
		if (recorder == null)
		{
			return;
		}
		recorder.stop();
		recorder = null;
	}


	private BufferedImage paintFullField(List<ShapeMap.ShapeLayer> shapeLayers)
	{
		fieldPane.reset();

		BufferedImage image = createImage();
		Graphics2D g2 = (Graphics2D) image.getGraphics();
		fieldPane.paint(g2, shapeLayers);
		return image;
	}


	private BufferedImage paintCurrentSelection(FieldPane currentFieldPane, List<ShapeMap.ShapeLayer> shapeLayers)
	{
		double currentOffsetX = currentFieldPane.getOffsetX();
		double currentOffsetY = currentFieldPane.getOffsetY();
		double currentScale = currentFieldPane.getScale();
		int currentWidth = currentFieldPane.getWidth();
		int currentHeight = currentFieldPane.getHeight();

		final double xLen = ((-currentOffsetX) / currentScale) * 2;
		final double yLen = ((-currentOffsetY) / currentScale) * 2;
		final double oldLenX = (xLen) * currentScale;
		final double oldLenY = (yLen) * currentScale;
		double normalizedScale = currentScale * Math.min(
				fieldPane.getWidth() / (double) currentWidth,
				fieldPane.getHeight() / (double) currentHeight
		);
		final double newLenX = (xLen) * normalizedScale;
		final double newLenY = (yLen) * normalizedScale;
		double offsetX = (currentOffsetX - ((newLenX - oldLenX) / 2.0));
		double offsetY = (currentOffsetY - ((newLenY - oldLenY) / 2.0));

		fieldPane.reset();
		fieldPane.getTransformation().setFieldTurn(currentFieldPane.getTransformation().getFieldTurn());
		fieldPane.setOffsetX(offsetX);
		fieldPane.setOffsetY(offsetY);
		fieldPane.setScale(normalizedScale);

		BufferedImage image = createImage();
		Graphics2D g2 = (Graphics2D) image.getGraphics();
		fieldPane.paint(g2, shapeLayers);
		return image;
	}


	@SneakyThrows
	private Path newFilePath(String prefix, String ending)
	{
		Files.createDirectories(BASE_SCREENCAST_PATH);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		sdf.setTimeZone(TimeZone.getDefault());
		String filename = prefix + "_" + sdf.format(new Date()) + ending;
		return BASE_SCREENCAST_PATH.resolve(filename).toAbsolutePath();
	}


	private BufferedImage createImage()
	{
		return new BufferedImage(fieldPane.getWidth(), fieldPane.getHeight(), BufferedImage.TYPE_INT_ARGB);
	}


	private void storeScreenshot(BufferedImage image)
	{
		try
		{
			Path path = newFilePath("screenshot", ".png");
			ImageIO.write(image, "png", path.toFile());
			log.info("Finished saving screenshot to: {}", path);
		} catch (IOException e)
		{
			log.warn("Could not take Screenshot", e);
		}
	}


	@RequiredArgsConstructor
	private class Recorder
	{
		private final VideoExporter videoExporter = new VideoExporter(
				newFilePath("screencast", ".mp4"),
				fieldPane.getWidth(),
				fieldPane.getHeight()
		);
		private final FieldPane currentFieldPane;
		private final Supplier<List<ShapeMap.ShapeLayer>> shapeLayerSupplier;
		private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


		public void startCaptureFullField() throws IOException
		{
			videoExporter.start();
			scheduler.scheduleAtFixedRate(() -> run(this::captureFullscreen), 0, PERIOD, TimeUnit.MILLISECONDS);
		}


		public void startCaptureCurrentSelection() throws IOException
		{
			videoExporter.start();
			scheduler.scheduleAtFixedRate(() -> run(this::captureCurrentSelection), 0, PERIOD, TimeUnit.MILLISECONDS);
		}


		public void stop()
		{
			scheduler.shutdown();
			try
			{
				if (!scheduler.awaitTermination(10, TimeUnit.SECONDS))
				{
					log.warn("Timed out waiting for video recorder to terminate");
				}
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			videoExporter.stop();
		}


		private void run(Runnable runnable)
		{
			try
			{
				runnable.run();
			} catch (OutOfMemoryError outOfMemoryError)
			{
				log.error("Run out of memory. Stop video recording", outOfMemoryError);
				stop();
			}
		}


		private void captureFullscreen()
		{
			List<ShapeMap.ShapeLayer> shapeLayers = shapeLayerSupplier.get();
			var image = paintFullField(shapeLayers);
			videoExporter.addImageToVideo(image);
		}


		private void captureCurrentSelection()
		{
			List<ShapeMap.ShapeLayer> shapeLayers = shapeLayerSupplier.get();
			var image = paintCurrentSelection(currentFieldPane, shapeLayers);
			videoExporter.addImageToVideo(image);
		}
	}
}
