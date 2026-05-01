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
import com.mycompany.panaderiacrud.util.ConexionDB;
import com.mycompany.panaderiacrud.util.DataChangedEvent;
import com.mycompany.panaderiacrud.util.DataChangeListener;
import com.mycompany.panaderiacrud.util.DataChangeNotifier;

public class ProductosPanel extends JPanel implements DataChangeListener {
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBusqueda;
    private List<Producto> productos;
    private List<Categoria> categorias;
    
    public ProductosPanel() {
        initComponents();
        cargarCategorias();
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
        btnAgregar.addActionListener(e -> mostrarDialogoProducto(null));
        
        JButton btnEditar = new JButton("Editar");
        btnEditar.setBackground(new Color(0, 120, 212));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(e -> editarProductoSeleccionado());
        
        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setBackground(new Color(220, 0, 0));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(e -> eliminarProductoSeleccionado());
        
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        
        // Campo de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBusqueda.setBackground(Color.WHITE);
        
        txtBusqueda = new JTextField(20);
        txtBusqueda.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar producto...");
        txtBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarProductos();
            }
        });
        
        panelBusqueda.add(txtBusqueda);
        
        panelSuperior.add(panelBotones, BorderLayout.WEST);
        panelSuperior.add(panelBusqueda, BorderLayout.EAST);
        
        // Tabla de productos
        modeloTabla = new DefaultTableModel(new Object[]{
            "ID", "Nombre", "Descripción", "Precio Unitario", "Stock", "Categoría"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        
        // Agregar componentes al panel principal
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void cargarCategorias() {
        categorias = new ArrayList<>();
        String sql = "SELECT * FROM categorias ORDER BY Nombre_Categoria";
        
        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categorias.add(new Categoria(
                    rs.getInt("ID_Categoria"),
                    rs.getString("Nombre_Categoria")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar las categorías: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cargarDatos() {
        productos = new ArrayList<>();
        modeloTabla.setRowCount(0);
        
        String sql = "SELECT p.*, c.Nombre_Categoria " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.ID_Categoria = c.ID_Categoria " +
                    "ORDER BY p.Nom_Producto";
        
        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Producto producto = new Producto(
                    rs.getInt("ID_PRODUCTO"),
                    rs.getString("Nom_Producto"),
                    rs.getString("descripcion"),
                    rs.getBigDecimal("Precio_Unidad"),
                    rs.getInt("Stock_Disponible"),
                    rs.getInt("ID_Categoria"),
                    rs.getString("Nombre_Categoria")
                );
                productos.add(producto);
                modeloTabla.addRow(new Object[]{
                    producto.getId(),
                    producto.getNombre(),
                    producto.getDescripcion(),
                    producto.getPrecio(),
                    producto.getStock(),
                    producto.getNombreCategoria()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar los productos: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarProductos() {
        String filtro = txtBusqueda.getText().toLowerCase();
        
        modeloTabla.setRowCount(0);
        for (Producto p : productos) {
            if (p.getNombre().toLowerCase().contains(filtro) ||
                (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(filtro)) ||
                p.getNombreCategoria().toLowerCase().contains(filtro)) {
                
                modeloTabla.addRow(new Object[]{
                    p.getId(),
                    p.getNombre(),
                    p.getDescripcion(),
                    p.getPrecio(),
                    p.getStock(),
                    p.getNombreCategoria()
                });
            }
        }
    }
    
    private void mostrarDialogoProducto(Producto producto) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(producto == null ? "Nuevo Producto" : "Editar Producto");
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
        if (producto != null) {
            txtNombre.setText(producto.getNombre());
        }
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(txtNombre, gbc);
        
        JLabel lblDescripcion = new JLabel("Descripción:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblDescripcion, gbc);
        
        JTextArea txtDescripcion = new JTextArea(3, 20);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        if (producto != null) {
            txtDescripcion.setText(producto.getDescripcion());
        }
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(new JScrollPane(txtDescripcion), gbc);
        
        JLabel lblPrecio = new JLabel("Precio Unitario:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblPrecio, gbc);
        
        JTextField txtPrecio = new JTextField(20);
        if (producto != null) {
            txtPrecio.setText(producto.getPrecio().toString());
        }
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(txtPrecio, gbc);
        
        JLabel lblStock = new JLabel("Stock:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblStock, gbc);
        
        JSpinner spnStock = new JSpinner(new SpinnerNumberModel(
            producto != null ? producto.getStock() : 0,
            0, Integer.MAX_VALUE, 1));
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(spnStock, gbc);
        
        JLabel lblCategoria = new JLabel("Categoría:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(lblCategoria, gbc);
        
        JComboBox<Categoria> cmbCategoria = new JComboBox<>();
        for (Categoria c : categorias) {
            cmbCategoria.addItem(c);
        }
        if (producto != null) {
            for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                if (cmbCategoria.getItemAt(i).getId() == producto.getIdCategoria()) {
                    cmbCategoria.setSelectedIndex(i);
                    break;
                }
            }
        }
        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(cmbCategoria, gbc);
        
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(0, 120, 212));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> {
            try {
                String nombre = txtNombre.getText().trim();
                String descripcion = txtDescripcion.getText().trim();
                BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
                int stock = (int) spnStock.getValue();
                Categoria categoria = (Categoria) cmbCategoria.getSelectedItem();
                
                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "El nombre no puede estar vacío",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(dialog,
                        "El precio unitario debe ser mayor que 0",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (stock < 0) {
                    JOptionPane.showMessageDialog(dialog,
                        "El stock no puede ser negativo",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (producto == null) {
                    // Insertar nuevo producto
                    String sql = "INSERT INTO productos (Nom_Producto, descripcion, Precio_Unidad, Stock_Disponible, ID_Categoria, Estado) VALUES (?, ?, ?, ?, ?, 'activo')";
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, nombre);
                        pstmt.setString(2, descripcion);
                        pstmt.setBigDecimal(3, precio);
                        pstmt.setInt(4, stock);
                        pstmt.setInt(5, categoria.getId());
                        pstmt.executeUpdate();
                        cargarDatos();
                        dialog.dispose();
                        
                        // Notificar a los otros paneles que se ha añadido un producto
                        DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.PRODUCTO_ADDED);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al guardar el producto: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // Actualizar producto existente
                    String sql = "UPDATE productos SET Nom_Producto = ?, descripcion = ?, Precio_Unidad = ?, Stock_Disponible = ?, ID_Categoria = ? WHERE ID_PRODUCTO = ?";
                    try (Connection conn = ConexionDB.getConexion();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, nombre);
                        pstmt.setString(2, descripcion);
                        pstmt.setBigDecimal(3, precio);
                        pstmt.setInt(4, stock);
                        pstmt.setInt(5, categoria.getId());
                        pstmt.setInt(6, producto.getId());
                        pstmt.executeUpdate();
                        cargarDatos();
                        dialog.dispose();
                        
                        // Notificar a los otros paneles que se ha actualizado un producto
                        DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.PRODUCTO_UPDATED);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error al actualizar el producto: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "El precio unitario debe ser un número válido",
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
    
    private void editarProductoSeleccionado() {
        int fila = tablaProductos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un producto para editar",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Producto producto = productos.stream()
            .filter(p -> p.getId() == id)
            .findFirst()
            .orElse(null);
        
        if (producto != null) {
            mostrarDialogoProducto(producto);
        }
    }
    
    private void eliminarProductoSeleccionado() {
        int fila = tablaProductos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un producto para eliminar",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) modeloTabla.getValueAt(fila, 0);
        String nombre = (String) modeloTabla.getValueAt(fila, 1);
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar el producto '" + nombre + "'?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM productos WHERE ID_PRODUCTO = ?";
            try (Connection conn = ConexionDB.getConexion();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                cargarDatos();
                
                // Notificar a los otros paneles que se ha eliminado un producto
                DataChangeNotifier.getInstance().fireDataChanged(DataChangedEvent.PRODUCTO_DELETED);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar el producto: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    public void onDataChanged(DataChangedEvent event) {
        // Actualizar las categorías si ha habido cambios en ellas
        if (event == DataChangedEvent.CATEGORIA_ADDED || 
            event == DataChangedEvent.CATEGORIA_UPDATED || 
            event == DataChangedEvent.CATEGORIA_DELETED) {
            cargarCategorias();
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
        
        @Override
        public String toString() {
            return nombre;
        }
    }
}