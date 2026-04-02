-- Migration: Split arch_application table - separate core fields from extended attributes
-- Version: meta_v2
-- Description: Split arch_application into core columns + ext_attrs JSON column
--              Extended fields (54) moved to JSON, core fields (40) remain as columns
-- Date: 2026-03-31

-- =============================================
-- 1. Add ext_attrs JSON column to store extended fields
-- =============================================
ALTER TABLE arch_application ADD COLUMN ext_attrs JSON COMMENT '扩展属性JSON存储' AFTER dr_recovery_level;

-- =============================================
-- 2. Create trigger to populate ext_attrs on INSERT
-- =============================================
DROP TRIGGER IF EXISTS trg_arch_application_before_insert;

DELIMITER //

CREATE TRIGGER trg_arch_application_before_insert
BEFORE INSERT ON arch_application
FOR EACH ROW
BEGIN
    -- Store extended fields into ext_attrs JSON
    SET NEW.ext_attrs = JSON_OBJECT(
        'description', NEW.description,
        'business_description', NEW.business_description,
        'service_object', NEW.service_object,
        'main_business_domain', NEW.main_business_domain,
        'secondary_business_domain', NEW.secondary_business_domain,
        'remark', NEW.remark,
        'login_user_field', NEW.login_user_field,
        'login_password_field', NEW.login_password_field,
        'is_deployed_dmz', NEW.is_deployed_dmz,
        'has_upload_function', NEW.has_upload_function,
        'pwd_error_freeze_mechanism', NEW.pwd_error_freeze_mechanism,
        'has_outbound_request', NEW.has_outbound_request,
        'outbound_request_desc', NEW.outbound_request_desc,
        'upload_function_desc', NEW.upload_function_desc,
        'upload_file_path', NEW.upload_file_path,
        'upload_path_executable', NEW.upload_path_executable,
        'upload_file_types', NEW.upload_file_types,
        'has_download_function', NEW.has_download_function,
        'download_function_desc', NEW.download_function_desc,
        'download_file_types', NEW.download_file_types,
        'is_app_application', NEW.is_app_application,
        'open_source_info', NEW.open_source_info,
        'access_url', NEW.access_url,
        'domain_name', NEW.domain_name,
        'network_mode', NEW.network_mode,
        'vendor_info', NEW.vendor_info,
        'auth_method', NEW.auth_method,
        'has_third_party_integration', NEW.has_third_party_integration,
        'third_party_integration_desc', NEW.third_party_integration_desc,
        'is_internet_line', NEW.is_internet_line,
        'xc_cloud_status', NEW.xc_cloud_status,
        'server_xc_desc', NEW.server_xc_desc,
        'server_xc_status', NEW.server_xc_status,
        'has_third_party_product', NEW.has_third_party_product,
        'third_party_is_xc', NEW.third_party_is_xc,
        'is_full_xc', NEW.is_full_xc,
        'db_server_xc_status', NEW.db_server_xc_status,
        'db_server_os', NEW.db_server_os,
        'database_xc_status', NEW.database_xc_status,
        'database_xc_desc', NEW.database_xc_desc,
        'partial_xc_desc', NEW.partial_xc_desc,
        'xc_classification', NEW.xc_classification,
        'third_party_xc_desc', NEW.third_party_xc_desc,
        'xc_overall_desc', NEW.xc_overall_desc,
        'system_protection_level', NEW.system_protection_level,
        'protection_level', NEW.protection_level,
        'customer_type', NEW.customer_type,
        'service_time_type', NEW.service_time_type,
        'service_window_desc', NEW.service_window_desc,
        'internal_user_scope', NEW.internal_user_scope,
        'usage_scope_desc', NEW.usage_scope_desc,
        'city_rpo', NEW.city_rpo,
        'city_rto', NEW.city_rto,
        'city_active_type', NEW.city_active_type,
        'has_city_environment', NEW.has_city_environment,
        'remote_rpo', NEW.remote_rpo,
        'remote_rto', NEW.remote_rto,
        'remote_active_type', NEW.remote_active_type,
        'has_dr_environment', NEW.has_dr_environment,
        'ops_level', NEW.ops_level,
        'old_ops_level', NEW.old_ops_level,
        'ops_unit', NEW.ops_unit,
        'remote_access_class', NEW.remote_access_class,
        'is_change_automation', NEW.is_change_automation,
        'change_deploy_time', NEW.change_deploy_time,
        'change_deploy_time_desc', NEW.change_deploy_time_desc,
        'main_business_hours', NEW.main_business_hours,
        'data_asset_approval_dept', NEW.data_asset_approval_dept,
        'is_containerized', NEW.is_containerized,
        'deployment_environment', NEW.deployment_environment,
        'deployment_location', NEW.deployment_location,
        'deployment_location_desc', NEW.deployment_location_desc,
        'dr_level', NEW.dr_level,
        'dr_recovery_level', NEW.dr_recovery_level
    );

    -- Clear extended fields from base row (they are now in ext_attrs)
    SET NEW.description = NULL;
    SET NEW.business_description = NULL;
    SET NEW.service_object = NULL;
    SET NEW.main_business_domain = NULL;
    SET NEW.secondary_business_domain = NULL;
    SET NEW.remark = NULL;
    SET NEW.login_user_field = NULL;
    SET NEW.login_password_field = NULL;
    SET NEW.is_deployed_dmz = NULL;
    SET NEW.has_upload_function = NULL;
    SET NEW.pwd_error_freeze_mechanism = NULL;
    SET NEW.has_outbound_request = NULL;
    SET NEW.outbound_request_desc = NULL;
    SET NEW.upload_function_desc = NULL;
    SET NEW.upload_file_path = NULL;
    SET NEW.upload_path_executable = NULL;
    SET NEW.upload_file_types = NULL;
    SET NEW.has_download_function = NULL;
    SET NEW.download_function_desc = NULL;
    SET NEW.download_file_types = NULL;
    SET NEW.is_app_application = NULL;
    SET NEW.open_source_info = NULL;
    SET NEW.access_url = NULL;
    SET NEW.domain_name = NULL;
    SET NEW.network_mode = NULL;
    SET NEW.vendor_info = NULL;
    SET NEW.auth_method = NULL;
    SET NEW.has_third_party_integration = NULL;
    SET NEW.third_party_integration_desc = NULL;
    SET NEW.is_internet_line = NULL;
    SET NEW.xc_cloud_status = NULL;
    SET NEW.server_xc_desc = NULL;
    SET NEW.server_xc_status = NULL;
    SET NEW.has_third_party_product = NULL;
    SET NEW.third_party_is_xc = NULL;
    SET NEW.is_full_xc = NULL;
    SET NEW.db_server_xc_status = NULL;
    SET NEW.db_server_os = NULL;
    SET NEW.database_xc_status = NULL;
    SET NEW.database_xc_desc = NULL;
    SET NEW.partial_xc_desc = NULL;
    SET NEW.xc_classification = NULL;
    SET NEW.third_party_xc_desc = NULL;
    SET NEW.xc_overall_desc = NULL;
    SET NEW.system_protection_level = NULL;
    SET NEW.protection_level = NULL;
    SET NEW.customer_type = NULL;
    SET NEW.service_time_type = NULL;
    SET NEW.service_window_desc = NULL;
    SET NEW.internal_user_scope = NULL;
    SET NEW.usage_scope_desc = NULL;
    SET NEW.city_rpo = NULL;
    SET NEW.city_rto = NULL;
    SET NEW.city_active_type = NULL;
    SET NEW.has_city_environment = NULL;
    SET NEW.remote_rpo = NULL;
    SET NEW.remote_rto = NULL;
    SET NEW.remote_active_type = NULL;
    SET NEW.has_dr_environment = NULL;
    SET NEW.ops_level = NULL;
    SET NEW.old_ops_level = NULL;
    SET NEW.ops_unit = NULL;
    SET NEW.remote_access_class = NULL;
    SET NEW.is_change_automation = NULL;
    SET NEW.change_deploy_time = NULL;
    SET NEW.change_deploy_time_desc = NULL;
    SET NEW.main_business_hours = NULL;
    SET NEW.data_asset_approval_dept = NULL;
    SET NEW.is_containerized = NULL;
    SET NEW.deployment_environment = NULL;
    SET NEW.deployment_location = NULL;
    SET NEW.deployment_location_desc = NULL;
    SET NEW.dr_level = NULL;
    SET NEW.dr_recovery_level = NULL;
