package com.sc.rate.limitor.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientInformation {

    private String clientId;
    private int requestLimit;
    private String unit;
}
