package org.browsermob.proxy.http;

import org.apache.commons.codec.binary.Base64;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.ServerSocket;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.browsermob.proxy.http.BrowserMobHttpClient;
import org.browsermob.proxy.http.BrowserMobHttpRequest;
import org.browsermob.proxy.http.BrowserMobHttpResponse;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

/**
 * BrowserMobHttpClient integration test.
 * 
 * @author Benjamin Hawkes-Lewis <contact@benjaminhawkeslewis.com>
 */
public class BrowserMobHttpClientIT {

	/**
	 * Jetty server to make requests against.
	 */
	private static Server server;

	/**
	 * Port accepting requests for the Jetty server.
	 */
	private static int port;

	/**
	 * External resource wrapping a embedded Jetty server listening on a free
	 * port on localhost and returning HTML
	 * <code>&lt;h1&gt;Hello World&lt;/h1&gt;</code> on <code>/html</code> and a
	 * 1x1 GIF on <code>/pixel</code>.
	 */
	@ClassRule
	public static ExternalResource embeddedServer = new ExternalResource() {

		/**
		 * Hello handler
		 * 
		 * Jetty Handler returning HTML Hello World to all requests.
		 */
		final class HelloHandler extends AbstractHandler {
			public void handle(String target, Request baseRequest,
					HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
				response.setContentType("text/html;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getWriter().print("<h1>Hello World</h1>");
			}
		}

		/**
		 * Pixel handler
		 * 
		 * Jetty Handler returning 1x1 GIF to all requests.
		 * 
		 */
		final class PixelHandler extends AbstractHandler {
			public void handle(String target, Request baseRequest,
					HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
				response.setContentType("image/gif");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getOutputStream()
						.write(Base64
								.decodeBase64("R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw=="));
			}
		}

		/**
		 * Return a free port
		 * 
		 * Adapted from Aviad Ben Dov's recipe <a href=
		 * "http://chaoticjava.com/posts/retrieving-a-free-port-for-socket-binding/"
		 * >Retrieving a Free Port for Socket Binding</a> (Chaotic Java,
		 * 2010-06-07)
		 * 
		 * @throws IOException
		 *             If there's an error opening a exploratory socket.
		 * @throws InterruptedException
		 *             If the thread was interrupted while closing the
		 *             exploratory socket.
		 * @return A free port
		 */
		private int getFreePort() throws IOException, InterruptedException {
			ServerSocket server = new ServerSocket(0);
			int port = server.getLocalPort();

			// Wait until port is closed
			synchronized (server) {
				server.close();
				while (!server.isClosed()) {
					server.wait();
				}
			}

			return port;
		}

		@Override
		protected void before() throws Throwable {
			// Rather than hardcoding a port, we get a free port and use that.
			port = getFreePort();
			server = new Server(port);
			ContextHandlerCollection contexts = new ContextHandlerCollection();

			ContextHandler htmlContext = new ContextHandler();
			htmlContext.setContextPath("/html");
			htmlContext.setHandler(new HelloHandler());

			ContextHandler pixelContext = new ContextHandler();
			pixelContext.setContextPath("/pixel");
			pixelContext.setHandler(new PixelHandler());

			contexts.setHandlers(new Handler[] { htmlContext, pixelContext });

			server.setHandler(contexts);
			server.start();
		}

		@Override
		protected void after() {

			boolean interrupted = false;

			synchronized (server) {

				// Stop Jetty

				try {
					server.stop();
				} catch (Exception e) {
					System.err.println("Failed to stop embedded Jetty server.");
					e.printStackTrace();
				}

				/**
				 * Wait until Jetty has stopped.
				 * 
				 * Interruption handling based on Goetz, B., et al. (2006), Java
				 * Concurrency in Practice, §7.1.1.
				 */
				try {
					while (!server.isStopped()) {
						try {
							server.wait();
						} catch (InterruptedException e) {
							interrupted = true;
							// We still want to wait for the server to stop, so
							// fall through and retry.
						}
					}
				} finally {

					// Do not swallow the interruption status.
					if (interrupted) {
						Thread.currentThread().interrupt();
					}

				}

			}

		}

	};

	/**
	 * Can the client prepare a GET request?
	 */
	@Test
	public void canPrepareGet() {
		BrowserMobHttpClient client = new BrowserMobHttpClient();
		BrowserMobHttpRequest request = client.newGet("http://127.0.0.1:"
				+ port + "/html");
		assertNotNull(request);
	}

	/**
	 * Having prepared a GET request, can the client execute it?
	 */
	@Test
	public void canExecuteOwnGet() {
		BrowserMobHttpClient client = new BrowserMobHttpClient();
		BrowserMobHttpRequest request = client.newGet("http://127.0.0.1:"
				+ port + "/html");
		BrowserMobHttpResponse response = client.execute(request);
		assertNotNull(response);
	}

	/**
	 * Do we default to not capturing the response bodies in the HAR log?
	 */
	@Test
	public void defaultToNotCapturingResponseBodyInHarLog() {
		BrowserMobHttpClient client = new BrowserMobHttpClient();
		BrowserMobHttpRequest request = client.newGet("http://127.0.0.1:"
				+ port + "/html");
		BrowserMobHttpResponse response = client.execute(request);

		assertNull(
				"HAR response content text should be null when not capturing",
				response.getEntry().getResponse().getContent().getText());
	}

	/**
	 * Can we opt-in to capturing response bodies in the HAR log?
	 */
	@Test
	public void canCaptureResponseBodyInHarLog() {
		BrowserMobHttpClient client = new BrowserMobHttpClient();
		client.setCaptureContent(true);
		BrowserMobHttpRequest request = client.newGet("http://127.0.0.1:"
				+ port + "/html?foobar=baz");
		BrowserMobHttpResponse response = client.execute(request);

		assertTrue(
				"For a text subtype response, response body text should be the same as the HAR entry's text field",
				response.getBody().equals(
						response.getEntry().getResponse().getContent()
								.getText()));
		assertTrue("HAR entry's text field should match HTML of response.",
				response.getEntry().getResponse().getContent().getText()
						.equals("<h1>Hello World</h1>"));
	}

	/**
	 * Do we encode binary response bodies as base64 in the HAR log?
	 */
	@Test
	public void base64EncodeBinaryResponseBodyInHarLog() {
		BrowserMobHttpClient client = new BrowserMobHttpClient();
		client.setCaptureContent(true);
		BrowserMobHttpRequest request = client.newGet("http://127.0.0.1:"
				+ port + "/pixel");
		BrowserMobHttpResponse response = client.execute(request);

		assertTrue(
				"HAR entry's text field should be base64",
				Base64.isBase64(response.getEntry().getResponse().getContent()
						.getText()));
		assertTrue(
				"HAR entry's text field should be a base64 encoding of the pixel",
				response.getEntry()
						.getResponse()
						.getContent()
						.getText()
						.equals("R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw=="));
	}

}
