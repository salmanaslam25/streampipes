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
package org.apache.streampipes.model.client.user;

import org.apache.streampipes.model.shared.annotation.TsModel;
import org.apache.streampipes.model.shared.api.Storable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

@TsModel
public class Group implements Storable {

  protected @SerializedName("_id") String groupId;
  protected @SerializedName("_rev") String rev;

  // This field should be called $type since this is the identifier used in the CouchDB view
  @SerializedName("$type")
  @JsonIgnore
  private String type = "group";

  private String groupName;

  private Set<String> roles;

  public Group() {
    this.roles = new HashSet<>();
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getRev() {
    return rev;
  }

  public void setRev(String rev) {
    this.rev = rev;
  }

  @Override
  public String getElementId() {
    return this.groupId;
  }

  @Override
  public void setElementId(String elementId) {
    this.groupId = elementId;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
