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

package org.apache.streampipes.storage.couchdb.utils;

import org.apache.streampipes.commons.environment.Environment;
import org.apache.streampipes.commons.environment.Environments;
import org.apache.streampipes.storage.couchdb.serializer.GsonSerializer;

import com.google.common.net.UrlEscapers;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;

public class Utils {

  public static final String USER_DB_NAME = "users";

  public static CouchDbClient getCouchDbDataProcessorDescriptionClient() {
    return getCouchDbGsonClient("data-processor");
  }

  public static String escapePathSegment(String urlSegment) {
    return UrlEscapers.urlPathSegmentEscaper().escape(urlSegment);
  }

  public static CouchDbClient getCouchDbDataStreamDescriptionClient() {
    return getCouchDbGsonClient("data-stream");
  }

  public static CouchDbClient getCouchDbDataSinkDescriptionClient() {
    return getCouchDbGsonClient("data-sink");
  }

  public static CouchDbClient getCouchDbImageClient() {
    return getCouchDbGsonClient("images");
  }

  public static CouchDbClient getCouchDbPipelineElementTemplateClient() {
    return getCouchDbGsonClient("pipelineelementtemplate-v2");
  }

  public static CouchDbClient getCouchDbPipelineCanvasMetadataClient() {
    return getCouchDbGsonClient("pipelinecanvasmetadata");
  }

  public static CouchDbClient getCouchDbGeneralConfigStorage() {
    return getCouchDbGsonClient("general-configuration");
  }

  public static CouchDbClient getCouchDbFileMetadataClient() {
    return getCouchDbGsonClient("filemetadata");
  }

  public static CouchDbClient getCouchDbAdapterInstanceClient() {
    return getCouchDbAdapterClient("adapterinstance");
  }

  public static CouchDbClient getCouchDbAdapterInstanceBackupClient() {
    return new CouchDbClient(props("adapterinstance_backup"));
  }

  public static CouchDbClient getCouchDbAdapterDescriptionClient() {
    return getCouchDbAdapterClient("adapterdescription");
  }

  public static CouchDbClient getCouchDbPipelineClient() {
    return getCouchDbGsonClient("pipeline");
  }

  public static CouchDbClient getCouchDbUserClient() {
    return getCouchDbPrincipalClient(USER_DB_NAME);
  }

  public static CouchDbClient getCouchDbNotificationClient() {
    return getCouchDbStandardSerializerClient("notification");
  }

  public static CouchDbClient getCouchDbPipelineCategoriesClient() {
    return getCouchDbStandardSerializerClient("pipelinecategories");
  }

  public static CouchDbClient getCouchDbGsonClient(String dbname) {
    CouchDbClient dbClient = new CouchDbClient(props(dbname));
    dbClient.setGsonBuilder(GsonSerializer.getGsonBuilder());
    return dbClient;
  }

  private static CouchDbClient getCouchDbPrincipalClient(String dbname) {
    CouchDbClient dbClient = new CouchDbClient(props(dbname));
    dbClient.setGsonBuilder(GsonSerializer.getPrincipalGsonBuilder());
    return dbClient;
  }

  private static CouchDbClient getCouchDbAdapterClient(String dbname) {
    CouchDbClient dbClient = new CouchDbClient(props(dbname));
    dbClient.setGsonBuilder(GsonSerializer.getAdapterGsonBuilder());
    return dbClient;
  }

  private static CouchDbClient getCouchDbStandardSerializerClient(String dbname) {
    return new CouchDbClient(props(dbname));
  }

  public static CouchDbClient getCouchDbClient(String database, boolean createIfNotExists) {
    return new CouchDbClient(props(database, createIfNotExists));
  }

  private static CouchDbProperties props(String dbname,
                                         boolean createDbIfNotExists) {
    var env = getEnvironment();
    return new CouchDbProperties(
        dbname,
        createDbIfNotExists,
        env.getCouchDbProtocol().getValueOrDefault(),
        env.getCouchDbHost().getValueOrDefault(),
        env.getCouchDbPort().getValueOrDefault(),
        env.getCouchDbUsername().getValueOrDefault(),
        env.getCouchDbPassword().getValueOrDefault());
  }

  private static CouchDbProperties props(String dbname) {
    return props(dbname, true);
  }

  public static String getDatabaseRoute(String databaseName) {
    return toUrl() + "/" + databaseName;
  }

  private static String toUrl() {
    var env = getEnvironment();
    return env.getCouchDbProtocol().getValueOrDefault()
        + "://" + env.getCouchDbHost().getValueOrDefault()
        + ":" + env.getCouchDbPort().getValueOrDefault();
  }

  public static Request getRequest(String route) {
    return append(Request.Get(route));
  }

  public static Request postRequest(String route,
                                    String payload) {
    return append(Request.Post(route).bodyString(payload, ContentType.APPLICATION_JSON));
  }

  public static Request deleteRequest(String route) {
    return append(Request.Delete(route));
  }

  public static Request putRequest(String route,
                                   String payload) {
    return append(Request.Put(route).bodyString(payload, ContentType.APPLICATION_JSON));
  }

  public static Request putRequest(String route,
                                   byte[] payload,
                                   String contentType) {
    return append(Request.Put(route).bodyByteArray(payload, ContentType.getByMimeType(contentType)));
  }

  private static Environment getEnvironment() {
    return Environments.getEnvironment();
  }

  public static Request append(Request req) {
    String encodedAuth = getAuthHeaderValue();
    req
        .setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
        .connectTimeout(1000)
        .socketTimeout(100000);

    return req;
  }

  private static String getAuthHeaderValue() {
    var env = getEnvironment();
    var auth = getUserAndPassword(env);
    var encoded = Base64.encodeBase64(auth.getBytes());
    return new String(encoded);
  }

  private static String getUserAndPassword(Environment env) {
    return env.getCouchDbUsername().getValueOrDefault()
        + ":"
        + env.getCouchDbPassword().getValueOrDefault();
  }
}
