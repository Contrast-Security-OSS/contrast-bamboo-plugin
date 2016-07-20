<h2>Select a Teamserver Profile:</h2>
[#if profiles?size != 0]
<p>
 <select class="select" id="profileSelect" name="profileSelect" title="profile select">
    [#list profiles as profile]
    <option>${profile}</option>
    [/#list]
 </select>
</p>
[#else]
<p>You have not yet configured a Teamserver profile. Please do so in the Administrator Settings, under Teamserver Profiles.</p>
[/#if]