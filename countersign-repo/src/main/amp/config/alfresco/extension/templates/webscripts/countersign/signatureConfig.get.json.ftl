{
	<#list config?keys as it>
  		"${it}":"${config[it]}"<#if it_has_next>,</#if>
	</#list>
}