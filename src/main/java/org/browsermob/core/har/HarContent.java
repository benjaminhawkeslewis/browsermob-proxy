package org.browsermob.core.har;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class HarContent {
    private long size;
    private Long compression;
    private String mimeType = "";
    
    /**
     * Response body sent from the server or loaded from the cache.
     * This field is populated with textual content only. The text field is
     * either HTTP decoded text or a encoded (e.g. "base64") representation
     * of the response body. Left out if information is unavailable.
     */
    private String text;
    
    /**
     * Encoding used for text field.
     */
    private String encoding;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Long getCompression() {
        return compression;
    }

    public void setCompression(Long compression) {
        this.compression = compression;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
	/**
	 * Get the encoding of text (e.g. "base64")
	 * 
	 * @param encoding
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
