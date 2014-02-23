-- Function: update_tableactions()

-- DROP FUNCTION update_tableactions();

CREATE OR REPLACE FUNCTION update_tableactions()
  RETURNS trigger AS
$BODY$
    BEGIN
        --
        -- Create a row in emp_audit to reflect the operation performed on emp,
        -- make use of the special variable TG_OP to work out the operation.
        --
        IF (TG_OP = 'DELETE') THEN
            delete from table_actions_inner where id = OLD.id;
	    RETURN OLD;
        ELSIF (TG_OP = 'UPDATE') THEN
	    delete from table_actions_inner where id = OLD.id;
	    insert into table_actions_inner select NEW.*;
            RETURN NEW;
        ELSIF (TG_OP = 'INSERT') THEN
        
	    insert into table_actions_inner (
            link, table_name, label_action, is_action)
		VALUES (new.link, new.table_name, new.label_action, new.is_action);
            RETURN NEW;
        END IF;
        RETURN NULL; -- result is ignored since this is an AFTER trigger
    END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION update_tableactions()
  OWNER TO postgres;
