package de.kriegel.studip.client.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import de.kriegel.studip.client.auth.Credentials;

/**
 * HttpClient used to communicate via HTTP Requests in a simple way
 * 
 * @author Josef
 *
 */
public class BasicHttpClient {

	private static final Logger log = LogManager.getLogger(BasicHttpClient.class);

	private final String USER_AGENT = "Mozilla/5.0";

	private URI baseUri;
	private Credentials credentials;

	HttpClientBuilder clientBuilder;

	/**
	 * 
	 * @param baseUri
	 * @param credentials
	 */
	public BasicHttpClient(URI baseUri, Credentials credentials) {
		assert baseUri != null;
		assert credentials != null;

		this.baseUri = baseUri;
		this.credentials = credentials;

		configureHttpClientBuilder();
	}

	/**
	 * 
	 */
	private void configureHttpClientBuilder() {
		clientBuilder = HttpClientBuilder.create();
		clientBuilder.setConnectionTimeToLive(2000, TimeUnit.MILLISECONDS);
		clientBuilder.setUserAgent(USER_AGENT);

		CookieStore cookieStore = new BasicCookieStore();
		clientBuilder.setDefaultCookieStore(cookieStore);

		List<Header> defaultHeaders = new ArrayList<>();

		// add basic authorization header
		String toEncode = credentials.getUsername() + ":" + credentials.getPassword();
		String b64AuthEncoded = Base64.getEncoder().encodeToString(toEncode.getBytes());
		defaultHeaders.add(new BasicHeader(HttpHeaders.AUTHORIZATION, "Basic " + b64AuthEncoded));

		clientBuilder.setDefaultHeaders(defaultHeaders);
	}

	/**
	 * 
	 * @return
	 */
	private CloseableHttpClient getHttpClient() {
		assert clientBuilder != null;

		return clientBuilder.build();
	}

	public static String getResponseBody(HttpResponse response) {
		assert response != null;

		String responseBody;
		try {
			HttpEntity entity = response.getEntity();

			responseBody = EntityUtils.toString(entity);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}

		return responseBody;
	}

	/**
	 * 
	 * @param subpath
	 * @return
	 * @throws ClientProtocolException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public HttpResponse get(String subpath) throws ClientProtocolException, URISyntaxException, IOException {
		return get(new URI(subpath));
	}

	/**
	 * 
	 * @param subpath
	 * @return
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public HttpResponse get(URI subpath)
			throws URISyntaxException, ClientProtocolException, UnknownHostException, IOException {
		assert subpath != null;

		CloseableHttpClient client = getHttpClient();

		URI requestUri = new URI(baseUri.toString() + subpath.toString());
		log.debug("GET " + requestUri);
		HttpGet get = new HttpGet(requestUri);

		HttpResponse response = client.execute(get);

		// client.close();

		return response;
	}

	/**
	 * 
	 * @param subpath
	 * @param params
	 * @return
	 * @throws ClientProtocolException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public HttpResponse postJson(String subpath, JSONObject params)
			throws ClientProtocolException, URISyntaxException, IOException {
		return postJson(new URI(subpath), params);
	}

	/**
	 * 
	 * @param subpath
	 * @param params
	 * @return
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public HttpResponse postJson(URI subpath, JSONObject params)
			throws URISyntaxException, ClientProtocolException, IOException {
		assert subpath != null;

		HttpClient httpClient = getHttpClient();

		URI requestUri = new URI(baseUri.toString() + subpath.toString());
		log.debug("POST " + requestUri);
		HttpPost post = new HttpPost(requestUri);

		if (params != null && params.size() != 0) {
			post.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

			StringEntity entity = new StringEntity(params.toString());
			post.setEntity(entity);
		}

		HttpResponse response = httpClient.execute(post);

		// try {
		// StringWriter writer = new StringWriter();
		// IOUtils.copy(new InputStreamReader(response.getEntity().getContent(),
		// "utf-8"), writer);
		// String responseContent = writer.toString();
		// log.debug(responseContent);
		// } catch (UnsupportedOperationException | IOException e) {
		// log.error(e);
		// }

		return response;
	}

	/**
	 * 
	 * @param subpath
	 * @param urlEncodedMap
	 * @return
	 * @throws ClientProtocolException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public HttpResponse postURLEncoded(String subpath, Map<String, String> urlEncodedMap)
			throws ClientProtocolException, URISyntaxException, IOException {
		return postURLEncoded(new URI(subpath), urlEncodedMap);
	}

	/**
	 * 
	 * @param subpath
	 * @param urlEncodedMap
	 * @return
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public HttpResponse postURLEncoded(URI subpath, Map<String, String> urlEncodedMap)
			throws URISyntaxException, ClientProtocolException, IOException {
		assert subpath != null;

		HttpClient httpClient = getHttpClient();

		URI requestUri = new URI(baseUri.toString() + subpath.toString());
		HttpPost post = new HttpPost(requestUri);

		if (urlEncodedMap != null && !urlEncodedMap.isEmpty()) {
			post.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			urlEncodedMap.entrySet().stream().forEach(e -> {
				urlParameters.add(new BasicNameValuePair(e.getKey(), e.getValue()));
			});

			post.setEntity(new UrlEncodedFormEntity(urlParameters));
		}

		HttpResponse response = httpClient.execute(post);

		// try {
		// StringWriter writer = new StringWriter();
		// IOUtils.copy(new InputStreamReader(response.getEntity().getContent(),
		// "utf-8"), writer);
		// String responseContent = writer.toString();
		// log.debug(responseContent);
		// } catch (UnsupportedOperationException | IOException e) {
		// log.error(e);
		// }

		return response;
	}

}