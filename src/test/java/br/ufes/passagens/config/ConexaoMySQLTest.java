package br.ufes.passagens.config;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConexaoMySQLTest {
    @Test
    void expoeOsTresMetodosExigidosPeloProfessor() throws Exception {
        assertEquals(Connection.class, ConexaoMySQL.class.getMethod("Conectar").getReturnType());
        assertNotNull(ConexaoMySQL.class.getMethod("InserirDadosNoMySQL", String.class, Object[].class));
        assertNotNull(ConexaoMySQL.class.getMethod("Desconectar"));
    }
}
