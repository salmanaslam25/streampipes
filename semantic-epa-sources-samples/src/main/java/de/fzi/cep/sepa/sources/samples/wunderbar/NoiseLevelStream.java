package de.fzi.cep.sepa.sources.samples.wunderbar;

import java.util.ArrayList;
import java.util.List;

import de.fzi.cep.sepa.model.builder.EpProperties;
import de.fzi.cep.sepa.model.impl.EventSchema;
import de.fzi.cep.sepa.model.impl.EventStream;
import de.fzi.cep.sepa.model.impl.eventproperty.EventProperty;
import de.fzi.cep.sepa.model.impl.graph.SepDescription;
import de.fzi.cep.sepa.sources.samples.config.SourcesConfig;

public class NoiseLevelStream extends AbstractWunderbarStream {
	
	@Override
	public EventStream declareModel(SepDescription sep) {
		EventStream stream = prepareStream(sep, WunderbarVariables.NOISE_LEVEL);
		
		EventSchema schema = new EventSchema();
		List<EventProperty> properties = new ArrayList<>();
		
		properties.add(timestampProperty());
		properties.add(EpProperties.doubleEp("noiseLevel", "http://schema.org/noiseLevel"));
		
		schema.setEventProperties(properties);
		stream.setEventSchema(schema);
		stream.setIconUrl(SourcesConfig.iconBaseUrl + "/SoundLevel.png");
		return stream;
	}


}
