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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

/**
 * BrowserMobHttpClient integration test
 * 
 * @author Benjamin Hawkes-Lewis <contact@benjaminhawkeslewis.com>
 */
public class BrowserMobHttpClientTest {
	
	/**
	 * Jetty server to make requests against 
	 */
	private static Server server;
	
	/**
	 * Port accepting requests for the Jetty server
	 */
	private static int port;
	
	/**
	 * Hello handler
	 * 
	 * Jetty Handler returning HTML Hello World to all requests.
	 * 
	 * @author Benjamin Hawkes-Lewis <contact@benjaminhawkeslewis.com>
	 *
	 */
	public static class HelloHandler extends AbstractHandler
	{
	    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) 
	        throws IOException, ServletException
	    {
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
	 * @author Benjamin Hawkes-Lewis <contact@benjaminhawkeslewis.com>
	 *
	 */
	public static class PixelHandler extends AbstractHandler
	{
	    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) 
	        throws IOException, ServletException
	    {
	        response.setContentType("image/gif");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	        response.getOutputStream().write(
	        		Base64.decodeBase64("R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw=="));
	    }
	}
	
	/**
	 * Return a free port
	 * 
	 * Hat tip:
	 * @throws IOException
	 * @throws InterruptedException 
	 * @see http://chaoticjava.com/posts/retrieving-a-free-port-for-socket-binding/
	 */
	public static int getFreePort()
            throws IOException, InterruptedException {
		ServerSocket server = new ServerSocket(0);
		int port = server.getLocalPort();
		
		// Wait until port is closed
		synchronized(server){
			server.close();
			while (!server.isClosed()){
		        server.wait();
		    }
		}
		
		return port;
	}
	
	/**
	 * Set up a Jetty server
	 * 
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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

	/**
	 * Shut down our Jetty server
	 * 
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
		// Wait until Jetty is stopped
		synchronized(server){
			server.stop();
			while (!server.isStopped()){
		        server.wait();
		    }
		}
	}
	
	@Test
	public void canCreateGetRequest() {
		BrowserMobHttpClient client = new BrowserMobHttpClient();
		BrowserMobHttpRequest request = client.newGet("http://127.0.0.1:" + port + "/html");
		assertNotNull(request);
	}
	
	@Test
	public void canExecuteGetRequest() {
		BrowserMobHttpClient client = new BrowserMobHttpClient();
		BrowserMobHttpRequest request = client.newGet("http://127.0.0.1:" + port + "/html");
		BrowserMobHttpResponse response = client.execute(request);
		assertNotNull(response);		
	}

	@Test
	public void defaultsToNotCapturingContentOnGet() {
		BrowserMobHttpClient client = new BrowserMobHttpClient();
		BrowserMobHttpRequest request = client.newGet("http://127.0.0.1:" + port + "/html");
		BrowserMobHttpResponse response = client.execute(request);
		
		assertNull("HAR response content text should be null when not capturing", response.getEntry().getResponse().getContent().getText());
	}
	
	@Test
	public void canCaptureContentOnGet() {
		BrowserMobHttpClient client = new BrowserMobHttpClient();
		client.setCaptureContent(true);
		BrowserMobHttpRequest request = client.newGet("http://127.0.0.1:" + port + "/html");
		BrowserMobHttpResponse response = client.execute(request);
		
		assertTrue("For a text subtype response, response body text should be the same as the HAR entry's text field", response.getBody().equals(response.getEntry().getResponse().getContent().getText()));
		assertTrue("HAR entry's text field should match HTML of response.", response.getEntry().getResponse().getContent().getText().equals("<h1>Hello World</h1>"));
	}
	
	@Test
	public void canCaptureBinaryContentOnGet() {
		BrowserMobHttpClient client = new BrowserMobHttpClient();
		client.setCaptureContent(true);
		BrowserMobHttpRequest request = client.newGet("http://127.0.0.1:" + port + "/pixel");
		BrowserMobHttpResponse response = client.execute(request);
		assertTrue("HAR entry's text field should be base64", Base64.isBase64(response.getEntry().getResponse().getContent().getText()));
		assertTrue("HAR entry's text field should be a base64 encoding of the pixel", response.getEntry().getResponse().getContent().getText().equals("R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw=="));
	}

}
