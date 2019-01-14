package de.kriegel.studip.client.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Authenticator;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

/**
 * HttpClient used to communicate via HTTP Requests in a simple way
 *
 * @author Josef
 */
public class BasicHttpClient {

	private static final Logger log = LoggerFactory.getLogger(BasicHttpClient.class);

	private URI baseUri;
	private de.kriegel.studip.client.auth.Credentials credentials;
	private ExecutorService executorService;

	Builder clientBuilder;

	/**
	 * @param baseUri
	 * @param credentials
	 */
	public BasicHttpClient(URI baseUri, de.kriegel.studip.client.auth.Credentials credentials,
			ExecutorService executorService) {
		assert baseUri != null;
		assert credentials != null;
		assert executorService != null;

		this.baseUri = baseUri;
		this.credentials = credentials;
		this.executorService = executorService;

		configureHttpClientBuilder();
	}

	/**
	 *
	 */
	private void configureHttpClientBuilder() {
		clientBuilder = new Builder();
		clientBuilder.connectTimeout(2000, TimeUnit.MILLISECONDS);

		clientBuilder.cookieJar(new CookieJar() {

			private List<Cookie> cookies;

			@Override
			public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
				this.cookies = cookies;
			}

			@Override
			public List<Cookie> loadForRequest(HttpUrl url) {
				if (cookies != null)
					return cookies;
				return new ArrayList<Cookie>();

			}

		});

		clientBuilder.authenticator(new Authenticator() {
			@Override
			public Request authenticate(Route route, Response response) throws IOException {
				if (response.request().header("Authorization") != null) {
					return null; // Give up, we've already attempted to authenticate.
				}

				log.debug("Authenticating for response: " + response);
				log.debug("Challenges: " + response.challenges());

				String credential = Credentials.basic(credentials.getUsername(), credentials.getPassword());
				return response.request().newBuilder().header("Authorization", credential).build();
			}
		});

	}

	/**
	 * @return
	 */
	private OkHttpClient getHttpClient() {
		assert clientBuilder != null;

		return clientBuilder.build();
	}

	public Future<String> getResponseBody(Response response) {
		assert response != null;

		Future<String> futureResponseBody = null;

		try {
			futureResponseBody = executorService.submit(new Callable<String>() {
				@Override
				public String call() throws Exception {
					return response.body().string();
				}
			});
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		return futureResponseBody;
	}

	/**
	 * @param subpath
	 * @return
	 * @throws ClientProtocolException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public Future<Response> get(String subpath) throws URISyntaxException, IOException {
		return get(new URI(subpath));
	}

	/**
	 * @param subpath
	 * @return
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public Future<Response> get(URI subpath) throws URISyntaxException, UnknownHostException, IOException {
		assert subpath != null;

		OkHttpClient httpClient = getHttpClient();

		Request request = new Request.Builder().url(baseUri.toString() + subpath.toString()).build();

		Future<Response> futureGetResponse = null;

		try {
			futureGetResponse = executorService.submit(new Callable<Response>() {
				@Override
				public Response call() throws Exception {
					return httpClient.newCall(request).execute();
				}
			});
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		return futureGetResponse;
	}

	/**
	 * @param subpath
	 * @param params
	 * @return
	 * @throws ClientProtocolException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public Future<Response> postJson(String subpath, JSONObject params) throws URISyntaxException, IOException {
		return postJson(new URI(subpath), params);
	}

	/**
	 * @param subpath
	 * @param params
	 * @return
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public Future<Response> postJson(URI subpath, JSONObject params) throws URISyntaxException, IOException {
		assert subpath != null;

		OkHttpClient httpClient = getHttpClient();

		log.debug("POST " + baseUri.toString() + subpath.toString());
		Request request = new Request.Builder().url(baseUri.toString() + subpath.toString())
				.post(RequestBody.create(MediaType.parse("application/json"), params.toString())).build();

		Future<Response> futurePostResponse = null;

		try {
			futurePostResponse = executorService.submit(new Callable<Response>() {
				@Override
				public Response call() throws Exception {
					return httpClient.newCall(request).execute();
				}
			});
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		return futurePostResponse;
	}

	/**
	 * @param subpath
	 * @param urlEncodedMap
	 * @return
	 * @throws ClientProtocolException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public Future<Response> postURLEncoded(String subpath, Map<String, String> urlEncodedMap)
			throws URISyntaxException, IOException {
		return postURLEncoded(new URI(subpath), urlEncodedMap);
	}

	/**
	 * @param subpath
	 * @param urlEncodedMap
	 * @return
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public Future<Response> postURLEncoded(URI subpath, Map<String, String> urlEncodedMap)
			throws URISyntaxException, IOException {
		assert subpath != null;

		OkHttpClient httpClient = getHttpClient();
		
		log.debug("POST " + baseUri.toString() + subpath.toString());
		Request.Builder requestBuilder = new Request.Builder().url(baseUri.toString() + subpath.toString());
		
		if (urlEncodedMap != null && !urlEncodedMap.isEmpty()) {
			FormBody.Builder formBody = new FormBody.Builder();
			
			urlEncodedMap.entrySet().stream().forEach(e -> {
				formBody.add(e.getKey(), e.getValue());
			});

			requestBuilder.post(formBody.build());
		}

		Request request = requestBuilder.build();
		
		Future<Response> futurePostResponse = null;

		try {
			futurePostResponse = executorService.submit(new Callable<Response>() {
				@Override
				public Response call() throws Exception {
					return httpClient.newCall(request).execute();
				}
			});
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		return futurePostResponse;
	}

}