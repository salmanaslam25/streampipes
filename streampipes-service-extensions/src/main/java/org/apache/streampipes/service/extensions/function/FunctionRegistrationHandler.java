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

package org.apache.streampipes.service.extensions.function;

import org.apache.streampipes.client.StreamPipesClient;
import org.apache.streampipes.model.function.FunctionDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The {@code FunctionRegistrationHandler} class is responsible for registering
 * a list of functions with the StreamPipes backend system.
 *
 * <p>Note: This class only handles the registration of the functions in the backend.
 * The actual execution of the registered functions occurs in the extension service.
 *
 * <p>Once the registration is successful, a log entry is created to indicate
 * the completion of the process.
 */
public class FunctionRegistrationHandler extends RegistrationHandler {

  private static final Logger LOG = LoggerFactory.getLogger(FunctionRegistrationHandler.class);

  public FunctionRegistrationHandler(List<FunctionDefinition> functions) {
    super(functions);
  }

  @Override
  protected void performRequest(StreamPipesClient client) {
    client.adminApi()
          .registerFunctions(functions);
  }

  @Override
  protected void logSuccess() {
    LOG.info("Successfully registered functions {}", functions.toString());
  }
}
