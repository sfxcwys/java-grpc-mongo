package com.github.sfxcwys.energy.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.bson.Document;

import java.io.IOException;

public class EnergyServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("mydb");
        MongoCollection<Document> collection = database.getCollection("energy");

        Server server = ServerBuilder.forPort(50051)
                .addService(new EnergyServiceImpl(collection))
                .build();

        server.start();
        System.out.println("Started EnergyServer");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received Shutdown Request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));

        server.awaitTermination();
    }
}
