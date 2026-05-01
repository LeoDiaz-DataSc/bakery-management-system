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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import com.toedter.calendar.JDateChooser;
import com.mycompany.panaderiacrud.util.ConexionDB;
import com.mycompany.panaderiacrud.util.DataChangedEvent;
import com.mycompany.panaderiacrud.util.DataChangeListener;
import com.mycompany.panaderiacrud.util.DataChangeNotifier;

public class ComprasPanel extends JPanel implements DataChangeListener {
    private JTable tablaCompras;
    private DefaultTableModel modeloTabla;
    private JTextField txtBusqueda;
    private List<Compra> compras;
    private List<Ingrediente> ingredientes;
    private List<Proveedor> proveedores;
    private JDateChooser dateChooser;
    private DefaultTableModel modeloTablaIngredientes;
    private JLabel lblTotalValor;
    
    public ComprasPanel() {
        initComponents();
        cargarProveedores();
        cargarIngredientes();
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
        
        JButton btnAgregar = new JButton("Nueva Compra");
        btnAgregar.setBackground(new Color(0, 120, 212));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(e -> mostrarDialogoCompra(null));
        
        JButton btnVerDetalles = new JButton("Ver Detalles");
        btnVerDetalles.setBackground(new Color(0, 120, 212));
        btnVerDetalles.setForeground(Color.WHITE);
        btnVerDetalles.setFocusPainted(false);
        btnVerDetalles.addActionListener(e -> verDetallesCompra());
        
        panelBotones.add(btnAgregar);
        panelBotones.add(btnVerDetalles);
        
        // Panel de búsqueda y filtros
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBusqueda.setBackground(Color.WHITE);
        
        // Filtro de fecha
        JLabel lblFecha = new JLabel("Fecha:");
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.addPropertyChangeListener("date", e -> filtrarCompras());
        
        // Campo de búsqueda
        txtBusqueda = new JTextField(20);
        txtBusqueda.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar compra...");
        txtBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarCompras();
            }
        });
        
        panelBusqueda.add(lblFecha);
        panelBusqueda.add(dateChooser);
        panelBusqueda.add(txtBusqueda);
        
        panelSuperior.add(panelBotones, BorderLayout.WEST);
        panelSuperior.add(panelBusqueda, BorderLayout.EAST);
        
        // Tabla de compras
        modeloTabla = new DefaultTableModel(new Object[]{
            "ID", "Fecha", "Proveedor", "Total"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaCompras = new JTable(modeloTabla);
        tablaCompras.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaCompras.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(tablaCompras);
        
        // Agregar componentes al panel principal
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void cargarIngredientes() {
        ingredientes = new ArrayList<>();
        String sql = "SELECT * FROM ingredientes ORDER BY Nom_Ingrediente";
        
        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ingredientes.add(new Ingrediente(
                    rs.getInt("ID_INGREDIENTE"),
                    rs.getString("Nom_Ingrediente"),
                    rs.getString("Unidad_Med"),
                    rs.getBigDecimal("Costo_Unidad"),
                    rs.getInt("Stock_Actual"),
                    rs.getInt("Stock_Minimo"),
                    rs.getInt("ID_Proveedor"),
                    ""
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar los ingredientes: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
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
        compras = new ArrayList<>();
        modeloTabla.setRowCount(0);
        
        String sql = "SELECT c.*, p.Nombre_Proveedor FROM compras c " +
                    "LEFT JOIN proveedores p ON c.ID_Proveedor = p.ID_Proveedor " +
                    "ORDER BY c.Fecha_Compra DESC";
        
        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Compra compra = new Compra(
                    rs.getInt("ID_Compra"),
                    rs.getDate("Fecha_Compra"),
                    rs.getInt("ID_Proveedor"),
                    rs.getString("Nombre_Proveedor"),
                    rs.getBigDecimal("Total")
                );
                compras.add(compra);
                modeloTabla.addRow(new Object[]{
                    compra.getId(),
                    compra.getFecha(),
                    compra.getNombreProveedor(),
                    compra.getTotal()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar las compras: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarCompras() {
        String busqueda = txtBusqueda.getText().toLowerCase();
        Date fechaSeleccionada = dateChooser.getDate();
        modeloTabla.setRowCount(0);
        
        for (Compra compra : compras) {
            boolean coincideFecha = fechaSeleccionada == null || 
                compra.getFecha().equals(new java.sql.Date(fechaSeleccionada.getTime()));
            boolean coincideBusqueda = compra.getNombreProveedor().toLowerCase().contains(busqueda);
            
            if (coincideFecha && coincideBusqueda) {
                modeloTabla.addRow(new Object[]{
                    compra.getId(),
                    compra.getFecha(),
                    compra.getNombreProveedor(),
                    compra.getTotal()
                });
            }
        }
    }
    
    private void mostrarDialogoCompra(Compra compra) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(compra == null ? "Nueva Compra" : "Editar Compra");
        dialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Campos del formulario
        JLabel lblFecha = new JLabel("Fecha:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblFecha, gbc);
        
        JDateChooser dateChooserCompra = new JDateChooser();
        dateChooserCompra.setDateFormatString("dd/MM/yyyy");
        if (compra != null) {
            dateChooserCompra.setDate(compra.getFecha());
        } else {
            dateChooserCompra.setDate(new Date());
        }
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(dateChooserCompra, gbc);
        
        JLabel lblProveedor = new JLabel("Proveedor:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblProveedor, gbc);
        
        JComboBox<Proveedor> cmbProveedor = new JComboBox<>();
        for (Proveedor p : proveedores) {
            cmbProveedor.addItem(p);
        }
        if (compra != null) {
            for (int i = 0; i < cmbProveedor.getItemCount(); i++) {
                if (cmbProveedor.getItemAt(i).getId() == compra.getIdProveedor()) {
                    cmbProveedor.setSelectedIndex(i);
                    break;
                }
            }
        }
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(cmbProveedor, gbc);
        
        // Tabla de ingredientes
        JLabel lblIngredientes = new JLabel("Ingredientes:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblIngredientes, gbc);
        
        modeloTablaIngredientes = new DefaultTableModel(new Object[]{
            "Ingrediente", "Cantidad", "Unidad", "Precio Unitario", "Subtotal"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 2 && column != 4;
            }
        };
        
        JTable tablaIngredientes = new JTable(modeloTablaIngredientes);
        tablaIngredientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPaneIngredientes = new JScrollPane(tablaIngredientes);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridheight = 2;
        panel.add(scrollPaneIngredientes, gbc);
        
        // Botones para la tabla de ingredientes
        JPanel panelBotonesIngredientes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnAgregarIngrediente = new JButton("Agregar Ingrediente");
        btnAgregarIngrediente.addActionListener(e -> {
            modeloTablaIngredientes.addRow(new Object[]{null, 1, "", java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO});
        });
        
        JButton btnEliminarIngrediente = new JButton("Eliminar Ingrediente");
        btnEliminarIngrediente.addActionListener(e -> {
            int fila = tablaIngredientes.getSelectedRow();
            if (fila != -1) {
                modeloTablaIngredientes.removeRow(fila);
                actualizarTotal();
            }
        });
        
        panelBotonesIngredientes.add(btnAgregarIngrediente);
        panelBotonesIngredientes.add(btnEliminarIngrediente);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridheight = 1;
        panel.add(panelBotonesIngredientes, gbc);
        
        // Total
        JLabel lblTotal = new JLabel("Total:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(lblTotal, gbc);
        
        lblTotalValor = new JLabel("$0.00");
        lblTotalValor.setFont(lblTotalValor.getFont().deriveFont(Font.BOLD));
        gbc.gridx = 1;
        gbc.gridy = 5;
        panel.add(lblTotalValor, gbc);
        
        // Cargar ingredientes existentes si se está editando
        if (compra != null) {
            String sql = "SELECT i.*, dc.Cantidad, dc.Precio_Unitario FROM ingredientes i " +
                        "JOIN detalles_compra dc ON i.ID_INGREDIENTE = dc.ID_Ingrediente " +
                        "WHERE dc.ID_Compra = ?";
            try (Connection conn = ConexionDB.getConexion();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, compra.getId());
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    modeloTablaIngredientes.addRow(new Object[]{
                        new Ingrediente(
                            rs.getInt("ID_INGREDIENTE"),
                            rs.getString("Nom_Ingrediente"),
                            rs.getString("Unidad_Med"),
                            rs.getBigDecimal("Costo_Unidad"),
                            rs.getInt("Stock_Actual"),
                            rs.getInt("Stock_Minimo"),
                            rs.getInt("ID_Proveedor"),
                            ""
                        ),
                        rs.getInt("Cantidad"),
                        rs.getString("Unidad_Med"),
                        rs.getBigDecimal("Precio_Unitario"),
                        rs.getBigDecimal("Precio_Unitario").multiply(new BigDecimal(rs.getInt("Cantidad")))
                    });
                }
                actualizarTotal();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(dialog,
                    "Error al cargar los ingredientes: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        // Configurar el editor de la columna de ingredientes
        tablaIngredientes.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JComboBox<Ingrediente>() {
            {
                for (Ingrediente i : ingredientes) {
                    addItem(i);
                }
            }
        }));
        
        // Configurar el editor de la columna de cantidad
        tablaIngredientes.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()));
        
        // Configurar el editor de la columna de precio unitario
        tablaIngredientes.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JTextField()));
        
        // Agregar listener para actualizar el subtotal cuando cambia la cantidad o el precio
        modeloTablaIngredientes.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int fila = e.getFirstRow();
                int columna = e.getColumn();
                if (columna == 0 || columna == 1 || columna == 3) {
                    Ingrediente ingrediente = (Ingrediente) modeloTablaIngredientes.getValueAt(fila, 0);
                    int cantidad = 1;
                    BigDecimal precioUnitario = BigDecimal.ZERO;
                    try {
                        cantidad = Integer.parseInt(modeloTablaIngredientes.getValueAt(fila, 1).toString());
                    } catch (NumberFormatException ex) {
                        cantidad = 1;
                        modeloTablaIngredientes.setValueAt(1, fila, 1);
                    }
                    Object valorPrecio = modeloTablaIngredientes.getValueAt(fila, 3);
                    if (valorPrecio instanceof BigDecimal) {
                        precioUnitario = (BigDecimal) valorPrecio;
                    } else {
                        try {
                            precioUnitario = new BigDecimal(valorPrecio.toString());
                            modeloTablaIngredientes.setValueAt(precioUnitario, fila, 3);
                        } catch (Exception ex) {
                            precioUnitario = ingrediente != null ? ingrediente.getCosto() : BigDecimal.ZERO;
                            modeloTablaIngredientes.setValueAt(precioUnitario, fila, 3);
                        }
                    }
                    if (ingrediente != null) {
                        modeloTablaIngredientes.setValueAt(ingrediente.getUnidad(), fila, 2);
                        modeloTablaIngredientes.setValueAt(
                            precioUnitario.multiply(new BigDecimal(cantidad)),
                            fila, 4);
                        actualizarTotal();
                    }
                }
            }
        });
        
        // Función para actualizar el total
        Runnable actualizarTotal = () -> {
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < modeloTablaIngredientes.getRowCount(); i++) {
                BigDecimal subtotal = (BigDecimal) modeloTablaIngredientes.getValueAt(i, 4);
                total = total.add(subtotal);
            }
            lblTotalValor.setText("$" + total.toString());
        };
        
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(0, 120, 212));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> {
            try {
                Date fecha = dateChooserCompra.getDate();
                Proveedor proveedor = (Proveedor) cmbProveedor.getSelectedItem();
                
                if (fecha == null) {
                    JOptionPane.showMessageDialog(dialog,
                        "Debe seleccionar una fecha",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (proveedor == null) {
                    JOptionPane.showMessageDialog(dialog,
                        "Debe seleccionar un proveedor",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (modeloTablaIngredientes.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(dialog,
                        "Debe agregar al menos un ingrediente",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Calcular el total
                BigDecimal total = BigDecimal.ZERO;
                for (int i = 0; i < modeloTablaIngredientes.getRowCount(); i++) {
                    total = total.add((BigDecimal) modeloTablaIngredientes.getValueAt(i, 4));
                }
                
                if (compra == null) {
                    // Insertar nueva compra
                    String sql = "INSERT INTO compras (Fecha_Compra, ID_Proveedor, Total) VALUES (?, ?, ?)";
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setDate(1, new java.sql.Date(fecha.getTime()));
                        pstmt.setInt(2, proveedor.getId());
                        pstmt.setBigDecimal(3, total);
                        pstmt.executeUpdate();
                        
                        // Obtener el ID de la compra insertada
                        ResultSet rs = pstmt.getGeneratedKeys();
                        int idCompra = 0;
                        if (rs.next()) {
                            idCompra = rs.getInt(1);
                        }
                        
                        // Insertar los detalles de la compra
                        sql = "INSERT INTO detalles_compra (ID_Compra, ID_Ingrediente, Cantidad, Precio_Unitario, Subtotal) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement pstmtDetalles = conn.prepareStatement(sql)) {
                            for (int i = 0; i < modeloTablaIngredientes.getRowCount(); i++) {
                                Ingrediente ingrediente = (Ingrediente) modeloTablaIngredientes.getValueAt(i, 0);
                                int cantidad = Integer.parseInt(modeloTablaIngredientes.getValueAt(i, 1).toString());
                                BigDecimal precioUnitario = (BigDecimal) modeloTablaIngredientes.getValueAt(i, 3);
                                BigDecimal subtotal = (BigDecimal) modeloTablaIngredientes.getValueAt(i, 4);
                                pstmtDetalles.setInt(1, idCompra);
                                pstmtDetalles.setInt(2, ingrediente.getId());
                                pstmtDetalles.setInt(3, cantidad);
                                pstmtDetalles.setBigDecimal(4, precioUnitario);
                                pstmtDetalles.setBigDecimal(5, subtotal);
                                pstmtDetalles.addBatch();
                            }
                            pstmtDetalles.executeBatch();
                        }
                        
                        // Notificar a otros paneles sobre la nueva compra
                        DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.INGREDIENTE_UPDATED);
                        
                        cargarDatos();
                        dialog.dispose();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al guardar la compra: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // Actualizar compra existente
                    String sql = "UPDATE compras SET Fecha_Compra = ?, ID_Proveedor = ?, Total = ? WHERE ID_Compra = ?";
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setDate(1, new java.sql.Date(fecha.getTime()));
                        pstmt.setInt(2, proveedor.getId());
                        pstmt.setBigDecimal(3, total);
                        pstmt.setInt(4, compra.getId());
                        pstmt.executeUpdate();
                        
                        // Eliminar los detalles existentes
                        sql = "DELETE FROM detalles_compra WHERE ID_Compra = ?";
                        try (PreparedStatement pstmtEliminar = conn.prepareStatement(sql)) {
                            pstmtEliminar.setInt(1, compra.getId());
                            pstmtEliminar.executeUpdate();
                        }
                        
                        // Insertar los nuevos detalles
                        sql = "INSERT INTO detalles_compra (ID_Compra, ID_Ingrediente, Cantidad, Precio_Unitario, Subtotal) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement pstmtDetalles = conn.prepareStatement(sql)) {
                            for (int i = 0; i < modeloTablaIngredientes.getRowCount(); i++) {
                                Ingrediente ingrediente = (Ingrediente) modeloTablaIngredientes.getValueAt(i, 0);
                                int cantidad = Integer.parseInt(modeloTablaIngredientes.getValueAt(i, 1).toString());
                                BigDecimal precioUnitario = (BigDecimal) modeloTablaIngredientes.getValueAt(i, 3);
                                BigDecimal subtotal = (BigDecimal) modeloTablaIngredientes.getValueAt(i, 4);
                                pstmtDetalles.setInt(1, compra.getId());
                                pstmtDetalles.setInt(2, ingrediente.getId());
                                pstmtDetalles.setInt(3, cantidad);
                                pstmtDetalles.setBigDecimal(4, precioUnitario);
                                pstmtDetalles.setBigDecimal(5, subtotal);
                                pstmtDetalles.addBatch();
                            }
                            pstmtDetalles.executeBatch();
                        }
                        
                        // Notificar a otros paneles sobre la eliminación de una compra
                        DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.INGREDIENTE_UPDATED);
                        
                        cargarDatos();
                        dialog.dispose();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al actualizar la compra: " + ex.getMessage(),
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
    
    private void verDetallesCompra() {
        int fila = tablaCompras.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione una compra para ver sus detalles",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Compra compra = compras.stream()
            .filter(c -> c.getId() == id)
            .findFirst()
            .orElse(null);
        
        if (compra != null) {
            mostrarDialogoCompra(compra);
        }
    }
    
    @Override
    public void onDataChanged(DataChangedEvent event) {
        // Actualizar los ingredientes si ha habido cambios en ellos
        if (event == DataChangedEvent.INGREDIENTE_ADDED || 
            event == DataChangedEvent.INGREDIENTE_UPDATED || 
            event == DataChangedEvent.INGREDIENTE_DELETED) {
            cargarIngredientes();
        }
        
        // Actualizar los proveedores si ha habido cambios en ellos
        if (event == DataChangedEvent.PROVEEDOR_ADDED || 
            event == DataChangedEvent.PROVEEDOR_UPDATED || 
            event == DataChangedEvent.PROVEEDOR_DELETED) {
            cargarProveedores();
        }
    }
    
    private void actualizarTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < modeloTablaIngredientes.getRowCount(); i++) {
            BigDecimal subtotal = (BigDecimal) modeloTablaIngredientes.getValueAt(i, 4);
            total = total.add(subtotal);
        }
        lblTotalValor.setText("$" + total.toString());
    }
    
    private static class Compra {
        private final int id;
        private final java.sql.Date fecha;
        private final int idProveedor;
        private final String nombreProveedor;
        private final BigDecimal total;
        
        public Compra(int id, java.sql.Date fecha, int idProveedor, String nombreProveedor, 
                     BigDecimal total) {
            this.id = id;
            this.fecha = fecha;
            this.idProveedor = idProveedor;
            this.nombreProveedor = nombreProveedor;
            this.total = total;
        }
        
        public int getId() {
            return id;
        }
        
        public java.sql.Date getFecha() {
            return fecha;
        }
        
        public int getIdProveedor() {
            return idProveedor;
        }
        
        public String getNombreProveedor() {
            return nombreProveedor;
        }
        
        public BigDecimal getTotal() {
            return total;
        }
    }
    
    private static class Ingrediente {
        private final int id;
        private final String nombre;
        private final String unidad;
        private final BigDecimal costo;
        private final int stockActual;
        private final int stockMinimo;
        private final int idProveedor;
        private final String nombreProveedor;
        
        public Ingrediente(int id, String nombre, String unidad, BigDecimal costo,
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
        
        public BigDecimal getCosto() {
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
        
        @Override
        public String toString() {
            return nombre;
        }
    }
    
    private static class Proveedor {
        private final int id;
        private final String nombre;
        private final String contacto;
        private final String telefono;
        private final String email;
        private final String direccion;
        
        public Proveedor(int id, String nombre, String contacto, String telefono, 
                        String email, String direccion) {
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