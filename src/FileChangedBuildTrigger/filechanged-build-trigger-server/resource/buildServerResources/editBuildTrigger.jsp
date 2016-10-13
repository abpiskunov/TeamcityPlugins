<%@ include file="/include.jsp" %>
<%@ page import="fileChangedBuildTrigger.BuildTrigger" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" type="jetbrains.buildServer.controllers.BasePropertiesBean" scope="request"/>

<tr class="noBorder" >
    <td colspan="2">
        <em>Will add a build to the queue when changes are detected in a specified file.</em>
    </td>
</tr>

<tr class="noBorder" >
    <td><label for="<%=BuildTrigger.FILETOMONITOR_PARAM%>">File path: <l:star/></label></td>
    <td>
       <props:textProperty name="<%=BuildTrigger.FILETOMONITOR_PARAM%>" style="width:100%;"/>
      <span class="smallNote">
          Full path to a file to monitor for changes.
      </span>
        <span class="error" id="error_<%=BuildTrigger.FILETOMONITOR_PARAM%>"></span>
    </td>
</tr>

<tr class="noBorder" >
    <td><label for="<%=BuildTrigger.POLL_INTERVAL_PARAM%>">Polling interval: </label></td>
    <td>
       <props:textProperty name="<%=BuildTrigger.POLL_INTERVAL_PARAM%>" style="width:10em;"/>
      <span class="smallNote">
         Trigger polling interval in seconds. Default value is 30 sec.
      </span>
        <span class="error" id="error_<%=BuildTrigger.POLL_INTERVAL_PARAM%>"></span>
    </td>
</tr>
