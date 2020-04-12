package com.github.sfxcwys.energy.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.proto.energy.EnergyServiceGrpc;
import com.proto.energy.StoreEnergyRequest;
import com.proto.energy.StoreEnergyResponse;
import io.grpc.stub.StreamObserver;
import org.bson.Document;

public class EnergyServiceImpl extends EnergyServiceGrpc.EnergyServiceImplBase {

    private MongoClient mongoClient = MongoClients.create("mongodv://localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("mydb");
    private MongoCollection<Document> collection = database.getCollection("energy");

    @Override
    public void storeEnergy(StoreEnergyRequest request, StreamObserver<StoreEnergyResponse> responseObserver) {
        super.storeEnergy(request, responseObserver);
    }
}
