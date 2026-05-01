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

public class VentasPanel extends JPanel implements DataChangeListener {
    private JTable tablaVentas;
    private DefaultTableModel modeloTabla;
    private JTextField txtBusqueda;
    private List<Venta> ventas;
    private List<Producto> productos;
    private JDateChooser dateChooser;
    private DefaultTableModel modeloTablaProductos;
    private JLabel lblTotalValor;
    
    public VentasPanel() {
        initComponents();
        cargarProductos();
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
        
        JButton btnAgregar = new JButton("Nueva Venta");
        btnAgregar.setBackground(new Color(0, 120, 212));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(e -> mostrarDialogoVenta(null));
        
        JButton btnVerDetalles = new JButton("Ver Detalles");
        btnVerDetalles.setBackground(new Color(0, 120, 212));
        btnVerDetalles.setForeground(Color.WHITE);
        btnVerDetalles.setFocusPainted(false);
        btnVerDetalles.addActionListener(e -> verDetallesVenta());
        
        panelBotones.add(btnAgregar);
        panelBotones.add(btnVerDetalles);
        
        // Panel de búsqueda y filtros
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBusqueda.setBackground(Color.WHITE);
        
        // Filtro de fecha
        JLabel lblFecha = new JLabel("Fecha:");
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.addPropertyChangeListener("date", e -> filtrarVentas());
        
        // Campo de búsqueda
        txtBusqueda = new JTextField(20);
        txtBusqueda.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar venta...");
        txtBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarVentas();
            }
        });
        
        panelBusqueda.add(lblFecha);
        panelBusqueda.add(dateChooser);
        panelBusqueda.add(txtBusqueda);
        
        panelSuperior.add(panelBotones, BorderLayout.WEST);
        panelSuperior.add(panelBusqueda, BorderLayout.EAST);
        
        // Tabla de ventas
        modeloTabla = new DefaultTableModel(new Object[]{
            "ID", "Fecha", "Cliente", "Total"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaVentas = new JTable(modeloTabla);
        tablaVentas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaVentas.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(tablaVentas);
        
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
                    rs.getInt("ID_PRODUCTO"),
                    rs.getString("Nom_Producto"),
                    rs.getString("descripcion"),
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
    
    private void cargarDatos() {
        ventas = new ArrayList<>();
        modeloTabla.setRowCount(0);
        
        String sql = "SELECT * FROM ventas ORDER BY Fecha_Venta DESC";
        
        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Venta venta = new Venta(
                    rs.getInt("ID_Venta"),
                    rs.getDate("Fecha_Venta"),
                    rs.getString("Cliente"),
                    rs.getBigDecimal("Total")
                );
                ventas.add(venta);
                modeloTabla.addRow(new Object[]{
                    venta.getId(),
                    venta.getFecha(),
                    venta.getCliente(),
                    venta.getTotal()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar las ventas: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarVentas() {
        String busqueda = txtBusqueda.getText().toLowerCase();
        Date fechaSeleccionada = dateChooser.getDate();
        modeloTabla.setRowCount(0);
        
        for (Venta venta : ventas) {
            boolean coincideFecha = fechaSeleccionada == null || 
                venta.getFecha().equals(new java.sql.Date(fechaSeleccionada.getTime()));
            boolean coincideBusqueda = venta.getCliente().toLowerCase().contains(busqueda);
            
            if (coincideFecha && coincideBusqueda) {
                modeloTabla.addRow(new Object[]{
                    venta.getId(),
                    venta.getFecha(),
                    venta.getCliente(),
                    venta.getTotal()
                });
            }
        }
    }
    
    private void mostrarDialogoVenta(Venta venta) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(venta == null ? "Nueva Venta" : "Editar Venta");
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
        
        JDateChooser dateChooserVenta = new JDateChooser();
        dateChooserVenta.setDateFormatString("dd/MM/yyyy");
        if (venta != null) {
            dateChooserVenta.setDate(venta.getFecha());
        } else {
            dateChooserVenta.setDate(new Date());
        }
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(dateChooserVenta, gbc);
        
        JLabel lblCliente = new JLabel("Cliente:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblCliente, gbc);
        
        JTextField txtCliente = new JTextField(20);
        if (venta != null) {
            txtCliente.setText(venta.getCliente());
        }
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(txtCliente, gbc);
        
        // Tabla de productos
        JLabel lblProductos = new JLabel("Productos:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblProductos, gbc);
        
        modeloTablaProductos = new DefaultTableModel(new Object[]{
            "Producto", "Cantidad", "Precio Unitario", "Subtotal"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 2 && column != 3;
            }
        };
        
        JTable tablaProductos = new JTable(modeloTablaProductos);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPaneProductos = new JScrollPane(tablaProductos);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridheight = 2;
        panel.add(scrollPaneProductos, gbc);
        
        // Botones para la tabla de productos
        JPanel panelBotonesProductos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnAgregarProducto = new JButton("Agregar Producto");
        btnAgregarProducto.addActionListener(e -> {
            modeloTablaProductos.addRow(new Object[]{null, 1, BigDecimal.ZERO, BigDecimal.ZERO});
        });
        
        JButton btnEliminarProducto = new JButton("Eliminar Producto");
        btnEliminarProducto.addActionListener(e -> {
            int fila = tablaProductos.getSelectedRow();
            if (fila != -1) {
                modeloTablaProductos.removeRow(fila);
                actualizarTotal();
            }
        });
        
        panelBotonesProductos.add(btnAgregarProducto);
        panelBotonesProductos.add(btnEliminarProducto);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridheight = 1;
        panel.add(panelBotonesProductos, gbc);
        
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
        
        // Cargar productos existentes si se está editando
        if (venta != null) {
            String sql = "SELECT p.*, dv.Cantidad FROM productos p " +
                        "JOIN detalles_venta dv ON p.ID_Producto = dv.ID_Producto " +
                        "WHERE dv.ID_Venta = ?";
            try (Connection conn = ConexionDB.getConexion();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, venta.getId());
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    modeloTablaProductos.addRow(new Object[]{
                        new Producto(
                            rs.getInt("ID_Producto"),
                            rs.getString("Nom_Producto"),
                            rs.getString("descripcion"),
                            rs.getBigDecimal("Precio_Unidad"),
                            rs.getInt("Stock_Disponible"),
                            rs.getInt("ID_Categoria"),
                            ""
                        ),
                        rs.getInt("Cantidad"),
                        rs.getBigDecimal("Precio_Unidad"),
                        rs.getBigDecimal("Precio_Unidad").multiply(new BigDecimal(rs.getInt("Cantidad")))
                    });
                }
                actualizarTotal();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(dialog,
                    "Error al cargar los productos: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        // Configurar el editor de la columna de productos
        tablaProductos.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JComboBox<Producto>() {
            {
                for (Producto p : productos) {
                    addItem(p);
                }
            }
        }));
        
        // Configurar el editor de la columna de cantidad
        tablaProductos.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()));
        
        // Agregar listener para actualizar el subtotal cuando cambia la cantidad o el producto
        modeloTablaProductos.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int fila = e.getFirstRow();
                int columna = e.getColumn();
                
                if (columna == 0 || columna == 1) {
                    Producto producto = (Producto) modeloTablaProductos.getValueAt(fila, 0);
                    int cantidad = 1;
                    try {
                        cantidad = Integer.parseInt(modeloTablaProductos.getValueAt(fila, 1).toString());
                    } catch (NumberFormatException ex) {
                        cantidad = 1;
                        modeloTablaProductos.setValueAt(1, fila, 1);
                    }
                    
                    if (producto != null) {
                        modeloTablaProductos.setValueAt(producto.getPrecio(), fila, 2);
                        modeloTablaProductos.setValueAt(
                            producto.getPrecio().multiply(new BigDecimal(cantidad)),
                            fila, 3);
                        actualizarTotal();
                    }
                }
            }
        });
        
        // Función para actualizar el total
        Runnable actualizarTotal = () -> {
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < modeloTablaProductos.getRowCount(); i++) {
                total = total.add((BigDecimal) modeloTablaProductos.getValueAt(i, 3));
            }
            lblTotalValor.setText("$" + total.toString());
        };
        
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(0, 120, 212));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> {
            try {
                Date fecha = dateChooserVenta.getDate();
                String cliente = txtCliente.getText().trim();
                
                if (fecha == null) {
                    JOptionPane.showMessageDialog(dialog,
                        "Debe seleccionar una fecha",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (cliente.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "El nombre del cliente no puede estar vacío",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (modeloTablaProductos.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(dialog,
                        "Debe agregar al menos un producto",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Calcular el total
                BigDecimal total = BigDecimal.ZERO;
                for (int i = 0; i < modeloTablaProductos.getRowCount(); i++) {
                    total = total.add((BigDecimal) modeloTablaProductos.getValueAt(i, 3));
                }
                
                if (venta == null) {
                    // Insertar nueva venta
                    String sql = "INSERT INTO ventas (Fecha_Venta, Cliente, Total) VALUES (?, ?, ?)";
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setDate(1, new java.sql.Date(fecha.getTime()));
                        pstmt.setString(2, cliente);
                        pstmt.setBigDecimal(3, total);
                        pstmt.executeUpdate();
                        
                        // Obtener el ID de la venta insertada
                        ResultSet rs = pstmt.getGeneratedKeys();
                        int idVenta = 0;
                        if (rs.next()) {
                            idVenta = rs.getInt(1);
                        }
                        
                        // Insertar los detalles de la venta
                        sql = "INSERT INTO detalles_venta (ID_Venta, ID_Producto, Cantidad, Precio_Unitario, Subtotal) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement pstmtDetalles = conn.prepareStatement(sql)) {
                            for (int i = 0; i < modeloTablaProductos.getRowCount(); i++) {
                                Producto producto = (Producto) modeloTablaProductos.getValueAt(i, 0);
                                int cantidad = Integer.parseInt(modeloTablaProductos.getValueAt(i, 1).toString());
                                BigDecimal precioUnitario = (BigDecimal) modeloTablaProductos.getValueAt(i, 2);
                                BigDecimal subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
                                pstmtDetalles.setInt(1, idVenta);
                                pstmtDetalles.setInt(2, producto.getId());
                                pstmtDetalles.setInt(3, cantidad);
                                pstmtDetalles.setBigDecimal(4, precioUnitario);
                                pstmtDetalles.setBigDecimal(5, subtotal);
                                pstmtDetalles.addBatch();
                            }
                            pstmtDetalles.executeBatch();
                        }
                        
                        cargarDatos();
                        dialog.dispose();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al guardar la venta: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // Actualizar venta existente
                    String sql = "UPDATE ventas SET Fecha_Venta = ?, Cliente = ?, Total = ? WHERE ID_Venta = ?";
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setDate(1, new java.sql.Date(fecha.getTime()));
                        pstmt.setString(2, cliente);
                        pstmt.setBigDecimal(3, total);
                        pstmt.setInt(4, venta.getId());
                        pstmt.executeUpdate();
                        
                        // Eliminar los detalles existentes
                        sql = "DELETE FROM detalles_venta WHERE ID_Venta = ?";
                        try (PreparedStatement pstmtEliminar = conn.prepareStatement(sql)) {
                            pstmtEliminar.setInt(1, venta.getId());
                            pstmtEliminar.executeUpdate();
                        }
                        
                        // Insertar los nuevos detalles
                        sql = "INSERT INTO detalles_venta (ID_Venta, ID_Producto, Cantidad, Precio_Unitario, Subtotal) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement pstmtDetalles = conn.prepareStatement(sql)) {
                            for (int i = 0; i < modeloTablaProductos.getRowCount(); i++) {
                                Producto producto = (Producto) modeloTablaProductos.getValueAt(i, 0);
                                int cantidad = Integer.parseInt(modeloTablaProductos.getValueAt(i, 1).toString());
                                BigDecimal precioUnitario = (BigDecimal) modeloTablaProductos.getValueAt(i, 2);
                                BigDecimal subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
                                pstmtDetalles.setInt(1, venta.getId());
                                pstmtDetalles.setInt(2, producto.getId());
                                pstmtDetalles.setInt(3, cantidad);
                                pstmtDetalles.setBigDecimal(4, precioUnitario);
                                pstmtDetalles.setBigDecimal(5, subtotal);
                                pstmtDetalles.addBatch();
                            }
                            pstmtDetalles.executeBatch();
                        }
                        
                        cargarDatos();
                        dialog.dispose();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al actualizar la venta: " + ex.getMessage(),
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
    
    private void verDetallesVenta() {
        int fila = tablaVentas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione una venta para ver sus detalles",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Venta venta = ventas.stream()
            .filter(v -> v.getId() == id)
            .findFirst()
            .orElse(null);
        
        if (venta != null) {
            mostrarDialogoVenta(venta);
        }
    }
    
    private void actualizarTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < modeloTablaProductos.getRowCount(); i++) {
            BigDecimal subtotal = (BigDecimal) modeloTablaProductos.getValueAt(i, 3);
            total = total.add(subtotal);
        }
        lblTotalValor.setText("$" + total.toString());
    }
    
    @Override
    public void onDataChanged(DataChangedEvent event) {
        // Actualizar los productos si ha habido cambios en ellos
        if (event == DataChangedEvent.PRODUCTO_ADDED || 
            event == DataChangedEvent.PRODUCTO_UPDATED || 
            event == DataChangedEvent.PRODUCTO_DELETED) {
            cargarProductos();
        }
    }
    
    private static class Venta {
        private final int id;
        private final java.sql.Date fecha;
        private final String cliente;
        private final BigDecimal total;
        
        public Venta(int id, java.sql.Date fecha, String cliente, BigDecimal total) {
            this.id = id;
            this.fecha = fecha;
            this.cliente = cliente;
            this.total = total;
        }
        
        public int getId() {
            return id;
        }
        
        public java.sql.Date getFecha() {
            return fecha;
        }
        
        public String getCliente() {
            return cliente;
        }
        
        public BigDecimal getTotal() {
            return total;
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
}