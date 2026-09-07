--liquibase formatted sql

--changeset andriy:ai-0001-analysis-schema
CREATE TABLE ai_analysis_requests (
    analysis_request_id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    requested_by_operator_id UUID,
    requested_by_operator_display_name VARCHAR(160) NOT NULL DEFAULT 'Unknown operator',
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    failure_reason TEXT
);

CREATE INDEX idx_ai_analysis_requests_customer_id ON ai_analysis_requests(customer_id);
CREATE INDEX idx_ai_analysis_requests_requested_by_operator_id ON ai_analysis_requests(requested_by_operator_id);
CREATE INDEX idx_ai_analysis_requests_status ON ai_analysis_requests(status);

CREATE TABLE ai_analysis_results (
    analysis_result_id UUID PRIMARY KEY,
    analysis_request_id UUID NOT NULL UNIQUE REFERENCES ai_analysis_requests(analysis_request_id),
    customer_id UUID NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    summary TEXT NOT NULL,
    recommendations TEXT NOT NULL,
    model_name VARCHAR(120) NOT NULL,
    prompt_version VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_ai_analysis_results_customer_id ON ai_analysis_results(customer_id);

CREATE TABLE ai_analysis_evidence (
    evidence_id UUID PRIMARY KEY,
    analysis_result_id UUID NOT NULL REFERENCES ai_analysis_results(analysis_result_id),
    source_type VARCHAR(40) NOT NULL,
    source_reference TEXT NOT NULL,
    excerpt TEXT NOT NULL,
    relevance_score NUMERIC(6, 4) NOT NULL
);

CREATE INDEX idx_ai_analysis_evidence_analysis_result_id ON ai_analysis_evidence(analysis_result_id);
