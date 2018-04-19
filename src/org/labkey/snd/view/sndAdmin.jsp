<%@ page import="org.labkey.snd.SNDController" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>

<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>

<style type="text/css">
    .snd-margin-top {
        margin-top: 15px;
    }

    .snd-button {
        width: 200px;
    }

</style>

<labkey:panel title="Links">
    <div class="col-xs-12">
        <a class="labkey-text-link" href="<%=h(buildURL(SNDController.SecurityAction.class))%>">SND Security</a>
    </div>

</labkey:panel>

<labkey:panel title="Controls">

    <div class="col-xs-12 row clearfix">
        Controls used for SND module setup.
    </div>
    <div class="col-xs-12 snd-margin-top">
        <a id="snd_refresh_cache" class="labkey-button snd-button">Refresh Narrative Cache</a><span id="snd_refresh_cache_msg">&nbsp<i class="fa fa-spinner fa-spin">&nbsp</i> Refreshing cache...</span>
    </div>
    <div class="col-xs-12 snd-margin-top">
        <a id="snd_populate_qc" class="labkey-button snd-button">Populate QC States</a><span id="snd_populate_qc_msg">&nbsp<i class="fa fa-spinner fa-spin">&nbsp</i> Populating QC states...</span>
    </div>

    <script type="text/javascript">

        (function($) {

            $('#snd_refresh_cache').on('click', startRefreshNarrativeCache);
            $('#snd_populate_qc').on('click', insertQCStates);

            function showRefreshingCacheMsg(show) {
                if (show === true) {
                    $('#snd_refresh_cache_msg').show();
                } else {
                    $('#snd_refresh_cache_msg').hide();
                }
            }

            function showPopulatingQcStateMsg(show) {
                if (show === true) {
                    $('#snd_populate_qc_msg').show();
                } else {
                    $('#snd_populate_qc_msg').hide();
                }
            }

            function narrativeCacheSuccess () {
                showRefreshingCacheMsg(false);
                LABKEY.Utils.alert("Success","Narrative cache refreshed");
            }

            function startRefreshNarrativeCache() {
                if (confirm("Refreshing the cache may take a very long time.  Refresh cache?")) {
                    showRefreshingCacheMsg(true);
                    LABKEY.Ajax.request({
                        success: narrativeCacheSuccess,
                        failure: handleFailure,
                        url: LABKEY.ActionURL.buildURL('snd', 'refreshNarrativeCache.api'),
                        params: {},
                        scope: this
                    })
                }
            }

            function insertQCStateSuccess() {
                showPopulatingQcStateMsg(false);
                LABKEY.Utils.alert("Success","QC states inserted");
            }

            function handleFailure(e) {
                showPopulatingQcStateMsg(false);
                if (e.status === 401) {
                    LABKEY.Utils.alert("Error","Unauthorized");
                }
                else if (JSON.parse(e.responseText).exception) {
                    LABKEY.Utils.alert("Error", JSON.parse(e.responseText).exception);
                }
                else {
                    LABKEY.Utils.alert("Error", "Unknown failure");
                }
            }

            function insertQCStates() {
                showPopulatingQcStateMsg(true);
                LABKEY.Ajax.request({
                    success: insertQCStateSuccess,
                    failure: handleFailure,
                    url: LABKEY.ActionURL.buildURL('snd', 'populateQCStates.api'),
                    params: {},
                    scope: this
                })
            }

            $(document).ready(function() {
                showRefreshingCacheMsg(false);
                showPopulatingQcStateMsg(false);
            })

        })(jQuery);


    </script>


</labkey:panel>