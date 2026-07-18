package com.irp.core.agent;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_steps")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgentStep {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "agent_run_id", nullable = false)
    private UUID agentRunId;

    @Column(name = "step_index", nullable = false)
    private int stepIndex;

    @Column(name = "tool_name", nullable = false, length = 100)
    private String toolName;

    /**
     * A tool's output can be a JSON object or a JSON array depending on which tool ran
     * (e.g. post_investigation_result returns an object, search_errors returns a list) -
     * JsonNode stores either shape as-is rather than forcing a single Java type.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tool_input", columnDefinition = "jsonb")
    private JsonNode toolInput;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tool_output", columnDefinition = "jsonb")
    private JsonNode toolOutput;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AgentStep(UUID agentRunId, int stepIndex, String toolName, JsonNode toolInput, JsonNode toolOutput) {
        this.agentRunId = agentRunId;
        this.stepIndex = stepIndex;
        this.toolName = toolName;
        this.toolInput = toolInput;
        this.toolOutput = toolOutput;
    }
}
