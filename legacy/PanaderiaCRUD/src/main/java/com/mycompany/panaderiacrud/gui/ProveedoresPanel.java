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

public class ProveedoresPanel extends JPanel implements DataChangeListener {
    private JTable tablaProveedores;
    private DefaultTableModel modeloTabla;
    private JTextField txtBusqueda;
    private List<Proveedor> proveedores;
    
    public ProveedoresPanel() {
        initComponents();
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
        btnAgregar.addActionListener(e -> mostrarDialogoProveedor(null));
        
        JButton btnEditar = new JButton("Editar");
        btnEditar.setBackground(new Color(0, 120, 212));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(e -> editarProveedorSeleccionado());
        
        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setBackground(new Color(220, 0, 0));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(e -> eliminarProveedorSeleccionado());
        
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        
        // Campo de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBusqueda.setBackground(Color.WHITE);
        
        txtBusqueda = new JTextField(20);
        txtBusqueda.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar proveedor...");
        txtBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarProveedores();
            }
        });
        
        panelBusqueda.add(txtBusqueda);
        
        panelSuperior.add(panelBotones, BorderLayout.WEST);
        panelSuperior.add(panelBusqueda, BorderLayout.EAST);
        
        // Tabla de proveedores
        modeloTabla = new DefaultTableModel(new Object[]{
            "ID", "Nombre", "Contacto", "Teléfono", "Email", "Dirección"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaProveedores = new JTable(modeloTabla);
        tablaProveedores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProveedores.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(tablaProveedores);
        
        // Agregar componentes al panel principal
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void cargarDatos() {
        proveedores = new ArrayList<>();
        modeloTabla.setRowCount(0);
        
        String sql = "SELECT * FROM proveedores ORDER BY Nombre_Proveedor";
        
        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Proveedor proveedor = new Proveedor(
                    rs.getInt("ID_Proveedor"),
                    rs.getString("Nombre_Proveedor"),
                    rs.getString("Contacto"),
                    rs.getString("Telefono"),
                    rs.getString("Email"),
                    rs.getString("Direccion")
                );
                proveedores.add(proveedor);
                modeloTabla.addRow(new Object[]{
                    proveedor.getId(),
                    proveedor.getNombre(),
                    proveedor.getContacto(),
                    proveedor.getTelefono(),
                    proveedor.getEmail(),
                    proveedor.getDireccion()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar los proveedores: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarProveedores() {
        String busqueda = txtBusqueda.getText().toLowerCase();
        modeloTabla.setRowCount(0);
        
        for (Proveedor proveedor : proveedores) {
            if (proveedor.getNombre().toLowerCase().contains(busqueda) ||
                proveedor.getContacto().toLowerCase().contains(busqueda) ||
                proveedor.getEmail().toLowerCase().contains(busqueda)) {
                modeloTabla.addRow(new Object[]{
                    proveedor.getId(),
                    proveedor.getNombre(),
                    proveedor.getContacto(),
                    proveedor.getTelefono(),
                    proveedor.getEmail(),
                    proveedor.getDireccion()
                });
            }
        }
    }
    
    private void mostrarDialogoProveedor(Proveedor proveedor) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(proveedor == null ? "Nuevo Proveedor" : "Editar Proveedor");
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
        if (proveedor != null) {
            txtNombre.setText(proveedor.getNombre());
        }
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(txtNombre, gbc);
        
        JLabel lblContacto = new JLabel("Contacto:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblContacto, gbc);
        
        JTextField txtContacto = new JTextField(20);
        if (proveedor != null) {
            txtContacto.setText(proveedor.getContacto());
        }
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(txtContacto, gbc);
        
        JLabel lblTelefono = new JLabel("Teléfono:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblTelefono, gbc);
        
        JTextField txtTelefono = new JTextField(20);
        if (proveedor != null) {
            txtTelefono.setText(proveedor.getTelefono());
        }
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(txtTelefono, gbc);
        
        JLabel lblEmail = new JLabel("Email:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblEmail, gbc);
        
        JTextField txtEmail = new JTextField(20);
        if (proveedor != null) {
            txtEmail.setText(proveedor.getEmail());
        }
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(txtEmail, gbc);
        
        JLabel lblDireccion = new JLabel("Dirección:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(lblDireccion, gbc);
        
        JTextArea txtDireccion = new JTextArea(3, 20);
        txtDireccion.setLineWrap(true);
        txtDireccion.setWrapStyleWord(true);
        if (proveedor != null) {
            txtDireccion.setText(proveedor.getDireccion());
        }
        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(new JScrollPane(txtDireccion), gbc);
        
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(0, 120, 212));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String contacto = txtContacto.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String email = txtEmail.getText().trim();
            String direccion = txtDireccion.getText().trim();
            
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "El nombre no puede estar vacío",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (proveedor == null) {
                // Insertar nuevo proveedor
                String sql = "INSERT INTO proveedores (Nombre_Proveedor, Contacto, Telefono, Email, Direccion) VALUES (?, ?, ?, ?, ?)";
                try (Connection conn = ConexionDB.getConexion();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, nombre);
                    pstmt.setString(2, contacto);
                    pstmt.setString(3, telefono);
                    pstmt.setString(4, email);
                    pstmt.setString(5, direccion);
                    pstmt.executeUpdate();
                    cargarDatos();
                    dialog.dispose();
                    
                    // Notificar a los otros paneles que se ha añadido un proveedor
                    DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.PROVEEDOR_ADDED);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog,
                        "Error al guardar el proveedor: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Actualizar proveedor existente
                String sql = "UPDATE proveedores SET Nombre_Proveedor = ?, Contacto = ?, Telefono = ?, Email = ?, Direccion = ? WHERE ID_Proveedor = ?";
                try (Connection conn = ConexionDB.getConexion();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, nombre);
                    pstmt.setString(2, contacto);
                    pstmt.setString(3, telefono);
                    pstmt.setString(4, email);
                    pstmt.setString(5, direccion);
                    pstmt.setInt(6, proveedor.getId());
                    pstmt.executeUpdate();
                    cargarDatos();
                    dialog.dispose();
                    
                    // Notificar a los otros paneles que se ha actualizado un proveedor
                    DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.PROVEEDOR_UPDATED);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog,
                        "Error al actualizar el proveedor: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
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
    
    private void editarProveedorSeleccionado() {
        int fila = tablaProveedores.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un proveedor para editar",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Proveedor proveedor = proveedores.stream()
            .filter(p -> p.getId() == id)
            .findFirst()
            .orElse(null);
        
        if (proveedor != null) {
            mostrarDialogoProveedor(proveedor);
        }
    }
    
    private void eliminarProveedorSeleccionado() {
        int fila = tablaProveedores.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un proveedor para eliminar",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modeloTabla.getValueAt(fila, 0);
        String nombre = (String) modeloTabla.getValueAt(fila, 1);
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar el proveedor '" + nombre + "'?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM proveedores WHERE ID_Proveedor = ?";
            try (Connection conn = ConexionDB.getConexion();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                cargarDatos();
                
                // Notificar a los otros paneles que se ha eliminado un proveedor
                DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.PROVEEDOR_DELETED);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar el proveedor: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    public void onDataChanged(DataChangedEvent event) {
        // Actualizar los datos si ha habido cambios en los proveedores
        if (event == DataChangedEvent.PROVEEDOR_ADDED || 
            event == DataChangedEvent.PROVEEDOR_UPDATED || 
            event == DataChangedEvent.PROVEEDOR_DELETED) {
            cargarDatos();
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
    }
} 