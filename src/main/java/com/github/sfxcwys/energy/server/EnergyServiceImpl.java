package com.github.sfxcwys.energy.server;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.proto.energy.EnergyData;
import com.proto.energy.EnergyServiceGrpc;
import com.proto.energy.ReadEnergyRequest;
import com.proto.energy.ReadEnergyResponse;
import com.proto.energy.Status;
import com.proto.energy.StoreEnergyRequest;
import com.proto.energy.StoreEnergyResponse;
import io.grpc.stub.StreamObserver;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class EnergyServiceImpl extends EnergyServiceGrpc.EnergyServiceImplBase {

    private MongoCollection<Document> collection;

    public EnergyServiceImpl(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    @Override
    public void readEnergy(ReadEnergyRequest request, StreamObserver<ReadEnergyResponse> responseObserver) {
        Timestamp startDateTime = request.getStartDatetime();
        Instant startInstant = Instant.ofEpochSecond(startDateTime.getSeconds(), startDateTime.getNanos());

        Timestamp endDateTime = request.getEndDatetime();
        Instant endInstant = Instant.ofEpochSecond(endDateTime.getSeconds(), endDateTime.getNanos());

        int spaceshipId = request.getSpaceshipId();

        FindIterable<Document> findIt = this.collection.find(new Document().append("spaceship_id", new Document().append(
                "$eq", spaceshipId))
                .append("datetime", new Document().append("$gte", startInstant)
                        .append("$lte", endInstant)));

        List<EnergyData> energyDataList = new ArrayList<>();
        ReadEnergyResponse readEnergyResponse;
        try (MongoCursor<Document> cursor = findIt.iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                System.out.println("Document found: " + doc);

                long date = doc.getDate("datetime")
                        .getTime();
                Timestamp datetime = Timestamps.fromMillis(date);
                int value = doc.getInteger("value");
                energyDataList.add(EnergyData.newBuilder()
                        .setValue(value)
                        .setDatetime(datetime)
                        .build());
            }
        } catch (Exception e) {
            readEnergyResponse = ReadEnergyResponse.newBuilder()
                    .setStatus(Status.FAILED)
                    .build();
            responseObserver.onNext(readEnergyResponse);
            responseObserver.onCompleted();

        }

        readEnergyResponse = ReadEnergyResponse.newBuilder()
                .setStatus(Status.SUCCESS)
                .addAllData(energyDataList)
                .build();
        responseObserver.onNext(readEnergyResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void storeEnergy(StoreEnergyRequest request, StreamObserver<StoreEnergyResponse> responseObserver) {
        List<Document> docsToInsert = new ArrayList<>();
        StoreEnergyResponse.Builder storeEnergyResponseBuilder = StoreEnergyResponse.newBuilder();

        for (EnergyData energyData : request.getDataList()) {
            Timestamp datetime = energyData.getDatetime();
            Instant instant = Instant.ofEpochSecond(datetime.getSeconds(), datetime.getNanos());
            Document eachDoc = new Document("spaceship_id", request.getSpaceshipId()).append("datetime",
                    instant)
                    .append("value", energyData.getValue());
            docsToInsert.add(eachDoc);
        }

        try {
            this.collection.insertMany(docsToInsert);
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
