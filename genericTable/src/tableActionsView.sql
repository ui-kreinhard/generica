-- View: table_actions

-- DROP VIEW table_actions;

CREATE OR REPLACE VIEW table_actions AS 
        (         SELECT table_actions_inner.id, table_actions_inner.link, table_actions_inner.table_name, table_actions_inner.label_action, table_actions_inner.is_action
                   FROM table_actions_inner
        UNION ALL 
                 SELECT 0 AS id, '/fireAction.xhtml?sp=checkin_checkout_time'::character varying(100) AS link, 1021736083 AS table_name, 'Check in'::character varying(45) AS label_action, false AS is_action
                  WHERE (( SELECT count(*) AS count
                           FROM checkin_checkout
                          WHERE "current_user"() = "current_user"())) <= 0)
UNION ALL 
         SELECT 0 AS id, '/dynamicForm.xhtml?viewname=worktimes_checkout'::character varying(100) AS link, 1021736083 AS table_name, 'Check out'::character varying(45) AS label_action, true AS is_action
          WHERE (( SELECT count(*) AS count
                   FROM checkin_checkout
                  WHERE "current_user"() = "current_user"())) > 0;

ALTER TABLE table_actions
  OWNER TO postgres;


-- Trigger: update_tableactions_trig on table_actions

-- DROP TRIGGER update_tableactions_trig ON table_actions;

CREATE TRIGGER update_tableactions_trig
  INSTEAD OF INSERT OR UPDATE OR DELETE
  ON table_actions
  FOR EACH ROW
  EXECUTE PROCEDURE update_tableactions();

