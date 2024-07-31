package com.morris.quizly.models.system;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Flag {
    FLAG_ONE("flag_one"),
    FLAG_TWO("flag_two"),
    FLAG_LOCK("flag_lock");

    private final String code;

    Flag(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static Flag fromCode(String code) {
        for (Flag flag : values()) {
            if (flag.code.equalsIgnoreCase(code)) {
                return flag;
            }
        }
        throw new IllegalArgumentException("Unknown flag code: " + code);
    }
}
