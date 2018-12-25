[#if profiles?size != 0]
    <div class="field-group">
        <label>TeamServer Profile</label>
        <select class="select" id="profileSelect" name="profile_select" title="profile select">
            [#list profiles as profile]
                [#if profile==profile_select]
                    <option value="${profile}" selected = "selected">${profile}</option>
                [#else]
                    <option value="${profile}">${profile}</option>
                [/#if]
            [/#list]
        </select>
    </div>

    <div class="field-group">
        <label for="server_name">Server Name: </label>
        <input type="text" name="server_name" class="text" value="${server_name}" required>
    </div>

    <div class="field-group">
        <label>Application Name</label>
        <input class="text" type="text" name="app_name" required="true" value="${app_name}"></input>
    </div>

    [@ww.checkbox labelKey="Passive" name="passive" toggle="true"/]

    [#assign index = 0]
    <fieldset id = "thresholds">
        [#list thresholds as threshold]
        <div id = "threshold_${threshold_index+1}">
            <br>
            <h2>Threshold Condition</h2>
            <hr>
            <div class="field-group">
                <label>Threshold Count</label>
                <input class="text short-field" type="number" name="count_${threshold_index+1}" required='true' value = ${threshold.count}></input>
                <div class="description">Enter the minimum number of vulnerabilities that will trigger a build failure</div>
            </div>
            <div class="field-group">
                <label>Threshold Severity</label>
                <select class="select" name="severity_select_${threshold_index+1}">
                    [#list severities as severity]
                        [#if severity==threshold.severity_select]
                            <option value=${severity} selected = "selected">${severity}</option>
                        [#else]
                            <option value=${severity}>${severity}</option>
                        [/#if]
                    [/#list]
                </select>
            </div>
            <div class="field-group">
                <label>Threshold Vulnerability Type</label>
                <select class="select" name="type_select_${threshold_index+1}">
                    <option selected = "selected" value = "Any"> Any </option>
                    [#list types as type]
                        [#if type==threshold.type_select]
                            <option value=${type} selected = "selected">${type}</option>
                        [#else]
                            <option value=${type}>${type}</option>
                        [/#if]
                    [/#list]
                </select>
            </div><br>
            <button type = "button" class="aui-button" onclick="removePanel(${threshold_index+1})">Remove</button>
        </div>
        [#assign index = threshold_index+1]
        [/#list]
    </fieldset>
    <input type="hidden" id="starting_index" value = ${index} />
    <script type="text/javascript">
        var current_index = parseInt(AJS.$("#starting_index").val())+1;
        function addPanel(){
            AJS.$("#thresholds").append(generatePanel(current_index));
            current_index++;
        }
        function removePanel(index){
            var id = "#threshold_"+index;
            AJS.$(id).remove();
            current_index--;
        }
        function generatePanel(index){
            return '<div id = "threshold_'+index+'">'+
                '<h2>Threshold Condition</h2>'+
                '<hr>'+
                '<div class="field-group">'+
                    '<label>Threshold Count</label>'+
                    '<input class="text short-field" type="number" name="count_'+index+'" required="true" value = 0></input>'+
                    '<div class="description">Enter the minimum number of vulnerabilities that will trigger a build failure</div>'+
                '</div>'+
                '<div class="field-group">'+
                    '<label>Threshold Severity</label>'+
                    '<select class="select" name="severity_select_'+index+'">'+
                        [#list severities as severity]
                            '<option value=${severity}>${severity}</option>'+
                        [/#list]
                    '</select>'+
                '</div>'+
                '<div class="field-group">'+
                    '<label>Threshold Vulnerability Type</label>'+
                    '<select class="select" name="type_select_'+index+'">'+
                        '<option selected = "selected" value = "Any"> Any </option>'+
                        [#list types as type]
                            '<option value=${type}>${type}</option>'+
                        [/#list]
                    '</select>'+
                '</div><br>'+
                '<button type = "button" class="aui-button" onclick="removePanel('+index+')">Remove</button>'+
            '</div>';
        }

    </script>
    <hr>
    <button type="button" class="aui-button" onclick = "addPanel()">
        <span class="aui-icon aui-icon-small aui-iconfont-add"></span> Add new threshold condition
    </button>
[#else]
    <p>You have not yet configured a TeamServer profile. Please do so in the Administrator Settings, under the TeamServer Profiles tab.</p>
[/#if]
