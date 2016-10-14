package fileChangedBuildTrigger;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerService;
import jetbrains.buildServer.buildTriggers.BuildTriggeringPolicy;
import jetbrains.buildServer.serverSide.BuildCustomizerFactory;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class BuildTrigger extends BuildTriggerService {
  public static String PLUGIN_NAME = "fileChangedBuildTrigger";
  public static String PLUGIN_DISPLAY_NAME = "File changed build trigger";
  public static String PLUGIN_DESCRIPTION = "Wait for a change of a specified file";

  public static String FILETOMONITOR_PARAM = "filetomonitor.build.trigger.filechanged";
  public static String ISSEMAPHORE_PARAM = "is.semaphore.build.trigger.filechanged";
  public static String VARIABLEPREFIX_PARAM = "variable.prefix..interval.build.trigger.filechanged";
  public static String POLL_INTERVAL_PARAM = "poll.interval.build.trigger.filechanged";

  public static final Integer DEFAULT_POLL_INTERVAL = 30;

  @NotNull
  private final PluginDescriptor pluginDescriptor;
  @NotNull
  private final FileChangedBuildTriggerPolicy policy;

  public BuildTrigger(final PluginDescriptor pluginDescriptor,
                      final EventDispatcher<BuildServerListener> buildServerListener,
                      final BuildCustomizerFactory buildCustomizerFactory) {
    this.policy = new FileChangedBuildTriggerPolicy( buildCustomizerFactory);
    this.pluginDescriptor = pluginDescriptor;
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
    return policy;
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
    return pluginDescriptor.getPluginResourcesPath("editBuildTrigger.jsp");
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
