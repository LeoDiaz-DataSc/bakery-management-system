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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import com.mycompany.panaderiacrud.util.ConexionDB;
import com.mycompany.panaderiacrud.util.DataChangedEvent;
import com.mycompany.panaderiacrud.util.DataChangeListener;
import com.mycompany.panaderiacrud.util.DataChangeNotifier;
import javax.swing.table.TableCellEditor;

public class RecetasPanel extends JPanel implements DataChangeListener {
    private JTable tablaRecetas;
    private DefaultTableModel modeloTabla;
    private JTextField txtBusqueda;
    private List<Receta> recetas;
    private List<Producto> productos;
    private List<Ingrediente> ingredientes;
    
    public RecetasPanel() {
        initComponents();
        cargarProductos();
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
        
        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.setBackground(new Color(0, 120, 212));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(e -> mostrarDialogoReceta(null));
        
        JButton btnEditar = new JButton("Editar");
        btnEditar.setBackground(new Color(0, 120, 212));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(e -> editarRecetaSeleccionada());
        
        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setBackground(new Color(220, 0, 0));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(e -> eliminarRecetaSeleccionada());
        
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        
        // Campo de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBusqueda.setBackground(Color.WHITE);
        
        txtBusqueda = new JTextField(20);
        txtBusqueda.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar receta...");
        txtBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarRecetas();
            }
        });
        
        panelBusqueda.add(txtBusqueda);
        
        panelSuperior.add(panelBotones, BorderLayout.WEST);
        panelSuperior.add(panelBusqueda, BorderLayout.EAST);
        
        // Tabla de recetas
        modeloTabla = new DefaultTableModel(new Object[]{
            "ID", "Producto", "Descripción", "Costo Total", "Ingredientes"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaRecetas = new JTable(modeloTabla);
        tablaRecetas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaRecetas.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(tablaRecetas);
        
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
    
    private void cargarDatos() {
        recetas = new ArrayList<>();
        modeloTabla.setRowCount(0);
        
        String sql = "SELECT r.*, p.Nom_Producto FROM recetas r " +
                    "LEFT JOIN productos p ON r.ID_Producto = p.ID_PRODUCTO " +
                    "ORDER BY p.Nom_Producto";
        
        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Receta receta = new Receta(
                    rs.getInt("ID_Receta"),
                    rs.getInt("ID_Producto"),
                    rs.getString("Nom_Producto"),
                    rs.getString("Instrucciones_Adicionales"),
                    rs.getBigDecimal("Costo_Total")
                );
                recetas.add(receta);
                
                // Contar cuántos ingredientes tiene la receta
                String sqlContarIngredientes = "SELECT COUNT(*) FROM detalles_receta WHERE ID_Receta = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlContarIngredientes)) {
                    pstmt.setInt(1, receta.getId());
                    try (ResultSet rsContar = pstmt.executeQuery()) {
                        rsContar.next();
                        modeloTabla.addRow(new Object[]{
                            receta.getId(),
                            receta.getNombreProducto(),
                            receta.getDescripcion(),
                            receta.getCostoTotal(),
                            rsContar.getInt(1) + " ingredientes"
                        });
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar las recetas: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarRecetas() {
        String busqueda = txtBusqueda.getText().toLowerCase();
        modeloTabla.setRowCount(0);
        
        for (Receta receta : recetas) {
            if (receta.getNombreProducto().toLowerCase().contains(busqueda) ||
                receta.getDescripcion().toLowerCase().contains(busqueda)) {
                modeloTabla.addRow(new Object[]{
                    receta.getId(),
                    receta.getNombreProducto(),
                    receta.getDescripcion(),
                    receta.getCostoTotal(),
                    "Ver Detalles"
                });
            }
        }
    }
    
    private void mostrarDialogoReceta(Receta receta) {
        cargarIngredientes();
        if (productos == null) cargarProductos();
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(receta == null ? "Nueva Receta" : "Editar Receta");
        dialog.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel lblProducto = new JLabel("Producto:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblProducto, gbc);
        JComboBox<Producto> cmbProducto = new JComboBox<>();
        for (Producto p : productos) {
            cmbProducto.addItem(p);
        }
        if (receta != null) {
            for (int i = 0; i < cmbProducto.getItemCount(); i++) {
                if (cmbProducto.getItemAt(i).getId() == receta.getIdProducto()) {
                    cmbProducto.setSelectedIndex(i);
                    break;
                }
            }
        }
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(cmbProducto, gbc);
        JLabel lblDescripcion = new JLabel("Descripción:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblDescripcion, gbc);
        JTextArea txtDescripcion = new JTextArea(3, 20);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        if (receta != null) {
            txtDescripcion.setText(receta.getDescripcion());
        }
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(new JScrollPane(txtDescripcion), gbc);
        JLabel lblIngredientes = new JLabel("Ingredientes:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblIngredientes, gbc);
        DefaultTableModel modeloTablaIngredientes = new DefaultTableModel(new Object[]{
            "Ingrediente", "Cantidad", "Unidad", "Costo"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 1;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Ingrediente.class;
                if (columnIndex == 1) return Double.class;
                if (columnIndex == 2) return String.class;
                if (columnIndex == 3) return java.math.BigDecimal.class;
                return Object.class;
            }
        };
        JTable tablaIngredientes = new JTable(modeloTablaIngredientes);
        tablaIngredientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPaneIngredientes = new JScrollPane(tablaIngredientes);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridheight = 2;
        panel.add(scrollPaneIngredientes, gbc);
        JPanel panelBotonesIngredientes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregarIngrediente = new JButton("Agregar Ingrediente");
        btnAgregarIngrediente.addActionListener(e -> {
            modeloTablaIngredientes.addRow(new Object[]{null, 0.0, "", java.math.BigDecimal.ZERO});
        });
        JButton btnEliminarIngrediente = new JButton("Eliminar Ingrediente");
        btnEliminarIngrediente.addActionListener(e -> {
            int fila = tablaIngredientes.getSelectedRow();
            if (fila != -1) {
                modeloTablaIngredientes.removeRow(fila);
            }
        });
        panelBotonesIngredientes.add(btnAgregarIngrediente);
        panelBotonesIngredientes.add(btnEliminarIngrediente);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridheight = 1;
        panel.add(panelBotonesIngredientes, gbc);
        // Editor personalizado para la columna de ingredientes
        TableCellEditor editorIngrediente = new DefaultCellEditor(new JComboBox<Ingrediente>(ingredientes.toArray(new Ingrediente[0])));
        tablaIngredientes.getColumnModel().getColumn(0).setCellEditor(editorIngrediente);
        // Editor para cantidad
        tablaIngredientes.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()));
        // Listener para actualizar unidad y costo automáticamente al seleccionar ingrediente o cambiar cantidad
        final boolean[] actualizando = {false};
        modeloTablaIngredientes.addTableModelListener(e -> {
            if (actualizando[0]) return;
            int fila = e.getFirstRow();
            int columna = e.getColumn();
            if (fila >= 0) {
                Object objIngrediente = modeloTablaIngredientes.getValueAt(fila, 0);
                Object objCantidad = modeloTablaIngredientes.getValueAt(fila, 1);
                if (objIngrediente instanceof Ingrediente) {
                    Ingrediente ingrediente = (Ingrediente) objIngrediente;
                    double cantidad = 0.0;
                    try {
                        cantidad = objCantidad instanceof Number ? ((Number)objCantidad).doubleValue() : Double.parseDouble(objCantidad.toString());
                    } catch (Exception ex) { cantidad = 0.0; }
                    String unidadActual = (String) modeloTablaIngredientes.getValueAt(fila, 2);
                    String nuevaUnidad = ingrediente.getUnidad();
                    java.math.BigDecimal nuevoCosto = ingrediente.getCosto().multiply(new java.math.BigDecimal(cantidad));
                    Object costoActual = modeloTablaIngredientes.getValueAt(fila, 3);
                    boolean cambio = false;
                    if (!nuevaUnidad.equals(unidadActual)) cambio = true;
                    if (!(nuevoCosto.equals(costoActual))) cambio = true;
                    if (cambio) {
                        actualizando[0] = true;
                        modeloTablaIngredientes.setValueAt(nuevaUnidad, fila, 2);
                        modeloTablaIngredientes.setValueAt(nuevoCosto, fila, 3);
                        actualizando[0] = false;
                    }
                } else {
                    actualizando[0] = true;
                    modeloTablaIngredientes.setValueAt("", fila, 2);
                    modeloTablaIngredientes.setValueAt(java.math.BigDecimal.ZERO, fila, 3);
                    actualizando[0] = false;
                }
            }
        });
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(0, 120, 212));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> {
            try {
                Producto producto = (Producto) cmbProducto.getSelectedItem();
                String descripcion = txtDescripcion.getText().trim();
                
                if (descripcion.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "La descripción no puede estar vacía",
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
                
                // Calcular el costo total
                BigDecimal costoTotal = BigDecimal.ZERO;
                for (int i = 0; i < modeloTablaIngredientes.getRowCount(); i++) {
                    costoTotal = costoTotal.add((BigDecimal) modeloTablaIngredientes.getValueAt(i, 3));
                }
                
                if (receta == null) {
                    // Insertar nueva receta
                    String sql = "INSERT INTO recetas (ID_Producto, Instrucciones_Adicionales, Costo_Total) VALUES (?, ?, ?)";
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setInt(1, producto.getId());
                        pstmt.setString(2, descripcion);
                        pstmt.setBigDecimal(3, costoTotal);
                        pstmt.executeUpdate();
                        
                        // Obtener el ID de la receta insertada
                        ResultSet rs = pstmt.getGeneratedKeys();
                        int idReceta = 0;
                        if (rs.next()) {
                            idReceta = rs.getInt(1);
                        }
                        
                        // Insertar los detalles de la receta
                        sql = "INSERT INTO detalles_receta (ID_Receta, ID_Ingrediente, Cantidad) VALUES (?, ?, ?)";
                        try (PreparedStatement pstmtDetalles = conn.prepareStatement(sql)) {
                            for (int i = 0; i < modeloTablaIngredientes.getRowCount(); i++) {
                                Ingrediente ingrediente = (Ingrediente) modeloTablaIngredientes.getValueAt(i, 0);
                                double cantidad = (double) modeloTablaIngredientes.getValueAt(i, 1);
                                
                                pstmtDetalles.setInt(1, idReceta);
                                pstmtDetalles.setInt(2, ingrediente.getId());
                                pstmtDetalles.setDouble(3, cantidad);
                                pstmtDetalles.addBatch();
                            }
                            pstmtDetalles.executeBatch();
                        }
                        
                        cargarDatos();
                        dialog.dispose();
                        
                        // Notificar a otros paneles sobre el cambio en las recetas
                        DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.PRODUCTO_UPDATED);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al guardar la receta: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // Actualizar receta existente
                    String sql = "UPDATE recetas SET ID_Producto = ?, Instrucciones_Adicionales = ?, Costo_Total = ? WHERE ID_Receta = ?";
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, producto.getId());
                        pstmt.setString(2, descripcion);
                        pstmt.setBigDecimal(3, costoTotal);
                        pstmt.setInt(4, receta.getId());
                        pstmt.executeUpdate();
                        
                        // Eliminar los detalles existentes
                        sql = "DELETE FROM detalles_receta WHERE ID_Receta = ?";
                        try (PreparedStatement pstmtEliminar = conn.prepareStatement(sql)) {
                            pstmtEliminar.setInt(1, receta.getId());
                            pstmtEliminar.executeUpdate();
                        }
                        
                        // Insertar los nuevos detalles
                        sql = "INSERT INTO detalles_receta (ID_Receta, ID_Ingrediente, Cantidad) VALUES (?, ?, ?)";
                        try (PreparedStatement pstmtDetalles = conn.prepareStatement(sql)) {
                            for (int i = 0; i < modeloTablaIngredientes.getRowCount(); i++) {
                                Ingrediente ingrediente = (Ingrediente) modeloTablaIngredientes.getValueAt(i, 0);
                                double cantidad = (double) modeloTablaIngredientes.getValueAt(i, 1);
                                
                                pstmtDetalles.setInt(1, receta.getId());
                                pstmtDetalles.setInt(2, ingrediente.getId());
                                pstmtDetalles.setDouble(3, cantidad);
                                pstmtDetalles.addBatch();
                            }
                            pstmtDetalles.executeBatch();
                        }
                        
                        cargarDatos();
                        dialog.dispose();
                        
                        // Notificar a otros paneles sobre el cambio en las recetas
                        DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.PRODUCTO_UPDATED);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al actualizar la receta: " + ex.getMessage(),
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
    
    private void editarRecetaSeleccionada() {
        int fila = tablaRecetas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione una receta para editar",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Receta receta = recetas.stream()
            .filter(r -> r.getId() == id)
            .findFirst()
            .orElse(null);
        
        if (receta != null) {
            mostrarDialogoReceta(receta);
        }
    }
    
    private void eliminarRecetaSeleccionada() {
        int fila = tablaRecetas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione una receta para eliminar",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modeloTabla.getValueAt(fila, 0);
        String nombre = (String) modeloTabla.getValueAt(fila, 1);
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar la receta para '" + nombre + "'?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM recetas WHERE ID_Receta = ?";
            try (Connection conn = ConexionDB.getConexion();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                cargarDatos();
                
                // Notificar a otros paneles sobre la eliminación de una receta
                DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.PRODUCTO_UPDATED);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar la receta: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    public void onDataChanged(DataChangedEvent event) {
        // Actualizar los productos si ha habido cambios en ellos
        if (event == DataChangedEvent.PRODUCTO_ADDED || 
            event == DataChangedEvent.PRODUCTO_UPDATED || 
            event == DataChangedEvent.PRODUCTO_DELETED) {
            cargarProductos();
            cargarDatos();
        }
        
        // Actualizar los ingredientes si ha habido cambios en ellos
        if (event == DataChangedEvent.INGREDIENTE_ADDED || 
            event == DataChangedEvent.INGREDIENTE_UPDATED || 
            event == DataChangedEvent.INGREDIENTE_DELETED) {
            // Recargar ingredientes para los diálogos
            cargarIngredientes();
            cargarDatos();
        }
    }
    
    private static class Receta {
        private final int id;
        private final int idProducto;
        private final String nombreProducto;
        private final String descripcion;
        private final BigDecimal costoTotal;
        
        public Receta(int id, int idProducto, String nombreProducto, String descripcion, BigDecimal costoTotal) {
            this.id = id;
            this.idProducto = idProducto;
            this.nombreProducto = nombreProducto;
            this.descripcion = descripcion;
            this.costoTotal = costoTotal;
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
        
        public String getDescripcion() {
            return descripcion;
        }
        
        public BigDecimal getCostoTotal() {
            return costoTotal;
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