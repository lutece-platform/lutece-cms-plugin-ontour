-- liquibase formatted sql
-- changeset ontour:init_core_ontour.sql
-- preconditions onFail:MARK_RAN onError:WARN

--
-- Data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'ONTOUR_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url) VALUES
('ONTOUR_MANAGEMENT','ontour.adminFeature.ManageTours.name',1,'jsp/admin/plugins/ontour/ManageTours.jsp','ontour.adminFeature.ManageTours.description',0,'ontour','SITE','ti ti-route',NULL);

--
-- Data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'ONTOUR_MANAGEMENT';
INSERT INTO core_user_right (id_right,id_user) VALUES ('ONTOUR_MANAGEMENT',1);

--
-- RBAC: every permission on every tour for the admin account
--
DELETE FROM core_admin_role_resource WHERE role_key = 'ontour_manager';
DELETE FROM core_user_role WHERE role_key = 'ontour_manager';
DELETE FROM core_admin_role WHERE role_key = 'ontour_manager';
INSERT INTO core_admin_role (role_key, role_description) VALUES ('ontour_manager', 'Guided tours administrator (onTour)');
INSERT INTO core_admin_role_resource (role_key, resource_type, resource_id, permission) VALUES ('ontour_manager', 'ONTOUR_TOUR', '*', '*');
INSERT INTO core_user_role (role_key, id_user) VALUES ('ontour_manager', 1);

--
-- Data for table core_datastore: position of the tour launcher, edited in the site properties (onTour tab)
--
DELETE FROM core_datastore WHERE entity_key LIKE 'ontour.site_property.launcher.%';
INSERT INTO core_datastore (entity_key, entity_value) VALUES ('ontour.site_property.launcher.bo.select', 'bottom_right');
INSERT INTO core_datastore (entity_key, entity_value) VALUES ('ontour.site_property.launcher.bo.select.options', 'bottom_right|bottom_center|bottom_left|top_right|top_center|top_left|hidden');
INSERT INTO core_datastore (entity_key, entity_value) VALUES ('ontour.site_property.launcher.fo.select', 'bottom_right');
INSERT INTO core_datastore (entity_key, entity_value) VALUES ('ontour.site_property.launcher.fo.select.options', 'bottom_right|bottom_center|bottom_left|top_right|top_center|top_left|hidden');

--
-- Data for table core_datastore: look of the guided tours (CSS custom properties of ontour.css), one set for the back office and one for
-- the front office, edited in the site properties
--
DELETE FROM core_datastore WHERE entity_key LIKE 'ontour.site_property.style.%';
INSERT INTO core_datastore (entity_key, entity_value) VALUES ('ontour.site_property.style.bo.color', '#1f5fbf');
INSERT INTO core_datastore (entity_key, entity_value) VALUES ('ontour.site_property.style.bo.color_contrast', '#ffffff');
INSERT INTO core_datastore (entity_key, entity_value) VALUES ('ontour.site_property.style.bo.offset', '1.25rem');
INSERT INTO core_datastore (entity_key, entity_value) VALUES ('ontour.site_property.style.bo.z_index', '1040');
INSERT INTO core_datastore (entity_key, entity_value) VALUES ('ontour.site_property.style.fo.color', '#1f5fbf');
INSERT INTO core_datastore (entity_key, entity_value) VALUES ('ontour.site_property.style.fo.color_contrast', '#ffffff');
INSERT INTO core_datastore (entity_key, entity_value) VALUES ('ontour.site_property.style.fo.offset', '1.25rem');
INSERT INTO core_datastore (entity_key, entity_value) VALUES ('ontour.site_property.style.fo.z_index', '1040');
