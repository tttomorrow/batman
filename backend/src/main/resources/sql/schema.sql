-- 系统用户
CREATE TABLE IF NOT EXISTS sys_user (
  user_id bigserial,
  username varchar(50) NOT NULL,
  password varchar(100),
  salt varchar(20),
  email varchar(100),
  mobile varchar(100),
  status int,
  create_user_id int8,
  create_time timestamp,
  PRIMARY KEY (user_id),
  UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS sys_instance (
  id bigserial,
  instance_name varchar(50) NOT NULL,
  instance_version varchar(20),
  instance_ip   varchar(100),
  instance_port int,
  os_user varchar(100),
  os_password varchar(100),
  backup_path  varchar(256),
  db_data_path varchar(256),
  db_port int,
  create_time timestamp,
  remark varchar(500),
  PRIMARY KEY (id),
  UNIQUE (instance_name)
);
SELECT setval(pg_get_serial_sequence('sys_instance', 'id'), nextval(pg_get_serial_sequence('sys_instance', 'id'))+1,false);

CREATE TABLE IF NOT EXISTS sys_encrypt(
  id bigserial,
  instance_id bigint,
  encrypt_key varchar(32),
  encrypt_iv varchar(32),
  PRIMARY KEY (id),
  FOREIGN KEY(instance_id) REFERENCES sys_instance(id)
);
SELECT setval(pg_get_serial_sequence('sys_encrypt', 'id'), nextval(pg_get_serial_sequence('sys_encrypt', 'id'))+1,false);

CREATE TABLE IF NOT EXISTS sys_job (
  id bigserial,
  instance_id bigint,
  name varchar(50) NOT NULL,
  type varchar(20),
  option varchar(20),
  schedule varchar(50),
  start_time timestamp,
  create_time timestamp,
  active bool,
  remark varchar(300),
  PRIMARY KEY (id),
  UNIQUE (instance_id, name),
  FOREIGN KEY(instance_id) REFERENCES sys_instance(id)
);
SELECT setval(pg_get_serial_sequence('sys_job', 'id'), nextval(pg_get_serial_sequence('sys_job', 'id'))+1,false);

CREATE TABLE IF NOT EXISTS sys_task (
  id bigserial,
  instance_id bigint,
  job_id bigint,
  name varchar(50) NOT NULL,
  type varchar(20),
  start_time timestamp,
  end_time timestamp,
  progress int,
  status varchar(20),
  size  varchar(20),
  backup_data_path  varchar(256),
  backup_instance_name varchar(64),
  backup_id varchar(64),
  PRIMARY KEY (id)
);
SELECT setval(pg_get_serial_sequence('sys_task', 'id'), nextval(pg_get_serial_sequence('sys_task', 'id'))+1,false);

-- 系统用户Token
CREATE TABLE IF NOT EXISTS sys_user_token (
  user_id bigserial,
  token varchar(100) NOT NULL,
  expire_time timestamp,
  update_time timestamp,
  PRIMARY KEY (user_id),
  UNIQUE (token)
);

-- 系统验证码
CREATE TABLE IF NOT EXISTS sys_captcha (
  uuid varchar(36) NOT NULL,
  code varchar(6) NOT NULL,
  expire_time timestamp,
  PRIMARY KEY (uuid)
);

-- 系统日志
CREATE TABLE IF NOT EXISTS sys_log (
  id bigserial,
  username varchar(50),
  log_ip varchar(50),
  instance_name varchar(50),
  operation varchar(50),
  operation_time timestamp,
  PRIMARY KEY (id)
);

INSERT INTO sys_user (user_id, username, password, salt, email, mobile, status, create_user_id, create_time) VALUES ('1', 'admin', '9ec9750e709431dad22365cabc5c625482e574c74adaebba7dd02f1129e4ce1d', 'YzcmCZNvbXocrsz9dm8e', 'contact@opengauss.org', '19612345678', '1', '1', now())
ON conflict(user_id)
DO UPDATE SET
create_time = now();

--  quartz自带表结构
CREATE TABLE IF NOT EXISTS qrtz_job_details
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  JOB_NAME  VARCHAR(200) NOT NULL,
  JOB_GROUP VARCHAR(200) NOT NULL,
  DESCRIPTION VARCHAR(250) NULL,
  JOB_CLASS_NAME   VARCHAR(250) NOT NULL,
  IS_DURABLE BOOL NOT NULL,
  IS_NONCONCURRENT BOOL NOT NULL,
  IS_UPDATE_DATA BOOL NOT NULL,
  REQUESTS_RECOVERY BOOL NOT NULL,
  JOB_DATA BYTEA NULL,
  PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
);

