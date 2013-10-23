<#assign dateFormat = "yyyy-MM-dd'T'HH:mm:ss">
<#assign tjsFormat = "yyyy,MM,dd,HH,mm,ss">
<#macro headline signature>
<@compress single_line=true>
<#if signature.assocs["csign:signedBy"]??>
<#assign signedBy=signature.assocs["csign:signedBy"]>
<div>Signed by: ${signedBy[0].properties["cm:firstName"]} ${signedBy[0].properties["cm:lastName"]}<br/></div>
<#else>
<div>Signed by: Deleted User: ${signature.properties["cm:creator"]}<br/></div>
</#if>
</@compress>
</#macro>
<#macro workflowText task>
<@compress single_line=true>
<#if task.properties.bpm_completionDate??>
<#assign endDate = task.properties.bpm_completionDate?datetime(dateFormat)>
</#if>
<div>
<#if task.description??>Task: ${task.description?html}<#else></#if><br/>
<#if task.owner.firstName??>Done by: ${task.owner.firstName} ${task.owner.lastName}</#if><br/>
<#if endDate??>Completed: ${endDate?string(displayFormat)}</#if></div>
</@compress>
</#macro>
{
    "timeline":
    {
        "headline":"CounterSign Signature Timeline",
        "type":"default",
		"text":"${doc.properties["cm:name"]}",
		"startDate":"${doc.properties["cm:created"]?string(tjsFormat)}",
        "date": [
			{
                "startDate":"${doc.properties["cm:created"]?string(tjsFormat)}",
				"endDate":"${doc.properties["cm:created"]?string(tjsFormat)}",
                "headline":"Document Created",
                "text":"<div>Document Created by: ${doc.properties["cm:creator"]}<br/> On Date: ${doc.properties["cm:created"]?string(displayFormat)}</div>",
                "asset":
                {
                    "media":"",
                    "credit":"",
                    "caption":""
                }
            }
            <#if (signatures?size > 0)>,</#if>
			<#list signatures as signature>
			{
                "startDate":"${signature.properties["csign:signatureDate"]?string(tjsFormat)}",
				"endDate":"${signature.properties["csign:signatureDate"]?string(tjsFormat)}",
                "headline":"Document Signed",
                "text":"<@headline signature/>",
                "asset":
                {
                    "media":"",
                    "credit":"",
                    "caption":""
                }  
			}<#if signature_has_next>,</#if>
			</#list>
			<#if (workflowInstances?size > 0)>,
				<#list workflowInstances as instance>
					<#list instance.tasks as task>
					{
						<#assign startDate = task.properties.bpm_startDate?datetime(dateFormat)>
						
						"startDate":"${startDate?string(tjsFormat)}",
						<#if task.properties.bpm_completionDate??>
						<#assign endDate = task.properties.bpm_completionDate?datetime(dateFormat)>
						"endDate":"${endDate?string(tjsFormat)}",
						<#else>
						"endDate":"${startDate?string(tjsFormat)}",
						</#if>
		                "headline":"${task.title!""}",
		                "text":"<@workflowText task/>",
		                "asset":
		                {
		                    "media":"",
		                    "credit":"",
		                    "caption":""
		                }  
					}<#if task_has_next>,</#if>
					</#list>
					<#if instance_has_next>,</#if>
				</#list>
			</#if>
        ]
    }
}