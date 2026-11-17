package com.stars.bigbang.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enregistre un ensemble de nombres dans une seule colonne texte : {12, 15, 20} devient "12,15,20".
 * Plus simple qu'une table de jointure quand on n'a jamais besoin de chercher par l'un de ces nombres.
 */
@Converter
public class LongSetConverter implements AttributeConverter<Set<Long>, String> {

    @Override
    public String convertToDatabaseColumn(Set<Long> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    @Override
    public Set<Long> convertToEntityAttribute(String column) {
        if (column == null || column.isBlank()) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(column.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
