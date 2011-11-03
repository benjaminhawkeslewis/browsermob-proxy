package org.browsermob.core.har;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * HarContent unit test
 * 
 * @author Benjamin Hawkes-Lewis <contact@benjaminhawkeslewis.com>
 */
public class HarContentTest {

	/**
	 * Can we set size?
	 */
	@Test
	public void canSetSize() {
		HarContent sut = new HarContent();
		assertEquals(sut.getSize(), 0);
		sut.setSize(10);
		assertEquals(sut.getSize(), 10);
	}

	/**
	 * Can we set compression?
	 */
	@Test
	public void canSetCompression() {
		HarContent sut = new HarContent();
		assertEquals(sut.getCompression(), 0);
		sut.setCompression(10);
		assertEquals(sut.getCompression(), 10);
	}

	/**
	 * Can we set MIME type?
	 */
	@Test
	public void canSetMimeType() {
		HarContent sut = new HarContent();
		assertTrue(sut.getMimeType().equals(""));
		sut.setMimeType("text/html; charset=utf-8");
		assertTrue(sut.getMimeType().equals("text/html; charset=utf-8"));
	}

	/**
	 * Can we set text?
	 */
	@Test
	public void canSetText() {
		HarContent sut = new HarContent();
		// Optional field, so starts null.
		assertNull(sut.getText());
		sut.setText("example");
		assertTrue(sut.getText().equals("example"));
	}

	/**
	 * Can we set encoding?
	 */
	@Test
	public void canSetEncoding() {
		HarContent sut = new HarContent();
		// Optional field, so starts null.
		assertNull(sut.getEncoding());
		sut.setEncoding("base64");
		assertTrue(sut.getEncoding().equals("base64"));
	}

}
