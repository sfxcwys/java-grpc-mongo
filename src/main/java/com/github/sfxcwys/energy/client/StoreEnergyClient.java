package com.github.sfxcwys.energy.client;

import com.google.protobuf.util.Timestamps;
import com.proto.energy.EnergyData;
import com.proto.energy.EnergyServiceGrpc;
import com.proto.energy.StoreEnergyRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StoreEnergyClient {
    private Random randomGenerator = new Random();
    List<Integer> yearList = IntStream.rangeClosed(2019, 2020).boxed().collect(Collectors.toList());
    List<Integer> monthList = IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList());
    List<Integer> dayList = IntStream.rangeClosed(1, 30).boxed().collect(Collectors.toList());
    List<Integer> hourList = IntStream.rangeClosed(0, 23).boxed().collect(Collectors.toList());
    List<Integer> minuteList = IntStream.rangeClosed(0, 59).boxed().collect(Collectors.toList());
    List<Integer> secondList = IntStream.rangeClosed(0, 59).boxed().collect(Collectors.toList());
    int spaceshipId = 11;

    public static void main(String[] args) throws InterruptedException, ParseException {
        System.out.println("Hello I'm a gRPC client for StoreEnergy");

        StoreEnergyClient main = new StoreEnergyClient();
        main.run();
    }

    private void run() throws InterruptedException, ParseException {
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

    private List<EnergyData> generateEnergyData() throws ParseException {
        List<EnergyData> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int year = yearList.get(randomGenerator.nextInt(yearList.size()));
            int month = monthList.get(randomGenerator.nextInt(monthList.size()));
            int day = dayList.get(randomGenerator.nextInt(dayList.size()));
            int hour = hourList.get(randomGenerator.nextInt(hourList.size()));
            int minute = minuteList.get(randomGenerator.nextInt(minuteList.size()));
            int second = secondList.get(randomGenerator.nextInt(secondList.size()));
            int value = randomGenerator.nextInt(10) + 1;
            String datetime = String.format("%d-%d-%dT%d:%d:%dZ", year, month, day, hour, minute, second);
            result.add(EnergyData.newBuilder()
                    .setDatetime(Timestamps.parse(datetime))
                    .setValue(value)
                    .build());
        }
        return result;
    }
}
