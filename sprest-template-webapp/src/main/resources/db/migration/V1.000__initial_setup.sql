--
-- PostgreSQL database dump
--

-- Dumped from database version 16.1
-- Dumped by pg_dump version 16.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: access_right; Type: TABLE; Schema: public; Owner: sprest_user
--

CREATE TABLE public.access_right (
    id integer NOT NULL,
    name character varying(255)
);


--
-- Name: announcement; Type: TABLE; Schema: public; Owner: sprest_user
--

CREATE TABLE public.announcement (
    id integer NOT NULL,
    body_html character varying(1000) NOT NULL,
    end_date timestamp(6) without time zone NOT NULL,
    heading character varying(200) NOT NULL,
    start_date timestamp(6) without time zone NOT NULL
);


--
-- Name: app_user; Type: TABLE; Schema: public; Owner: sprest_user
--

CREATE TABLE public.app_user (
    id integer NOT NULL,
    active boolean NOT NULL,
    email character varying(1000),
    first_name character varying(1000),
    last_name character varying(1000),
    password character varying(100) NOT NULL,
    password_reset_token character varying(64),
    password_reset_token_valid_until timestamp(6) without time zone,
    title character varying(100),
    user_name character varying(50) NOT NULL
);


--
-- Name: app_user_access_rights; Type: TABLE; Schema: public; Owner: sprest_user
--

CREATE TABLE public.app_user_access_rights (
    app_user_id integer NOT NULL,
    access_rights_id integer NOT NULL
);


--
-- Data for Name: access_right; Type: TABLE DATA; Schema: public; Owner: sprest_user
--

COPY public.access_right (id, name) FROM stdin;
\.


--
-- Data for Name: announcement; Type: TABLE DATA; Schema: public; Owner: sprest_user
--

COPY public.announcement (id, body_html, end_date, heading, start_date) FROM stdin;
\.


--
-- Data for Name: app_user; Type: TABLE DATA; Schema: public; Owner: sprest_user
--

COPY public.app_user (id, active, email, first_name, last_name, password, password_reset_token, password_reset_token_valid_until, title, user_name) FROM stdin;
\.


--
-- Data for Name: app_user_access_rights; Type: TABLE DATA; Schema: public; Owner: sprest_user
--

COPY public.app_user_access_rights (app_user_id, access_rights_id) FROM stdin;
\.


--
-- Name: access_right access_right_pkey; Type: CONSTRAINT; Schema: public; Owner: sprest_user
--

ALTER TABLE ONLY public.access_right
    ADD CONSTRAINT access_right_pkey PRIMARY KEY (id);


--
-- Name: announcement announcement_pkey; Type: CONSTRAINT; Schema: public; Owner: sprest_user
--

ALTER TABLE ONLY public.announcement
    ADD CONSTRAINT announcement_pkey PRIMARY KEY (id);


--
-- Name: app_user_access_rights app_user_access_rights_pkey; Type: CONSTRAINT; Schema: public; Owner: sprest_user
--

ALTER TABLE ONLY public.app_user_access_rights
    ADD CONSTRAINT app_user_access_rights_pkey PRIMARY KEY (app_user_id, access_rights_id);


--
-- Name: app_user app_user_pkey; Type: CONSTRAINT; Schema: public; Owner: sprest_user
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT app_user_pkey PRIMARY KEY (id);


--
-- Name: app_user uc_user_email; Type: CONSTRAINT; Schema: public; Owner: sprest_user
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT uc_user_email UNIQUE (email);


--
-- Name: app_user uc_user_name; Type: CONSTRAINT; Schema: public; Owner: sprest_user
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT uc_user_name UNIQUE (user_name);


--
-- Name: idx_lastname; Type: INDEX; Schema: public; Owner: sprest_user
--

CREATE INDEX idx_lastname ON public.app_user USING btree (last_name);


--
-- Name: idx_user_email; Type: INDEX; Schema: public; Owner: sprest_user
--

CREATE INDEX idx_user_email ON public.app_user USING btree (email);


--
-- Name: idx_user_name; Type: INDEX; Schema: public; Owner: sprest_user
--

CREATE INDEX idx_user_name ON public.app_user USING btree (user_name);


--
-- Name: app_user_access_rights fkokx19susdcrevtgus3i3ldrft; Type: FK CONSTRAINT; Schema: public; Owner: sprest_user
--

ALTER TABLE ONLY public.app_user_access_rights
    ADD CONSTRAINT fkokx19susdcrevtgus3i3ldrft FOREIGN KEY (app_user_id) REFERENCES public.app_user(id);


--
-- Name: app_user_access_rights fkthdiayiif9qbypyu04df7t7ti; Type: FK CONSTRAINT; Schema: public; Owner: sprest_user
--

ALTER TABLE ONLY public.app_user_access_rights
    ADD CONSTRAINT fkthdiayiif9qbypyu04df7t7ti FOREIGN KEY (access_rights_id) REFERENCES public.access_right(id);


--
-- PostgreSQL database dump complete
--

