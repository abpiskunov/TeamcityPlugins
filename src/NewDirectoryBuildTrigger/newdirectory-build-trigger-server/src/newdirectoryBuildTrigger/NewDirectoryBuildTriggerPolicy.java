package newdirectoryBuildTrigger;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.buildTriggers.PolledBuildTrigger;
import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.BuildCustomizer;
import jetbrains.buildServer.serverSide.BuildCustomizerFactory;
import jetbrains.buildServer.serverSide.BuildPromotion;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/*
    Quick implementation for monitoring a path and trigger a build if there is a new sub folders added.
    Can be refactored to be more structured later if needed.
 */

public class NewDirectoryBuildTriggerPolicy extends PolledBuildTrigger {
        private static final Logger LOG = Logger.getInstance(Loggers.VCS_CATEGORY + NewDirectoryBuildTriggerPolicy.class);

    private final BuildCustomizerFactory buildCustomizerFactory;

    public NewDirectoryBuildTriggerPolicy(final BuildCustomizerFactory buildCustomizerFactory) {
        this.buildCustomizerFactory = buildCustomizerFactory;
    }

    @Override
    public void triggerBuild(PolledTriggerContext context) throws BuildTriggerException {
        final Map<String, String> props = context.getTriggerDescriptor().getProperties();
        final String pathToMonitor = props.get(BuildTrigger.PATHTOMONITOR_PARAM);

        try
        {
            if (pathToMonitor == null || pathToMonitor.isEmpty())
            {
                LOG.info("path to monitor is not provided");
                return;
            }

            File rootDirectory = new File(pathToMonitor);
            if (!rootDirectory.exists())
            {
                LOG.info("directory does not exist '" + pathToMonitor + "'");
                return;
            }

            File[] directories = rootDirectory.listFiles(File::isDirectory);
            if (directories.length <= 0)
            {
                LOG.info("no sub directories found in '" + pathToMonitor + "'");
                return;
            }

            HashSet<String> oldChildren = new HashSet<String>();
            HashSet<String> newChildren = new HashSet<String>();
            String newestSubDirectory = "";
            Date newestSubDirectoryDate = new Date(Long.MIN_VALUE);
            for (File subDirectory : directories)
            {
                final String name = subDirectory.getName();
                newChildren.add(name);

                final Date subDirectoryDate = new Date(subDirectory.lastModified());
                if (subDirectoryDate.after(newestSubDirectoryDate))
                {
                    newestSubDirectoryDate = subDirectoryDate;
                    newestSubDirectory = name;
                }
            }

            boolean shouldTrigger = false;
            String oldSubDirectoriesString = context.getCustomDataStorage().getValue(pathToMonitor);
            if (oldSubDirectoriesString == null || oldSubDirectoriesString.isEmpty())
            {
                shouldTrigger = true;
            }
            else
            {
                oldChildren.addAll(Arrays.asList(oldSubDirectoriesString.split(";")));
            }

            if (!oldChildren.containsAll(newChildren))
            {
                // there are some new sub directories
                shouldTrigger = true;
            }

            oldChildren.addAll(newChildren);
            String storeDirectoriesString = "";
            for(String old : oldChildren)
            {
                if (newChildren.contains(old))
                {
                    if (!storeDirectoriesString.isEmpty())
                    {
                        storeDirectoriesString += ";";
                    }
                    storeDirectoriesString = storeDirectoriesString + old;
                }
            }

            context.getCustomDataStorage().putValue(pathToMonitor, storeDirectoriesString);

            if (shouldTrigger == true) {
                String newFolderParamName = props.get(BuildTrigger.NEWDIRECTORY_PARAM);
                if (StringUtil.isEmptyOrSpaces(newFolderParamName))
                {
                    newFolderParamName = BuildTrigger.DEFAULT_NEWFOLDER_PARAM;
                }

                BuildCustomizer customizer = buildCustomizerFactory.createBuildCustomizer(context.getBuildType(), null);
                HashMap<String, String> customParameters = new HashMap<String, String>();
                customParameters.put(newFolderParamName, newestSubDirectory);
                customParameters.put("env." + newFolderParamName, newestSubDirectory);
                customizer.setParameters(customParameters);

                BuildPromotion promotion = customizer.createPromotion();
                promotion.addToQueue(BuildTrigger.PLUGIN_DISPLAY_NAME + ": Triggered by changes in " + pathToMonitor);
            }
        } catch (Exception e) {
            LOG.error("error while trying to trigger build for '" + pathToMonitor + "'", e);
            throw new BuildTriggerException(
                    BuildTrigger.PLUGIN_DISPLAY_NAME + " failed with error: " + e.getMessage(), e);
        }
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
