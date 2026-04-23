package htw.wahlomat.wahlomat.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AnswerOption {
    STIMME_VOLL_ZU(4),
    STIMME_ZU(3),
    NEUTRAL(2),
    STIMME_NICHT_ZU(1),
    STIMME_UEBERHAUPT_NICHT_ZU(0),
    FRAGE_UEBERSPRINGEN(-1);

private final int value;

AnswerOption(int value) {
    this.value = value;
}

@JsonValue
public int getValue() {
    return value;
}

@JsonCreator
public static AnswerOption fromValue(int value) {
    for (AnswerOption option : values()) {
        if (option.value == value) {
            return option;
        }
    }
    throw new IllegalArgumentException("Ungültiger Wert: " + value);
}
}
