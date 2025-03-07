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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { TreeNode } from '@ali-hm/angular-tree-component';
import {
    DataType,
    EventProperty,
    EventPropertyList,
    EventPropertyNested,
    EventPropertyPrimitive,
    EventPropertyUnion,
    EventSchema,
    SemanticType,
    FieldStatusInfo,
} from '@streampipes/platform-services';
import { EditEventPropertyComponent } from '../../../../dialog/edit-event-property/edit-event-property.component';
import { DialogService, PanelType } from '@streampipes/shared-ui';
import { StaticValueTransformService } from '../../../../services/static-value-transform.service';
import { EventPropertyUtilsService } from '../../../../services/event-property-utils.service';
import { ShepherdService } from '../../../../../services/tour/shepherd.service';
import { IdGeneratorService } from '../../../../../core-services/id-generator/id-generator.service';

@Component({
    selector: 'sp-event-property-row',
    templateUrl: './event-property-row.component.html',
    styleUrls: ['./event-property-row.component.scss'],
})
export class EventPropertyRowComponent implements OnInit {
    @Input() node: TreeNode;
    @Input() isEditable = true;
    @Input() eventSchema: EventSchema = new EventSchema();
    @Input() originalEventSchema: EventSchema;
    @Input() countSelected: number;
    @Input() fieldStatusInfo: Record<string, FieldStatusInfo>;

    @Output() isEditableChange = new EventEmitter<boolean>();
    @Output() eventSchemaChange = new EventEmitter<EventSchema>();
    @Output() originalEventSchemaChange = new EventEmitter<EventSchema>();
    @Output() refreshTreeEmitter = new EventEmitter<boolean>();
    @Output() countSelectedChange = new EventEmitter<number>();

    label: string;

    isPrimitive = false;
    isNested = false;
    isList = false;
    isStaticValue = false;

    timestampProperty = false;
    showFieldStatus = false;

    runtimeType: string;
    originalRuntimeType: string;
    originalRuntimeName: string;
    originalProperty: EventPropertyUnion;

    constructor(
        private staticValueService: StaticValueTransformService,
        private dialogService: DialogService,
        private epUtils: EventPropertyUtilsService,
        private shepherdService: ShepherdService,
        private idGeneratorService: IdGeneratorService,
    ) {}

    ngOnInit() {
        this.label = this.getLabel(this.node.data);
        this.isPrimitive = this.isEventPropertyPrimitive(this.node.data);
        this.isList = this.isEventPropertyList(this.node.data);
        this.isNested = this.isEventPropertyNested(this.node.data);
        this.isStaticValue = this.staticValueService.isStaticValueProperty(
            this.node.data.elementId,
        );
        this.timestampProperty = this.isTimestampProperty(this.node.data);

        if (this.node.data instanceof EventProperty) {
            this.originalProperty = this.epUtils.findPropertyByElementId(
                this.originalEventSchema.eventProperties,
                this.node.data.elementId,
            );
            this.checkAndDisplayProperties();
        }

        if (!this.node.data.propertyScope) {
            this.node.data.propertyScope = 'MEASUREMENT_PROPERTY';
        }
    }

    private checkAndDisplayProperties() {
        if (this.originalProperty) {
            this.applyDisplayedProperties(this.originalProperty);
        } else {
            this.applyDisplayedProperties(this.node.data);
        }
    }

    private applyDisplayedProperties(ep: EventProperty) {
        this.originalRuntimeName = ep.runtimeName;
        this.showFieldStatus =
            this.fieldStatusInfo &&
            this.fieldStatusInfo[this.originalRuntimeName] !== undefined;
        if (this.isPrimitive) {
            this.originalRuntimeType = this.parseType(
                (ep as EventPropertyPrimitive).runtimeType,
            );
            this.runtimeType = this.parseType(
                (this.node.data as EventPropertyPrimitive).runtimeType,
            );
        }
    }

    private parseType(runtimeType: string) {
        return runtimeType.split('#')[1].toUpperCase();
    }