END//

DELIMITER ;

-- =============================================
-- 3. Create trigger to populate ext_attrs on UPDATE
-- =============================================
DROP TRIGGER IF EXISTS trg_arch_application_before_update;

DELIMITER //

CREATE TRIGGER trg_arch_application_before_update
BEFORE UPDATE ON arch_application
FOR EACH ROW
BEGIN
    -- Store extended fields into ext_attrs JSON
    SET NEW.ext_attrs = JSON_OBJECT(
        'description', NEW.description,
        'business_description', NEW.business_description,
        'service_object', NEW.service_object,
        'main_business_domain', NEW.main_business_domain,
        'secondary_business_domain', NEW.secondary_business_domain,
        'remark', NEW.remark,
        'login_user_field', NEW.login_user_field,
        'login_password_field', NEW.login_password_field,
        'is_deployed_dmz', NEW.is_deployed_dmz,
        'has_upload_function', NEW.has_upload_function,
        'pwd_error_freeze_mechanism', NEW.pwd_error_freeze_mechanism,
        'has_outbound_request', NEW.has_outbound_request,
        'outbound_request_desc', NEW.outbound_request_desc,
        'upload_function_desc', NEW.upload_function_desc,
        'upload_file_path', NEW.upload_file_path,
        'upload_path_executable', NEW.upload_path_executable,
        'upload_file_types', NEW.upload_file_types,
        'has_download_function', NEW.has_download_function,
        'download_function_desc', NEW.download_function_desc,
        'download_file_types', NEW.download_file_types,
        'is_app_application', NEW.is_app_application,
        'open_source_info', NEW.open_source_info,
        'access_url', NEW.access_url,
        'domain_name', NEW.domain_name,
        'network_mode', NEW.network_mode,
        'vendor_info', NEW.vendor_info,
        'auth_method', NEW.auth_method,
        'has_third_party_integration', NEW.has_third_party_integration,
        'third_party_integration_desc', NEW.third_party_integration_desc,
        'is_internet_line', NEW.is_internet_line,
        'xc_cloud_status', NEW.xc_cloud_status,
        'server_xc_desc', NEW.server_xc_desc,
        'server_xc_status', NEW.server_xc_status,
        'has_third_party_product', NEW.has_third_party_product,
        'third_party_is_xc', NEW.third_party_is_xc,
        'is_full_xc', NEW.is_full_xc,
        'db_server_xc_status', NEW.db_server_xc_status,
        'db_server_os', NEW.db_server_os,
        'database_xc_status', NEW.database_xc_status,
        'database_xc_desc', NEW.database_xc_desc,
        'partial_xc_desc', NEW.partial_xc_desc,
        'xc_classification', NEW.xc_classification,
        'third_party_xc_desc', NEW.third_party_xc_desc,
        'xc_overall_desc', NEW.xc_overall_desc,
        'system_protection_level', NEW.system_protection_level,
        'protection_level', NEW.protection_level,
        'customer_type', NEW.customer_type,
        'service_time_type', NEW.service_time_type,
        'service_window_desc', NEW.service_window_desc,
        'internal_user_scope', NEW.internal_user_scope,
        'usage_scope_desc', NEW.usage_scope_desc,
        'city_rpo', NEW.city_rpo,
        'city_rto', NEW.city_rto,
        'city_active_type', NEW.city_active_type,
        'has_city_environment', NEW.has_city_environment,
        'remote_rpo', NEW.remote_rpo,
        'remote_rto', NEW.remote_rto,
        'remote_active_type', NEW.remote_active_type,
        'has_dr_environment', NEW.has_dr_environment,
        'ops_level', NEW.ops_level,
        'old_ops_level', NEW.old_ops_level,
        'ops_unit', NEW.ops_unit,
        'remote_access_class', NEW.remote_access_class,
        'is_change_automation', NEW.is_change_automation,
        'change_deploy_time', NEW.change_deploy_time,
        'change_deploy_time_desc', NEW.change_deploy_time_desc,
        'main_business_hours', NEW.main_business_hours,
        'data_asset_approval_dept', NEW.data_asset_approval_dept,
        'is_containerized', NEW.is_containerized,
        'deployment_environment', NEW.deployment_environment,
        'deployment_location', NEW.deployment_location,
        'deployment_location_desc', NEW.deployment_location_desc,
        'dr_level', NEW.dr_level,
        'dr_recovery_level', NEW.dr_recovery_level
    );

    -- Clear extended fields from base row (they are now in ext_attrs)
    SET NEW.description = NULL;
    SET NEW.business_description = NULL;
    SET NEW.service_object = NULL;
    SET NEW.main_business_domain = NULL;
    SET NEW.secondary_business_domain = NULL;
    SET NEW.remark = NULL;
    SET NEW.login_user_field = NULL;
    SET NEW.login_password_field = NULL;
    SET NEW.is_deployed_dmz = NULL;
    SET NEW.has_upload_function = NULL;
    SET NEW.pwd_error_freeze_mechanism = NULL;
    SET NEW.has_outbound_request = NULL;
    SET NEW.outbound_request_desc = NULL;
    SET NEW.upload_function_desc = NULL;
    SET NEW.upload_file_path = NULL;
    SET NEW.upload_path_executable = NULL;
    SET NEW.upload_file_types = NULL;
    SET NEW.has_download_function = NULL;
    SET NEW.download_function_desc = NULL;
    SET NEW.download_file_types = NULL;
    SET NEW.is_app_application = NULL;
    SET NEW.open_source_info = NULL;
    SET NEW.access_url = NULL;
    SET NEW.domain_name = NULL;
    SET NEW.network_mode = NULL;
    SET NEW.vendor_info = NULL;
    SET NEW.auth_method = NULL;
    SET NEW.has_third_party_integration = NULL;
    SET NEW.third_party_integration_desc = NULL;
    SET NEW.is_internet_line = NULL;
    SET NEW.xc_cloud_status = NULL;
    SET NEW.server_xc_desc = NULL;
    SET NEW.server_xc_status = NULL;
    SET NEW.has_third_party_product = NULL;
    SET NEW.third_party_is_xc = NULL;
    SET NEW.is_full_xc = NULL;
    SET NEW.db_server_xc_status = NULL;
    SET NEW.db_server_os = NULL;
    SET NEW.database_xc_status = NULL;
    SET NEW.database_xc_desc = NULL;
    SET NEW.partial_xc_desc = NULL;
    SET NEW.xc_classification = NULL;
    SET NEW.third_party_xc_desc = NULL;
    SET NEW.xc_overall_desc = NULL;
    SET NEW.system_protection_level = NULL;
    SET NEW.protection_level = NULL;
    SET NEW.customer_type = NULL;
    SET NEW.service_time_type = NULL;
    SET NEW.service_window_desc = NULL;
    SET NEW.internal_user_scope = NULL;
    SET NEW.usage_scope_desc = NULL;
    SET NEW.city_rpo = NULL;
    SET NEW.city_rto = NULL;
    SET NEW.city_active_type = NULL;
    SET NEW.has_city_environment = NULL;
    SET NEW.remote_rpo = NULL;
    SET NEW.remote_rto = NULL;
    SET NEW.remote_active_type = NULL;
    SET NEW.has_dr_environment = NULL;
    SET NEW.ops_level = NULL;
    SET NEW.old_ops_level = NULL;
    SET NEW.ops_unit = NULL;
    SET NEW.remote_access_class = NULL;
    SET NEW.is_change_automation = NULL;
    SET NEW.change_deploy_time = NULL;
    SET NEW.change_deploy_time_desc = NULL;
    SET NEW.main_business_hours = NULL;
    SET NEW.data_asset_approval_dept = NULL;
    SET NEW.is_containerized = NULL;
    SET NEW.deployment_environment = NULL;
    SET NEW.deployment_location = NULL;
    SET NEW.deployment_location_desc = NULL;
    SET NEW.dr_level = NULL;
    SET NEW.dr_recovery_level = NULL;
