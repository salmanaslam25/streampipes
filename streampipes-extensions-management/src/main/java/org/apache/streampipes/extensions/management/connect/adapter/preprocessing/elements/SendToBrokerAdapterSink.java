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
package org.apache.streampipes.extensions.management.connect.adapter.preprocessing.elements;

import org.apache.streampipes.commons.environment.Environment;
import org.apache.streampipes.commons.environment.Environments;
import org.apache.streampipes.dataformat.SpDataFormatDefinition;
import org.apache.streampipes.dataformat.SpDataFormatManager;
import org.apache.streampipes.extensions.api.connect.IAdapterPipelineElement;
import org.apache.streampipes.extensions.api.monitoring.SpMonitoringManager;
import org.apache.streampipes.extensions.management.monitoring.ExtensionsLogger;
import org.apache.streampipes.messaging.EventProducer;
import org.apache.streampipes.messaging.SpProtocolManager;
import org.apache.streampipes.model.connect.adapter.AdapterDescription;
import org.apache.streampipes.model.grounding.KafkaTransportProtocol;
import org.apache.streampipes.model.grounding.TransportProtocol;

import java.util.Map;

public class SendToBrokerAdapterSink implements IAdapterPipelineElement {

  protected AdapterDescription adapterDescription;
  protected SpDataFormatDefinition dataFormatDefinition;
  protected TransportProtocol protocol;
  private final EventProducer producer;

  public SendToBrokerAdapterSink(AdapterDescription adapterDescription) {
    this.adapterDescription = adapterDescription;
    this.protocol = adapterDescription
        .getEventGrounding()
        .getTransportProtocol();

    if (getEnvironment().getSpDebug().getValueOrDefault()) {
      modifyProtocolForDebugging(this.protocol);
    }

    var producerOpt = SpProtocolManager.INSTANCE.findDefinition(this.protocol);
    if (producerOpt.isPresent()) {
      this.producer = producerOpt.get().getProducer(this.protocol);

      this.dataFormatDefinition = SpDataFormatManager.getFormatDefinition();

      producer.connect();
    } else {
      throw new RuntimeException("Could not find protocol");
    }
  }

  @Override
  public Map<String, Object> process(Map<String, Object> event) {
    try {
      if (event != null) {
        sendToBroker(dataFormatDefinition.fromMap(event));
        SpMonitoringManager.INSTANCE.increaseOutCounter(
            adapterDescription.getElementId(),
            System.currentTimeMillis());
      }
    } catch (RuntimeException e) {
      new ExtensionsLogger(adapterDescription.getElementId()).error(e);
    }
    return null;
  }

  protected void sendToBroker(byte[] event) throws RuntimeException {
    producer.publish(event);
  }

  public void modifyProtocolForDebugging(TransportProtocol protocol) {
    protocol.setBrokerHostname("localhost");
    if (protocol instanceof KafkaTransportProtocol) {
      ((KafkaTransportProtocol) protocol).setKafkaPort(9094);
    }
  }

  private Environment getEnvironment() {
    return Environments.getEnvironment();
  }

}


