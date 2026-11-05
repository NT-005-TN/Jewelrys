ALTER TABLE audit_log ALTER COLUMN user_id DROP NOT NULL;

CREATE FUNCTION workshop_audit() RETURNS trigger AS $$
DECLARE
    before_data jsonb;
    after_data jsonb;
    actor bigint;
    item_id bigint;
BEGIN
    actor := NULLIF(current_setting('app.user_id', true), '')::bigint;
    IF TG_OP <> 'INSERT' THEN before_data := to_jsonb(OLD); END IF;
    IF TG_OP <> 'DELETE' THEN after_data := to_jsonb(NEW); END IF;
    item_id := COALESCE((after_data->>'id')::bigint, (before_data->>'id')::bigint);
    -- Never copy credentials, reset/verification tokens or session hashes to audit.
    before_data := before_data - ARRAY['password_hash','verification_token','password_reset_token','refresh_token_hash'];
    after_data := after_data - ARRAY['password_hash','verification_token','password_reset_token','refresh_token_hash'];
    INSERT INTO audit_log(user_id, action, table_name, record_id, old_values, new_values)
    VALUES (actor, CASE TG_OP WHEN 'INSERT' THEN 'CREATE' ELSE TG_OP END,
            TG_TABLE_NAME, item_id, before_data::json, after_data::json);
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER audit_users AFTER INSERT OR UPDATE OR DELETE ON users FOR EACH ROW EXECUTE FUNCTION workshop_audit();
CREATE TRIGGER audit_clients AFTER INSERT OR UPDATE OR DELETE ON clients FOR EACH ROW EXECUTE FUNCTION workshop_audit();
CREATE TRIGGER audit_products AFTER INSERT OR UPDATE OR DELETE ON products FOR EACH ROW EXECUTE FUNCTION workshop_audit();
CREATE TRIGGER audit_orders AFTER INSERT OR UPDATE OR DELETE ON orders FOR EACH ROW EXECUTE FUNCTION workshop_audit();
CREATE TRIGGER audit_materials AFTER INSERT OR UPDATE OR DELETE ON materials FOR EACH ROW EXECUTE FUNCTION workshop_audit();
