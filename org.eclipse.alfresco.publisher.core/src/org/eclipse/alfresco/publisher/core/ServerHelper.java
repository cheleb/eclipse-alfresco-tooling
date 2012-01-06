package org.eclipse.alfresco.publisher.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHelper {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ServerHelper.class);

	public static boolean reload(String url, String login, String password,
			IProgressMonitor monitor) {
		URL urlToParse;
		try {
			urlToParse = new URL(url);
		} catch (MalformedURLException e1) {
			return false;
		}

		HttpHost targetHost = new HttpHost(urlToParse.getHost(),
				urlToParse.getPort(), urlToParse.getProtocol());

		DefaultHttpClient httpclient = new DefaultHttpClient();

		httpclient.getCredentialsProvider().setCredentials(
				new AuthScope(targetHost.getHostName(), targetHost.getPort()),
				new UsernamePasswordCredentials(login, password));

		// Create AuthCache instance
		AuthCache authCache = new BasicAuthCache();
		// Generate BASIC scheme object and add it to the local auth cache
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(targetHost, basicAuth);

		// Add AuthCache to the execution context
		BasicHttpContext localcontext = new BasicHttpContext();
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("reset", "on"));
		formparams.add(new BasicNameValuePair("submit", "Refresh Web Scripts"));
		HttpPost httpPost = new HttpPost(url);

		monitor.worked(1);

		BufferedReader bufferedReader = null;
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams,
					"UTF-8");
			httpPost.setEntity(entity);
			final HttpResponse httpResponse = httpclient.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				monitor.worked(1);
				return true;
			}

			InputStream content = httpResponse.getEntity().getContent();
			bufferedReader = new BufferedReader(new InputStreamReader(content));
			String line;
			final StringBuilder builder = new StringBuilder();
			while ((line = bufferedReader.readLine()) != null) {
				builder.append(line).append("\n");
			}
			bufferedReader.close();

			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {

					MessageDialog.openError(null, "Not 200 response: "
							+ httpResponse.getStatusLine(), builder.toString());
				}
			});

		} catch (final ClientProtocolException e) {
			LOGGER.error("", e);
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {

					MessageDialog.openError(null, "Client protocol error",
							e.getLocalizedMessage());
				}
			});
		} catch (final IOException e) {
			LOGGER.error("", e);
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {

					MessageDialog.openError(null, "IOException",
							e.getLocalizedMessage());
				}
			});
		} finally {
			try {
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (IOException e) {
				LOGGER.error("", e);
			}
		}
		return false;
	}

}
