package com.github.sfxcwys.energy.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.proto.energy.EnergyData;
import com.proto.energy.EnergyServiceGrpc;
import com.proto.energy.Status;
import com.proto.energy.StoreEnergyRequest;
import com.proto.energy.StoreEnergyResponse;
import io.grpc.stub.StreamObserver;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class EnergyServiceImpl extends EnergyServiceGrpc.EnergyServiceImplBase {

    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("mydb");
    private MongoCollection<Document> collection = database.getCollection("energy");

    @Override
    public void storeEnergy(StoreEnergyRequest request, StreamObserver<StoreEnergyResponse> responseObserver) {
        List<Document> docsToInsert = new ArrayList<>();
        StoreEnergyResponse.Builder storeEnergyResponseBuilder = StoreEnergyResponse.newBuilder();

        for (EnergyData energyData : request.getDataList()) {
            Document eachDoc = new Document("spaceship_id", request.getSpaceshipId()).append("datetime",
                    energyData.getDatetime())
                    .append("value", energyData.getValue());
            docsToInsert.add(eachDoc);
        }

        try {
            collection.insertMany(docsToInsert);
            System.out.println(String.format("Successfully inserted %d records", docsToInsert.size()));
            storeEnergyResponseBuilder.setStatus(Status.SUCCESS);
        } catch (Exception e) {
            System.out.println("Error when inserting into Mongo. Reason: " + e.getMessage());
            storeEnergyResponseBuilder.setStatus(Status.FAILED);
        }

        responseObserver.onNext(storeEnergyResponseBuilder.build());
        responseObserver.onCompleted();
    }
}
