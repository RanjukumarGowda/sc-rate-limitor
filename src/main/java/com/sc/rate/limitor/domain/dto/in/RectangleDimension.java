package com.sc.rate.limitor.domain.dto.in;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RectangleDimension {

    private final double length;
    private final double width;
}
