/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.panaderiacrud;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.UIManager;
import com.mycompany.panaderiacrud.gui.LoginFrame;

/**
 *
 * @author kotli
 */
public class PanaderiaCRUD {

    public static void main(String[] args) {
        try {
            // Configurar el Look and Feel moderno
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Error al configurar el Look and Feel: " + ex.getMessage());
        }
        
        // Iniciar la ventana de login
        java.awt.EventQueue.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
