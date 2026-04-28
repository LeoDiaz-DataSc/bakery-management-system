package com.mycompany.panaderiacrud.gui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.border.EmptyBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import java.sql.*;
import com.mycompany.panaderiacrud.util.ConexionDB;

public class DashboardPanel extends JPanel {
    public DashboardPanel() {
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Panel superior con estadísticas
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setBackground(Color.WHITE);
        
        // Tarjetas de estadísticas
        statsPanel.add(createStatCard("Ventas Hoy", "$1,234", "↑ 12%", Color.GREEN));
        statsPanel.add(createStatCard("Productos", "45", "↓ 5%", Color.RED));
        statsPanel.add(createStatCard("Ingredientes", "28", "↑ 3%", Color.GREEN));
        statsPanel.add(createStatCard("Proveedores", "15", "→", Color.BLUE));
        
        // Panel de gráficos
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsPanel.setBackground(Color.WHITE);
        
        // Gráfico de ventas
        DefaultCategoryDataset datasetVentas = new DefaultCategoryDataset();
        datasetVentas.addValue(1200, "Ventas", "Lun");
        datasetVentas.addValue(1500, "Ventas", "Mar");
        datasetVentas.addValue(1800, "Ventas", "Mié");
        datasetVentas.addValue(2000, "Ventas", "Jue");
        datasetVentas.addValue(2200, "Ventas", "Vie");
        datasetVentas.addValue(2500, "Ventas", "Sáb");
        datasetVentas.addValue(3000, "Ventas", "Dom");
        
        JFreeChart chartVentas = ChartFactory.createBarChart(
            "Ventas Semanales",
            "Día",
            "Monto ($)",
            datasetVentas
        );
        
        // Gráfico de productos
        DefaultPieDataset datasetProductos = new DefaultPieDataset();
        datasetProductos.setValue("Pan", 30);
        datasetProductos.setValue("Pasteles", 25);
        datasetProductos.setValue("Galletas", 20);
        datasetProductos.setValue("Otros", 25);
        
        JFreeChart chartProductos = ChartFactory.createPieChart(
            "Distribución de Productos",
            datasetProductos,
            true,
            true,
            false
        );
        
        // Agregar gráficos al panel
        chartsPanel.add(new ChartPanel(chartVentas));
        chartsPanel.add(new ChartPanel(chartProductos));
        
        // Agregar paneles al panel principal
        add(statsPanel, BorderLayout.NORTH);
        add(chartsPanel, BorderLayout.CENTER);
    }
    
    private JPanel createStatCard(String title, String value, String trend, Color trendColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(new Color(100, 100, 100));
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel lblTrend = new JLabel(trend);
        lblTrend.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTrend.setForeground(trendColor);
        
        card.add(lblTitle);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(lblValue);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(lblTrend);
        
        return card;
    }
} 