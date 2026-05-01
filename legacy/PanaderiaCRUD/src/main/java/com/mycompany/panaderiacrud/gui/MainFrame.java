package com.mycompany.panaderiacrud.gui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.border.EmptyBorder;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class MainFrame extends JFrame {
    private JPanel contentPanel;
    private String rolUsuario;
    private CardLayout cardLayout;
    
    public MainFrame(String rolUsuario) {
        this.rolUsuario = rolUsuario;
        initComponents();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("Sistema de Panadería");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Panel principal con BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Panel lateral (menú)
        JPanel sidePanel = new JPanel();
        sidePanel.setBackground(new Color(0, 120, 212));
        sidePanel.setPreferredSize(new Dimension(250, 0));
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(new EmptyBorder(20, 10, 20, 10));
        
        // Logo y título
        JLabel lblLogo = new JLabel("Panadería");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidePanel.add(lblLogo);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Botones del menú
        String[] menuItems = {
            "Dashboard", "Productos", "Ingredientes", "Recetas",
            "Categorías", "Proveedores", "Ventas", "Compras",
            "Costos", "Usuarios"
        };
        
        for (String item : menuItems) {
            JButton btn = createMenuButton(item);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidePanel.add(btn);
            sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        // Panel de contenido
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        
        // Agregar paneles de contenido
        contentPanel.add(new DashboardPanel(), "Dashboard");
        contentPanel.add(new CategoriasPanel(), "Categorías");
        contentPanel.add(new ProveedoresPanel(), "Proveedores");
        contentPanel.add(new IngredientesPanel(), "Ingredientes");
        contentPanel.add(new ProductosPanel(), "Productos");
        contentPanel.add(new RecetasPanel(), "Recetas");
        contentPanel.add(new VentasPanel(), "Ventas");
        contentPanel.add(new ComprasPanel(), "Compras");
        contentPanel.add(new CostosProduccionPanel(), "Costos");
        contentPanel.add(new UsuariosPanel(), "Usuarios");
        // TODO: Agregar los demás paneles
        
        // Agregar paneles al panel principal
        mainPanel.add(sidePanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Agregar panel principal al frame
        add(mainPanel);
        
        // Mostrar el dashboard por defecto
        cardLayout.show(contentPanel, "Dashboard");
    }
    
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 120, 212));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setMaximumSize(new Dimension(230, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(15);
        
        // Agregar icono según el texto
        try {
            switch (text) {
                case "Dashboard":
                    btn.setIcon(new ImageIcon(createIcon("📊")));
                    break;
                case "Productos":
                    btn.setIcon(new ImageIcon(createIcon("🍞")));
                    break;
                case "Ingredientes":
                    btn.setIcon(new ImageIcon(createIcon("🥚")));
                    break;
                case "Recetas":
                    btn.setIcon(new ImageIcon(createIcon("📝")));
                    break;
                case "Categorías":
                    btn.setIcon(new ImageIcon(createIcon("📑")));
                    break;
                case "Proveedores":
                    btn.setIcon(new ImageIcon(createIcon("🏢")));
                    break;
                case "Ventas":
                    btn.setIcon(new ImageIcon(createIcon("💰")));
                    break;
                case "Compras":
                    btn.setIcon(new ImageIcon(createIcon("🛒")));
                    break;
                case "Costos":
                    btn.setIcon(new ImageIcon(createIcon("📈")));
                    break;
                case "Usuarios":
                    btn.setIcon(new ImageIcon(createIcon("👥")));
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error al crear icono para " + text + ": " + e.getMessage());
        }
        
        btn.addActionListener(e -> {
            // Cambiar el panel de contenido según el botón seleccionado
            cardLayout.show(contentPanel, text);
        });
        
        return btn;
    }
    
    private byte[] createIcon(String emoji) {
        // Crear una imagen simple con el emoji
        BufferedImage image = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(0, 120, 212));
        g2d.fillRect(0, 0, 24, 24);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        g2d.drawString(emoji, 4, 18);
        g2d.dispose();
        
        // Convertir la imagen a bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
} 