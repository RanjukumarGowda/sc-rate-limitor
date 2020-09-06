package com.sc.rate.limitor.domain.dto.in;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TriangleDimension {

    private final double base;
    private final double height;
}
