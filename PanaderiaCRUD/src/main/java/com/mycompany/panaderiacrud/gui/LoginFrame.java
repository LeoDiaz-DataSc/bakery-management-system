package com.mycompany.panaderiacrud.gui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.panaderiacrud.model.UsuarioDAO;
import com.mycompany.panaderiacrud.model.UsuarioDAO.UsuarioAutenticado;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JButton btnLogin;
    
    public LoginFrame() {
        initComponents();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("Sistema de Panadería - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Panel principal con gradiente
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(255, 255, 255);
                Color color2 = new Color(240, 240, 240);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        
        // Panel de login
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBackground(new Color(255, 255, 255, 200));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Título
        JLabel lblTitulo = new JLabel("Sistema de Panadería");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(lblTitulo, gbc);
        
        // Usuario
        JLabel lblUsuario = new JLabel("Correo:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        loginPanel.add(lblUsuario, gbc);
        
        txtUsuario = new JTextField(20);
        txtUsuario.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingrese su correo");
        gbc.gridx = 1;
        gbc.gridy = 1;
        loginPanel.add(txtUsuario, gbc);
        
        // Contraseña
        JLabel lblContrasena = new JLabel("Contraseña:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(lblContrasena, gbc);
        
        txtContrasena = new JPasswordField(20);
        txtContrasena.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingrese su contraseña");
        gbc.gridx = 1;
        gbc.gridy = 2;
        loginPanel.add(txtContrasena, gbc);
        
        // Botón de login
        btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setBackground(new Color(0, 120, 212));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        loginPanel.add(btnLogin, gbc);
        
        // Agregar panel de login al panel principal
        mainPanel.add(loginPanel);
        
        // Configurar el frame
        add(mainPanel);
        pack();
        
        // Eventos
        btnLogin.addActionListener(e -> validarLogin());
        txtContrasena.addActionListener(e -> validarLogin());
        
        // Establecer tamaño mínimo
        setMinimumSize(new Dimension(400, 300));
    }
    
    private void validarLogin() {
        String correo = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        
        if (correo.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor ingrese correo y contraseña", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            UsuarioAutenticado usuario = UsuarioDAO.autenticarUsuario(correo, contrasena);
            if (usuario != null) {
                JOptionPane.showMessageDialog(this, 
                    "Bienvenido " + usuario.getNombre(), 
                    "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Abrir ventana principal
                this.dispose();
                new MainFrame(usuario.getRol()).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Credenciales incorrectas o usuario inactivo", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                txtContrasena.setText("");
                txtContrasena.requestFocus();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al intentar iniciar sesión: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            System.err.println("Error de login: " + e.getMessage());
        }
    }
} 