CREATE TABLE IF NOT EXISTS qrtz_triggers
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  TRIGGER_NAME VARCHAR(200) NOT NULL,
  TRIGGER_GROUP VARCHAR(200) NOT NULL,
  JOB_NAME  VARCHAR(200) NOT NULL,
  JOB_GROUP VARCHAR(200) NOT NULL,
  DESCRIPTION VARCHAR(250) NULL,
  NEXT_FIRE_TIME BIGINT NULL,
  PREV_FIRE_TIME BIGINT NULL,
  PRIORITY INTEGER NULL,
  TRIGGER_STATE VARCHAR(16) NOT NULL,
  TRIGGER_TYPE VARCHAR(8) NOT NULL,
  START_TIME BIGINT NOT NULL,
  END_TIME BIGINT NULL,
  CALENDAR_NAME VARCHAR(200) NULL,
  MISFIRE_INSTR SMALLINT NULL,
  JOB_DATA BYTEA NULL,
  PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
  FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
  REFERENCES QRTZ_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP)
);

CREATE TABLE IF NOT EXISTS qrtz_simple_triggers
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  TRIGGER_NAME VARCHAR(200) NOT NULL,
  TRIGGER_GROUP VARCHAR(200) NOT NULL,
  REPEAT_COUNT BIGINT NOT NULL,
  REPEAT_INTERVAL BIGINT NOT NULL,
  TIMES_TRIGGERED BIGINT NOT NULL,
  PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
  FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
  REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE IF NOT EXISTS qrtz_cron_triggers
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  TRIGGER_NAME VARCHAR(200) NOT NULL,
  TRIGGER_GROUP VARCHAR(200) NOT NULL,
  CRON_EXPRESSION VARCHAR(120) NOT NULL,
  TIME_ZONE_ID VARCHAR(80),
  PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
  FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
  REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE IF NOT EXISTS qrtz_simprop_triggers
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  TRIGGER_NAME VARCHAR(200) NOT NULL,
  TRIGGER_GROUP VARCHAR(200) NOT NULL,
  STR_PROP_1 VARCHAR(512) NULL,
  STR_PROP_2 VARCHAR(512) NULL,
  STR_PROP_3 VARCHAR(512) NULL,
  INT_PROP_1 INT NULL,
  INT_PROP_2 INT NULL,
  LONG_PROP_1 BIGINT NULL,
  LONG_PROP_2 BIGINT NULL,
  DEC_PROP_1 NUMERIC(13,4) NULL,
  DEC_PROP_2 NUMERIC(13,4) NULL,
  BOOL_PROP_1 BOOL NULL,
  BOOL_PROP_2 BOOL NULL,
  PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
  FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
  REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE IF NOT EXISTS qrtz_blob_triggers
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  TRIGGER_NAME VARCHAR(200) NOT NULL,
  TRIGGER_GROUP VARCHAR(200) NOT NULL,
  BLOB_DATA BYTEA NULL,
  PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
  FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
  REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE IF NOT EXISTS qrtz_calendars
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  CALENDAR_NAME  VARCHAR(200) NOT NULL,
  CALENDAR BYTEA NOT NULL,
  PRIMARY KEY (SCHED_NAME,CALENDAR_NAME)
);


CREATE TABLE IF NOT EXISTS qrtz_paused_trigger_grps
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  TRIGGER_GROUP  VARCHAR(200) NOT NULL,
  PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP)
);

