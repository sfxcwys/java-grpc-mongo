package com.github.sfxcwys.energy.client;

import com.proto.energy.EnergyData;
import com.proto.energy.EnergyServiceGrpc;
import com.proto.energy.StoreEnergyRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EnergyClient {
    private Random randomGenerator = new Random();
    List<Integer> yearList = IntStream.rangeClosed(2019, 2020).boxed().collect(Collectors.toList());
    List<Integer> monthList = IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList());
    List<Integer> dayList = IntStream.rangeClosed(1, 30).boxed().collect(Collectors.toList());
    List<Integer> hourList = IntStream.rangeClosed(0, 23).boxed().collect(Collectors.toList());
    List<Integer> minuteList = IntStream.rangeClosed(0, 59).boxed().collect(Collectors.toList());
    List<Integer> secondList = IntStream.rangeClosed(0, 59).boxed().collect(Collectors.toList());
    int spaceshipId = 11;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello I'm a gRPC client for Energy");

        EnergyClient main = new EnergyClient();
        main.run();
    }

    private void run() throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        EnergyServiceGrpc.EnergyServiceBlockingStub energyClient = EnergyServiceGrpc.newBlockingStub(channel);

        for (int i = 0; i < 5; i++) {
            List<EnergyData> energyData = generateEnergyData();
            StoreEnergyRequest storeEnergyRequest = StoreEnergyRequest.newBuilder()
                    .addAllData(energyData)
                    .setSpaceshipId(spaceshipId)
                    .build();
            energyClient.storeEnergy(storeEnergyRequest);
            System.out.println(String.format("Inserted data for spaceshipId %d with %d energyData", spaceshipId,
                    energyData.size()));
            TimeUnit.SECONDS.sleep(1);
        }
    }

    private List<EnergyData> generateEnergyData() {
        List<EnergyData> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int year = yearList.get(randomGenerator.nextInt(yearList.size()));
            int month = monthList.get(randomGenerator.nextInt(monthList.size()));
            int day = dayList.get(randomGenerator.nextInt(dayList.size()));
            int hour = hourList.get(randomGenerator.nextInt(hourList.size()));
            int minute = minuteList.get(randomGenerator.nextInt(minuteList.size()));
            int second = secondList.get(randomGenerator.nextInt(secondList.size()));
            int value = randomGenerator.nextInt(10) + 1;
            String datetime = String.format("%d-%d-%dT%d:%d:%d", year, month, day, hour, minute, second);
            result.add(EnergyData.newBuilder()
                    .setDatetime(datetime)
                    .setValue(value)
                    .build());
        }
        return result;
    }
}