END//

DELIMITER ;

-- =============================================
-- 4. Create view for backward compatibility
-- =============================================
DROP VIEW IF EXISTS v_arch_application;

CREATE OR REPLACE VIEW v_arch_application AS
SELECT
    -- Core fields (40)
    id,
    app_code,
    app_name,
    app_name_en,
    app_type,
    importance_level,
    system_layer,
    lifecycle,
    business_domain,
    department_id,
    department_name,
    team_id,
    team_name,
    pm_name,
    pm_email,
    tech_lead,
    tech_lead_email,
    deployment_type,
    access_type,
    sla_level,
    version,
    go_live_date,
    retire_date,
    status,
    parent_app_id,
    implementation_unit,
    implementation_division,
    implementation_team,
    implementation_method,
    implementation_type,
    is_internet_app,
    is_payment_app,
    is_online_banking_app,
    is_bill_app,
    is_electronic_banking_app,
    is_mobile_app,
    is_internet_finance_app,
    -- BaseEntity fields
    create_time,
    update_time,
    creator,
    modifier,
    deleted,
    -- Extended fields extracted from JSON (54)
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.description')) AS description,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.business_description')) AS business_description,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.service_object')) AS service_object,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.main_business_domain')) AS main_business_domain,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.secondary_business_domain')) AS secondary_business_domain,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.remark')) AS remark,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.login_user_field')) AS login_user_field,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.login_password_field')) AS login_password_field,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.is_deployed_dmz')) AS is_deployed_dmz,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.has_upload_function')) AS has_upload_function,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.pwd_error_freeze_mechanism')) AS pwd_error_freeze_mechanism,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.has_outbound_request')) AS has_outbound_request,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.outbound_request_desc')) AS outbound_request_desc,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.upload_function_desc')) AS upload_function_desc,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.upload_file_path')) AS upload_file_path,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.upload_path_executable')) AS upload_path_executable,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.upload_file_types')) AS upload_file_types,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.has_download_function')) AS has_download_function,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.download_function_desc')) AS download_function_desc,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.download_file_types')) AS download_file_types,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.is_app_application')) AS is_app_application,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.open_source_info')) AS open_source_info,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.access_url')) AS access_url,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.domain_name')) AS domain_name,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.network_mode')) AS network_mode,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.vendor_info')) AS vendor_info,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.auth_method')) AS auth_method,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.has_third_party_integration')) AS has_third_party_integration,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.third_party_integration_desc')) AS third_party_integration_desc,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.is_internet_line')) AS is_internet_line,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.xc_cloud_status')) AS xc_cloud_status,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.server_xc_desc')) AS server_xc_desc,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.server_xc_status')) AS server_xc_status,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.has_third_party_product')) AS has_third_party_product,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.third_party_is_xc')) AS third_party_is_xc,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.is_full_xc')) AS is_full_xc,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.db_server_xc_status')) AS db_server_xc_status,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.db_server_os')) AS db_server_os,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.database_xc_status')) AS database_xc_status,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.database_xc_desc')) AS database_xc_desc,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.partial_xc_desc')) AS partial_xc_desc,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.xc_classification')) AS xc_classification,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.third_party_xc_desc')) AS third_party_xc_desc,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.xc_overall_desc')) AS xc_overall_desc,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.system_protection_level')) AS system_protection_level,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.protection_level')) AS protection_level,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.customer_type')) AS customer_type,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.service_time_type')) AS service_time_type,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.service_window_desc')) AS service_window_desc,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.internal_user_scope')) AS internal_user_scope,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.usage_scope_desc')) AS usage_scope_desc,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.city_rpo')) AS city_rpo,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.city_rto')) AS city_rto,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.city_active_type')) AS city_active_type,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.has_city_environment')) AS has_city_environment,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.remote_rpo')) AS remote_rpo,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.remote_rto')) AS remote_rto,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.remote_active_type')) AS remote_active_type,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.has_dr_environment')) AS has_dr_environment,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.ops_level')) AS ops_level,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.old_ops_level')) AS old_ops_level,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.ops_unit')) AS ops_unit,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.remote_access_class')) AS remote_access_class,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.is_change_automation')) AS is_change_automation,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.change_deploy_time')) AS change_deploy_time,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.change_deploy_time_desc')) AS change_deploy_time_desc,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.main_business_hours')) AS main_business_hours,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.data_asset_approval_dept')) AS data_asset_approval_dept,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.is_containerized')) AS is_containerized,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.deployment_environment')) AS deployment_environment,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.deployment_location')) AS deployment_location,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.deployment_location_desc')) AS deployment_location_desc,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.dr_level')) AS dr_level,
    JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.dr_recovery_level')) AS dr_recovery_level
