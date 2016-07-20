[#if profiles?size != 0]
    <div class="field-group">
        <label>Teamserver Profile</label>
        <select class="select" id="profileSelect" name="profileSelect" title="profile select">
            [#list profiles as profile]
            <option>${profile}</option>
            [/#list]
        </select>
    </div>
    <div class="field-group">
        <label>Threshold Count</label>
        <input class="text short-field" type="number"name="count" required='true'></input>
        <div class="description">Enter the minimum number of vulnerabilities that will trigger a build failure</div>
    </div>
    <div class="field-group">
        <label>Threshold Severity</label>
        <select class="select" name="severity" required='true'>
            <option>None</option>
            <option>Note</option>
            <option>Low</option>
            <option>Medium</option>
            <option>High</option>
            <option>Critical</option>
        </select>
    </div>
    <div class="field-group">
        <label>Threshold Vulnerability Type</label>
        <select class="select" name="type" required='true'>
            <option>authorization-missing-deny</option>
            <option>authorization-rules-misordered</option>
            <option>autocomplete-missing</option>
            <option>cache-control-disabled</option>
            <option>cache-controls-missing</option>
            <option>clickjacking-control-missing</option>
            <option>cmd-injection</option>
            <option>compilation-debug</option>
            <option>cookie-flags-missing</option>
            <option>crypto-bad-ciphers</option>
            <option>crypto-bad-mac</option>
            <option>crypto-weak-randomness</option>
            <option>csp-header-insecure</option>
            <option>csp-header-missing</option>
            <option>csrf</option>
            <option>custom-errors-off</option>
            <option>escape-templates-off</option>
            <option>event-validation-disabled</option>
            <option>expression-language-injection</option>
            <option>forms-auth-protection</option>
            <option>forms-auth-redirect</option>
            <option>forms-auth-ssl</option>
            <option>hardcoded-key</option>
            <option>hardcoded-password</option>
            <option>header-checking-disabled</option>
            <option>header-injection</option>
            <option>hql-injection</option>
            <option>hsts-header-missing</option>
            <option>http-only-disabled</option>
            <option>httponly</option>
            <option>insecure-auth-protocol</option>
            <option>insecure-jsp-access</option>
            <option>ldap-injection</option>
            <option>log-injection</option>
            <option>max-request-length</option>
            <option>nosql-injection</option>
            <option>parameter-pollution</option>
            <option>path-traversal</option>
            <option>plaintext-conn-strings</option>
            <option>reflected-xss</option>
            <option>reflection-injection</option>
            <option>request-validation-control-disabled</option>
            <option>request-validation-disabled</option>
            <option>role-manager-protection</option>
            <option>role-manager-ssl</option>
            <option>secure-flag-missing</option>
            <option>session-regenerate</option>
            <option>session-rewriting</option>
            <option>session-timeout</option>
            <option>spring-unchecked-autobinding</option>
            <option>sql-injection</option>
            <option>stored-xss</option>
            <option>trace-enabled</option>
            <option>trace-enabled-aspx</option>
            <option>trust-boundary-violation</option>
            <option>unsafe-code-execution</option>
            <option>unsafe-readlin</option>
            <option>unsafe-xml-decode</option>
            <option>untrusted-deserializatio</option>
            <option>unvalidated-forward</option>
            <option>unvalidated-redirect</option>
            <option>verb-tampering</option>
            <option>version-header-enabled</option>
            <option>viewstate-encryption-disabled</option>
            <option>viewstate-mac-disabled</option>
            <option>wcf-detect-replays</option>
            <option>wcf-exception-details</option>
            <option>wcf-metadata-enabled</option>
            <option>weak-membership-config</option>
            <option>xcontenttype-header-missing</option>
            <option>xpath-injection</option>
            <option>xxe</option>
            <option>xxssprotection-header-disabled</option>

        </select>
    </div>
[#else]
<p>You have not yet configured a Teamserver profile. Please do so in the Administrator Settings, under the Teamserver Profiles tab.</p>
[/#if]
