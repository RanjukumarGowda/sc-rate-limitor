package com.sc.rate.limitor.domain.tokenbucket;

import com.sc.rate.limitor.domain.dto.ClientInformation;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientTokenBucketProviderTest {

    private final ClientTokenBucketProvider clientTokenBucketProvider = new ClientTokenBucketProvider();

    @Test
    public void givenValidClientInfo_shouldReturnValidTokenBucket() {
        var clientInformation = ClientInformation.builder()
                .requestLimit(10).clientId("1").unit("Minute").build();

        var bucket = clientTokenBucketProvider.getClientBandwidth(clientInformation);
        var consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);

        assertThat(consumptionProbe.getRemainingTokens()).isEqualTo(9);
        assertThat(consumptionProbe.isConsumed()).isTrue();

    }
}
