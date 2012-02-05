package org.eclipse.alfresco.publisher.core.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.eclipse.alfresco.publisher.core.AlfrescoFileUtils;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.alfresco.publisher.core.OperationCanceledException;
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

	public static void stopServer(AlfrescoPreferenceHelper preferences)
			throws IOException {

		if (preferences.isAlfresco()) {
			stopAlfresco(preferences);
		} else {
			stopTomcat(preferences);
		}

		ProcessBuilder processBuilder;
		if (preferences.isAlfresco()) {
			processBuilder = new ProcessBuilder("scripts/ctl.sh", "stop");
		} else {

			processBuilder = new ProcessBuilder(
					"java",
					"-cp",
					"bin/bootstrap.jar:bin/commons-daemon.jar:bin/tomcat-juli.jar",
					"org.apache.catalina.startup.Bootstrap", "stop");
		}
		processBuilder.directory(new File(preferences.getServerPath()));

		Process start = processBuilder.start();
		try {
			int r = start.waitFor();
			LOGGER.info("Stopping "
					+ (preferences.isAlfresco() ? "alfresco" : "share") + ": "
					+ (r == 0 ? "OK" : "ERROR"));
		} catch (InterruptedException e) {
			throw new OperationCanceledException(e.getLocalizedMessage(), e);
		}
	}

	private static void stopTomcat(AlfrescoPreferenceHelper preferences)
			throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp",
				"bin/bootstrap.jar:bin/commons-daemon.jar:bin/tomcat-juli.jar",
				"org.apache.catalina.startup.Bootstrap", "stop");

		processBuilder.directory(new File(preferences.getServerPath()));

		Process start = processBuilder.start();
		try {
			int r = start.waitFor();
			LOGGER.info("Stopping tomcat: " + (r == 0 ? "OK" : "ERROR"));
		} catch (InterruptedException e) {
			throw new OperationCanceledException(e.getLocalizedMessage(), e);
		}

	}

	private static void stopAlfresco(AlfrescoPreferenceHelper preferences)
			throws IOException {
		File catalinaPidFile = new File(preferences.getAlfrescoHome(),
				AlfrescoFileUtils.path("tomcat", "temp", "catalina.pid"));
		if (catalinaPidFile.exists()) {
			ProcessBuilder processBuilder = new ProcessBuilder(
					"bin/shutdown.sh", "10", "-force");
			processBuilder.directory(new File(preferences.getServerPath()));
			processBuilder.environment().put("CATALINA_PID",
					catalinaPidFile.getAbsolutePath());
			Process start = processBuilder.start();
			try {
				int r = start.waitFor();
				LOGGER.info("Stopping alfresco: " + (r == 0 ? "OK" : "ERROR"));
			} catch (InterruptedException e) {
				throw new OperationCanceledException(e.getLocalizedMessage(), e);
			}
		}
	}

	public static void startServer(AlfrescoPreferenceHelper preferences)
			throws IOException {
		ProcessBuilder processBuilder = null;
		Map<String, String> environment = null;
		if (preferences.isAlfresco()) {
			processBuilder = new ProcessBuilder("scripts/ctl.sh", "start");
			environment = processBuilder.environment();
			environment.put("CATALINA_PID", preferences.getServerPath()
					+ "/temp/catalina.pid");
			// processBuilder = new ProcessBuilder("bin/startup.sh");
			// processBuilder.directory(new File(serverPath).getParentFile());

		} else {
			processBuilder = new ProcessBuilder("bin/startup.sh");
			environment = processBuilder.environment();
			environment
					.put("JAVA_OPTS",
							"-XX:MaxPermSize=512m -Xms128m -Xmx768m -Dalfresco.home="
									+ preferences.getAlfrescoHome()
									+ " -Dcom.sun.management.jmxremote -Dsun.security.ssl.allowUnsafeRenegotiation=true");
		}
		processBuilder.directory(new File(preferences.getServerPath()));

		Process start = processBuilder.start();
		try {
			start.waitFor();
		} catch (InterruptedException e) {
			throw new OperationCanceledException(e.getLocalizedMessage(), e);
		}

	}
}
