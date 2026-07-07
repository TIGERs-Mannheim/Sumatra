package edu.tigers.sumatra.persistence.serializer;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


/**
 * High-performance single copy data output (stream).
 * For this 1MiB memory maps (see mmap) at the end of the file are utilized to minimize copies.
 * Writes go directly to the {@link MemorySegment} via {@link ValueLayout} accessors instead of
 * a wrapping {@link ByteBuffer}: the wrapper would re-validate the FFM scope on every put and
 * carry its own position/limit state on top of the segment's bounds check.
 */
public class MappedDataOutputStream implements AutoCloseable
{
	// BUFFER_SIZE needs to be a multiple of the system page size for best performance (x86: 4KiB, Apple ARM: 64KiB)
	public static final long BUFFER_SIZE = 1024L * 1024; // 1MiB buffers

	private static final MemorySegment EMPTY = MemorySegment.ofArray(new byte[0]);
	// Big-endian to match the byte order produced by the previous ByteBuffer-backed writes.
	private static final ValueLayout.OfLong LONG_BE = ValueLayout.JAVA_LONG_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);
	private static final ValueLayout.OfInt INT_BE = ValueLayout.JAVA_INT_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);

	private final ByteBuffer bytes8 = ByteBuffer.allocate(8);
	private final ByteBuffer bytes4 = ByteBuffer.allocate(4);

	private final FileChannel channel;
	private MemorySegment segment;
	private long position;
	private Arena bufferArena;


	public MappedDataOutputStream(Path path) throws IOException
	{
		//READ is necessary, as java doesn't allow write-only memory maps.
		channel = FileChannel.open(
				path,
				StandardOpenOption.CREATE, StandardOpenOption.SPARSE, StandardOpenOption.WRITE, StandardOpenOption.READ
		);
		segment = EMPTY;
	}


	/**
	 * Special constructor for /dev/null: pre-installs a fixed-size heap-backed segment so the
	 * stream never tries to mmap a non-mappable path. Writes that exceed the segment's capacity
	 * will trigger a regular allocateBuffer() call (which may then fail on /dev/null, but the
	 * bootstrap metadata serializer is sized to fit).
	 */
	MappedDataOutputStream(Path path, MemorySegment scratch) throws IOException
	{
		this(path);
		this.segment = scratch;
	}


	public long getPos() throws IOException
	{
		return channel.size() - segment.byteSize() + position;
	}


	public void write(byte b) throws IOException
	{
		if (position >= segment.byteSize())
		{
			allocateBuffer();
		}
		segment.set(ValueLayout.JAVA_BYTE, position, b);
		position++;
	}


	public void write(byte[] b) throws IOException
	{
		long remaining = segment.byteSize() - position;
		if (b.length <= remaining)
		{
			MemorySegment.copy(b, 0, segment, ValueLayout.JAVA_BYTE, position, b.length);
			position += b.length;
		} else
		{
			int split = (int) remaining;
			MemorySegment.copy(b, 0, segment, ValueLayout.JAVA_BYTE, position, split);
			position += split;
			allocateBuffer();
			//Does not handle the special case of b being larger than the buffer sizes
			MemorySegment.copy(b, split, segment, ValueLayout.JAVA_BYTE, position, b.length - split);
			position += b.length - split;
		}
	}


	public void write(short s) throws IOException
	{
		write((int) s);
	}


	/**
	 * Integers are compressed with VarInt compression,
	 * exploiting the fact that most integers are small positive values.
	 * This compression leads to 1 (values 0 to 127) up to 5 byte long integer values.
	 */
	public void write(int value) throws IOException
	{
		int continuationBytes = (31 - Integer.numberOfLeadingZeros(value)) / 7;
		for (int i = 0; i < continuationBytes; ++i)
		{
			write(((byte) ((value & 0x7F) | 0x80)));
			value >>>= 7;
		}
		write((byte) value);
	}


	public void write(long l) throws IOException
	{
		if (position + Long.BYTES <= segment.byteSize())
		{
			segment.set(LONG_BE, position, l);
			position += Long.BYTES;
		} else
		{
			// Straddles a buffer boundary: serialise via a heap scratch and split-write byte-wise.
			bytes8.clear();
			bytes8.putLong(l);
			write(bytes8.array());
		}
	}


	public void write(boolean b) throws IOException
	{
		write((byte) (b ? 1 : 0));
	}


	public void write(char c) throws IOException
	{
		write((short) c);
	}


	public void write(float f) throws IOException
	{
		int i = Float.floatToIntBits(f);
		if (position + Integer.BYTES <= segment.byteSize())
		{
			segment.set(INT_BE, position, i);
			position += Integer.BYTES;
		} else
		{
			bytes4.clear();
			bytes4.putInt(i);
			write(bytes4.array());
		}
	}


	/**
	 * Doubles are compressed by being serialized as floats.
	 */
	public void write(double d) throws IOException
	{
		write((float) d);
	}


	public void write(String s) throws IOException
	{
		byte[] b = s.getBytes(StandardCharsets.UTF_8);
		write(b.length);
		write(b);
	}


	@Override
	public void close() throws IOException
	{
		// getPos() reads segment.byteSize() / position, so capture before closeBuffer() wipes them.
		long endPos = getPos();
		closeBuffer();
		channel.truncate(Math.max(endPos, 0)); // Remove overallocation overhead, /dev/null can lead to negative sizes
		channel.close();
	}


	private void allocateBuffer() throws IOException
	{
		closeBuffer();
		// Confined to the calling thread: PersistenceDb funnels all stream work — open, write,
		// and close — through a single-threaded executor (PersistenceDb-IO). Allocation, every
		// subsequent put, and arena.close() therefore share that thread, so we avoid the
		// stamp-locked liveness check that shared arenas pay on every put.
		Arena arena = Arena.ofConfined();
		try
		{
			segment = channel.map(
					FileChannel.MapMode.READ_WRITE, channel.size(), BUFFER_SIZE, arena);
			bufferArena = arena;
			position = 0;
		} catch (Throwable t)
		{
			arena.close();
			throw t;
		}
	}


	private void closeBuffer()
	{
		if (bufferArena != null)
		{
			// Closing the arena unmaps the region — required on Windows where
			// mapped sections lock the underlying file until released.
			bufferArena.close();
			bufferArena = null;
			segment = EMPTY;
			position = 0;
		}
	}
}