FROM arch_application;

-- =============================================
-- 5. Migrate existing data to ext_attrs JSON
-- =============================================
-- This script populates ext_attrs from existing column values for rows that have data
UPDATE arch_application
SET ext_attrs = JSON_OBJECT(
    'description', description,
    'business_description', business_description,
    'service_object', service_object,
    'main_business_domain', main_business_domain,
    'secondary_business_domain', secondary_business_domain,
    'remark', remark,
    'login_user_field', login_user_field,
    'login_password_field', login_password_field,
    'is_deployed_dmz', is_deployed_dmz,
    'has_upload_function', has_upload_function,
    'pwd_error_freeze_mechanism', pwd_error_freeze_mechanism,
    'has_outbound_request', has_outbound_request,
    'outbound_request_desc', outbound_request_desc,
    'upload_function_desc', upload_function_desc,
    'upload_file_path', upload_file_path,
    'upload_path_executable', upload_path_executable,
    'upload_file_types', upload_file_types,
    'has_download_function', has_download_function,
    'download_function_desc', download_function_desc,
    'download_file_types', download_file_types,
    'is_app_application', is_app_application,
    'open_source_info', open_source_info,
    'access_url', access_url,
    'domain_name', domain_name,
    'network_mode', network_mode,
    'vendor_info', vendor_info,
    'auth_method', auth_method,
    'has_third_party_integration', has_third_party_integration,
    'third_party_integration_desc', third_party_integration_desc,
    'is_internet_line', is_internet_line,
    'xc_cloud_status', xc_cloud_status,
    'server_xc_desc', server_xc_desc,
    'server_xc_status', server_xc_status,
    'has_third_party_product', has_third_party_product,
    'third_party_is_xc', third_party_is_xc,
    'is_full_xc', is_full_xc,
    'db_server_xc_status', db_server_xc_status,
    'db_server_os', db_server_os,
    'database_xc_status', database_xc_status,
    'database_xc_desc', database_xc_desc,
    'partial_xc_desc', partial_xc_desc,
    'xc_classification', xc_classification,
    'third_party_xc_desc', third_party_xc_desc,
    'xc_overall_desc', xc_overall_desc,
    'system_protection_level', system_protection_level,
    'protection_level', protection_level,
    'customer_type', customer_type,
    'service_time_type', service_time_type,
    'service_window_desc', service_window_desc,
    'internal_user_scope', internal_user_scope,
    'usage_scope_desc', usage_scope_desc,
    'city_rpo', city_rpo,
    'city_rto', city_rto,
    'city_active_type', city_active_type,
    'has_city_environment', has_city_environment,
    'remote_rpo', remote_rpo,
    'remote_rto', remote_rto,
    'remote_active_type', remote_active_type,
    'has_dr_environment', has_dr_environment,
    'ops_level', ops_level,
    'old_ops_level', old_ops_level,
    'ops_unit', ops_unit,
    'remote_access_class', remote_access_class,
    'is_change_automation', is_change_automation,
    'change_deploy_time', change_deploy_time,
    'change_deploy_time_desc', change_deploy_time_desc,
    'main_business_hours', main_business_hours,
    'data_asset_approval_dept', data_asset_approval_dept,
    'is_containerized', is_containerized,
    'deployment_environment', deployment_environment,
    'deployment_location', deployment_location,
    'deployment_location_desc', deployment_location_desc,
    'dr_level', dr_level,
    'dr_recovery_level', dr_recovery_level
)
WHERE ext_attrs IS NULL OR JSON_EXTRACT(ext_attrs, '$.description') IS NULL;

