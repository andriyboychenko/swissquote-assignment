package com.example.swissquote.infrastructure.persistence;

import com.example.swissquote.application.analysis.AiAnalysisRepository;
import com.example.swissquote.domain.analysis.AiAnalysisEvidence;
import com.example.swissquote.domain.analysis.AiAnalysisRequest;
import com.example.swissquote.domain.analysis.AiAnalysisResult;
import com.example.swissquote.domain.analysis.AiAnalysisStatus;
import com.example.swissquote.domain.analysis.RiskLevel;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcAiAnalysisRepository implements AiAnalysisRepository {

    private static final String UPSERT_REQUEST_SQL = """
            INSERT INTO ai_analysis_requests (
                analysis_request_id,
                customer_id,
                requested_by_operator_id,
                status,
                requested_at,
                started_at,
                completed_at,
                failure_reason
            )
            VALUES (
                :analysisRequestId,
                :customerId,
                :requestedByOperatorId,
                :status,
                :requestedAt,
                :startedAt,
                :completedAt,
                :failureReason
            )
            ON CONFLICT (analysis_request_id) DO UPDATE SET
                status = EXCLUDED.status,
                started_at = EXCLUDED.started_at,
                completed_at = EXCLUDED.completed_at,
                failure_reason = EXCLUDED.failure_reason
            """;
    private static final String INSERT_RESULT_SQL = """
            INSERT INTO ai_analysis_results (
                analysis_result_id,
                analysis_request_id,
                customer_id,
                risk_level,
                summary,
                recommendations,
                model_name,
                prompt_version,
                created_at
            )
            VALUES (
                :analysisResultId,
                :analysisRequestId,
                :customerId,
                :riskLevel,
                :summary,
                :recommendations,
                :modelName,
                :promptVersion,
                :createdAt
            )
            ON CONFLICT (analysis_request_id) DO UPDATE SET
                risk_level = EXCLUDED.risk_level,
                summary = EXCLUDED.summary,
                recommendations = EXCLUDED.recommendations,
                model_name = EXCLUDED.model_name,
                prompt_version = EXCLUDED.prompt_version,
                created_at = EXCLUDED.created_at
            """;
    private static final String INSERT_EVIDENCE_SQL = """
            INSERT INTO ai_analysis_evidence (
                evidence_id,
                analysis_result_id,
                source_type,
                source_reference,
                excerpt,
                relevance_score
            )
            VALUES (
                :evidenceId,
                :analysisResultId,
                :sourceType,
                :sourceReference,
                :excerpt,
                :relevanceScore
            )
            """;
    private static final String DELETE_EVIDENCE_SQL = """
            DELETE FROM ai_analysis_evidence
            WHERE analysis_result_id = :analysisResultId
            """;
    private static final String FIND_BY_CUSTOMER_SQL = """
            SELECT
                request.analysis_request_id,
                request.customer_id,
                request.requested_by_operator_id,
                request.status,
                request.requested_at,
                request.started_at,
                request.completed_at,
                request.failure_reason,
                result.analysis_result_id,
                result.risk_level,
                result.summary,
                result.recommendations,
                result.model_name,
                result.prompt_version,
                result.created_at AS result_created_at
            FROM ai_analysis_requests request
            LEFT JOIN ai_analysis_results result ON result.analysis_request_id = request.analysis_request_id
            WHERE request.customer_id = :customerId
            ORDER BY request.requested_at DESC
            """;
    private static final String FIND_EVIDENCE_SQL = """
            SELECT evidence_id, source_type, source_reference, excerpt, relevance_score
            FROM ai_analysis_evidence
            WHERE analysis_result_id = :analysisResultId
            ORDER BY relevance_score DESC, source_reference ASC
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcAiAnalysisRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AiAnalysisRequest save(AiAnalysisRequest request) {
        jdbcTemplate.update(UPSERT_REQUEST_SQL, toRequestParameters(request));

        if (request.result() != null) {
            jdbcTemplate.update(INSERT_RESULT_SQL, toResultParameters(request, request.result()));
            jdbcTemplate.update(
                    DELETE_EVIDENCE_SQL,
                    new MapSqlParameterSource("analysisResultId", request.result().analysisResultId())
            );
            request.result().evidence().forEach(evidence -> jdbcTemplate.update(
                    INSERT_EVIDENCE_SQL,
                    toEvidenceParameters(request.result().analysisResultId(), evidence)
            ));
        }

        return request;
    }

    @Override
    public List<AiAnalysisRequest> findByCustomerId(UUID customerId) {
        return jdbcTemplate.query(
                FIND_BY_CUSTOMER_SQL,
                new MapSqlParameterSource("customerId", customerId),
                (resultSet, rowNumber) -> mapRequest(resultSet, findEvidence(resultSet))
        );
    }

    private List<AiAnalysisEvidence> findEvidence(ResultSet resultSet) throws SQLException {
        UUID analysisResultId = resultSet.getObject("analysis_result_id", UUID.class);
        if (analysisResultId == null) {
            return List.of();
        }

        return jdbcTemplate.query(
                FIND_EVIDENCE_SQL,
                new MapSqlParameterSource("analysisResultId", analysisResultId),
                JdbcAiAnalysisRepository::mapEvidence
        );
    }

    private static MapSqlParameterSource toRequestParameters(AiAnalysisRequest request) {
        return new MapSqlParameterSource()
                .addValue("analysisRequestId", request.analysisRequestId())
                .addValue("customerId", request.customerId())
                .addValue("requestedByOperatorId", request.requestedByOperatorId())
                .addValue("status", request.status().name())
                .addValue("requestedAt", Timestamp.from(request.requestedAt()))
                .addValue("startedAt", toTimestamp(request.startedAt()))
                .addValue("completedAt", toTimestamp(request.completedAt()))
                .addValue("failureReason", request.failureReason());
    }

    private static MapSqlParameterSource toResultParameters(AiAnalysisRequest request, AiAnalysisResult result) {
        return new MapSqlParameterSource()
                .addValue("analysisResultId", result.analysisResultId())
                .addValue("analysisRequestId", request.analysisRequestId())
                .addValue("customerId", request.customerId())
                .addValue("riskLevel", result.riskLevel().name())
                .addValue("summary", result.summary())
                .addValue("recommendations", result.recommendations())
                .addValue("modelName", result.modelName())
                .addValue("promptVersion", result.promptVersion())
                .addValue("createdAt", Timestamp.from(result.createdAt()));
    }

    private static MapSqlParameterSource toEvidenceParameters(UUID analysisResultId, AiAnalysisEvidence evidence) {
        return new MapSqlParameterSource()
                .addValue("evidenceId", evidence.evidenceId())
                .addValue("analysisResultId", analysisResultId)
                .addValue("sourceType", evidence.sourceType())
                .addValue("sourceReference", evidence.sourceReference())
                .addValue("excerpt", evidence.excerpt())
                .addValue("relevanceScore", evidence.relevanceScore());
    }

    private static Timestamp toTimestamp(java.time.Instant value) {
        return value == null ? null : Timestamp.from(value);
    }

    private static AiAnalysisRequest mapRequest(ResultSet resultSet, List<AiAnalysisEvidence> evidence) throws SQLException {
        UUID resultId = resultSet.getObject("analysis_result_id", UUID.class);
        AiAnalysisResult result = resultId == null ? null : new AiAnalysisResult(
                resultId,
                RiskLevel.valueOf(resultSet.getString("risk_level")),
                resultSet.getString("summary"),
                resultSet.getString("recommendations"),
                resultSet.getString("model_name"),
                resultSet.getString("prompt_version"),
                resultSet.getTimestamp("result_created_at").toInstant(),
                evidence
        );

        return new AiAnalysisRequest(
                resultSet.getObject("analysis_request_id", UUID.class),
                resultSet.getObject("customer_id", UUID.class),
                resultSet.getObject("requested_by_operator_id", UUID.class),
                AiAnalysisStatus.valueOf(resultSet.getString("status")),
                resultSet.getTimestamp("requested_at").toInstant(),
                toInstant(resultSet, "started_at"),
                toInstant(resultSet, "completed_at"),
                resultSet.getString("failure_reason"),
                result
        );
    }

    private static java.time.Instant toInstant(ResultSet resultSet, String columnName) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static AiAnalysisEvidence mapEvidence(ResultSet resultSet, int rowNumber) throws SQLException {
        return new AiAnalysisEvidence(
                resultSet.getObject("evidence_id", UUID.class),
                resultSet.getString("source_type"),
                resultSet.getString("source_reference"),
                resultSet.getString("excerpt"),
                resultSet.getBigDecimal("relevance_score")
        );
    }
}
