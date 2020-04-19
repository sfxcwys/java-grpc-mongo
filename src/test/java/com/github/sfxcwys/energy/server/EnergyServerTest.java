package com.github.sfxcwys.energy.server;

import com.google.protobuf.util.Timestamps;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.proto.energy.EnergyData;
import com.proto.energy.EnergyServiceGrpc;
import com.proto.energy.Status;
import com.proto.energy.StoreEnergyRequest;
import com.proto.energy.StoreEnergyResponse;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class EnergyServerTest {

    private int SPACESHIP_ID = 11;

    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private MongoClient client;
    private MongoServer server;
    private MongoCollection<Document> collection;

    private EnergyServiceGrpc.EnergyServiceBlockingStub blockingStub;

    @Before
    public void setUp() {
        // Create an in-memory MongoServer.
        server = new MongoServer(new MemoryBackend());

        // Bind on a random local port.
        InetSocketAddress serverAddress = server.bind();

        // Connect to the random local port created.
        client = MongoClients.create("mongodb://" + serverAddress.getHostString() + ":" + serverAddress.getPort());
        collection = client.getDatabase("testdb").getCollection("test_collection");
    }

    @After
    public void tearDown() {
        client.close();
        server.shutdown();
    }

    /**
     * Note for testing grpc: To test the server, make calls with a real stub using the in-process channel, and verify
     * behaviors or state changes from the client side.
     *
     * Test storeEnergy with valid input.
     */
    @Test
    public void energyServiceImpl_storeNonEmptyEnergyData() throws Exception {
        // Arrange

        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(new EnergyServiceImpl(collection)).build().start());

        EnergyServiceGrpc.EnergyServiceBlockingStub blockingStub = EnergyServiceGrpc.newBlockingStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));

        int value = 11;
        String datetime = String.format("%d-%d-%dT%d:%d:%dZ", 2020, 1, 1, 1, 1, 1);
        EnergyData energyData = EnergyData.newBuilder().setDatetime(Timestamps.parse(datetime)).setValue(value).build();

        // Act
        StoreEnergyRequest storeEnergyRequest = StoreEnergyRequest.newBuilder().setSpaceshipId(SPACESHIP_ID).addData(energyData).build();
        StoreEnergyResponse reply =
                blockingStub.storeEnergy(storeEnergyRequest);

        // Assert
        assertEquals(Status.SUCCESS, reply.getStatus());
        assertEquals(1, collection.countDocuments());
    }

    /**
     * Test storeEnergy with empty EnergyData.
     */
    @Test
    public void energyServiceImpl_storeEmptyEnergyData() throws Exception {
        // Arrange
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(new EnergyServiceImpl(collection)).build().start());

        EnergyServiceGrpc.EnergyServiceBlockingStub blockingStub = EnergyServiceGrpc.newBlockingStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));

        // Act
        StoreEnergyRequest storeEnergyRequest = StoreEnergyRequest.newBuilder().setSpaceshipId(SPACESHIP_ID).build();
        StoreEnergyResponse reply =
                blockingStub.storeEnergy(storeEnergyRequest);

        // Assert
        assertEquals(Status.FAILED, reply.getStatus());
        assertEquals(0, collection.countDocuments());
    }
}
