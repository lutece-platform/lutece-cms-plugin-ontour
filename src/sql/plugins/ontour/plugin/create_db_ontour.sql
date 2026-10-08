-- liquibase formatted sql
-- changeset ontour:create_db_ontour.sql
-- preconditions onFail:MARK_RAN onError:WARN

--
-- Structure for table ontour_user_tour
--
DROP TABLE IF EXISTS ontour_user_tour;

--
-- Structure for table ontour_step
--
DROP TABLE IF EXISTS ontour_step;

--
-- Structure for table ontour_tour
--
DROP TABLE IF EXISTS ontour_tour;
CREATE TABLE ontour_tour (
    id_tour int AUTO_INCREMENT,
    code varchar(100) NOT NULL,
    lang varchar(10) default '' NOT NULL,
    title varchar(255) NOT NULL,
    description long varchar,
    target varchar(2) default 'BO' NOT NULL,
    page_path varchar(512) default '' NOT NULL,
    page_parameters varchar(512) default '' NOT NULL,
    page_selector varchar(255) default '' NOT NULL,
    trigger_mode varchar(20) default 'manual' NOT NULL,
    show_launcher smallint default 1 NOT NULL,
    is_enabled smallint default 0 NOT NULL,
    animate smallint default 1 NOT NULL,
    duration int default 400 NOT NULL,
    overlay_color varchar(50) default '#000' NOT NULL,
    overlay_opacity int default 70 NOT NULL,
    smooth_scroll smallint default 0 NOT NULL,
    allow_close smallint default 1 NOT NULL,
    allow_scroll smallint default 1 NOT NULL,
    overlay_click_behavior varchar(20) default 'close' NOT NULL,
    stage_padding int default 10 NOT NULL,
    stage_radius int default 5 NOT NULL,
    disable_active_interaction smallint default 0 NOT NULL,
    advance_on_click smallint default 0 NOT NULL,
    skip_missing_element smallint default 0 NOT NULL,
    wait_for_element int default 0 NOT NULL,
    allow_keyboard_control smallint default 1 NOT NULL,
    popover_class varchar(255) default '' NOT NULL,
    popover_offset int default 10 NOT NULL,
    show_buttons varchar(50) default 'next,previous,close' NOT NULL,
    disable_buttons varchar(50) default '' NOT NULL,
    show_progress smallint default 0 NOT NULL,
    progress_label varchar(255) default '' NOT NULL,
    next_btn_label varchar(100) default '' NOT NULL,
    prev_btn_label varchar(100) default '' NOT NULL,
    done_btn_label varchar(100) default '' NOT NULL,
    PRIMARY KEY (id_tour)
);
CREATE UNIQUE INDEX ontour_tour_code ON ontour_tour (code, lang);
CREATE INDEX ontour_tour_target ON ontour_tour (target, is_enabled);

CREATE TABLE ontour_step (
    id_step int AUTO_INCREMENT,
    id_tour int NOT NULL,
    step_order int default 0 NOT NULL,
    element varchar(512) default '' NOT NULL,
    title varchar(255) default '' NOT NULL,
    description long varchar,
    side varchar(10) default '' NOT NULL,
    align varchar(10) default '' NOT NULL,
    show_buttons varchar(50),
    disable_buttons varchar(50),
    show_progress smallint default -1 NOT NULL,
    popover_class varchar(255) default '' NOT NULL,
    progress_label varchar(255) default '' NOT NULL,
    next_btn_label varchar(100) default '' NOT NULL,
    prev_btn_label varchar(100) default '' NOT NULL,
    done_btn_label varchar(100) default '' NOT NULL,
    disable_active_interaction smallint default -1 NOT NULL,
    advance_on_click smallint default -1 NOT NULL,
    skip_missing_element smallint default -1 NOT NULL,
    wait_for_element int default -1 NOT NULL,
    PRIMARY KEY (id_step)
);
CREATE INDEX ontour_step_tour ON ontour_step (id_tour, step_order);
ALTER TABLE ontour_step ADD CONSTRAINT fk_ontour_step_tour FOREIGN KEY (id_tour) REFERENCES ontour_tour (id_tour);

CREATE TABLE ontour_user_tour (
    user_type varchar(2) NOT NULL,
    user_id varchar(255) NOT NULL,
    tour_code varchar(100) NOT NULL,
    status varchar(10) NOT NULL,
    date_update timestamp default CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (user_type, user_id, tour_code)
);
CREATE INDEX ontour_user_tour_code ON ontour_user_tour (tour_code);
