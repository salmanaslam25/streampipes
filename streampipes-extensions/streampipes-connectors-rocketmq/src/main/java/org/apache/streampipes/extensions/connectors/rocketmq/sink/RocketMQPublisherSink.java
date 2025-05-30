/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.streampipes.extensions.connectors.rocketmq.sink;


import org.apache.streampipes.commons.exceptions.SpRuntimeException;
import org.apache.streampipes.dataformat.SpDataFormatDefinition;
import org.apache.streampipes.dataformat.JsonDataFormatDefinition;
import org.apache.streampipes.extensions.api.pe.IStreamPipesDataSink;
import org.apache.streampipes.extensions.api.pe.config.IDataSinkConfiguration;
import org.apache.streampipes.extensions.api.pe.context.EventSinkRuntimeContext;
import org.apache.streampipes.extensions.api.pe.param.IDataSinkParameters;
import org.apache.streampipes.model.DataSinkType;
import org.apache.streampipes.model.extensions.ExtensionAssetType;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.sdk.builder.DataSinkBuilder;
import org.apache.streampipes.sdk.builder.StreamRequirementsBuilder;
import org.apache.streampipes.sdk.builder.sink.DataSinkConfiguration;
import org.apache.streampipes.sdk.helpers.EpRequirements;
import org.apache.streampipes.sdk.helpers.Labels;
import org.apache.streampipes.sdk.helpers.Locales;

import com.google.common.annotations.VisibleForTesting;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;

import java.io.IOException;
import java.util.Map;

public class RocketMQPublisherSink implements IStreamPipesDataSink {

  public static final String TOPIC_KEY = "rocketmq-topic";
  public static final String ENDPOINT_KEY = "rocketmq-endpoint";

  private ClientServiceProvider provider;
  private Producer producer;

  private SpDataFormatDefinition spDataFormatDefinition;
  private RocketMQParameters params;

  public RocketMQPublisherSink() {
    this.provider = ClientServiceProvider.loadService();
  }

  @VisibleForTesting
  public RocketMQPublisherSink(ClientServiceProvider provider) {
    this.provider = provider;
  }

  @Override
  public IDataSinkConfiguration declareConfig() {
    return DataSinkConfiguration.create(
        RocketMQPublisherSink::new,
        DataSinkBuilder.create("org.apache.streampipes.sinks.brokers.jvm.rocketmq", 0)
            .category(DataSinkType.MESSAGING)
            .withLocales(Locales.EN)
            .withAssets(ExtensionAssetType.DOCUMENTATION, ExtensionAssetType.ICON)
            .requiredStream(StreamRequirementsBuilder
                .create()
                .requiredProperty(EpRequirements.anyProperty())
                .build())
            .requiredTextParameter(Labels.withId(ENDPOINT_KEY))
            .requiredTextParameter(Labels.withId(TOPIC_KEY))
            .build()
    );
  }


  @Override
  public void onPipelineStarted(IDataSinkParameters parameters, EventSinkRuntimeContext runtimeContext) {
    this.params = new RocketMQParameters(parameters);
    this.spDataFormatDefinition = new JsonDataFormatDefinition();

    ClientConfigurationBuilder builder = ClientConfiguration.newBuilder().setEndpoints(params.getEndpoint());
    ClientConfiguration configuration = builder.build();
    try {
      this.producer = provider.newProducerBuilder()
          .setTopics(params.getTopic())
          .setClientConfiguration(configuration)
          .build();
    } catch (ClientException e) {
      throw new SpRuntimeException(e);
    }
  }

  @Override
  public void onEvent(Event event) throws SpRuntimeException {
    Map<String, Object> rawMap = event.getRaw();
    byte[] jsonMessage = spDataFormatDefinition.fromMap(rawMap);

    Message message = provider.newMessageBuilder()
        .setTopic(params.getTopic())
        .setBody(jsonMessage)
        .build();
    try {
      producer.send(message);
    } catch (ClientException e) {
      throw new SpRuntimeException(e);
    }
  }

  @Override
  public void onPipelineStopped() {
    try {
      producer.close();
    } catch (IOException e) {
      throw new SpRuntimeException(e);
    }
  }
}
