CREATE TABLE ticket (
    id text PRIMARY KEY,
    type text NOT NULL,
    number_of_times_used integer NOT NULL,
    creation_time timestamp with time zone NOT NULL,
    ticket_granting_ticket_id text REFERENCES ticket (id) ON DELETE CASCADE,
    data jsonb NOT NULL
);

CREATE INDEX ticket_type_idx ON ticket (type);

DROP TABLE IF EXISTS locks;
DROP TABLE IF EXISTS serviceticket;
DROP TABLE IF EXISTS ticketgrantingticket;
DROP TABLE IF EXISTS transientsessionticket;
