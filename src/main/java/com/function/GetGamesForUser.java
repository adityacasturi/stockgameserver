package com.function;

import java.util.*;

import com.function.stockmarketgame.Game;
import com.function.stockmarketgame.GameInfo;
import com.function.stockmarketgame.UserPortfolio;
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

public class GetGamesForUser {

    public static String MASTER_KEY = "FfoB66b8mAcPAZrqiojscKmWXZUXWlq29ryLSMaeQEAEzlxfdgIs4faScQASns7UyTxoXMr2FWIxMOfCKnO2Ow==";
    public static String HOST = "https://stockmarketgamedb.documents.azure.com:443/";

    private CosmosClient client;

    private final String databaseName = "stockmarketgamedb";
    private final String gameContainerName = "games";
    private final String portfolioContainerName = "UserGamePortfolios";

    private CosmosDatabase database;
    private CosmosContainer gameContainer;
    private CosmosContainer portfolioContainer;


    @FunctionName("GetGamesForUser")
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
        gameContainer = createContainerIfNotExists(gameContainerName);
        portfolioContainer = createContainerIfNotExists(portfolioContainerName);

        // Parse query parameter
        final String userId = request.getQueryParameters().get("userId");

        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        queryOptions.setQueryMetricsEnabled(true);

        String query = "SELECT * FROM UserGamePortfolios u WHERE u.userId = '" + userId + "'";
        CosmosPagedIterable<UserPortfolio> userPortfolios = portfolioContainer.queryItems(query, queryOptions, UserPortfolio.class);

        // test change
        
        List<GameInfo> gameInfos = new ArrayList<>();
        while (userPortfolios.iterator().hasNext()) {
            UserPortfolio portfolio = userPortfolios.iterator().next();

            String gameId = portfolio.getGameId();
            String query1 = "SELECT * FROM games g WHERE g.gameId = '" + gameId + "'";
            CosmosPagedIterable<Game> games = gameContainer.queryItems(query1, que ryOptions, Game.class);
            String gameName = games.iterator().next().getName();

            GameInfo info = new GameInfo();
            info.setPortfolio(portfolio);
            info.setGameName(gameName);

            gameInfos.add(info);
        }

        return request.createResponseBuilder(HttpStatus.OK).body(gameInfos).build();
    }

    public void createDatabaseIfNotExists() throws Exception {
        CosmosDatabaseResponse cosmosDatabaseResponse = client.createDatabaseIfNotExists(databaseName);
        database = client.getDatabase(cosmosDatabaseResponse.getProperties().getId());
    }

    public CosmosContainer createContainerIfNotExists(String containerName) throws Exception {
        CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerName, "/id");
        CosmosContainerResponse cosmosContainerResponse =
                database.createContainerIfNotExists(containerProperties, ThroughputProperties.createManualThroughput(400));
        return database.getContainer(cosmosContainerResponse.getProperties().getId());
    }
}
