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

package org.apache.streampipes.model.staticproperty;

public abstract class DefaultStaticPropertyVisitor implements StaticPropertyVisitor {

  protected boolean ignoreValidation;

  public DefaultStaticPropertyVisitor(boolean ignoreValidation) {
    this.ignoreValidation = ignoreValidation;
  }

  public DefaultStaticPropertyVisitor() {
    this(false);
  }

  @Override
  public void visit(CollectionStaticProperty collectionStaticProperty) {
    collectionStaticProperty.getStaticPropertyTemplate().accept(this);
    collectionStaticProperty.getMembers().forEach(sp -> sp.accept(this));
  }

  @Override
  public void visit(StaticPropertyAlternative staticPropertyAlternative) {
    if (staticPropertyAlternative.getStaticProperty() != null) {
      staticPropertyAlternative.getStaticProperty().accept(this);
    }
  }

  @Override
  public void visit(StaticPropertyAlternatives staticPropertyAlternatives) {
    staticPropertyAlternatives.getAlternatives().forEach(alternative -> alternative.accept(this));
  }

  @Override
  public void visit(StaticPropertyGroup staticPropertyGroup) {
    staticPropertyGroup.getStaticProperties().forEach(sp -> sp.accept(this));
  }
}
