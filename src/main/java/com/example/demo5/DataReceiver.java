package com.example.demo5;

/**
 * Interface for controllers that need to receive data when switching scenes
 */
public interface DataReceiver {
    /**
     * Method to receive data from the previous scene
     *
     * @param data The data passed from the previous scene
     */
    void receiveData(Object data);
}
