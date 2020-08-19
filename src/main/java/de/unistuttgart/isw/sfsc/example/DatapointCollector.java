package de.unistuttgart.isw.sfsc.example;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.adapter.configuration.AdapterConfiguration;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.config.Constants;
import de.unistuttgart.isw.sfsc.example.services.messages.PLC4XMonitorUpdate;
import de.unistuttgart.isw.sfsc.example.services.messages.PLC4XMonitoringRequest;
import de.unistuttgart.isw.sfsc.example.services.messages.SFSCDatapointUpdate;
import de.unistuttgart.isw.sfsc.framework.api.SfscServiceApi;
import de.unistuttgart.isw.sfsc.framework.api.SfscServiceApiFactory;
import de.unistuttgart.isw.sfsc.framework.api.services.clientserver.SfscClient;
import de.unistuttgart.isw.sfsc.framework.api.services.pubsub.SfscSubscriber;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class DatapointCollector {
    static AdapterConfiguration adapterConfiguration = new AdapterConfiguration();
    static String ServiceName = "de.universitystuttgart.isw.sfsc.cloudplug.datapoint";

    public static void main(String[] args){
        try {
            SfscServiceApi serverSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(adapterConfiguration);

            serverSfscServiceApi.addRegistryStoreEventListener(sfscServiceDescriptorStoreEvent -> {
                if(sfscServiceDescriptorStoreEvent.getData().getServiceName().equals(ServiceName) &&
                        sfscServiceDescriptorStoreEvent.getStoreEventType() == StoreEvent.StoreEventType.CREATE){
                    // Subscribe auf den Service
                    Map<String, ByteString> customtags =  sfscServiceDescriptorStoreEvent.getData().getCustomTagsMap();

                    String sourceId = customtags.get(Constants.DATA_SOURCE_ID_TAG).toStringUtf8();
                    String variableName = customtags.get(Constants.VARIABLE_NAME_TAG).toStringUtf8();
                    String dimension = customtags.get(Constants.DATA_DIMENSION_TAG).toStringUtf8();
                    String dataType = customtags.get(Constants.DATA_TYPE_TAG).toStringUtf8();

                    serverSfscServiceApi.subscriber(sfscServiceDescriptorStoreEvent.getData(), message -> {
                        try {
                            SFSCDatapointUpdate sfscDatapointUpdate = SFSCDatapointUpdate.parseFrom(message);
                            // Send to bridge
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