CREATE TABLE IF NOT EXISTS qrtz_fired_triggers
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  ENTRY_ID VARCHAR(95) NOT NULL,
  TRIGGER_NAME VARCHAR(200) NOT NULL,
  TRIGGER_GROUP VARCHAR(200) NOT NULL,
  INSTANCE_NAME VARCHAR(200) NOT NULL,
  FIRED_TIME BIGINT NOT NULL,
  SCHED_TIME BIGINT NOT NULL,
  PRIORITY INTEGER NOT NULL,
  STATE VARCHAR(16) NOT NULL,
  JOB_NAME VARCHAR(200) NULL,
  JOB_GROUP VARCHAR(200) NULL,
  IS_NONCONCURRENT BOOL NULL,
  REQUESTS_RECOVERY BOOL NULL,
  PRIMARY KEY (SCHED_NAME,ENTRY_ID)
);

CREATE TABLE IF NOT EXISTS qrtz_scheduler_state
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  INSTANCE_NAME VARCHAR(200) NOT NULL,
  LAST_CHECKIN_TIME BIGINT NOT NULL,
  CHECKIN_INTERVAL BIGINT NOT NULL,
  PRIMARY KEY (SCHED_NAME,INSTANCE_NAME)
);

CREATE TABLE IF NOT EXISTS qrtz_locks
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  LOCK_NAME  VARCHAR(40) NOT NULL,
  PRIMARY KEY (SCHED_NAME,LOCK_NAME)
);

CREATE index IF NOT EXISTS idx_qrtz_j_req_recovery on qrtz_job_details(SCHED_NAME,REQUESTS_RECOVERY);
CREATE index IF NOT EXISTS idx_qrtz_j_grp on qrtz_job_details(SCHED_NAME,JOB_GROUP);

CREATE index IF NOT EXISTS idx_qrtz_t_j on qrtz_triggers(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE index IF NOT EXISTS idx_qrtz_t_jg on qrtz_triggers(SCHED_NAME,JOB_GROUP);
CREATE index IF NOT EXISTS idx_qrtz_t_c on qrtz_triggers(SCHED_NAME,CALENDAR_NAME);
CREATE index IF NOT EXISTS idx_qrtz_t_g on qrtz_triggers(SCHED_NAME,TRIGGER_GROUP);
CREATE index IF NOT EXISTS idx_qrtz_t_state on qrtz_triggers(SCHED_NAME,TRIGGER_STATE);
CREATE index IF NOT EXISTS idx_qrtz_t_n_state on qrtz_triggers(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE index IF NOT EXISTS idx_qrtz_t_n_g_state on qrtz_triggers(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE index IF NOT EXISTS idx_qrtz_t_next_fire_time on qrtz_triggers(SCHED_NAME,NEXT_FIRE_TIME);
CREATE index IF NOT EXISTS idx_qrtz_t_nft_st on qrtz_triggers(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
CREATE index IF NOT EXISTS idx_qrtz_t_nft_misfire on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
CREATE index IF NOT EXISTS idx_qrtz_t_nft_st_misfire on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
CREATE index IF NOT EXISTS idx_qrtz_t_nft_st_misfire_grp on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);

CREATE index IF NOT EXISTS idx_qrtz_ft_trig_inst_name on qrtz_fired_triggers(SCHED_NAME,INSTANCE_NAME);
CREATE index IF NOT EXISTS idx_qrtz_ft_inst_job_req_rcvry on qrtz_fired_triggers(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
CREATE index IF NOT EXISTS idx_qrtz_ft_j_g on qrtz_fired_triggers(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE index IF NOT EXISTS idx_qrtz_ft_jg on qrtz_fired_triggers(SCHED_NAME,JOB_GROUP);
CREATE index IF NOT EXISTS idx_qrtz_ft_t_g on qrtz_fired_triggers(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
CREATE index IF NOT EXISTS idx_qrtz_ft_tg on qrtz_fired_triggers(SCHED_NAME,TRIGGER_GROUP);

