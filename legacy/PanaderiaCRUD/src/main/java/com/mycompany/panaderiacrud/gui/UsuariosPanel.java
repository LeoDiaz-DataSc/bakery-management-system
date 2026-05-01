package com.mycompany.panaderiacrud.gui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import com.mycompany.panaderiacrud.util.ConexionDB;

public class UsuariosPanel extends JPanel {
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private JTextField txtBusqueda;
    private List<Usuario> usuarios;
    
    public UsuariosPanel() {
        initComponents();
        cargarDatos();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Panel superior con botones y búsqueda
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(Color.WHITE);
        
        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBotones.setBackground(Color.WHITE);
        
        JButton btnAgregar = new JButton("Nuevo Usuario");
        btnAgregar.setBackground(new Color(0, 120, 212));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(e -> mostrarDialogoUsuario(null));
        
        JButton btnEditar = new JButton("Editar Usuario");
        btnEditar.setBackground(new Color(0, 120, 212));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(e -> editarUsuario());
        
        JButton btnEliminar = new JButton("Eliminar Usuario");
        btnEliminar.setBackground(new Color(220, 53, 69));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(e -> eliminarUsuario());
        
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        
        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBusqueda.setBackground(Color.WHITE);
        
        txtBusqueda = new JTextField(20);
        txtBusqueda.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar usuario...");
        txtBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarUsuarios();
            }
        });
        
        panelBusqueda.add(txtBusqueda);
        
        panelSuperior.add(panelBotones, BorderLayout.WEST);
        panelSuperior.add(panelBusqueda, BorderLayout.EAST);
        
        // Tabla de usuarios
        modeloTabla = new DefaultTableModel(new Object[]{
            "ID", "Nombre", "Apellido", "Correo", "Rol"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        
        // Agregar componentes al panel principal
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void cargarDatos() {
        usuarios = new ArrayList<>();
        modeloTabla.setRowCount(0);
        
        String sql = "SELECT * FROM usuarios ORDER BY Nombre";
        
        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Usuario usuario = new Usuario(
                    rs.getInt("ID_Usuario"),
                    rs.getString("Nombre"),
                    rs.getString("Apellido"),
                    rs.getString("Correo"),
                    rs.getString("Rol")
                );
                usuarios.add(usuario);
                modeloTabla.addRow(new Object[]{
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getCorreo(),
                    usuario.getRol()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar los usuarios: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarUsuarios() {
        String busqueda = txtBusqueda.getText().toLowerCase();
        modeloTabla.setRowCount(0);
        
        for (Usuario usuario : usuarios) {
            if (usuario.getNombre().toLowerCase().contains(busqueda) ||
                usuario.getCorreo().toLowerCase().contains(busqueda) ||
                usuario.getRol().toLowerCase().contains(busqueda)) {
                modeloTabla.addRow(new Object[]{
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getCorreo(),
                    usuario.getRol()
                });
            }
        }
    }
    
    private void mostrarDialogoUsuario(Usuario usuario) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(usuario == null ? "Nuevo Usuario" : "Editar Usuario");
        dialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Campos del formulario
        JLabel lblNombre = new JLabel("Nombre:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblNombre, gbc);
        
        JTextField txtNombre = new JTextField(20);
        if (usuario != null) {
            txtNombre.setText(usuario.getNombre());
        }
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(txtNombre, gbc);
        
        JLabel lblApellido = new JLabel("Apellido:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblApellido, gbc);
        
        JTextField txtApellido = new JTextField(20);
        if (usuario != null) {
            txtApellido.setText(usuario.getApellido());
        }
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(txtApellido, gbc);
        
        JLabel lblCorreo = new JLabel("Correo:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblCorreo, gbc);
        
        JTextField txtCorreo = new JTextField(20);
        if (usuario != null) {
            txtCorreo.setText(usuario.getCorreo());
        }
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(txtCorreo, gbc);
        
        JLabel lblRol = new JLabel("Rol:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblRol, gbc);
        
        JComboBox<String> cmbRol = new JComboBox<>(new String[]{"Administrador", "Usuario"});
        if (usuario != null) {
            cmbRol.setSelectedItem(usuario.getRol());
        }
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(cmbRol, gbc);
        
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(0, 120, 212));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> {
            try {
                String nombre = txtNombre.getText().trim();
                String apellido = txtApellido.getText().trim();
                String correo = txtCorreo.getText().trim();
                String rol = (String) cmbRol.getSelectedItem();
                
                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "El nombre no puede estar vacío",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (correo.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "El correo no puede estar vacío",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (usuario == null) {
                    // Insertar nuevo usuario
                    String sql = "INSERT INTO usuarios (Nombre, Apellido, Correo, Rol) VALUES (?, ?, ?, ?)";
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, nombre);
                        pstmt.setString(2, apellido);
                        pstmt.setString(3, correo);
                        pstmt.setString(4, rol);
                        pstmt.executeUpdate();
                        
                        cargarDatos();
                        dialog.dispose();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al guardar el usuario: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // Actualizar usuario existente
                    String sql = "UPDATE usuarios SET Nombre = ?, Apellido = ?, Correo = ?, Rol = ? WHERE ID_Usuario = ?";
                    
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        int paramIndex = 1;
                        pstmt.setString(paramIndex++, nombre);
                        pstmt.setString(paramIndex++, apellido);
                        pstmt.setString(paramIndex++, correo);
                        pstmt.setString(paramIndex++, rol);
                        pstmt.setInt(paramIndex, usuario.getId());
                        pstmt.executeUpdate();
                        
                        cargarDatos();
                        dialog.dispose();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al actualizar el usuario: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(btnGuardar);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(panelBotones, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void editarUsuario() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un usuario para editar",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Usuario usuario = usuarios.stream()
            .filter(u -> u.getId() == id)
            .findFirst()
            .orElse(null);
        
        if (usuario != null) {
            mostrarDialogoUsuario(usuario);
        }
    }
    
    private void eliminarUsuario() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un usuario para eliminar",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modeloTabla.getValueAt(fila, 0);
        String nombre = (String) modeloTabla.getValueAt(fila, 1);
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar al usuario " + nombre + "?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM usuarios WHERE ID_Usuario = ?";
            try (Connection conn = ConexionDB.getConexion();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                
                cargarDatos();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar el usuario: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private static class Usuario {
        private final int id;
        private final String nombre;
        private final String apellido;
        private final String correo;
        private final String rol;
        
        public Usuario(int id, String nombre, String apellido, String correo, String rol) {
            this.id = id;
            this.nombre = nombre;
            this.apellido = apellido;
            this.correo = correo;
            this.rol = rol;
        }
        
        public int getId() {
            return id;
        }
        
        public String getNombre() {
            return nombre;
        }
        
        public String getApellido() {
            return apellido;
        }
        
        public String getCorreo() {
            return correo;
        }
        
        public String getRol() {
            return rol;
        }
    }
} 