function main()
{
	// get the document and associated sigs, add to model
	var doc = search.findNode(args["nodeRef"]);
	var signatures = doc.childAssocs["csign:signatures"];
	
	// get the workflow tasks for this document (completed and in-flight)
	var workflowInstances = countersign.getSignatureWorkflowHistory(args["nodeRef"]);
	
	// get the date display format for this timeline
	var dateFormat = countersign.getProperty("countersign.display.dateFormat");
	
	model.doc = doc;
	model.signatures = signatures;
	model.workflowInstances = workflowInstances;
	model.displayFormat = dateFormat;
}

main();