package ru.practicum.lib.enums;

import lombok.Getter;

public enum EventSort {
    EVENT_DATE("EVENT_DATE"),
    VIEWS("VIEWS");

    @Getter
    private final String title;

    EventSort(String title) {
        this.title = title;
    }
}