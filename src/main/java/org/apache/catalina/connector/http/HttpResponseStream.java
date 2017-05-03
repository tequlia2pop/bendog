package org.apache.catalina.connector.http;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.connector.ResponseStream;

/**
 * HTTP/1.1 连接器的响应流。
 * 如果使用了 HTTP/1.1，并且没有正确设置 Content-Length，此流将自动分块来应答。
 * 
 * @author tequlia2pop
 * @deprecated
 */
public final class HttpResponseStream extends ResponseStream {

	// ----------------------------------------------------------- 

	private static final int MAX_CHUNK_SIZE = 4096;

	private static final String CRLF = "\r\n";

	// ----------------------------------------------------- Instance Variables

	/**
	 * True if chunking is allowed.
	 */
	private boolean useChunking;

	/**
	 * True if printing a chunk.
	 */
	private boolean writingChunk;

	/**
	 * True if no content should be written.
	 */
	private boolean writeContent;

	// ----------------------------------------------------------- Constructors

	/**
	 * Construct a servlet output stream associated with the specified Request.
	 *
	 * @param response The associated response
	 */
	public HttpResponseStream(HttpResponseImpl response) {
		super(response);
		checkChunking(response);
		checkHead(response);

	}

	// -------------------------------------------- ServletOutputStream Methods

	@Override
	public void write(int b) throws IOException {
		if (suspended)
			return;

		if (!writeContent)
			return;

		if (useChunking && !writingChunk) {
			writingChunk = true;
			try {
				print("1\r\n");
				super.write(b);
				println();
			} finally {
				writingChunk = false;
			}
		} else {
			super.write(b);
		}

	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (suspended)
			return;

		if (!writeContent)
			return;

		if (useChunking && !writingChunk) {
			if (len > 0) {
				writingChunk = true;
				try {
					println(Integer.toHexString(len));
					super.write(b, off, len);
					println();
				} finally {
					writingChunk = false;
				}
			}
		} else {
			super.write(b, off, len);
		}
	}

	@Override
	public void close() throws IOException {
		if (suspended)
			throw new IOException(sm.getString("responseStream.suspended"));

		if (!writeContent)
			return;

		if (useChunking) {
			// Write the final chunk.
			writingChunk = true;
			try {
				print("0\r\n\r\n");
			} finally {
				writingChunk = false;
			}
		}
		super.close();
	}

	// -------------------------------------------------------- Package Methods

	void checkChunking(HttpResponseImpl response) {
		// 如果已经往流中写入了数据，我们不能改变块的模式
		if (count != 0)
			return;

		// Check the basic cases in which we chunk
		useChunking = (!response.isCommitted() && response.getContentLength() == -1
				&& response.getStatus() != HttpServletResponse.SC_NOT_MODIFIED);

		if (!response.isChunkingAllowed() && useChunking) {
			// 如果我们应该分块，但连接器禁止分块，则关闭连接
			response.setHeader("Connection", "close");
		}

		// Don't chunk is the connection will be closed
		useChunking = (useChunking && !response.isCloseConnection());
		if (useChunking) {
			response.setHeader("Transfer-Encoding", "chunked");
		} else if (response.isChunkingAllowed()) {
			response.removeHeader("Transfer-Encoding", "chunked");
		}
	}

	protected void checkHead(HttpResponseImpl response) {
		HttpServletRequest servletRequest = (HttpServletRequest) response.getRequest();
		if ("HEAD".equals(servletRequest.getMethod())) {
			writeContent = false;
		} else {
			writeContent = true;
		}
	}

}
