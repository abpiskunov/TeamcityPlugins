package fileChangedBuildTrigger;

import jetbrains.buildServer.buildTriggers.*;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public final class BuildTrigger extends BuildTriggerService {
  public static String PLUGIN_NAME = "fileChangedBuildTrigger";
  public static String PLUGIN_DISPLAY_NAME = "File changed build trigger";
  public static String PLUGIN_DESCRIPTION = "Wait for a change of a specified file";

  public static String FILETOMONITOR_PARAM = "filetomonitor.build.trigger.filechanged";
  public static String POLL_INTERVAL_PARAM = "poll.interval.build.trigger.filechanged";
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

        try {
          final String fileToMonitorPath = props.get(FILETOMONITOR_PARAM);

          if (fileToMonitorPath == null || fileToMonitorPath.isEmpty())
          {
            return;
          }

          File fileToMonitor = new File(fileToMonitorPath);
          if (!fileToMonitor.exists() || fileToMonitor.isDirectory())
          {
            context.getCustomDataStorage().putValue(fileToMonitorPath, "");
            return;
          }

          boolean shouldTrigger = false;
          SimpleDateFormat lastModifiedFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
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

          if (shouldTrigger == true) {
            // store last modified date of the file
            context.getCustomDataStorage()
                    .putValue(fileToMonitorPath, Long.toString(currentFileModifiedDate.getTime()));
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
