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
import com.mycompany.panaderiacrud.util.ConexionDB;

public class CostosProduccionPanel extends JPanel {
    private JTable tablaCostos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBusqueda;
    private List<CostoProduccion> costos;
    private List<Producto> productos;
    private List<Ingrediente> ingredientes;
    private DefaultTableModel modeloTablaIngredientes;
    private JLabel lblTotalValor;
    
    public CostosProduccionPanel() {
        initComponents();
        cargarProductos();
        cargarIngredientes();
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
        
        JButton btnAgregar = new JButton("Nuevo Costo");
        btnAgregar.setBackground(new Color(0, 120, 212));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(e -> mostrarDialogoCosto(null));
        
        JButton btnVerDetalles = new JButton("Ver Detalles");
        btnVerDetalles.setBackground(new Color(0, 120, 212));
        btnVerDetalles.setForeground(Color.WHITE);
        btnVerDetalles.setFocusPainted(false);
        btnVerDetalles.addActionListener(e -> verDetallesCosto());
        
        panelBotones.add(btnAgregar);
        panelBotones.add(btnVerDetalles);
        
        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBusqueda.setBackground(Color.WHITE);
        
        txtBusqueda = new JTextField(20);
        txtBusqueda.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar costo...");
        txtBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarCostos();
            }
        });
        
        panelBusqueda.add(txtBusqueda);
        
        panelSuperior.add(panelBotones, BorderLayout.WEST);
        panelSuperior.add(panelBusqueda, BorderLayout.EAST);
        
        // Tabla de costos
        modeloTabla = new DefaultTableModel(new Object[]{
            "ID", "Producto", "Costo Total", "Fecha Actualización"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaCostos = new JTable(modeloTabla);
        tablaCostos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaCostos.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(tablaCostos);
        
        // Agregar componentes al panel principal
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void cargarProductos() {
        productos = new ArrayList<>();
        String sql = "SELECT * FROM productos ORDER BY Nom_Producto";
        
        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                productos.add(new Producto(
                    rs.getInt("ID_Producto"),
                    rs.getString("Nom_Producto"),
                    rs.getString("Descripcion"),
                    rs.getBigDecimal("Precio_Unidad"),
                    rs.getInt("Stock_Disponible"),
                    rs.getInt("ID_Categoria"),
                    ""
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar los productos: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cargarIngredientes() {
        ingredientes = new ArrayList<>();
        String sql = "SELECT * FROM ingredientes ORDER BY Nom_Ingrediente";
        
        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ingredientes.add(new Ingrediente(
                    rs.getInt("ID_Ingrediente"),
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
    
    private void cargarDatos() {
        costos = new ArrayList<>();
        modeloTabla.setRowCount(0);
        
        String sql = "SELECT cp.*, p.Nom_Producto FROM costos_produccion cp " +
                    "LEFT JOIN productos p ON cp.ID_Producto = p.ID_Producto " +
                    "ORDER BY cp.Fecha_Calculo DESC";
        
        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                CostoProduccion costo = new CostoProduccion(
                    rs.getInt("ID_CostoProduccion"),
                    rs.getInt("ID_Producto"),
                    rs.getString("Nom_Producto"),
                    rs.getBigDecimal("Costo_Total"),
                    rs.getDate("Fecha_Calculo")
                );
                costos.add(costo);
                modeloTabla.addRow(new Object[]{
                    costo.getId(),
                    costo.getNombreProducto(),
                    costo.getCostoTotal(),
                    costo.getFechaActualizacion()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar los costos: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarCostos() {
        String busqueda = txtBusqueda.getText().toLowerCase();
        modeloTabla.setRowCount(0);
        
        for (CostoProduccion costo : costos) {
            if (costo.getNombreProducto().toLowerCase().contains(busqueda)) {
                modeloTabla.addRow(new Object[]{
                    costo.getId(),
                    costo.getNombreProducto(),
                    costo.getCostoTotal(),
                    costo.getFechaActualizacion()
                });
            }
        }
    }
    
    private void mostrarDialogoCosto(CostoProduccion costo) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(costo == null ? "Nuevo Costo" : "Editar Costo");
        dialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Campos del formulario
        JLabel lblProducto = new JLabel("Producto:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblProducto, gbc);
        
        JComboBox<Producto> cmbProducto = new JComboBox<>();
        for (Producto p : productos) {
            cmbProducto.addItem(p);
        }
        if (costo != null) {
            for (int i = 0; i < cmbProducto.getItemCount(); i++) {
                if (cmbProducto.getItemAt(i).getId() == costo.getIdProducto()) {
                    cmbProducto.setSelectedIndex(i);
                    break;
                }
            }
        }
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(cmbProducto, gbc);
        
        // Tabla de ingredientes
        JLabel lblIngredientes = new JLabel("Ingredientes:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblIngredientes, gbc);
        
        DefaultTableModel modeloTablaIngredientes = new DefaultTableModel(new Object[]{
            "Ingrediente", "Cantidad", "Unidad", "Costo Unitario", "Subtotal"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 2 && column != 3 && column != 4;
            }
        };
        
        JTable tablaIngredientes = new JTable(modeloTablaIngredientes);
        tablaIngredientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPaneIngredientes = new JScrollPane(tablaIngredientes);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        panel.add(scrollPaneIngredientes, gbc);
        
        // Total
        JLabel lblTotal = new JLabel("Costo Total:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblTotal, gbc);
        
        lblTotalValor = new JLabel("$0.00");
        lblTotalValor.setFont(lblTotalValor.getFont().deriveFont(Font.BOLD));
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(lblTotalValor, gbc);
        
        // Cargar ingredientes existentes si se está editando
        if (costo != null) {
            String sql = "SELECT i.*, r.Cantidad FROM ingredientes i " +
                        "JOIN recetas r ON i.ID_Ingrediente = r.ID_Ingrediente " +
                        "WHERE r.ID_Producto = ?";
            try (Connection conn = ConexionDB.getConexion();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, costo.getIdProducto());
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    modeloTablaIngredientes.addRow(new Object[]{
                        new Ingrediente(
                            rs.getInt("ID_Ingrediente"),
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
                        rs.getBigDecimal("Costo_Unidad"),
                        rs.getBigDecimal("Costo_Unidad").multiply(new BigDecimal(rs.getInt("Cantidad")))
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
        
        // Agregar listener para actualizar el subtotal cuando cambia la cantidad o el ingrediente
        modeloTablaIngredientes.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int fila = e.getFirstRow();
                int columna = e.getColumn();
                
                if (columna == 0 || columna == 1) {
                    Ingrediente ingrediente = (Ingrediente) modeloTablaIngredientes.getValueAt(fila, 0);
                    int cantidad = 1;
                    
                    try {
                        cantidad = Integer.parseInt(modeloTablaIngredientes.getValueAt(fila, 1).toString());
                    } catch (NumberFormatException ex) {
                        cantidad = 1;
                        modeloTablaIngredientes.setValueAt(1, fila, 1);
                    }
                    
                    if (ingrediente != null) {
                        modeloTablaIngredientes.setValueAt(ingrediente.getUnidad(), fila, 2);
                        modeloTablaIngredientes.setValueAt(ingrediente.getCosto(), fila, 3);
                        modeloTablaIngredientes.setValueAt(
                            ingrediente.getCosto().multiply(new BigDecimal(cantidad)),
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
                total = total.add((BigDecimal) modeloTablaIngredientes.getValueAt(i, 4));
            }
            lblTotalValor.setText("$" + total.toString());
        };
        
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(0, 120, 212));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> {
            try {
                Producto producto = (Producto) cmbProducto.getSelectedItem();
                
                if (producto == null) {
                    JOptionPane.showMessageDialog(dialog,
                        "Debe seleccionar un producto",
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
                
                if (costo == null) {
                    // Insertar nuevo costo
                    String sql = "INSERT INTO costos_produccion (ID_Producto, Costo_Total, Fecha_Calculo) VALUES (?, ?, ?)";
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, producto.getId());
                        pstmt.setBigDecimal(2, total);
                        pstmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                        pstmt.executeUpdate();
                        
                        cargarDatos();
                        dialog.dispose();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al guardar el costo: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // Actualizar costo existente
                    String sql = "UPDATE costos_produccion SET Costo_Total = ?, Fecha_Calculo = ? WHERE ID_CostoProduccion = ?";
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setBigDecimal(1, total);
                        pstmt.setDate(2, new java.sql.Date(System.currentTimeMillis()));
                        pstmt.setInt(3, costo.getId());
                        pstmt.executeUpdate();
                        
                        cargarDatos();
                        dialog.dispose();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al actualizar el costo: " + ex.getMessage(),
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
    
    private void verDetallesCosto() {
        int fila = tablaCostos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un costo para ver sus detalles",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modeloTabla.getValueAt(fila, 0);
        CostoProduccion costo = costos.stream()
            .filter(c -> c.getId() == id)
            .findFirst()
            .orElse(null);
        
        if (costo != null) {
            mostrarDialogoCosto(costo);
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
    
    private static class CostoProduccion {
        private final int id;
        private final int idProducto;
        private final String nombreProducto;
        private final BigDecimal costoTotal;
        private final java.sql.Date fechaActualizacion;
        
        public CostoProduccion(int id, int idProducto, String nombreProducto, 
                              BigDecimal costoTotal, java.sql.Date fechaActualizacion) {
            this.id = id;
            this.idProducto = idProducto;
            this.nombreProducto = nombreProducto;
            this.costoTotal = costoTotal;
            this.fechaActualizacion = fechaActualizacion;
        }
        
        public int getId() {
            return id;
        }
        
        public int getIdProducto() {
            return idProducto;
        }
        
        public String getNombreProducto() {
            return nombreProducto;
        }
        
        public BigDecimal getCostoTotal() {
            return costoTotal;
        }
        
        public java.sql.Date getFechaActualizacion() {
            return fechaActualizacion;
        }
    }
    
    private static class Producto {
        private final int id;
        private final String nombre;
        private final String descripcion;
        private final BigDecimal precio;
        private final int stock;
        private final int idCategoria;
        private final String nombreCategoria;
        
        public Producto(int id, String nombre, String descripcion, BigDecimal precio,
                       int stock, int idCategoria, String nombreCategoria) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.precio = precio;
            this.stock = stock;
            this.idCategoria = idCategoria;
            this.nombreCategoria = nombreCategoria;
        }
        
        public int getId() {
            return id;
        }
        
        public String getNombre() {
            return nombre;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
        
        public BigDecimal getPrecio() {
            return precio;
        }
        
        public int getStock() {
            return stock;
        }
        
        public int getIdCategoria() {
            return idCategoria;
        }
        
        public String getNombreCategoria() {
            return nombreCategoria;
        }
        
        @Override
        public String toString() {
            return nombre;
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
} 