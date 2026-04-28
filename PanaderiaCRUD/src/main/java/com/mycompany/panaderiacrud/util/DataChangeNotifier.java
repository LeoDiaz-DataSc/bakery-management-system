package com.mycompany.panaderiacrud.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que maneja la notificación de cambios en los datos a todos los listeners registrados.
 * Implementa el patrón Singleton para asegurar que sólo exista una instancia.
 */
public class DataChangeNotifier {
    private static DataChangeNotifier instance;
    private final List<DataChangeListener> listeners = new ArrayList<>();
    
    private DataChangeNotifier() {
        // Constructor privado para patrón Singleton
    }
    
    /**
     * Obtiene la instancia única del notificador.
     * @return La instancia única de DataChangeNotifier
     */
    public static synchronized DataChangeNotifier getInstance() {
        if (instance == null) {
            instance = new DataChangeNotifier();
        }
        return instance;
    }
    
    /**
     * Añade un nuevo listener para recibir notificaciones de cambios de datos.
     * @param listener El listener a añadir
     */
    public void addListener(DataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Elimina un listener.
     * @param listener El listener a eliminar
     */
    public void removeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notifica a todos los listeners registrados que ha ocurrido un cambio.
     * @param event El tipo de evento que ha ocurrido
     */
    public void fireDataChanged(DataChangedEvent event) {
        for (DataChangeListener listener : listeners) {
            listener.onDataChanged(event);
        }
    }
}
