package mil.nga.giat.geowave.cli.geoserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import mil.nga.giat.geowave.core.cli.annotations.GeowaveOperation;
import mil.nga.giat.geowave.core.cli.api.Command;
import mil.nga.giat.geowave.core.cli.api.OperationParams;
import mil.nga.giat.geowave.core.cli.operations.config.options.ConfigOptions;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

@GeowaveOperation(name = "listcv", parentOperation = GeoServerSection.class)
@Parameters(commandDescription = "List GeoServer Coverages")
public class GeoServerListCoveragesCommand implements
		Command
{
	private GeoServerRestClient geoserverClient = null;

	@Parameter(names = {
		"-ws",
		"--workspace"
	}, required = false, description = "<workspace name>")
	private String workspace;

	@Parameter(description = "<coverage store name>")
	private List<String> parameters = new ArrayList<String>();
	private String csName = null;

	@Override
	public boolean prepare(
			OperationParams params ) {
		if (geoserverClient == null) {
			// Get the local config for GeoServer
			File propFile = (File) params.getContext().get(
					ConfigOptions.PROPERTIES_FILE_CONTEXT);

			GeoServerConfig config = new GeoServerConfig(
					propFile);

			// Create the rest client
			geoserverClient = new GeoServerRestClient(
					config);
		}

		// Successfully prepared
		return true;
	}

	@Override
	public void execute(
			OperationParams params )
			throws Exception {
		if (parameters.size() != 1) {
			throw new ParameterException(
					"Requires argument: <coverage store name>");
		}

		if (workspace == null || workspace.isEmpty()) {
			workspace = geoserverClient.getConfig().getWorkspace();
		}

		csName = parameters.get(0);
		
		Response getCvgStoreResponse = geoserverClient.getCoverages(
				workspace,
				csName);

		if (getCvgStoreResponse.getStatus() == Status.OK.getStatusCode()) {
			System.out.println("\nGeoServer coverage list for '" + csName + "':");

			JSONObject jsonResponse = JSONObject.fromObject(getCvgStoreResponse.getEntity());
			JSONArray cvgArray = jsonResponse.getJSONArray("coverages");
			System.out.println(cvgArray.toString(2));
		}
		else {
			System.err.println("Error getting GeoServer coverage list for '" + csName + "'; code = " + getCvgStoreResponse.getStatus());
		}
	}
}
