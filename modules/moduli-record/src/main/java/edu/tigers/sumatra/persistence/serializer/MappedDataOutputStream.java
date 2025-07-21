/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


/**
 * High-performance single copy data output (stream).
 * For this 1MiB memory maps (see mmap) at the end of the file are utilized to minimize copies.
 * Exception-based Buffer overflow handling is used to accelerate the significantly more frequent regular execution path
 * in the performance critical .write methods.
 */
public class MappedDataOutputStream implements AutoCloseable
{
	// BUFFER_SIZE needs to be a multiple of the system page size for best performance (x86: 4KiB, Apple ARM: 64KiB)
	public static final long BUFFER_SIZE = 1024L * 1024; // 1MiB buffers

	private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);

	private final ByteBuffer bytes8 = ByteBuffer.allocate(8);
	private final ByteBuffer bytes4 = ByteBuffer.allocate(4);

	private final FileChannel channel;
	private ByteBuffer buffer;


	public MappedDataOutputStream(Path path) throws IOException
	{
		//READ is necessary, as java doesn't allow write-only memory maps.
		channel = FileChannel.open(
				path,
				StandardOpenOption.CREATE, StandardOpenOption.SPARSE, StandardOpenOption.WRITE, StandardOpenOption.READ
		);
		buffer = EMPTY;
	}


	/**
	 * Special constructor for /dev/null
	 */
	MappedDataOutputStream(Path path, ByteBuffer buffer) throws IOException
	{
		this(path);
		this.buffer = buffer;
	}


	public long getPos() throws IOException
	{
		return channel.size() - buffer.remaining();
	}


	public void write(byte b) throws IOException
	{
		try
		{
			buffer.put(b);
		} catch (BufferOverflowException e)
		{
			allocateBuffer();
			buffer.put(b);
		}
	}


	public void write(byte[] b) throws IOException
	{
		try
		{
			buffer.put(b);
		} catch (BufferOverflowException e)
		{
			int split = Math.min(b.length, buffer.remaining());
			buffer.put(b, 0, split);
			allocateBuffer();
			//Does not handle the special case of b being larger than the buffer sizes
			buffer.put(b, split, b.length - split);
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
		try
		{
			buffer.putLong(l);
		} catch (BufferOverflowException e)
		{
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
		try
		{
			buffer.putInt(i);
		} catch (BufferOverflowException e)
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
		closeBuffer();
		channel.truncate(Math.max(getPos(), 0)); // Remove overallocation overhead, /dev/null can lead to negative sizes
		channel.close();
	}


	private void allocateBuffer() throws IOException
	{
		closeBuffer();
		buffer = channel.map(FileChannel.MapMode.READ_WRITE, channel.size(), BUFFER_SIZE);
	}


	private void closeBuffer()
	{
		if(buffer.isDirect())
		{
			// The mapped buffer needs to be closed for Windows as Windows locks mapped file sections
			// See https://stackoverflow.com/questions/25238110/how-to-properly-close-mappedbytebuffer
			FieldSerializer.UNSAFE.invokeCleaner(buffer);
		}
	}
}
