package com.example.spotspeak.converter;

import com.example.spotspeak.entity.TraceType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TraceTypeConverter implements AttributeConverter<TraceType, String> {

	@Override
	public String convertToDatabaseColumn(TraceType attribute) throws IllegalArgumentException {
		if (attribute == null) {
			return null;
		}

		switch (attribute) {
			case TraceType.VIDEO:
				return "VIDEO";
			case TraceType.PHOTO:
				return "PHOTO";
			case TraceType.TEXTONLY:
				return "TEXTONLY";
			default:
				throw new IllegalArgumentException(
						attribute + " is not a valid enum value for TraceType");
		}
	}

	@Override
	public TraceType convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}

		switch (dbData) {
			case "VIDEO":
				return TraceType.VIDEO;
			case "PHOTO":
				return TraceType.PHOTO;
			case "TEXTONLY":
				return TraceType.TEXTONLY;

			default:
				throw new IllegalArgumentException(
						dbData + " is not valid entry in the database for TraceType");
		}
	}

}
