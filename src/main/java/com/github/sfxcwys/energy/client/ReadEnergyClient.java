package com.github.sfxcwys.energy.client;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import com.proto.energy.EnergyData;
import com.proto.energy.EnergyServiceGrpc;
import com.proto.energy.ReadEnergyRequest;
import com.proto.energy.ReadEnergyResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.text.ParseException;
import java.time.Instant;

public class ReadEnergyClient {
    private int spaceshipId = 11;

    public static void main(String[] args) throws InterruptedException, ParseException {
        System.out.println("Hello I'm a gRPC client for ReadEnergy");

        ReadEnergyClient main = new ReadEnergyClient();
        main.run();
    }

    private void run() throws InterruptedException, ParseException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        EnergyServiceGrpc.EnergyServiceBlockingStub energyClient = EnergyServiceGrpc.newBlockingStub(channel);

        String startDateTime = String.format("%d-%d-%dT%d:%d:%dZ", 2019, 1, 1, 0, 0, 0);
        String endDateTime = String.format("%d-%d-%dT%d:%d:%dZ", 2019, 7, 1, 0, 0, 0);
        ReadEnergyRequest readEnergyRequest = ReadEnergyRequest.newBuilder()
                .setSpaceshipId(spaceshipId)
                .setStartDatetime(Timestamps.parse(startDateTime))
                .setEndDatetime(Timestamps.parse(endDateTime))
                .build();
        System.out.println(String.format("Reading data with startDateTime: %s endDateTime: %s and spaceshipId: %s",
                startDateTime, endDateTime, spaceshipId));

        ReadEnergyResponse readEnergyResponse = energyClient.readEnergy(readEnergyRequest);
        System.out.println(String.format("%d records found with status: %s", readEnergyResponse.getDataCount(),
                readEnergyResponse.getStatus()));
        for (EnergyData energyData : readEnergyResponse.getDataList()) {
            Timestamp datetime = energyData.getDatetime();
            Instant instant = Instant.ofEpochSecond(datetime.getSeconds(), datetime.getNanos());
            System.out.println(String.format("Data found: datetime=%s, value=%s", instant.toString(),
                    energyData.getValue()));
        }
    }
}
