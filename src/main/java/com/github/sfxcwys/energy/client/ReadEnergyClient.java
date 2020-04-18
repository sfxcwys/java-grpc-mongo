package com.github.sfxcwys.energy.client;

import com.proto.energy.EnergyServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ReadEnergyClient {
    int spaceshipId = 11;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello I'm a gRPC client for ReadEnergy");

        ReadEnergyClient main = new ReadEnergyClient();
        main.run();
    }

    private void run() throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        EnergyServiceGrpc.EnergyServiceBlockingStub energyClient = EnergyServiceGrpc.newBlockingStub(channel);
    }
}
