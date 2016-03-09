package com.solo.search.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;

public class SearchHttpClient extends DefaultHttpClient {

	private static String USER_AGENT = "DDG-Android-%version";

	private HttpGet mRequest;
	private HttpEntity mEntity;
	public HttpResponse mResponse;
	private HttpPost mPost;
	private String mStrResult;
	private int mStatusCode;

	public SearchHttpClient(Context context, ClientConnectionManager cm, HttpParams httpParams) {
		setParams(httpParams);
		setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
		HttpProtocolParams.setUserAgent(httpParams, USER_AGENT);

		addRequestInterceptor(new HttpRequestInterceptor() {

			public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
				if (!request.containsHeader("Accept-Encoding")) {
					request.addHeader("Accept-Encoding", "gzip");
				}
			}

		});

		this.addResponseInterceptor(new HttpResponseInterceptor() {

			public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
				HttpEntity entity = response.getEntity();
				Header ceheader = entity.getContentEncoding();
				if (ceheader != null) {
					HeaderElement[] codecs = ceheader.getElements();
					for (int i = 0; i < codecs.length; i++) {
						if (codecs[i].getName().equalsIgnoreCase("gzip")) {
							response.setEntity(new GzipDecompressingEntity(response.getEntity()));
							return;
						}
					}
				}
			}

		});

	}

	static class GzipDecompressingEntity extends HttpEntityWrapper {

		public GzipDecompressingEntity(final HttpEntity entity) {
			super(entity);
		}

		@Override
		public InputStream getContent() throws IOException, IllegalStateException {

			// the wrapped entity's getContent() decides about repeatability
			InputStream wrappedin = wrappedEntity.getContent();

			return new GZIPInputStream(wrappedin);
		}

		@Override
		public long getContentLength() {
			// length of ungzipped content is not known
			return -1;
		}

	}

	public HttpEntity doPost(String url, List<NameValuePair> params) throws SearchHttpException {
		try {
			mPost = new HttpPost(url);
			mPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

			mResponse = execute(mPost);
			mStatusCode = mResponse.getStatusLine().getStatusCode();
			if (mStatusCode != HttpStatus.SC_OK) {
				throw new SearchHttpException(mStatusCode);
			}

			return mResponse.getEntity();
		} catch (SearchHttpException ddgEx) {
			throw ddgEx;
		} catch (Exception ex) {
			// unsupportencoding, clientprotocol, io
			throw new SearchHttpException(ex.getMessage());
		}
	}

	public String doPostString(String url, List<NameValuePair> params) throws SearchHttpException {
		try {
			HttpEntity entity = doPost(url, params);
			mStrResult = EntityUtils.toString(entity);
			// EntityUtils.consume(entity);
			return mStrResult;
		} catch (SearchHttpException ddgEx) {
			throw ddgEx;
		} catch (IOException ex) {
			// io
			throw new SearchHttpException(ex.getMessage());
		}
	}

	public HttpEntity doGet(String url) throws SearchHttpException {
		try {
			mRequest = new HttpGet(url);
			mResponse = execute(mRequest);
			mStatusCode = mResponse.getStatusLine().getStatusCode();
			if (mStatusCode != HttpStatus.SC_OK) {
				throw new SearchHttpException(mStatusCode);
			}

			mEntity = mResponse.getEntity();
			return mEntity;
		} catch (SearchHttpException ddgEx) {
			throw ddgEx;
		} catch (Exception ex) {
			// clientprotocol, io
			throw new SearchHttpException(ex.getMessage());
		}
	}

	public String doGetString(String url) throws SearchHttpException {
		try {
			mEntity = doGet(url);
			mStrResult = EntityUtils.toString(mEntity);
			// EntityUtils.consume(entity);
			return mStrResult;
		} catch (SearchHttpException ddgEx) {
			throw ddgEx;
		} catch (IOException ex) {
			// io
			throw new SearchHttpException(ex.getMessage());
		}
	}

	public HttpEntity doGet(String url, List<NameValuePair> params, boolean raw) throws SearchHttpException {
		try {
			String paramString = "";
			int paramSize = params.size();
			NameValuePair p;

			if (raw) {
				for (int i = 0; i < paramSize; i++) {
					p = params.get(i);
					paramString += p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
					if (i != paramSize - 1)
						paramString += "&";
				}
			} else {
				// default
				for (int i = 0; i < paramSize; i++) {
					p = params.get(i);
					paramString += p.getName() + "=" + p.getValue();
					if (i != paramSize - 1)
						paramString += "&";
				}
			}

			mRequest = new HttpGet(url + "?" + paramString);
			// Log.v("REQ",url + "?" + paramString);

			mResponse = execute(mRequest);
			mStatusCode = mResponse.getStatusLine().getStatusCode();
			if (mStatusCode != HttpStatus.SC_OK) {
				throw new SearchHttpException(mStatusCode);
			}

			return mResponse.getEntity();
		} catch (SearchHttpException ddgEx) {
			throw ddgEx;
		} catch (Exception ex) {
			// clientprotocol, io
			throw new SearchHttpException(ex.getMessage());
		}
	}

	public String doGetString(String url, List<NameValuePair> params) throws SearchHttpException {
		try {
			mEntity = doGet(url, params, false);
			mStrResult = EntityUtils.toString(mEntity);
			// EntityUtils.consume(entity);
			return mStrResult;
		} catch (SearchHttpException ddgEx) {
			throw ddgEx;
		} catch (IOException ex) {
			// io
			throw new SearchHttpException(ex.getMessage());
		}
	}

	public String rawGet(String url, List<NameValuePair> params) throws SearchHttpException {
		try {
			mEntity = doGet(url, params, true);
			mStrResult = EntityUtils.toString(mEntity);
			// EntityUtils.consume(entity);
			return mStrResult;
		} catch (SearchHttpException ddgEx) {
			throw ddgEx;
		} catch (IOException ex) {
			// io
			throw new SearchHttpException(ex.getMessage());
		}
	}

}
