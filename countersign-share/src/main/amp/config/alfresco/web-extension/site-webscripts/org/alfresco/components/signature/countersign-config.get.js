//get the signature config json from the repo tier service
function getConfig()
{
	try
	{
		// Get the CounterSign configuration
		var result = remote.call("/countersign/config");
		if (result.status == 200 && result != "{}")
		{
			return result;
		}
	}
	catch (e)
	{
	}

	return {};
}

model.config = getConfig();

