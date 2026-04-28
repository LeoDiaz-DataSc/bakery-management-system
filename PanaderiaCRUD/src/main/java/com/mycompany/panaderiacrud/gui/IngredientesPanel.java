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
import com.mycompany.panaderiacrud.util.DataChangeListener;
import com.mycompany.panaderiacrud.util.DataChangeNotifier;
import com.toedter.calendar.JDateChooser;

public class IngredientesPanel extends JPanel implements DataChangeListener {
    private JTable tablaIngredientes;
    private DefaultTableModel modeloTabla;
    private JTextField txtBusqueda;
    private List<Ingrediente> ingredientes;
    private List<Proveedor> proveedores;
    
    public IngredientesPanel() {
        initComponents();
        cargarProveedores();
        cargarDatos();
        
        // Registrar este panel como listener para cambios en los datos
        DataChangeNotifier.getInstance().addListener(this);
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
        btnAgregar.addActionListener(e -> mostrarDialogoIngrediente(null));
        
        JButton btnEditar = new JButton("Editar");
        btnEditar.setBackground(new Color(0, 120, 212));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(e -> editarIngredienteSeleccionado());
        
        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setBackground(new Color(220, 0, 0));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(e -> eliminarIngredienteSeleccionado());
        
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        
        // Campo de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBusqueda.setBackground(Color.WHITE);
        
        txtBusqueda = new JTextField(20);
        txtBusqueda.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar ingrediente...");
        txtBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarIngredientes();
            }
        });
        
        panelBusqueda.add(txtBusqueda);
        
        panelSuperior.add(panelBotones, BorderLayout.WEST);
        panelSuperior.add(panelBusqueda, BorderLayout.EAST);
        
        // Tabla de ingredientes
        modeloTabla = new DefaultTableModel(new Object[]{
            "ID", "Nombre", "Unidad", "Costo", "Stock Actual", "Stock Mínimo", "Proveedor"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaIngredientes = new JTable(modeloTabla);
        tablaIngredientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaIngredientes.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(tablaIngredientes);
        
        // Agregar componentes al panel principal
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void cargarProveedores() {
        proveedores = new ArrayList<>();
        String sql = "SELECT * FROM proveedores ORDER BY Nombre_Proveedor";
        
        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                proveedores.add(new Proveedor(
                    rs.getInt("ID_Proveedor"),
                    rs.getString("Nombre_Proveedor"),
                    rs.getString("Contacto"),
                    rs.getString("Telefono"),
                    rs.getString("Email"),
                    rs.getString("Direccion")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar los proveedores: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cargarDatos() {
        ingredientes = new ArrayList<>();
        modeloTabla.setRowCount(0);
        
        String sql = "SELECT i.*, p.Nombre_Proveedor FROM ingredientes i " +
                    "LEFT JOIN proveedores p ON i.ID_Proveedor = p.ID_Proveedor " +
                    "ORDER BY i.Nom_Ingrediente";
        
        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Ingrediente ingrediente = new Ingrediente(
                    rs.getInt("ID_Ingrediente"),
                    rs.getString("Nom_Ingrediente"),
                    rs.getString("Unidad_Med"),
                    rs.getBigDecimal("Costo_Unidad"),
                    rs.getInt("Stock_Actual"),
                    rs.getInt("Stock_Minimo"),
                    rs.getInt("ID_Proveedor"),
                    rs.getString("Nombre_Proveedor")
                );
                ingredientes.add(ingrediente);
                modeloTabla.addRow(new Object[]{
                    ingrediente.getId(),
                    ingrediente.getNombre(),
                    ingrediente.getUnidad(),
                    ingrediente.getCosto(),
                    ingrediente.getStockActual(),
                    ingrediente.getStockMinimo(),
                    ingrediente.getNombreProveedor()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar los ingredientes: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarIngredientes() {
        String busqueda = txtBusqueda.getText().toLowerCase();
        modeloTabla.setRowCount(0);
        
        for (Ingrediente ingrediente : ingredientes) {
            if (ingrediente.getNombre().toLowerCase().contains(busqueda) ||
                ingrediente.getUnidad().toLowerCase().contains(busqueda) ||
                ingrediente.getNombreProveedor().toLowerCase().contains(busqueda)) {
                modeloTabla.addRow(new Object[]{
                    ingrediente.getId(),
                    ingrediente.getNombre(),
                    ingrediente.getUnidad(),
                    ingrediente.getCosto(),
                    ingrediente.getStockActual(),
                    ingrediente.getStockMinimo(),
                    ingrediente.getNombreProveedor()
                });
            }
        }
    }
    
    private void mostrarDialogoIngrediente(Ingrediente ingrediente) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(ingrediente == null ? "Nuevo Ingrediente" : "Editar Ingrediente");
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
        if (ingrediente != null) {
            txtNombre.setText(ingrediente.getNombre());
        }
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(txtNombre, gbc);
        
        JLabel lblUnidad = new JLabel("Unidad:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblUnidad, gbc);
        
        JTextField txtUnidad = new JTextField(20);
        if (ingrediente != null) {
            txtUnidad.setText(ingrediente.getUnidad());
        }
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(txtUnidad, gbc);
        
        JLabel lblCosto = new JLabel("Costo:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblCosto, gbc);
        
        JTextField txtCosto = new JTextField(20);
        if (ingrediente != null) {
            txtCosto.setText(ingrediente.getCosto().toString());
        }
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(txtCosto, gbc);
        
        JLabel lblStockActual = new JLabel("Stock Actual:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblStockActual, gbc);
        
        JSpinner spnStockActual = new JSpinner(new SpinnerNumberModel(
            ingrediente != null ? ingrediente.getStockActual() : 0,
            0, Integer.MAX_VALUE, 1));
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(spnStockActual, gbc);
        
        JLabel lblStockMinimo = new JLabel("Stock Mínimo:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(lblStockMinimo, gbc);
        
        JSpinner spnStockMinimo = new JSpinner(new SpinnerNumberModel(
            ingrediente != null ? ingrediente.getStockMinimo() : 5,
            0, Integer.MAX_VALUE, 1));
        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(spnStockMinimo, gbc);
        
        JLabel lblProveedor = new JLabel("Proveedor:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(lblProveedor, gbc);
        
        JComboBox<Proveedor> cmbProveedor = new JComboBox<>();
        for (Proveedor p : proveedores) {
            cmbProveedor.addItem(p);
        }
        if (ingrediente != null) {
            for (int i = 0; i < cmbProveedor.getItemCount(); i++) {
                if (cmbProveedor.getItemAt(i).getId() == ingrediente.getIdProveedor()) {
                    cmbProveedor.setSelectedIndex(i);
                    break;
                }
            }
        }
        gbc.gridx = 1;
        gbc.gridy = 5;
        panel.add(cmbProveedor, gbc);
        
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(0, 120, 212));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> {
            try {
                String nombre = txtNombre.getText().trim();
                String unidad = txtUnidad.getText().trim();
                double costo = Double.parseDouble(txtCosto.getText().trim());
                int stockActual = (int) spnStockActual.getValue();
                int stockMinimo = (int) spnStockMinimo.getValue();
                Proveedor proveedor = (Proveedor) cmbProveedor.getSelectedItem();
                
                if (nombre.isEmpty() || unidad.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "El nombre y la unidad no pueden estar vacíos",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (costo <= 0) {
                    JOptionPane.showMessageDialog(dialog,
                        "El costo debe ser mayor que 0",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (stockMinimo < 0) {
                    JOptionPane.showMessageDialog(dialog,
                        "El stock mínimo no puede ser negativo",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (ingrediente == null) {
                    // Insertar nuevo ingrediente
                    String sql = "INSERT INTO ingredientes (Nom_Ingrediente, Unidad_Med, Costo_Unidad, Stock_Actual, Stock_Minimo, ID_Proveedor) VALUES (?, ?, ?, ?, ?, ?)";
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, nombre);
                        pstmt.setString(2, unidad);
                        pstmt.setDouble(3, costo);
                        pstmt.setInt(4, stockActual);
                        pstmt.setInt(5, stockMinimo);
                        pstmt.setInt(6, proveedor.getId());
                        pstmt.executeUpdate();
                        cargarDatos();
                        dialog.dispose();
                        
                        // Notificar a los otros paneles que se ha añadido un ingrediente
                        DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.INGREDIENTE_ADDED);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al guardar el ingrediente: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // Actualizar ingrediente existente
                    String sql = "UPDATE ingredientes SET Nom_Ingrediente = ?, Unidad_Med = ?, Costo_Unidad = ?, Stock_Actual = ?, Stock_Minimo = ?, ID_Proveedor = ? WHERE ID_Ingrediente = ?";
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, nombre);
                        pstmt.setString(2, unidad);
                        pstmt.setDouble(3, costo);
                        pstmt.setInt(4, stockActual);
                        pstmt.setInt(5, stockMinimo);
                        pstmt.setInt(6, proveedor.getId());
                        pstmt.setInt(7, ingrediente.getId());
                        pstmt.executeUpdate();
                        cargarDatos();
                        dialog.dispose();
                        
                        // Notificar a los otros paneles que se ha actualizado un ingrediente
                        DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.INGREDIENTE_UPDATED);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al actualizar el ingrediente: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "El costo debe ser un número válido",
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
    
    private void editarIngredienteSeleccionado() {
        int fila = tablaIngredientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un ingrediente para editar",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Ingrediente ingrediente = ingredientes.stream()
            .filter(i -> i.getId() == id)
            .findFirst()
            .orElse(null);
        
        if (ingrediente != null) {
            mostrarDialogoIngrediente(ingrediente);
        }
    }
    
    private void eliminarIngredienteSeleccionado() {
        int fila = tablaIngredientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un ingrediente para eliminar",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modeloTabla.getValueAt(fila, 0);
        String nombre = (String) modeloTabla.getValueAt(fila, 1);
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar el ingrediente '" + nombre + "'?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM ingredientes WHERE ID_Ingrediente = ?";
            try (Connection conn = ConexionDB.getConexion();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                cargarDatos();
                
                // Notificar a los otros paneles que se ha eliminado un ingrediente
                DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.INGREDIENTE_DELETED);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar el ingrediente: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    public void onDataChanged(DataChangedEvent event) {
        // Actualizar los proveedores si ha habido cambios en ellos
        if (event == DataChangedEvent.PROVEEDOR_ADDED || 
            event == DataChangedEvent.PROVEEDOR_UPDATED || 
            event == DataChangedEvent.PROVEEDOR_DELETED) {
            cargarProveedores();
        }
    }
    
    private static class Ingrediente {
        private final int id;
        private final String nombre;
        private final String unidad;
        private final java.math.BigDecimal costo;
        private final int stockActual;
        private final int stockMinimo;
        private final int idProveedor;
        private final String nombreProveedor;
        
        public Ingrediente(int id, String nombre, String unidad, java.math.BigDecimal costo,
                          int stockActual, int stockMinimo, int idProveedor, String nombreProveedor) {
            this.id = id;
            this.nombre = nombre;
            this.unidad = unidad;
            this.costo = costo;
            this.stockActual = stockActual;
            this.stockMinimo = stockMinimo;
            this.idProveedor = idProveedor;
            this.nombreProveedor = nombreProveedor;
        }
        
        public int getId() {
            return id;
        }
        
        public String getNombre() {
            return nombre;
        }
        
        public String getUnidad() {
            return unidad;
        }
        
        public java.math.BigDecimal getCosto() {
            return costo;
        }
        
        public int getStockActual() {
            return stockActual;
        }
        
        public int getStockMinimo() {
            return stockMinimo;
        }
        
        public int getIdProveedor() {
            return idProveedor;
        }
        
        public String getNombreProveedor() {
            return nombreProveedor;
        }
    }
    
    private static class Proveedor {
        private final int id;
        private final String nombre;
        private final String contacto;
        private final String telefono;
        private final String email;
        private final String direccion;
        
        public Proveedor(int id, String nombre, String contacto, String telefono, String email, String direccion) {
            this.id = id;
            this.nombre = nombre;
            this.contacto = contacto;
            this.telefono = telefono;
            this.email = email;
            this.direccion = direccion;
        }
        
        public int getId() {
            return id;
        }
        
        public String getNombre() {
            return nombre;
        }
        
        public String getContacto() {
            return contacto;
        }
        
        public String getTelefono() {
            return telefono;
        }
        
        public String getEmail() {
            return email;
        }
        
        public String getDireccion() {
            return direccion;
        }
        
        @Override
        public String toString() {
            return nombre;
        }
    }
}