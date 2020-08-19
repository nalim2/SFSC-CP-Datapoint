package de.unistuttgart.isw.sfsc.service;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.adapter.configuration.AdapterConfiguration;
import de.unistuttgart.isw.sfsc.config.Constants;
import de.unistuttgart.isw.sfsc.example.services.messages.PLC4XMonitorUpdate;
import de.unistuttgart.isw.sfsc.example.services.messages.PLC4XMonitorUpdate.Timestamp;
import de.unistuttgart.isw.sfsc.example.services.messages.PLC4XMonitoringRequest;
import de.unistuttgart.isw.sfsc.example.services.messages.SFSCDatapointUpdate;
import de.unistuttgart.isw.sfsc.framework.api.SfscServiceApi;
import de.unistuttgart.isw.sfsc.framework.api.SfscServiceApiFactory;
import de.unistuttgart.isw.sfsc.framework.api.services.channelfactory.SfscChannelFactoryParameter;
import de.unistuttgart.isw.sfsc.framework.api.services.clientserver.SfscServer;
import de.unistuttgart.isw.sfsc.framework.api.services.pubsub.SfscPublisher;
import de.unistuttgart.isw.sfsc.framework.api.services.pubsub.SfscPublisherParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Function;
public class DatapointService {
    static AdapterConfiguration bootstrapConfiguration1;
    static ByteString uuid = ByteString.copyFromUtf8(UUID.randomUUID().toString());
    static String ServiceName = "de.universitystuttgart.isw.sfsc.cloudplug.datapoint";
    static Logger log = LoggerFactory.getLogger(DatapointService.class);
    static ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(4);

    public static void main(String[] args) {
        bootstrapConfiguration1 = new AdapterConfiguration().setCoreHost(Constants.CORE_ADDRESS).setCorePubTcpPort(Constants.CORE_PORT);
        for (int threadCounter = 0; threadCounter < 10; threadCounter++){
            final  int finalThreadCounter = threadCounter;
            threadPoolExecutor.submit(() -> registerSFSCService("Test-Thread-Source-"+finalThreadCounter, "ThreadVariableName" + finalThreadCounter, "STRING", 1, "", ""));

        }
    }

    static void registerSFSCService(String sourceId, String variableName, String datatype, int dimension, String plc4xConnection, String plc4xAddress) {
        try {
            SfscServiceApi serverSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(bootstrapConfiguration1);
            Map<String, ByteString> customTags = new HashMap<>();
            customTags.put(Constants.DATA_SOURCE_ID_TAG, ByteString.copyFromUtf8(sourceId));
            customTags.put(Constants.VARIABLE_NAME_TAG, ByteString.copyFromUtf8(variableName));
            customTags.put(Constants.DATA_TYPE_TAG, ByteString.copyFromUtf8(datatype));
            customTags.put(Constants.DATA_DIMENSION_TAG, ByteString.copyFromUtf8(dimension + ""));
            SfscPublisher publisher = serverSfscServiceApi.publisher(new SfscPublisherParameter()
                    .setServiceName(ServiceName)
                    .setOutputMessageType(ByteString.copyFromUtf8(SFSCDatapointUpdate.class.getName()))
                    .setCustomTags(customTags)); // channel factory

            // TODO: this is just a dummy implementation here will be the integration of the PLC4X-Integration project
            for(int counter = 0; counter < 100000; counter++){

                SFSCDatapointUpdate updateMsg = SFSCDatapointUpdate.newBuilder()
                        .setTimestamp(System.currentTimeMillis())
                        .setValue(sourceId + " in iteration Counter=" + counter)
                        .build();
                publisher.publish(updateMsg);
                Thread.sleep(1000);

            }

            publisher.close();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Timeout Exception because there is no SFSC-Core available. \n Exception: " + e.getMessage());
        } catch (TimeoutException e) {
            log.error("Timeout Exception because there is no SFSC-Core available. \n Exception: " + e.getMessage());
            log.info("Try to restart the service in 10 seconds");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            registerSFSCService(sourceId, variableName, datatype, dimension, plc4xConnection, plc4xAddress);
        }
    }

}
