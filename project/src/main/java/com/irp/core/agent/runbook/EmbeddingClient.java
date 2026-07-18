package com.irp.core.agent.runbook;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.irp.core.agent.AiServiceProperties;
import com.irp.core.common.exception.ServiceUnavailableException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Calls irp-ai-service's POST /v1/embeddings. Unlike {@link com.irp.core.agent.AiInvestigationClient},
 * failures here are NOT swallowed - runbook upload and search are useless without a real
 * embedding, so callers need to see and handle the error rather than silently no-op.
 */
@Component
public class EmbeddingClient {

    private final RestClient restClient;

    public EmbeddingClient(RestClient.Builder restClientBuilder, AiServiceProperties properties) {
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl())
                .defaultHeader("X-Internal-Token", properties.internalToken())
                .build();
    }

    public List<float[]> embed(List<String> texts) {
        try {
            EmbeddingResponse response = restClient.post()
                    .uri("/v1/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new EmbeddingRequest(texts))
                    .retrieve()
                    .body(EmbeddingResponse.class);
            if (response == null) {
                throw new ServiceUnavailableException("The AI service returned an empty embedding response");
            }
            return response.embeddings().stream().map(this::toFloatArray).toList();
        } catch (ServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceUnavailableException("Could not reach the AI service to compute embeddings: " + e.getMessage());
        }
    }

    public float[] embedOne(String text) {
        return embed(List.of(text)).get(0);
    }

    private float[] toFloatArray(List<Double> values) {
        float[] result = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i).floatValue();
        }
        return result;
    }

    private record EmbeddingRequest(@JsonProperty("texts") List<String> texts) {
    }

    private record EmbeddingResponse(@JsonProperty("embeddings") List<List<Double>> embeddings) {
    }
}
