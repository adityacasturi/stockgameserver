package com.function.stockmarketgame;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Register {

    public static String MASTER_KEY = "FfoB66b8mAcPAZrqiojscKmWXZUXWlq29ryLSMaeQEAEzlxfdgIs4faScQASns7UyTxoXMr2FWIxMOfCKnO2Ow==";
    public static String HOST = "https://stockmarketgamedb.documents.azure.com:443/";
                            
    private CosmosClient client;

    private final String databaseName = "stockmarketgamedb";
    private final String containerName = "users";

    private CosmosDatabase database;
    private CosmosContainer container;

    @FunctionName("Register")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
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
    
        final String firstName = request.getQueryParameters().get("first_name");
        final String lastName = request.getQueryParameters().get("last_name");
        final String email = request.getQueryParameters().get("email");
        final String username = request.getQueryParameters().get("username");
        final String password = request.getQueryParameters().get("password");

        User newUser = new User();
        newUser.setId(UUID.randomUUID().toString());
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword(password);

        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
        container.createItem(newUser, new PartitionKey(newUser.getId()), cosmosItemRequestOptions);

        return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + firstName).build();
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