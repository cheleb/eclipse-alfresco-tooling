package core.helper;

import static org.eclipse.alfresco.publisher.core.AlfrescoFileUtils.path;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.alfresco.publisher.core.AMPFile;
import org.eclipse.alfresco.publisher.core.AlfrescoDeployementException;
import org.eclipse.alfresco.publisher.core.AlfrescoFileUtils;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.core.resources.IProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to invoke Alfresco MMT command.
 * 
 * @author olivier.nouguier@gmail.com
 * 
 */
public class AlfrescoMMTHelper {

	private static final Pattern ALFRESCO_MODULE_PATTERN = Pattern
			.compile("^Module '([^']+)' installed in '([^']+)'$");
	private static final Pattern ALFRESCO_TITLE_PATTERN = Pattern
			.compile("^\\s+-\\s+Title\\:\\s+(.*)$");
	private static final Pattern ALFRESCO_VERSION_PATTERN = Pattern
			.compile("^\\s+-\\s+Version\\:\\s+(.*)$");
	private static final Pattern ALFRESCO_DESCRIPTION_PATTERN = Pattern
			.compile("^\\s+-\\s+\\Description:\\s+(.*)$");
	private static final Pattern ALFRESCO_INSTALLATION_DATE_PATTERN = Pattern
			.compile("^\\s+-\\s+Install Date\\:\\s+(.*)$");

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AlfrescoMMTHelper.class);

	private AlfrescoPreferenceHelper preferences;
	private IProject project;

	private AlfrescoFileUtils alfrescoFileUtils;

	public AlfrescoMMTHelper(IProject project,
			AlfrescoFileUtils alfrescoFileUtils) {
		this.project = project;
		this.alfrescoFileUtils = alfrescoFileUtils;
		this.preferences = new AlfrescoPreferenceHelper(project);
	}

	public ProcessBuilder getApplyAMPProcessBuilder() {

		ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar",
				mmtAbsolutePath(), "install", ampAbsolutePath(),
				warAbsolutePath(), "-force");
		return processBuilder;
	}

	public ProcessBuilder getListAMPProcessBuilder() {

		ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar",
				mmtAbsolutePath(), "list", warAbsolutePath());
		return processBuilder;
	}

	private String warAbsolutePath() {
		return preferences.getWebappAbsolutePath() + ".war";
	}

	private String ampAbsolutePath() {
		String ampRelativePath = preferences.getTargetAmpLocation() + ".amp";
		LOGGER.info("Deploying AMP " + ampRelativePath);

		File ampAbsolutePath = project.getFile(ampRelativePath).getLocation()
				.toFile();
		return ampAbsolutePath.getAbsolutePath();
	}

	private String mmtAbsolutePath() {
		return path(preferences.getAlfrescoHome(), "bin", "alfresco-mmt.jar");
	}

	public void deleteExplodedWar() {
		try {
			alfrescoFileUtils.rm(preferences.getWebappAbsolutePath());
		} catch (IOException e) {
			throw new AlfrescoDeployementException(e);
		}
	}

	public List<AMPFile> getAMPFiles() {
		List<AMPFile> ampFiles2 = new ArrayList<AMPFile>();
		ProcessBuilder processBuilder = getListAMPProcessBuilder();
		try {
			Process process = processBuilder.start();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				Matcher moduleMatcher = ALFRESCO_MODULE_PATTERN.matcher(line);
				AMPFile ampFile;
				if (moduleMatcher.matches()) {
					ampFile = new AMPFile(moduleMatcher.group(1));
					Matcher titleMatcher = ALFRESCO_TITLE_PATTERN
							.matcher(bufferedReader.readLine());
					if (titleMatcher.matches()) {
						ampFile.setTitle(titleMatcher.group(1));
					}
					Matcher versionMatcher = ALFRESCO_VERSION_PATTERN
							.matcher(bufferedReader.readLine());
					if (versionMatcher.matches()) {
						ampFile.setVersion(versionMatcher.group(1));
					}
					Matcher InstallDateMatcher = ALFRESCO_INSTALLATION_DATE_PATTERN
							.matcher(bufferedReader.readLine());
					if (InstallDateMatcher.matches()) {
						ampFile.setInstallDate(InstallDateMatcher.group(1));
					}

					Matcher descriptionMatcher = ALFRESCO_DESCRIPTION_PATTERN
							.matcher(bufferedReader.readLine());
					if (descriptionMatcher.matches()) {
						ampFile.setDescription(descriptionMatcher.group(1));
					}
					ampFiles2.add(ampFile);
				}
			}
						
		} catch (IOException e) {
			throw new AlfrescoDeployementException(e);
		}
		return ampFiles2;
	}

}
