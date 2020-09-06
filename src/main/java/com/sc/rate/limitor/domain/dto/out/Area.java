package com.sc.rate.limitor.domain.dto.out;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Area {

    private final String shape;
    private final double area;

}
