package org.apache.catalina.connector.http;

import java.io.IOException;
import org.apache.catalina.connector.RequestStream;

/**
 * <code>RequestStream</code>的子类，提供了对块编码的支持。
 * 
 * @author tequlia2pop
 * @deprecated
 */
public class HttpRequestStream extends RequestStream {

	// ----------------------------------------------------- Instance Variables

	/**
	 * 是否使用块编码？
	 */
	protected boolean chunk = false;

	/**
	 * 如果是最后一个块则为 true。
	 */
	protected boolean endChunk = false;

	/**
	 * 块缓冲区.
	 */
	protected byte[] chunkBuffer = null;

	/**
	 * 块的长度。
	 */
	protected int chunkLength = 0;

	/**
	 * 块缓冲区的位置。
	 */
	protected int chunkPos = 0;

	/**
	 * HTTP/1.1 标识。
	 */
	protected boolean http11 = false;

	// ----------------------------------------------------------- Constructors

	/**
	 * Construct a servlet input stream associated with the specified Request.
	 *
	 * @param request The associated request
	 * @param response The associated response
	 */
	public HttpRequestStream(HttpRequestImpl request, HttpResponseImpl response) {
		super(request);
		String transferEncoding = request.getHeader("Transfer-Encoding");

		http11 = request.getProtocol().equals("HTTP/1.1");
		chunk = ((transferEncoding != null) && (transferEncoding.indexOf("chunked") != -1));

		if ((!chunk) && (length == -1)) {
			// Ask for connection close
			response.addHeader("Connection", "close");
		}
	}

	// --------------------------------------------------------- Public Methods

	@Override
	public void close() throws IOException {
		if (closed)
			throw new IOException(sm.getString("requestStream.close.closed"));

		if (chunk) {
			while (!endChunk) {
				int b = read();
				if (b < 0)
					break;
			}
		} else {
			if (http11 && (length > 0)) {
				while (count < length) {
					int b = read();
					if (b < 0)
						break;
				}
			}
		}

		closed = true;
	}

	@Override
	public int read() throws IOException {
		// 此流是否已经关闭？
		if (closed)
			throw new IOException(sm.getString("requestStream.read.closed"));

		if (chunk) {
			if (endChunk)
				return -1;
			if ((chunkBuffer == null) || (chunkPos >= chunkLength)) {
				if (!fillChunkBuffer())
					return -1;
			}
			return (chunkBuffer[chunkPos++] & 0xff);
		} else {
			return super.read();
		}
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		if (chunk) {
			int avail = chunkLength - chunkPos;
			if (avail == 0)
				fillChunkBuffer();
			avail = chunkLength - chunkPos;
			if (avail == 0)
				return -1;

			int toCopy = avail;
			if (avail > len)
				toCopy = len;
			System.arraycopy(chunkBuffer, chunkPos, b, off, toCopy);
			chunkPos += toCopy;
			return toCopy;
		} else {
			return super.read(b, off, len);
		}
	}

	// -------------------------------------------------------- Private Methods

	/**
	 * 填充块缓冲区。
	 */
	private synchronized boolean fillChunkBuffer() throws IOException {
		chunkPos = 0;
		
		try {
			String numberValue = readLineFromStream();
			if (numberValue != null)
				numberValue = numberValue.trim();
			chunkLength = Integer.parseInt(numberValue, 16);
		} catch (NumberFormatException e) {
			// 严重错误，无法解析块的长度
			chunkLength = 0;
			chunk = false;
			close();
			return false;
		}

		if (chunkLength == 0) {
			// Skipping trailing headers, if any
			String trailingLine = readLineFromStream();
			while (!trailingLine.equals(""))
				trailingLine = readLineFromStream();
			endChunk = true;
			return false;
			// TODO : Should the stream be automatically closed ?
		} else {
			if ((chunkBuffer == null) || (chunkLength > chunkBuffer.length))
				chunkBuffer = new byte[chunkLength];

			// 现在将整个块读入缓冲区

			int nbRead = 0;
			int currentRead = 0;

			while (nbRead < chunkLength) {
				try {
					currentRead = stream.read(chunkBuffer, nbRead, chunkLength - nbRead);
				} catch (Throwable t) {
					t.printStackTrace();
					throw new IOException();
				}
				if (currentRead < 0) {
					throw new IOException(sm.getString("requestStream.read.error"));
				}
				nbRead += currentRead;
			}

			// 忽略 CRLF
			String blank = readLineFromStream();
		}

		return true;
	}

	/**
	 * Reads the input stream, one line at a time. Reads bytes into an array,
	 * until it reads a certain number of bytes or reaches a newline character,
	 * which it reads into the array as well.
	 *
	 * @param input Input stream on which the bytes are read
	 * @return The line that was read, or <code>null</code> if end-of-file
	 *  was encountered
	 * @exception IOException   if an input or output exception has occurred
	 */
	private String readLineFromStream() throws IOException {
		StringBuffer sb = new StringBuffer();
		while (true) {
			int ch = super.read();
			if (ch < 0) {
				if (sb.length() == 0) {
					return null;
				} else {
					break;
				}
			} else if (ch == '\r') {
				continue;
			} else if (ch == '\n') {
				break;
			}
			sb.append((char) ch);
		}
		return sb.toString();
	}

}
