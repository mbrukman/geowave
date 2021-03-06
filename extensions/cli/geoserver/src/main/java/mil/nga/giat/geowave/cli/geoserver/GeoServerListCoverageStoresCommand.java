package mil.nga.giat.geowave.cli.geoserver;

import java.io.File;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import mil.nga.giat.geowave.core.cli.annotations.GeowaveOperation;
import mil.nga.giat.geowave.core.cli.api.Command;
import mil.nga.giat.geowave.core.cli.api.OperationParams;
import mil.nga.giat.geowave.core.cli.operations.config.options.ConfigOptions;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@GeowaveOperation(name = "listcs", parentOperation = GeoServerSection.class)
@Parameters(commandDescription = "List GeoServer coverage stores")
public class GeoServerListCoverageStoresCommand implements
		Command
{
	private GeoServerRestClient geoserverClient = null;

	@Parameter(names = {
		"-ws",
		"--workspace"
	}, required = false, description = "<workspace name>")
	private String workspace;

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
		if (workspace == null || workspace.isEmpty()) {
			workspace = geoserverClient.getConfig().getWorkspace();
		}

		Response listCvgStoresResponse = geoserverClient.getCoverageStores(workspace);

		if (listCvgStoresResponse.getStatus() == Status.OK.getStatusCode()) {
			System.out.println("\nGeoServer coverage stores list for '" + workspace + "':");

			JSONObject jsonResponse = JSONObject.fromObject(listCvgStoresResponse.getEntity());
			JSONArray cvgStores = jsonResponse.getJSONArray("coverageStores");
			System.out.println(cvgStores.toString(2));
		}
		else {
			System.err.println("Error getting GeoServer coverage stores list for '" + workspace + "'; code = " + listCvgStoresResponse.getStatus());
		}
	}
}
