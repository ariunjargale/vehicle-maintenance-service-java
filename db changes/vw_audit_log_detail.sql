CREATE OR REPLACE VIEW vw_audit_log_detail AS
    SELECT
        a.audit_id,
        ad.detail_id,
        a.operation_type,
        a.table_name,
        a.primary_key_value,
        ad.column_name,
        ad.old_value,
        ad.new_value,
        u.username AS "Performed by",
        a.performed_at
    FROM
        audit_log        a
        LEFT JOIN audit_log_detail ad ON a.audit_id = ad.audit_id
        LEFT JOIN users            u ON a.performed_by = u.user_id;