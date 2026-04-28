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
import com.mycompany.panaderiacrud.util.ConexionDB;
import com.mycompany.panaderiacrud.util.DataChangedEvent;
import com.mycompany.panaderiacrud.util.DataChangeNotifier;

public class CategoriasPanel extends JPanel {
    private JTable tablaCategorias;
    private DefaultTableModel modeloTabla;
    private JTextField txtBusqueda;
    private List<Categoria> categorias;
    
    public CategoriasPanel() {
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
        
        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.setBackground(new Color(0, 120, 212));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(e -> mostrarDialogoCategoria(null));
        
        JButton btnEditar = new JButton("Editar");
        btnEditar.setBackground(new Color(0, 120, 212));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(e -> editarCategoriaSeleccionada());
        
        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setBackground(new Color(220, 0, 0));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(e -> eliminarCategoriaSeleccionada());
        
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        
        // Campo de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBusqueda.setBackground(Color.WHITE);
        
        txtBusqueda = new JTextField(20);
        txtBusqueda.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar categoría...");
        txtBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarCategorias();
            }
        });
        
        panelBusqueda.add(txtBusqueda);
        
        panelSuperior.add(panelBotones, BorderLayout.WEST);
        panelSuperior.add(panelBusqueda, BorderLayout.EAST);
        
        // Tabla de categorías
        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Nombre"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaCategorias = new JTable(modeloTabla);
        tablaCategorias.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaCategorias.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(tablaCategorias);
        
        // Agregar componentes al panel principal
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void cargarDatos() {
        categorias = new ArrayList<>();
        modeloTabla.setRowCount(0);
        
        String sql = "SELECT * FROM categorias ORDER BY Nombre_Categoria";
        
        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Categoria categoria = new Categoria(
                    rs.getInt("ID_Categoria"),
                    rs.getString("Nombre_Categoria")
                );
                
                categorias.add(categoria);
                modeloTabla.addRow(new Object[]{
                    categoria.getId(),
                    categoria.getNombre()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar las categorías: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarCategorias() {
        String busqueda = txtBusqueda.getText().toLowerCase();
        modeloTabla.setRowCount(0);
        
        for (Categoria categoria : categorias) {
            if (categoria.getNombre().toLowerCase().contains(busqueda)) {
                modeloTabla.addRow(new Object[]{
                    categoria.getId(),
                    categoria.getNombre()
                });
            }
        }
    }
    
    private void mostrarDialogoCategoria(Categoria categoria) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(categoria == null ? "Nueva Categoría" : "Editar Categoría");
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
        if (categoria != null) {
            txtNombre.setText(categoria.getNombre());
        }
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(txtNombre, gbc);
        
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(0, 120, 212));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> {
            try {
                String nombre = txtNombre.getText().trim();
                
                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "El nombre no puede estar vacío",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (categoria == null) {
                    // Insertar nueva categoría
                    String sql = "INSERT INTO categorias (Nombre_Categoria) VALUES (?)";
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, nombre);
                        pstmt.executeUpdate();
                        cargarDatos();
                        dialog.dispose();
                        
                        // Notificar a los otros paneles que se ha añadido una categoría
                        DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.CATEGORIA_ADDED);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al guardar la categoría: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // Actualizar categoría existente
                    String sql = "UPDATE categorias SET Nombre_Categoria = ? WHERE ID_Categoria = ?";
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, nombre);
                        pstmt.setInt(2, categoria.getId());
                        pstmt.executeUpdate();
                        cargarDatos();
                        dialog.dispose();
                        
                        // Notificar a los otros paneles que se ha actualizado una categoría
                        DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.CATEGORIA_UPDATED);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al actualizar la categoría: " + ex.getMessage(),
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
    
    private void editarCategoriaSeleccionada() {
        int fila = tablaCategorias.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione una categoría para editar",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Categoria categoria = categorias.stream()
            .filter(c -> c.getId() == id)
            .findFirst()
            .orElse(null);
        
        if (categoria != null) {
            mostrarDialogoCategoria(categoria);
        }
    }
    
    private void eliminarCategoriaSeleccionada() {
        int fila = tablaCategorias.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione una categoría para eliminar",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modeloTabla.getValueAt(fila, 0);
        String nombre = (String) modeloTabla.getValueAt(fila, 1);
        
        // Verificar si hay productos usando esta categoría
        String sqlCheck = "SELECT COUNT(*) FROM productos WHERE ID_Categoria = ?";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this,
                        "No se puede eliminar la categoría porque hay productos que la utilizan",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al verificar productos: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar la categoría '" + nombre + "'?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM categorias WHERE ID_Categoria = ?";
            try (Connection conn = ConexionDB.getConexion();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                cargarDatos();
                
                // Notificar a los otros paneles que se ha eliminado una categoría
                DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.CATEGORIA_DELETED);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar la categoría: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private static class Categoria {
        private final int id;
        private final String nombre;
        
        public Categoria(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
        
        public int getId() {
            return id;
        }
        
        public String getNombre() {
            return nombre;
        }
    }
}