-- Trace indexes
CREATE INDEX IF NOT EXISTS idx_trace_location ON traces USING GIST (location);
CREATE INDEX IF NOT EXISTS idx_trace_active ON traces (is_active);
CREATE INDEX IF NOT EXISTS idx_trace_author ON traces (author_id);
CREATE INDEX IF NOT EXISTS idx_trace_event ON traces (event_id);

-- Comment indexes
CREATE INDEX IF NOT EXISTS idx_comment_author ON comments (author_id);
CREATE INDEX IF NOT EXISTS idx_comment_trace ON comments (trace_id);
