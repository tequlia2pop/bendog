package org.apache.catalina.connector;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * 标准<code>java.io.PrintWriter</code>的包装器，它可以跟踪是否写过任意字符
 * （即使它们仍然被缓冲在 PrintWriter 或任何其他 Writer 中，它使用的是底层 TCP/IP 套接字）。 
 * 对于在 ServletResponse 上的几个调用的语义，这是必需的，
 * 如果输出已经写过了，则需要抛出一个<code>IllegalStateException</code>。
 * 
 * 该类在每次调用 print()、 println() 和 write() 方法时都会自动刷新。
 * 
 * @author tequlia2pop
 * @deprecated
 */
public class ResponseWriter extends PrintWriter {

	// ----------------------------------------------------- Instance Variables

	/**
	 * The response stream to which we are attached.
	 */
	protected ResponseStream stream = null;

	// ------------------------------------------------------------ Constructor

	/**
	 * Construct a new ResponseWriter, wrapping the specified writer and
	 * attached to the specified response.
	 *
	 * @param writer OutputStreamWriter to which we are attached
	 * @param stream ResponseStream to which we are attached
	 */
	public ResponseWriter(OutputStreamWriter writer, ResponseStream stream) {
		super(writer);
		this.stream = stream;
		this.stream.setCommit(false);
	}

	// --------------------------------------------------------- Public Methods

	/**
	 * Flush this stream, and cause the response to be committed.
	 */
	@Override
	public void flush() {
		stream.setCommit(true);
		super.flush();
		stream.setCommit(false);
	}

	/**
	 * Print a boolean value.
	 *
	 * @param b The value to be printed
	 */
	@Override
	public void print(boolean b) {
		super.print(b);
		super.flush();
	}

	/**
	 * Print a character value.
	 *
	 * @param c The value to be printed
	 */
	@Override
	public void print(char c) {
		super.print(c);
		super.flush();
	}

	/**
	 * Print a character array value.
	 *
	 * @param ca The value to be printed
	 */
	@Override
	public void print(char ca[]) {
		super.print(ca);
		super.flush();
	}

	/**
	 * Print a double value.
	 *
	 * @param d The value to be printed
	 */
	@Override
	public void print(double d) {
		super.print(d);
		super.flush();
	}

	/**
	 * Print a float value.
	 *
	 * @param f The value to be printed
	 */
	@Override
	public void print(float f) {
		super.print(f);
		super.flush();
	}

	/**
	 * Print an integer value.
	 *
	 * @param i The value to be printed.
	 */
	@Override
	public void print(int i) {
		super.print(i);
		super.flush();
	}

	/**
	 * Print a long value.
	 *
	 * @param l The value to be printed
	 */
	@Override
	public void print(long l) {
		super.print(l);
		super.flush();
	}

	/**
	 * Print an object value.
	 *
	 * @param o The value to be printed
	 */
	@Override
	public void print(Object o) {
		super.print(o);
		super.flush();
	}

	@Override
	public void print(String s) {
		super.print(s);
		super.flush();
	}

	/**
	 * Terminate the current line by writing the line separator string.
	 */
	@Override
	public void println() {
		super.println();
		super.flush();
	}

	/**
	 * Print a boolean value and terminate the current line.
	 *
	 * @param b The value to be printed
	 */
	@Override
	public void println(boolean b) {
		super.println(b);
		super.flush();
	}

	/**
	 * Print a character value and terminate the current line.
	 *
	 * @param c The value to be printed
	 */
	@Override
	public void println(char c) {
		super.println(c);
		super.flush();
	}

	/**
	 * Print a character array value and terminate the current line.
	 *
	 * @param ca The value to be printed
	 */
	@Override
	public void println(char ca[]) {
		super.println(ca);
		super.flush();
	}

	/**
	 * Print a double value and terminate the current line.
	 *
	 * @param d The value to be printed
	 */
	@Override
	public void println(double d) {
		super.println(d);
		super.flush();
	}

	/**
	 * Print a float value and terminate the current line.
	 *
	 * @param f The value to be printed
	 */
	@Override
	public void println(float f) {
		super.println(f);
		super.flush();
	}

	/**
	 * Print an integer value and terminate the current line.
	 *
	 * @param i The value to be printed.
	 */
	@Override
	public void println(int i) {
		super.println(i);
		super.flush();
	}

	/**
	 * Print a long value and terminate the current line.
	 *
	 * @param l The value to be printed
	 */
	@Override
	public void println(long l) {
		super.println(l);
		super.flush();
	}

	/**
	 * Print an object value and terminate the current line.
	 *
	 * @param o The value to be printed
	 */
	@Override
	public void println(Object o) {
		super.println(o);
		super.flush();
	}

	/**
	 * Print a String value and terminate the current line.
	 *
	 * @param s The value to be printed
	 */
	@Override
	public void println(String s) {
		super.println(s);
		super.flush();
	}

	/**
	 * Write a single character.
	 *
	 * @param c The value to be written
	 */
	// @Override
	public void write(char c) {
		super.write(c);
		super.flush();
	}

	/**
	 * Write an array of characters.
	 *
	 * @param ca The value to be written
	 */
	@Override
	public void write(char ca[]) {
		super.write(ca);
		super.flush();
	}

	@Override
	public void write(char ca[], int off, int len) {
		super.write(ca, off, len);
		super.flush();
	}

	/**
	 * Write a String.
	 *
	 * @param s The value to be written
	 */
	@Override
	public void write(String s) {
		super.write(s);
		super.flush();
	}

	/**
	 * Write a portion of a String.
	 *
	 * @param s The String from which to write
	 * @param off Starting offset
	 * @param len Number of characters to write
	 */
	@Override
	public void write(String s, int off, int len) {
		super.write(s, off, len);
		super.flush();
	}

}
