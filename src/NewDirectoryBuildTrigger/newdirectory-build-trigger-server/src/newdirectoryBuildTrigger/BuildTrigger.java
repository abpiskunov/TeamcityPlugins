package newdirectoryBuildTrigger;

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
  public static String PLUGIN_NAME = "NewDirectoryBuildTrigger";
  public static String PLUGIN_DISPLAY_NAME = "New directory build trigger";
  public static String PLUGIN_DESCRIPTION = "Wait for a sub directory under given path";

  public static String PATHTOMONITOR_PARAM = "pathtomonitor.build.trigger.newdirectory";
  public static String NEWDIRECTORY_PARAM = "newdirectoryparam.build.trigger.newdirectory";
  public static String POLL_INTERVAL_PARAM = "poll.interval.build.trigger.newdirectory";
  public static final Integer DEFAULT_POLL_INTERVAL = 30;
  public static String DEFAULT_NEWFOLDER_PARAM = "TEAMCITY_NEW_DIRECTORY_NAME";

  @NotNull
  private final PluginDescriptor pluginDescriptor;
  @NotNull
  private final NewDirectoryBuildTriggerPolicy policy;

  public BuildTrigger(final PluginDescriptor pluginDescriptor,
                      final EventDispatcher<BuildServerListener> buildServerListener,
                      final BuildCustomizerFactory buildCustomizerFactory) {
    this.policy = new NewDirectoryBuildTriggerPolicy( buildCustomizerFactory);
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
