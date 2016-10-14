package newdirectoryBuildTrigger;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.buildTriggers.PolledBuildTrigger;
import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.BuildCustomizer;
import jetbrains.buildServer.serverSide.BuildCustomizerFactory;
import jetbrains.buildServer.serverSide.BuildPromotion;
import jetbrains.buildServer.serverSide.SBuildType;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BuildTriggerPolicy extends PolledBuildTrigger {
        private static final Logger LOG = Logger.getInstance(Loggers.VCS_CATEGORY + BuildTriggerPolicy.class);

    private final BuildCustomizerFactory buildCustomizerFactory;

    public BuildTriggerPolicy(final BuildCustomizerFactory buildCustomizerFactory) {
        this.buildCustomizerFactory = buildCustomizerFactory;
    }

    @Override
    public void triggerBuild(PolledTriggerContext context) throws BuildTriggerException {
        final Map<String, String> props = context.getTriggerDescriptor().getProperties();

        try
        {
            final String pathToMonitor = props.get(BuildTrigger.PATHTOMONITOR_PARAM);
            if (pathToMonitor == null || pathToMonitor.isEmpty())
            {
                return;
            }

            File rootDirectory = new File(pathToMonitor);
            //rootDirectory = rootDirectory.getCanonicalFile();
            if (!rootDirectory.exists())
            {
                return;
            }

            File[] directories = rootDirectory.listFiles(File::isDirectory);
            if (directories.length <= 0)
            {
                return;
            }

            HashSet<String> oldChildren = new HashSet<String>();
            HashSet<String> newChildren = new HashSet<String>();
            for (File subDirectory : directories)
            {
                newChildren.add(subDirectory.getName());
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

            if (!shouldTrigger && !oldChildren.containsAll(newChildren))
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

            context.getCustomDataStorage()
                    .putValue(pathToMonitor, storeDirectoriesString);

            if (shouldTrigger == true) {

                BuildCustomizer customizer = buildCustomizerFactory.createBuildCustomizer(context.getBuildType(), null);
                HashMap<String, String> customParameters = new HashMap<String, String>();
                customParameters.put("MYPARAMTER_ONE", "Val1");
                customParameters.put("env.TestEnv", "Val111111");
                customizer.setParameters(customParameters);

                BuildPromotion promotion = customizer.createPromotion();
                promotion.addToQueue(BuildTrigger.PLUGIN_NAME + ": Triggered by changes in " + pathToMonitor);
            }
        } catch (Exception e) {
            throw new BuildTriggerException(
                    BuildTrigger.PLUGIN_DISPLAY_NAME + " failed with error: " + e.getMessage(), e);
        }
    }
}
