package com.mycompany.panaderiacrud.util;

/**
 * Interfaz para los listeners (observadores) que serán notificados cuando 
 * ocurran cambios en los datos.
 */
public interface DataChangeListener {
    /**
     * Método llamado cuando ocurre un cambio en los datos.
     * @param event El tipo de evento que ocurrió
     */
    void onDataChanged(DataChangedEvent event);
}
