--
-- PostgreSQL database dump
--

\restrict GeZBm3LGCspfyZpvWPEIuoAcVogftkGlqujSEaDfHrXtxX6PBDi0DsdLvJt7f9J

-- Dumped from database version 18.6 (ac70818)
-- Dumped by pg_dump version 18.4

-- Started on 2026-11-10 20:25:26

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
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
-- TOC entry 219 (class 1259 OID 16564)
-- Name: citas; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.citas (
    id_cita integer NOT NULL,
    id_cliente integer NOT NULL,
    servicio character varying(100) NOT NULL,
    fecha date NOT NULL,
    hora time without time zone NOT NULL,
    estado character varying(30) DEFAULT 'Pendiente'::character varying NOT NULL,
    fecha_registro timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.citas OWNER TO neondb_owner;

--
-- TOC entry 220 (class 1259 OID 16575)
-- Name: citas_id_cita_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.citas_id_cita_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.citas_id_cita_seq OWNER TO neondb_owner;

--
-- TOC entry 3452 (class 0 OID 0)
-- Dependencies: 220
-- Name: citas_id_cita_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.citas_id_cita_seq OWNED BY public.citas.id_cita;


--
-- TOC entry 221 (class 1259 OID 16576)
-- Name: clientes; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.clientes (
    id_cliente integer NOT NULL,
    nombre character varying(100) NOT NULL,
    apellido character varying(100) NOT NULL,
    telefono character varying(30) NOT NULL,
    correo character varying(150) NOT NULL,
    contrasena character varying(255) NOT NULL,
    fecha_registro timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.clientes OWNER TO neondb_owner;

--
-- TOC entry 222 (class 1259 OID 16588)
-- Name: clientes_id_cliente_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

CREATE SEQUENCE public.clientes_id_cliente_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.clientes_id_cliente_seq OWNER TO neondb_owner;

--
-- TOC entry 3453 (class 0 OID 0)
-- Dependencies: 222
-- Name: clientes_id_cliente_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: neondb_owner
--

ALTER SEQUENCE public.clientes_id_cliente_seq OWNED BY public.clientes.id_cliente;


--
-- TOC entry 223 (class 1259 OID 16589)
-- Name: recuperacion_contrasenas; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.recuperacion_contrasenas (
    id_recuperacion integer NOT NULL,
    id_cliente integer NOT NULL,
    token_hash character varying(64) NOT NULL,
    fecha_expiracion timestamp without time zone NOT NULL,
    utilizado boolean DEFAULT false NOT NULL
);


ALTER TABLE public.recuperacion_contrasenas OWNER TO neondb_owner;

--
-- TOC entry 224 (class 1259 OID 16598)
-- Name: recuperacion_contrasenas_id_recuperacion_seq; Type: SEQUENCE; Schema: public; Owner: neondb_owner
--

ALTER TABLE public.recuperacion_contrasenas ALTER COLUMN id_recuperacion ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.recuperacion_contrasenas_id_recuperacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 3281 (class 2604 OID 16599)
-- Name: citas id_cita; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.citas ALTER COLUMN id_cita SET DEFAULT nextval('public.citas_id_cita_seq'::regclass);


--
-- TOC entry 3284 (class 2604 OID 16600)
-- Name: clientes id_cliente; Type: DEFAULT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.clientes ALTER COLUMN id_cliente SET DEFAULT nextval('public.clientes_id_cliente_seq'::regclass);


--
-- TOC entry 3288 (class 2606 OID 16602)
-- Name: citas citas_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.citas
    ADD CONSTRAINT citas_pkey PRIMARY KEY (id_cita);


--
-- TOC entry 3291 (class 2606 OID 16604)
-- Name: clientes clientes_correo_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.clientes
    ADD CONSTRAINT clientes_correo_key UNIQUE (correo);


--
-- TOC entry 3293 (class 2606 OID 16606)
-- Name: clientes clientes_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.clientes
    ADD CONSTRAINT clientes_pkey PRIMARY KEY (id_cliente);


--
-- TOC entry 3295 (class 2606 OID 16608)
-- Name: recuperacion_contrasenas recuperacion_contrasenas_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.recuperacion_contrasenas
    ADD CONSTRAINT recuperacion_contrasenas_pkey PRIMARY KEY (id_recuperacion);


--
-- TOC entry 3297 (class 2606 OID 16610)
-- Name: recuperacion_contrasenas recuperacion_contrasenas_token_hash_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.recuperacion_contrasenas
    ADD CONSTRAINT recuperacion_contrasenas_token_hash_key UNIQUE (token_hash);


--
-- TOC entry 3289 (class 1259 OID 16611)
-- Name: idx_citas_horario_ocupado; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE UNIQUE INDEX idx_citas_horario_ocupado ON public.citas USING btree (fecha, hora) WHERE ((estado)::text <> 'Cancelada'::text);


--
-- TOC entry 3298 (class 2606 OID 16612)
-- Name: citas fk_cliente; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.citas
    ADD CONSTRAINT fk_cliente FOREIGN KEY (id_cliente) REFERENCES public.clientes(id_cliente) ON DELETE CASCADE;


--
-- TOC entry 3299 (class 2606 OID 16617)
-- Name: recuperacion_contrasenas recuperacion_contrasenas_id_cliente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.recuperacion_contrasenas
    ADD CONSTRAINT recuperacion_contrasenas_id_cliente_fkey FOREIGN KEY (id_cliente) REFERENCES public.clientes(id_cliente) ON DELETE CASCADE;


--
-- TOC entry 2062 (class 826 OID 16399)
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: public; Owner: cloud_admin
--

ALTER DEFAULT PRIVILEGES FOR ROLE cloud_admin IN SCHEMA public GRANT ALL ON SEQUENCES TO neon_superuser WITH GRANT OPTION;


--
-- TOC entry 2063 (class 826 OID 16398)
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: public; Owner: cloud_admin
--

ALTER DEFAULT PRIVILEGES FOR ROLE cloud_admin IN SCHEMA public GRANT SELECT,INSERT,DELETE,TRUNCATE,MAINTAIN,UPDATE ON TABLES TO neon_superuser WITH GRANT OPTION;


-- Completed on 2026-11-10 20:25:40

--
-- PostgreSQL database dump complete
--

\unrestrict GeZBm3LGCspfyZpvWPEIuoAcVogftkGlqujSEaDfHrXtxX6PBDi0DsdLvJt7f9J

