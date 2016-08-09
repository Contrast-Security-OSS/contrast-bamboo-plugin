[#if profiles?size != 0]
    ${webResourceManager.requireResource("com.contrastsecurity.contrast-bamboo-plugin:task-helper")}
    <div class="field-group">
        <label>TeamServer Profile</label>
        <select class="select" id="profileSelect" name="profile_select" title="profile select">
            [#list profiles as profile]
                [#if profile==profile_select]
                    <option value=${profile} selected = "selected">${profile}</option>
                [#else]
                    <option value=${profile}>${profile}</option>
                [/#if]
            [/#list]
        </select>
    </div>
    <div class="field-group">
        <label>Application Name</label>
        <input class="text" type="text" name="app_name" required="true" value="${app_name}"></input>
    </div>
    [#assign index = 0]
    [#list thresholds as threshold]
    <hr>
    <h2>Threshold Settings - ${threshold_index+1}</h2>
    <hr>
    <fieldset>
        <div class="field-group">
            <label>Threshold Count</label>
            <input class="text short-field" type="number" name="count_${threshold_index+1}" required='true' value = ${threshold.count}></input>
            <div class="description">Enter the minimum number of vulnerabilities that will trigger a build failure</div>
        </div>
        <div class="field-group">
            <label>Threshold Severity</label>
            <select class="select" name="severity_select_${threshold_index+1}" required='true'>
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
            <select class="select" name="type_select_${threshold_index+1}" required='true'>
                <option selected = "selected" value = ""> None </option>
                [#list types as type]
                    [#if type==threshold.type_select]
                        <option value=${type} selected = "selected">${type}</option>
                    [#else]
                        <option value=${type}>${type}</option>
                    [/#if]
                [/#list]
            </select>
        </div>
    </fieldset>
    [#assign index = threshold_index+1]
    [/#list]
    <input type="hidden" name="starting_index" value = ${index} />
    <button class="aui-button" onclick = "addPanel()">
        <span class="aui-icon aui-icon-small aui-iconfont-add"></span> Add new threshold
    </button>
[#else]
    <p>You have not yet configured a TeamServer profile. Please do so in the Administrator Settings, under the TeamServer Profiles tab.</p>
[/#if]
