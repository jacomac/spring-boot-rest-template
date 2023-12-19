-- public.store definition

-- Drop table

-- DROP TABLE public.store;

CREATE TABLE public.store
(
    id     serial4      NOT NULL,
    "name" varchar(255) NOT NULL,
    CONSTRAINT store_pkey PRIMARY KEY (id)
);


-- public.item definition

-- Drop table

-- DROP TABLE public.item;

CREATE TABLE public.item
(
    id            serial4      NOT NULL,
    creation_date timestamp(6) NULL,
    "name"        varchar(255) NOT NULL,
    store_id      int4 NULL,
    CONSTRAINT item_pkey PRIMARY KEY (id),
    CONSTRAINT fki0c87m5jy5qxw8orrf2pugs6h FOREIGN KEY (store_id) REFERENCES public.store (id)
);