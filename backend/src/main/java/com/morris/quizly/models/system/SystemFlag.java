package com.morris.quizly.models.system;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@Builder
@ToString
public class SystemFlag {
    private Instant timestamp;
    private FlagType flagType;
}
