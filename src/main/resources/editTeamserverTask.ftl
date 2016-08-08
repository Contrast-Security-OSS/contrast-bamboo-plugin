[#if profiles?size != 0]
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
    <div class="field-group">
        <label>Threshold Count</label>
        <input class="text short-field" type="number"name="count" required='true' value = ${count}></input>
        <div class="description">Enter the minimum number of vulnerabilities that will trigger a build failure</div>
    </div>
    <div class="field-group">
        <label>Threshold Severity</label>
        <select class="select" name="severity_select" required='true'>
            [#list severities as severity]
                [#if severity==severity_select]
                    <option value=${severity} selected = "selected">${severity}</option>
                [#else]
                    <option value=${severity}>${severity}</option>
                [/#if]
            [/#list]
        </select>
    </div>
    <div class="field-group">
        <label>Threshold Vulnerability Type</label>
        <select class="select" name="type_select" required='true'>
            [#list types as type]
                [#if type==type_select]
                    <option value=${type} selected = "selected">${type}</option>
                [#else]
                    <option value=${type}>${type}</option>
                [/#if]
            [/#list]
        </select>
    </div>
[#else]
    <p>You have not yet configured a TeamServer profile. Please do so in the Administrator Settings, under the TeamServer Profiles tab.</p>
[/#if]
