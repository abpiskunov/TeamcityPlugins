package fileChangedBuildTrigger;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.buildTriggers.PolledBuildTrigger;
import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.BuildCustomizer;
import jetbrains.buildServer.serverSide.BuildCustomizerFactory;
import jetbrains.buildServer.serverSide.BuildPromotion;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
    Quick implementation for monitoring a file  and trigger a build if it was modified.
    If file is a semaphore (file that starts with [Semaphore] and all next lines having key/value pairs. if checkbox
    "Is Semaphore" is checked, all those keys will be added as build parameters and env variables. Also, to avoid
    duplication of parameters, it is possible to specify a prefix for those keys in the trigger settings.

    Can be refactored to be more structured later if needed.
 */

public class FileChangedBuildTriggerPolicy extends PolledBuildTrigger {
    private static String SEMAPHORE_HEADER = "[Semaphore]";
    private static final Logger LOG = Logger.getInstance(Loggers.VCS_CATEGORY + FileChangedBuildTriggerPolicy.class);

    private final BuildCustomizerFactory buildCustomizerFactory;

    public FileChangedBuildTriggerPolicy(final BuildCustomizerFactory buildCustomizerFactory) {
        this.buildCustomizerFactory = buildCustomizerFactory;
    }

    @Override
    public void triggerBuild(PolledTriggerContext context) throws BuildTriggerException {
        final Map<String, String> props = context.getTriggerDescriptor().getProperties();
        final String fileToMonitorPath = props.get(BuildTrigger.FILETOMONITOR_PARAM);

        try {
            final boolean shouldTrigger = ShouldTriggerBuildForFile(context, fileToMonitorPath);
            if (shouldTrigger == false) {
                return;
            }

            BuildCustomizer customizer = buildCustomizerFactory.createBuildCustomizer(context.getBuildType(), null);

            final String isSemaphoreString = props.get(BuildTrigger.ISSEMAPHORE_PARAM);
            if (Boolean.parseBoolean(isSemaphoreString))
            {
                String prefix = props.get(BuildTrigger.VARIABLEPREFIX_PARAM);
                FileInputStream fis = new FileInputStream(new File(fileToMonitorPath));
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(fis)))
                {
                    String line = reader.readLine();
                    if (line != null && line.equalsIgnoreCase(SEMAPHORE_HEADER)) {
                        LOG.info("File '" + fileToMonitorPath
                                + "' is a semaphore, getting it's values and adding build parameters.");
                        // if file indeed is a semaphore, read each line and add key/value pairs as build parameters
                        HashMap<String, String> customParameters = new HashMap<String, String>();
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split("=");
                            if (parts.length == 2) {
                                // if line has correct format add it
                                final String key = prefix + parts[0];
                                final String value = parts[1];
                                customParameters.put(key, value);
                                customParameters.put("env." + key, value);
                            }
                        }
                        if (!customParameters.isEmpty()) {
                            customizer.setParameters(customParameters);
                        }
                    } else {
                        LOG.info("File '" + fileToMonitorPath + "' has incorrect format and is not a semaphore.");
                    }
                }
            }

            BuildPromotion promotion = customizer.createPromotion();
            promotion.addToQueue(BuildTrigger.PLUGIN_DISPLAY_NAME + ": Triggered by changes in " + fileToMonitorPath);

        } catch (Exception e) {
            LOG.error("error while trying to trigger build for file '" + fileToMonitorPath + "'", e);
            throw new BuildTriggerException(BuildTrigger.PLUGIN_DISPLAY_NAME + " failed with error: " + e.getMessage(), e);
        }
    }

    private boolean ShouldTriggerBuildForFile(PolledTriggerContext context, String fileToMonitorPath) {
        if (fileToMonitorPath == null || fileToMonitorPath.isEmpty())
        {
            LOG.info("path for file to monitor is not provided");
            return false;
        }

        File fileToMonitor = new File(fileToMonitorPath);
        if (!fileToMonitor.exists() || fileToMonitor.isDirectory())
        {
            LOG.info("file does not exist '" + fileToMonitorPath + "'");
            context.getCustomDataStorage().putValue(fileToMonitorPath, "");
            return false;
        }

        boolean shouldTrigger = false;
        Date currentFileModifiedDate = new Date(fileToMonitor.lastModified());
        String lastChangeTimeString = context.getCustomDataStorage().getValue(fileToMonitorPath);
        if (lastChangeTimeString == null || lastChangeTimeString.isEmpty())
        {
            shouldTrigger = true;
        }
        else
        {
            Date lastModifiedDate = new Date(Long.valueOf(lastChangeTimeString));
            if (currentFileModifiedDate.compareTo(lastModifiedDate) > 0) {
                shouldTrigger = true;
            }
        }

        if (shouldTrigger) {
            // store last modified date of the file
            context.getCustomDataStorage()
                   .putValue(fileToMonitorPath, Long.toString(currentFileModifiedDate.getTime()));
        }

        return shouldTrigger;
    }

    @Override
    public int getPollInterval(@NotNull PolledTriggerContext context) {
        final Map<String, String> props = context.getTriggerDescriptor().getProperties();

        final String poll_interval = props.get(BuildTrigger.POLL_INTERVAL_PARAM);

        if (poll_interval == null) {
            return BuildTrigger.DEFAULT_POLL_INTERVAL;
        }

        try {
            return Integer.parseInt(poll_interval);
        } catch (NumberFormatException e) {
            return BuildTrigger.DEFAULT_POLL_INTERVAL;
        }
    }
}
