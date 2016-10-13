<%@ include file="/include.jsp" %>
<%@ page import="newdirectoryBuildTrigger.BuildTrigger" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" type="jetbrains.buildServer.controllers.BasePropertiesBean" scope="request"/>

<tr class="noBorder" >
    <td colspan="2">
        <em>Will add a build to the queue when new directory is created under given path.</em>
    </td>
</tr>

<tr class="noBorder" >
    <td><label for="<%=BuildTrigger.PATHTOMONITOR_PARAM%>">Path<l:star/></label></td>
    <td>
       <props:textProperty name="<%=BuildTrigger.PATHTOMONITOR_PARAM%>" style="width:100%;"/>
      <span class="smallNote">
          Full path to a directory to monitor.
      </span>
        <span class="error" id="error_<%=BuildTrigger.PATHTOMONITOR_PARAM%>"></span>
    </td>
</tr>

<tr class="noBorder" >
    <td><label for="<%=BuildTrigger.POLL_INTERVAL_PARAM%>">Polling interval</label></td>
    <td>
       <props:textProperty name="<%=BuildTrigger.POLL_INTERVAL_PARAM%>" style="width:10em;"/>
      <span class="smallNote">
         Trigger polling interval in seconds. Default value is 30 sec.
      </span>
        <span class="error" id="error_<%=BuildTrigger.POLL_INTERVAL_PARAM%>"></span>
    </td>
</tr>
