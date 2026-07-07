package edu.tigers.sumatra.gui.visualizer.presenter.recorder;

import edu.tigers.sumatra.math.Hysteresis;
import lombok.extern.log4j.Log4j2;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


@Log4j2
public class VideoExporter implements AutoCloseable
{
	private static final int FPS = 24;
	private static final int DEFAULT_QUEUE_WARNING_LOW = 300;
	private static final int DEFAULT_QUEUE_WARNING_HIGH = 350;
	private static final int POLL_TIMEOUT_MS = 200;

	private final BlockingDeque<BufferedImage> frameBuffer = new LinkedBlockingDeque<>();
	private final Hysteresis maxFramesInBufferHyst = new Hysteresis(
			DEFAULT_QUEUE_WARNING_LOW, DEFAULT_QUEUE_WARNING_HIGH);

	private final Path filePath;
	private final int targetWidth;
	private final int targetHeight;

	private final SeekableByteChannel out;
	private final AWTSequenceEncoder encoder;

	private final AtomicBoolean running = new AtomicBoolean(true);

	private Thread workerThread;

	private BufferedImage reusableFrame;
	private Graphics2D reusableG2d;


	public VideoExporter(final Path filePath, final int width, final int height)
			throws IOException
	{
		this.filePath = filePath;
		this.targetWidth = width;
		this.targetHeight = height;

		out = NIOUtils.writableChannel(filePath.toFile());
		encoder = new AWTSequenceEncoder(out, Rational.R(FPS, 1));
	}


	public void start()
	{
		reusableFrame = new BufferedImage(
				targetWidth,
				targetHeight,
				BufferedImage.TYPE_3BYTE_BGR
		);

		reusableG2d = reusableFrame.createGraphics();
		reusableG2d.setRenderingHint(
				RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR
		);
		reusableG2d.setRenderingHint(
				RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED
		);
		reusableG2d.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF
		);

		maxFramesInBufferHyst.setOnUpperCallback(this::notifySlowVideoProcessing);

		workerThread = new Thread(this::process, "VideoExporter");
		workerThread.setUncaughtExceptionHandler(
				(t, e) -> log.error("Uncaught exception in {} thread", t.getName(), e));
		workerThread.start();
	}


	public void stop()
	{
		running.set(false);
		log.info(
				"Stopping video recording, still {} frames to process, closing may take a while...",
				frameBuffer.size()
		);
	}


	/**
	 * Records one frame for the video.
	 */
	public void addImageToVideo(final BufferedImage image)
	{
		if (image == null)
		{
			return;
		}

		maxFramesInBufferHyst.update(frameBuffer.size());
		if (maxFramesInBufferHyst.isLower())
		{
			frameBuffer.add(image);
		}
	}


	private void notifySlowVideoProcessing()
	{
		log.warn("slow video processing, starting to drop frames. Try a lower resolution next time.");
	}


	private void handleFrame(final BufferedImage image) throws IOException
	{
		if (image == null)
		{
			return;
		}

		encoder.encodeImage(image);
	}


	private void process()
	{
		try
		{
			while (running.get() || !frameBuffer.isEmpty())
			{
				var image = fetchImage();
				if (image.isPresent())
				{
					handleFrame(image.get());
				}
			}
		} catch (Exception e)
		{
			log.error("Error while encoding video", e);
		} finally
		{
			log.info("Start finalizing video");

			try
			{
				encoder.finish();
			} catch (Exception e)
			{
				log.error("Error while finalizing video", e);
			}

			try
			{
				out.close();
			} catch (Exception e)
			{
				log.warn("Failed to close output", e);
			}

			log.info("Finished recording video to {}", filePath);
		}
	}


	private Optional<BufferedImage> fetchImage()
	{
		try
		{
			BufferedImage image = frameBuffer.pollFirst(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
			return Optional.ofNullable(image);
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			running.set(false);
		}
		return Optional.empty();
	}


	@Override
	public void close()
	{
		stop();

		if (workerThread != null)
		{
			try
			{
				workerThread.join();
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}
	}
}