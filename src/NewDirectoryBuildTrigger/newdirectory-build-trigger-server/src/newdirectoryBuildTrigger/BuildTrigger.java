package newdirectoryBuildTrigger;

import jetbrains.buildServer.buildTriggers.*;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public final class BuildTrigger extends BuildTriggerService {
  public static String PLUGIN_NAME = "newDirectoryBuildTrigger";
  public static String PLUGIN_DISPLAY_NAME = "New directory build trigger";
  public static String PLUGIN_DESCRIPTION = "Wait for a sub directory under given path";

  public static String PATHTOMONITOR_PARAM = "pathtomonitor.build.trigger.newdirectory";
  public static String POLL_INTERVAL_PARAM = "poll.interval.build.trigger.newdirectory";
  private static final Integer DEFAULT_POLL_INTERVAL = 30;

  @NotNull
  private final PluginDescriptor myPluginDescriptor;

  public BuildTrigger(@NotNull final PluginDescriptor pluginDescriptor) {
    myPluginDescriptor = pluginDescriptor;
  }

  @NotNull
  @Override
  public String getName() {
    return PLUGIN_NAME;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return PLUGIN_DISPLAY_NAME;
  }

  @NotNull
  @Override
  public String describeTrigger(@NotNull BuildTriggerDescriptor buildTriggerDescriptor) {
    return PLUGIN_DESCRIPTION;
  }

  @NotNull
  @Override
  public BuildTriggeringPolicy getBuildTriggeringPolicy() {
    return new PolledBuildTrigger() {
      @Override
      public void triggerBuild(@NotNull PolledTriggerContext context) throws BuildTriggerException {
        final Map<String, String> props = context.getTriggerDescriptor().getProperties();

        try
        {
          final String pathToMonitor = props.get(PATHTOMONITOR_PARAM);
          if (pathToMonitor == null || pathToMonitor.isEmpty() || Files.notExists(Paths.get(pathToMonitor)))
          {
            return;
          }

          File[] directories = new File(pathToMonitor).listFiles(File::isDirectory);
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
            SBuildType buildType = context.getBuildType();
            buildType.addToQueue(getDisplayName());
          }
        } catch (Exception e) {
          throw new BuildTriggerException(getDisplayName() + " failed with error: " + e.getMessage(), e);
        }
      }

      @Override
      public int getPollInterval(@NotNull PolledTriggerContext context) {
        final Map<String, String> props = context.getTriggerDescriptor().getProperties();

        final String poll_interval = props.get(POLL_INTERVAL_PARAM);

        if (poll_interval == null) {
                return DEFAULT_POLL_INTERVAL;
        }

        try {
          return Integer.parseInt(poll_interval);
        } catch (NumberFormatException e) {
          return DEFAULT_POLL_INTERVAL;
        }
      }
    };
  }

  @Override
  public PropertiesProcessor getTriggerPropertiesProcessor() {
    return new PropertiesProcessor() {
      public Collection<InvalidProperty> process(Map<String, String> properties) {
        final ArrayList<InvalidProperty> invalidProps = new ArrayList<InvalidProperty>();
        return invalidProps;
      }
    };
  }

  @Override
  public String getEditParametersUrl() {
    return myPluginDescriptor.getPluginResourcesPath("editBuildTrigger.jsp");
  }

  @Override
  public boolean isMultipleTriggersPerBuildTypeAllowed() {
    return true;
  }

  @Override
  public Map<String, String> getDefaultTriggerProperties() {
    final Map<String, String> defaultProps = new HashMap<String, String>(1);
    defaultProps.put(POLL_INTERVAL_PARAM, DEFAULT_POLL_INTERVAL.toString());
    return defaultProps;
  }
}
