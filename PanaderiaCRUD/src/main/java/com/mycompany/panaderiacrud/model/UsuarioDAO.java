package com.mycompany.panaderiacrud.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.mycompany.panaderiacrud.util.ConexionDB;

public class UsuarioDAO {
    public static class UsuarioAutenticado {
        private final String correo;
        private final String rol;
        private final String nombre;
        
        public UsuarioAutenticado(String correo, String rol, String nombre) {
            this.correo = correo;
            this.rol = rol;
            this.nombre = nombre;
        }
        
        public String getCorreo() { return correo; }
        public String getRol() { return rol; }
        public String getNombre() { return nombre; }
    }
    
    public static UsuarioAutenticado autenticarUsuario(String correo, String contrasena) throws SQLException {
        // Primero verificamos si el usuario existe
        String sqlCheck = "SELECT COUNT(*) FROM usuarios WHERE Correo = ?";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
            
            pstmt.setString(1, correo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("Usuario no encontrado: " + correo);
                    return null;
                }
            }
        }
        
        // Si el usuario existe, intentamos autenticar
        String sql = "SELECT Nombre, Apellido, Rol FROM usuarios WHERE Correo = ? AND Contrasena = ?";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, correo);
            pstmt.setString(2, contrasena);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Usuario autenticado exitosamente: " + correo);
                    return new UsuarioAutenticado(
                        correo,
                        rs.getString("Rol"),
                        rs.getString("Nombre") + " " + rs.getString("Apellido")
                    );
                } else {
                    System.out.println("Contraseña incorrecta para usuario: " + correo);
                    return null;
                }
            }
        }
    }
    
    public static void cambiarContrasena(String correo, String nuevaContrasena) throws SQLException {
        String sql = "UPDATE usuarios SET Contrasena = ? WHERE Correo = ?";
        
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nuevaContrasena);
            pstmt.setString(2, correo);
            pstmt.executeUpdate();
        }
    }
    
    public static boolean existeUsuario(String correo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE Correo = ?";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, correo);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
} 