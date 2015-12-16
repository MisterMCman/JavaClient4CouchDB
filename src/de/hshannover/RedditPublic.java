package de.hshannover;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

public class RedditPublic {

	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	public static final String BaseDomain = "http://www.reddit.com";

	public static JSONObject getObject(final String surl) throws IOException {
		GenericUrl url = new GenericUrl(surl);

		HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();

		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse response = request.execute();

		JSONObject jo = null;

		try {
			if (response.isSuccessStatusCode()) {
				String json = response.parseAsString();

				// Parse with org.json
				JSONTokener tokener = null;
				tokener = new JSONTokener(json);
				jo = new JSONObject(tokener);
			}
		} finally {
			response.disconnect();
		}

		return jo;
	}

	public static JSONArray getArray(final String surl) throws IOException {
		GenericUrl url = new GenericUrl(surl);

		HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();

		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse response = request.execute();

		JSONArray ja = null;

		try {
			if (response.isSuccessStatusCode()) {
				String json = response.parseAsString();

				// Parse with org.json
				JSONTokener tokener = null;
				tokener = new JSONTokener(json);
				ja = new JSONArray(tokener);
			}
		} finally {
			response.disconnect();
		}

		return ja;
	}

}
