package com.tbscg.doosan.test;

import com.tbscg.doosan.magnolia.dxp.workflow.service.DynamoDBClientFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsSyncClientBuilder;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.dynamodb.services.local.main.ServerRunner;
import software.amazon.dynamodb.services.local.server.DynamoDBProxyServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;

@Slf4j
public class LocalDynamoService extends DynamoDBClientFactoryImpl {

    private DynamoDBProxyServer dynamoDbserver;
    private int port;

    public void start() throws Exception {
        start(findFreePort());
    }

    public void start(int port) throws Exception {
        this.port = port;
        System.setProperty("sqlite4java.library.path", "./native-libs");

        String currentDir = System.getProperty("user.dir");
        log.info("Current dir using System: {}", currentDir);

        dynamoDbserver = ServerRunner.createServerFromCommandLineArgs(new String[]{"-inMemory", "-sharedDb", "-port", String.valueOf(this.port)});
        dynamoDbserver.start();
    }

    @Override
    public DynamoDbClient getAmazonDynamoDB() {
        return DynamoDbClient.builder()
                .region(Region.EU_CENTRAL_1)
                .endpointOverride(URI.create("http://localhost:" + this.port))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("dummyKey", "dummySecret")))
                .build();
    }

    public void stop() throws Exception {
        dynamoDbserver.stop();
    }

    private int findFreePort() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new Exception("Failed to get free port", e);
        }
    }

}
