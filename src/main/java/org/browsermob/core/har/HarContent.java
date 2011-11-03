package org.browsermob.core.har;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Represents the response content.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class HarContent {
	private long size;
	private long compression;
	private String mimeType = "";

	/**
	 * Response body sent from the server or loaded from the cache. This field
	 * is populated with textual content only. The text field is either HTTP
	 * decoded text or a encoded (e.g. "base64") representation of the response
	 * body. Left out if information is unavailable.
	 */
	private String text;

	/**
	 * Encoding used for text field.
	 */
	private String encoding;

	
	/**
	 * Returns length of the returned content in bytes.
	 * @return Size in bytes
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Set the length of the returned content in bytes.
	 * @param size Size in bytes
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * Get the number of bytes saved by compressing the response, if compressed.
	 * @return Saving in bytes
	 */
	public long getCompression() {
		return compression;
	}

	/**
	 * Set the number of bytes saved by compressing the response, if compressed.
	 * @param compression Saving in bytes
	 */
	public void setCompression(long compression) {
		this.compression = compression;
	}

	/**
	 * Get the media type of the response body, including parameters if
	 * available.
	 * @return Media type, e.g "text/html; charset=utf-8"
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Set the media type of the response body, including parameters if
	 * available.
	 * @param mimeType Media type, e.g "text/html; charset=utf-8"
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Returns the text of the response body.
	 * 
	 * Note this may be re-encoded (check {@link getEncoding} for the encoding).
	 * 
	 * @return The text of the response body.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Set the text of the response body.
	 * @param text Text of the response body, potentially re-encoded.
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Get the re-encoding of the text of the response body (e.g. "base64").
	 * 
	 * Will be null if the text was HTTP decoded (decompressed and unchunked),
	 * than transcoded from its original character set into UTF-8.
	 * 
	 * @return Encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Set the encoding of text (e.g. "base64")
	 * 
	 * @param encoding
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

}
