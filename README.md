# Realty Leads

Realty Leads es el microservicio encargado de gestionar los leads o contactos comerciales interesados en los inmuebles y servicios de RealtyHub. Su propósito es organizar la información de los contactos y apoyar el seguimiento de las oportunidades comerciales.

## Funcionalidades

- Creación de leads cuando un cliente se interesa en una propiedad.
- Asignación automática del lead a un agente según zona, carga de trabajo y tasa de conversión.
- Seguimiento del estado del lead hasta su cierre o descarte.
- Publica eventos cuando se crea, se acepta una oferta o se descarta un lead.

## Tecnologías

Java 21, Spring Boot, PostgreSQL, Flyway y RabbitMQ.

## Puerto

El servicio se ejecuta en el puerto `8084`.
