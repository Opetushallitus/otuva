drop table if exists cas_tickets cascade;

create table Cas_Tickets (
    creation_Time timestamp(6) with time zone not null,
    expiration_Time timestamp(6) with time zone not null,
    last_Used_Time timestamp(6) with time zone,
    id varchar(768) not null,
    parent_Id varchar(1024),
    principal_Id varchar(1024),
    type varchar(1024) not null,
    service varchar(2048),
    body text,
    attributes json,
    primary key (id)
);

create index cas_tickets_parent_id_idx on Cas_Tickets (parent_Id);