-- =============================================
-- 6. Optional: Clear legacy columns after data migration
-- Note: Uncomment this section AFTER verifying the migration works
-- =============================================
/*
ALTER TABLE arch_application
    DROP COLUMN description,
    DROP COLUMN business_description,
    DROP COLUMN service_object,
    DROP COLUMN main_business_domain,
    DROP COLUMN secondary_business_domain,
    DROP COLUMN remark,
    DROP COLUMN login_user_field,
    DROP COLUMN login_password_field,
    DROP COLUMN is_deployed_dmz,
    DROP COLUMN has_upload_function,
    DROP COLUMN pwd_error_freeze_mechanism,
    DROP COLUMN has_outbound_request,
    DROP COLUMN outbound_request_desc,
    DROP COLUMN upload_function_desc,
    DROP COLUMN upload_file_path,
    DROP COLUMN upload_path_executable,
    DROP COLUMN upload_file_types,
    DROP COLUMN has_download_function,
    DROP COLUMN download_function_desc,
    DROP COLUMN download_file_types,
    DROP COLUMN is_app_application,
    DROP COLUMN open_source_info,
    DROP COLUMN access_url,
    DROP COLUMN domain_name,
    DROP COLUMN network_mode,
    DROP COLUMN vendor_info,
    DROP COLUMN auth_method,
    DROP COLUMN has_third_party_integration,
    DROP COLUMN third_party_integration_desc,
    DROP COLUMN is_internet_line,
    DROP COLUMN xc_cloud_status,
    DROP COLUMN server_xc_desc,
    DROP COLUMN server_xc_status,
    DROP COLUMN has_third_party_product,
    DROP COLUMN third_party_is_xc,
    DROP COLUMN is_full_xc,
    DROP COLUMN db_server_xc_status,
    DROP COLUMN db_server_os,
    DROP COLUMN database_xc_status,
    DROP COLUMN database_xc_desc,
    DROP COLUMN partial_xc_desc,
    DROP COLUMN xc_classification,
    DROP COLUMN third_party_xc_desc,
    DROP COLUMN xc_overall_desc,
    DROP COLUMN system_protection_level,
    DROP COLUMN protection_level,
    DROP COLUMN customer_type,
    DROP COLUMN service_time_type,
    DROP COLUMN service_window_desc,
    DROP COLUMN internal_user_scope,
    DROP COLUMN usage_scope_desc,
    DROP COLUMN city_rpo,
    DROP COLUMN city_rto,
    DROP COLUMN city_active_type,
    DROP COLUMN has_city_environment,
    DROP COLUMN remote_rpo,
    DROP COLUMN remote_rto,
    DROP COLUMN remote_active_type,
    DROP COLUMN has_dr_environment,
    DROP COLUMN ops_level,
    DROP COLUMN old_ops_level,
    DROP COLUMN ops_unit,
    DROP COLUMN remote_access_class,
    DROP COLUMN is_change_automation,
    DROP COLUMN change_deploy_time,
    DROP COLUMN change_deploy_time_desc,
    DROP COLUMN main_business_hours,
    DROP COLUMN data_asset_approval_dept,
    DROP COLUMN is_containerized,
    DROP COLUMN deployment_environment,
    DROP COLUMN deployment_location,
    DROP COLUMN deployment_location_desc,
    DROP COLUMN dr_level,
    DROP COLUMN dr_recovery_level;
*/


