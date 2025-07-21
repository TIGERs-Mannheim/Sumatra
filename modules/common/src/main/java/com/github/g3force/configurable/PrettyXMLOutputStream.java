/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package com.github.g3force.configurable;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * {@link OutputStream} implementation that formats XML files
 */
public class PrettyXMLOutputStream extends OutputStream
{
	private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	private final String encoding;
	private final OutputStream outStream;


	public PrettyXMLOutputStream(final OutputStream outStream, final String encoding)
	{
		super();
		this.outStream = outStream;
		this.encoding = encoding;
	}


	@Override
	public void write(final byte[] b, final int off, final int len)
	{
		buffer.write(b, off, len);
	}


	@Override
	public void write(final int b)
	{
		buffer.write(b);
	}


	@Override
	public void flush() throws IOException
	{
		if (buffer.size() > 0)
		{
			// Parse document from buffer
			final DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
			Document doc = null;
			try
			{
				final DocumentBuilder b = f.newDocumentBuilder();

				final InputSource sourceXML = new InputSource(new ByteArrayInputStream(buffer.toByteArray()));
				doc = b.parse(sourceXML);
			} catch (final ParserConfigurationException err)
			{
				throw new IOException("DocumentBuilder could not be created!", err);
			} catch (final SAXException err)
			{
				throw new IOException("Unable to parse the document which should be written!", err);
			}


			// Pretty print
			final DOMImplementationLS domLS = (DOMImplementationLS) doc.getImplementation();
			final LSSerializer serializer = domLS.createLSSerializer();

			final DOMConfiguration domConfig = serializer.getDomConfig();
			domConfig.setParameter("format-pretty-print", Boolean.TRUE);
			serializer.setNewLine("\n");


			// Prepare output...
			final LSOutput output = domLS.createLSOutput();
			output.setEncoding(encoding);
			output.setByteStream(outStream);

			// Write!
			serializer.write(doc, output);

			buffer.reset();
		}
	}


	@Override
	public void close() throws IOException
	{
		if (buffer != null)
		{
			flush();

			buffer.close();
			buffer = null;
		}
	}
}