    private isEventPropertyPrimitive(instance: EventProperty): boolean {
        return instance instanceof EventPropertyPrimitive;
    }

    private isEventPropertyNested(instance: EventProperty): boolean {
        return instance instanceof EventPropertyNested;
    }

    private isEventPropertyList(instance: EventProperty): boolean {
        return instance instanceof EventPropertyList;
    }

    public getLabel(eventProperty: EventProperty) {
        if (eventProperty.label && eventProperty.label !== '') {
            return eventProperty.label;
        } else if (
            eventProperty.runtimeName !== undefined &&
            eventProperty.runtimeName !== ''
        ) {
            return eventProperty.runtimeName;
        }
        if (this.isEventPropertyNested(eventProperty)) {
            return 'Nested Property';
        }
        if (eventProperty instanceof EventSchema) {
            return '';
        }
        return 'Property';
    }

    isTimestampProperty(node) {
        if (node.semanticType !== undefined && SemanticType.isTimestamp(node)) {
            node.runtimeType = DataType.LONG;
            return true;
        } else {
            return false;
        }
    }

    public openEditDialog(data): void {
        const dialogRef = this.dialogService.open(EditEventPropertyComponent, {
            panelType: PanelType.SLIDE_IN_PANEL,
            title: 'Edit field ' + data.runtimeName,
            width: '50vw',
            data: {
                property: data,
                originalProperty: this.originalProperty,
                isEditable: this.isEditable,
            },
        });
        this.shepherdService.trigger('adapter-edit-field-clicked');

        dialogRef.afterClosed().subscribe(refresh => {
            this.timestampProperty = this.isTimestampProperty(this.node.data);
            this.label = this.getLabel(this.node.data);
            this.checkAndDisplayProperties();
            this.refreshTreeEmitter.emit(true);
        });
    }

    public selectProperty(id: string, eventProperties: any): void {
        if (!this.isEditable) {
            return;
        }
        eventProperties = eventProperties || this.eventSchema.eventProperties;
        for (const eventProperty of eventProperties) {
            if (
                eventProperty.eventProperties &&
                eventProperty.eventProperties.length > 0
            ) {
                if (eventProperty.id === id) {
                    if (eventProperty.selected) {
                        eventProperty.selected = undefined;
                        this.countSelected--;
                        this.selectProperty(
                            'none',
                            eventProperty.eventProperties,
                        );
                    } else {
                        eventProperty.selected = true;
                        this.countSelected++;
                        this.selectProperty(
                            'all',
                            eventProperty.eventProperties,
                        );
                    }
                } else if (id === 'all') {
                    eventProperty.selected = true;
                    this.countSelected++;
                    this.selectProperty('all', eventProperty.eventProperties);
                } else if (id === 'none') {
                    eventProperty.selected = undefined;
                    this.countSelected--;
                    this.selectProperty('none', eventProperty.eventProperties);
                } else {
                    this.selectProperty(id, eventProperty.eventProperties);
                }
            } else {
                if (eventProperty.id === id) {
                    if (eventProperty.selected) {
                        eventProperty.selected = undefined;
                        this.countSelected--;
                    } else {
                        eventProperty.selected = true;
                        this.countSelected++;
                    }
                } else if (id === 'all') {
                    eventProperty.selected = true;
                    this.countSelected++;
                } else if (id === 'none') {
                    eventProperty.selected = undefined;
                    this.countSelected--;
                }
            }
        }
        this.countSelectedChange.emit(this.countSelected);
        this.refreshTreeEmitter.emit(false);
    }

    public addNestedProperty(eventProperty: EventPropertyNested): void {
        const uuid: string = this.idGeneratorService.generate(25);
        if (!eventProperty.eventProperties) {
            eventProperty.eventProperties = new Array<EventPropertyUnion>();
        }
        const property: EventPropertyNested = new EventPropertyNested();
        property.elementId = uuid;
        eventProperty.eventProperties.push(property);
        this.refreshTreeEmitter.emit(false);
    }
}