-- =============================================
-- ROLLBACK SCRIPT
-- =============================================
-- To rollback this migration, run the following:

/*
-- 1. Drop triggers
DROP TRIGGER IF EXISTS trg_arch_application_before_insert;
DROP TRIGGER IF EXISTS trg_arch_application_before_update;

-- 2. Drop view
DROP VIEW IF EXISTS v_arch_application;

-- 3. Re-add dropped columns (if step 6 was executed)
ALTER TABLE arch_application
    ADD COLUMN description TEXT COMMENT '应用描述',
    ADD COLUMN business_description TEXT COMMENT '业务描述',
    ADD COLUMN service_object VARCHAR(128) COMMENT '服务对象',
    ADD COLUMN main_business_domain VARCHAR(64) COMMENT '主要业务域',
    ADD COLUMN secondary_business_domain VARCHAR(64) COMMENT '次要业务域',
    ADD COLUMN remark TEXT COMMENT '备注',
    ADD COLUMN login_user_field VARCHAR(64) COMMENT '登录用户名字段',
    ADD COLUMN login_password_field VARCHAR(64) COMMENT '登录密码字段',
    ADD COLUMN is_deployed_dmz TINYINT DEFAULT 0 COMMENT '是否部署DMZ区',
    ADD COLUMN has_upload_function TINYINT DEFAULT 0 COMMENT '是否有上传功能',
    ADD COLUMN pwd_error_freeze_mechanism VARCHAR(128) COMMENT '密码错误冻结机制',
    ADD COLUMN has_outbound_request TINYINT DEFAULT 0 COMMENT '是否有外网请求',
    ADD COLUMN outbound_request_desc VARCHAR(256) COMMENT '外网请求说明',
    ADD COLUMN upload_function_desc VARCHAR(256) COMMENT '上传功能说明',
    ADD COLUMN upload_file_path VARCHAR(256) COMMENT '上传文件路径',
    ADD COLUMN upload_path_executable VARCHAR(64) COMMENT '上传路径是否可执行',
    ADD COLUMN upload_file_types VARCHAR(128) COMMENT '上传文件类型',
    ADD COLUMN has_download_function TINYINT DEFAULT 0 COMMENT '是否有下载功能',
    ADD COLUMN download_function_desc VARCHAR(256) COMMENT '下载功能说明',
    ADD COLUMN download_file_types VARCHAR(128) COMMENT '下载文件类型',
    ADD COLUMN is_app_application TINYINT DEFAULT 0 COMMENT '是否APP应用',
    ADD COLUMN open_source_info VARCHAR(256) COMMENT '开源信息',
    ADD COLUMN access_url VARCHAR(256) COMMENT '访问地址',
    ADD COLUMN domain_name VARCHAR(128) COMMENT '域名',
    ADD COLUMN network_mode VARCHAR(32) COMMENT '网络模式',
    ADD COLUMN vendor_info VARCHAR(256) COMMENT '厂商信息',
    ADD COLUMN auth_method VARCHAR(64) COMMENT '认证方式',
    ADD COLUMN has_third_party_integration TINYINT DEFAULT 0 COMMENT '是否集成第三方',
    ADD COLUMN third_party_integration_desc VARCHAR(256) COMMENT '第三方集成说明',
    ADD COLUMN is_internet_line TINYINT DEFAULT 0 COMMENT '是否互联网线路',
    ADD COLUMN xc_cloud_status VARCHAR(32) COMMENT '信创云状态',
    ADD COLUMN server_xc_desc VARCHAR(256) COMMENT '服务器信创说明',
    ADD COLUMN server_xc_status VARCHAR(32) COMMENT '服务器信创状态',
    ADD COLUMN has_third_party_product TINYINT DEFAULT 0 COMMENT '是否有第三方产品',
    ADD COLUMN third_party_is_xc TINYINT DEFAULT 0 COMMENT '第三方是否信创',
    ADD COLUMN is_full_xc TINYINT DEFAULT 0 COMMENT '是否全信创',
    ADD COLUMN db_server_xc_status VARCHAR(32) COMMENT '数据库服务器信创状态',
    ADD COLUMN db_server_os VARCHAR(64) COMMENT '数据库服务器操作系统',
    ADD COLUMN database_xc_status VARCHAR(32) COMMENT '数据库信创状态',
    ADD COLUMN database_xc_desc VARCHAR(256) COMMENT '数据库信创说明',
    ADD COLUMN partial_xc_desc VARCHAR(512) COMMENT '部分信创说明',
    ADD COLUMN xc_classification VARCHAR(32) COMMENT '信创分类',
    ADD COLUMN third_party_xc_desc VARCHAR(256) COMMENT '第三方信创说明',
    ADD COLUMN xc_overall_desc VARCHAR(512) COMMENT '信创总体说明',
    ADD COLUMN system_protection_level VARCHAR(32) COMMENT '所属系统等级保护安全等级',
    ADD COLUMN protection_level VARCHAR(32) COMMENT '等级保护安全等级',
    ADD COLUMN customer_type VARCHAR(32) COMMENT '客户类型',
    ADD COLUMN service_time_type VARCHAR(32) COMMENT '服务时间类型',
    ADD COLUMN service_window_desc VARCHAR(256) COMMENT '服务窗口补充说明',
    ADD COLUMN internal_user_scope VARCHAR(128) COMMENT '内部用户范围',
    ADD COLUMN usage_scope_desc VARCHAR(512) COMMENT '使用范围补充说明',
    ADD COLUMN city_rpo VARCHAR(32) COMMENT '同城RPO',
    ADD COLUMN city_rto VARCHAR(32) COMMENT '同城RTO',
    ADD COLUMN city_active_type VARCHAR(32) COMMENT '同城双活类型',
    ADD COLUMN has_city_environment TINYINT DEFAULT 0 COMMENT '是否具备同城环境',
    ADD COLUMN remote_rpo VARCHAR(32) COMMENT '异地RPO',
    ADD COLUMN remote_rto VARCHAR(32) COMMENT '异地RTO',
    ADD COLUMN remote_active_type VARCHAR(32) COMMENT '异地双活类型',
    ADD COLUMN has_dr_environment TINYINT DEFAULT 0 COMMENT '是否具备灾备环境',
    ADD COLUMN ops_level VARCHAR(16) COMMENT '运维等级',
    ADD COLUMN old_ops_level VARCHAR(16) COMMENT '旧运维等级',
    ADD COLUMN ops_unit VARCHAR(128) COMMENT '运维单位',
    ADD COLUMN remote_access_class VARCHAR(16) COMMENT '远程访问权限分类',
    ADD COLUMN is_change_automation TINYINT DEFAULT 0 COMMENT '是否变更自动化',
    ADD COLUMN change_deploy_time VARCHAR(64) COMMENT '变更投产时点',
    ADD COLUMN change_deploy_time_desc VARCHAR(256) COMMENT '变更投产时点补充说明',
    ADD COLUMN main_business_hours VARCHAR(64) COMMENT '主要业务时段',
    ADD COLUMN data_asset_approval_dept VARCHAR(256) COMMENT '电子数据资产提取审批部门',
    ADD COLUMN is_containerized TINYINT DEFAULT 0 COMMENT '是否容器化部署',
    ADD COLUMN deployment_environment VARCHAR(64) COMMENT '部署环境',
    ADD COLUMN deployment_location VARCHAR(128) COMMENT '部署地点',
    ADD COLUMN deployment_location_desc VARCHAR(512) COMMENT '部署地点补充说明',
    ADD COLUMN dr_level VARCHAR(16) COMMENT '灾备等级',
    ADD COLUMN dr_recovery_level VARCHAR(16) COMMENT '灾备恢复能力等级';

-- 4. Restore data from ext_attrs to columns
UPDATE arch_application a
INNER JOIN (
    SELECT id,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.description')) AS description,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.business_description')) AS business_description,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.service_object')) AS service_object,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.main_business_domain')) AS main_business_domain,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.secondary_business_domain')) AS secondary_business_domain,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.remark')) AS remark,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.login_user_field')) AS login_user_field,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.login_password_field')) AS login_password_field,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.is_deployed_dmz')) AS is_deployed_dmz,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.has_upload_function')) AS has_upload_function,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.pwd_error_freeze_mechanism')) AS pwd_error_freeze_mechanism,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.has_outbound_request')) AS has_outbound_request,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.outbound_request_desc')) AS outbound_request_desc,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.upload_function_desc')) AS upload_function_desc,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.upload_file_path')) AS upload_file_path,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.upload_path_executable')) AS upload_path_executable,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.upload_file_types')) AS upload_file_types,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.has_download_function')) AS has_download_function,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.download_function_desc')) AS download_function_desc,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.download_file_types')) AS download_file_types,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.is_app_application')) AS is_app_application,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.open_source_info')) AS open_source_info,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.access_url')) AS access_url,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.domain_name')) AS domain_name,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.network_mode')) AS network_mode,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.vendor_info')) AS vendor_info,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.auth_method')) AS auth_method,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.has_third_party_integration')) AS has_third_party_integration,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.third_party_integration_desc')) AS third_party_integration_desc,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.is_internet_line')) AS is_internet_line,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.xc_cloud_status')) AS xc_cloud_status,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.server_xc_desc')) AS server_xc_desc,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.server_xc_status')) AS server_xc_status,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.has_third_party_product')) AS has_third_party_product,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.third_party_is_xc')) AS third_party_is_xc,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.is_full_xc')) AS is_full_xc,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.db_server_xc_status')) AS db_server_xc_status,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.db_server_os')) AS db_server_os,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.database_xc_status')) AS database_xc_status,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.database_xc_desc')) AS database_xc_desc,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.partial_xc_desc')) AS partial_xc_desc,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.xc_classification')) AS xc_classification,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.third_party_xc_desc')) AS third_party_xc_desc,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.xc_overall_desc')) AS xc_overall_desc,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.system_protection_level')) AS system_protection_level,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.protection_level')) AS protection_level,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.customer_type')) AS customer_type,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.service_time_type')) AS service_time_type,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.service_window_desc')) AS service_window_desc,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.internal_user_scope')) AS internal_user_scope,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.usage_scope_desc')) AS usage_scope_desc,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.city_rpo')) AS city_rpo,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.city_rto')) AS city_rto,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.city_active_type')) AS city_active_type,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.has_city_environment')) AS has_city_environment,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.remote_rpo')) AS remote_rpo,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.remote_rto')) AS remote_rto,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.remote_active_type')) AS remote_active_type,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.has_dr_environment')) AS has_dr_environment,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.ops_level')) AS ops_level,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.old_ops_level')) AS old_ops_level,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.ops_unit')) AS ops_unit,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.remote_access_class')) AS remote_access_class,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.is_change_automation')) AS is_change_automation,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.change_deploy_time')) AS change_deploy_time,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.change_deploy_time_desc')) AS change_deploy_time_desc,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.main_business_hours')) AS main_business_hours,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.data_asset_approval_dept')) AS data_asset_approval_dept,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.is_containerized')) AS is_containerized,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.deployment_environment')) AS deployment_environment,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.deployment_location')) AS deployment_location,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.deployment_location_desc')) AS deployment_location_desc,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.dr_level')) AS dr_level,
        JSON_UNQUOTE(JSON_EXTRACT(ext_attrs, '$.dr_recovery_level')) AS dr_recovery_level
    FROM arch_application
    WHERE ext_attrs IS NOT NULL
) AS t ON a.id = t.id
SET
    a.description = t.description,
    a.business_description = t.business_description,
    a.service_object = t.service_object,
    a.main_business_domain = t.main_business_domain,
    a.secondary_business_domain = t.secondary_business_domain,
    a.remark = t.remark,
    a.login_user_field = t.login_user_field,
    a.login_password_field = t.login_password_field,
    a.is_deployed_dmz = t.is_deployed_dmz,
    a.has_upload_function = t.has_upload_function,
    a.pwd_error_freeze_mechanism = t.pwd_error_freeze_mechanism,
    a.has_outbound_request = t.has_outbound_request,
    a.outbound_request_desc = t.outbound_request_desc,
    a.upload_function_desc = t.upload_function_desc,
    a.upload_file_path = t.upload_file_path,
    a.upload_path_executable = t.upload_path_executable,
    a.upload_file_types = t.upload_file_types,
    a.has_download_function = t.has_download_function,
    a.download_function_desc = t.download_function_desc,
    a.download_file_types = t.download_file_types,
    a.is_app_application = t.is_app_application,
    a.open_source_info = t.open_source_info,
    a.access_url = t.access_url,
    a.domain_name = t.domain_name,
    a.network_mode = t.network_mode,
    a.vendor_info = t.vendor_info,
    a.auth_method = t.auth_method,
    a.has_third_party_integration = t.has_third_party_integration,
    a.third_party_integration_desc = t.third_party_integration_desc,
    a.is_internet_line = t.is_internet_line,
    a.xc_cloud_status = t.xc_cloud_status,
    a.server_xc_desc = t.server_xc_desc,
    a.server_xc_status = t.server_xc_status,
    a.has_third_party_product = t.has_third_party_product,
    a.third_party_is_xc = t.third_party_is_xc,
    a.is_full_xc = t.is_full_xc,
    a.db_server_xc_status = t.db_server_xc_status,
    a.db_server_os = t.db_server_os,
    a.database_xc_status = t.database_xc_status,
    a.database_xc_desc = t.database_xc_desc,
    a.partial_xc_desc = t.partial_xc_desc,
    a.xc_classification = t.xc_classification,
    a.third_party_xc_desc = t.third_party_xc_desc,
    a.xc_overall_desc = t.xc_overall_desc,
    a.system_protection_level = t.system_protection_level,
    a.protection_level = t.protection_level,
    a.customer_type = t.customer_type,
    a.service_time_type = t.service_time_type,
    a.service_window_desc = t.service_window_desc,
    a.internal_user_scope = t.internal_user_scope,
    a.usage_scope_desc = t.usage_scope_desc,
    a.city_rpo = t.city_rpo,
    a.city_rto = t.city_rto,
    a.city_active_type = t.city_active_type,
    a.has_city_environment = t.has_city_environment,
    a.remote_rpo = t.remote_rpo,
    a.remote_rto = t.remote_rto,
    a.remote_active_type = t.remote_active_type,
    a.has_dr_environment = t.has_dr_environment,
    a.ops_level = t.ops_level,
    a.old_ops_level = t.old_ops_level,
    a.ops_unit = t.ops_unit,
    a.remote_access_class = t.remote_access_class,
    a.is_change_automation = t.is_change_automation,
    a.change_deploy_time = t.change_deploy_time,
    a.change_deploy_time_desc = t.change_deploy_time_desc,
    a.main_business_hours = t.main_business_hours,
    a.data_asset_approval_dept = t.data_asset_approval_dept,
    a.is_containerized = t.is_containerized,
    a.deployment_environment = t.deployment_environment,
    a.deployment_location = t.deployment_location,
    a.deployment_location_desc = t.deployment_location_desc,
    a.dr_level = t.dr_level,
    a.dr_recovery_level = t.dr_recovery_level;

-- 5. Drop ext_attrs column
ALTER TABLE arch_application DROP COLUMN ext_attrs;
*/
