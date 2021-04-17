package com.function.stockmarketgame;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Signin {

    public static String MASTER_KEY = "FfoB66b8mAcPAZrqiojscKmWXZUXWlq29ryLSMaeQEAEzlxfdgIs4faScQASns7UyTxoXMr2FWIxMOfCKnO2Ow==";
    public static String HOST = "https://stockmarketgamedb.documents.azure.com:443/";
                            
    private CosmosClient client;

    private final String databaseName = "stockmarketgamedb";
    private final String containerName = "users";

    private CosmosDatabase database;
    private CosmosContainer container;
    
    @FunctionName("Signin")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws Exception {
        context.getLogger().info("Java HTTP trigger processed a request.");

        client = new CosmosClientBuilder()
        .endpoint(HOST)
        .key(MASTER_KEY)
        .preferredRegions(Collections.singletonList("West US"))
        .consistencyLevel(ConsistencyLevel.EVENTUAL)
        .buildClient();

        createDatabaseIfNotExists();
        createContainerIfNotExists();

        final String email = request.getQueryParameters().get("email");
        final String password = request.getQueryParameters().get("password");

        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        queryOptions.setQueryMetricsEnabled(true);

        String query = "SELECT * FROM users u WHERE u.email = '" + email + "'";
        CosmosPagedIterable<User> users = container.queryItems(query, queryOptions, User.class);
        
        if (users.iterator().hasNext()) {
            User existing = users.iterator().next();
            if ( existing.getPassword().equals(password)) {
                return request.createResponseBuilder(HttpStatus.OK).body("Successful login").build();
            } else {
                return request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body("Failed login").build();
            }
        } else {
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body("Unknown user").build();
        }
    }

    public void createDatabaseIfNotExists() throws Exception {
        CosmosDatabaseResponse cosmosDatabaseResponse = client.createDatabaseIfNotExists(databaseName);
        database = client.getDatabase(cosmosDatabaseResponse.getProperties().getId());
    }

    public void createContainerIfNotExists() throws Exception {
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(containerName, "/id");
        CosmosContainerResponse cosmosContainerResponse =
            database.createContainerIfNotExists(containerProperties, ThroughputProperties.createManualThroughput(400));
        container = database.getContainer(cosmosContainerResponse.getProperties().getId());
    }
}
