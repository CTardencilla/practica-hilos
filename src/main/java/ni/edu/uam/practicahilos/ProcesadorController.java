package ni.edu.uam.practicahilos;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 * Controlador principal de la aplicación JavaFX para la gestión de hilos.
 * Desarrollado para NicaData Solutions S.A. / Universidad Americana (UAM).
 * 
 * Cumple con los objetivos de aprendizaje:
 * - RA3: Desarrollo de aplicaciones con interfaz gráfica y programación concurrente.
 * - IL3.1: Implementación de hilos con Thread y Runnable en tareas concurrentes.
 * - IL3.3: Gestión del ciclo de vida y detención controlada con interrupt() y Platform.runLater().
 */
public class ProcesadorController {

    // =========================================================================
    // Componentes FXML - Práctica Base (Paso 2 y 4)
    // =========================================================================
    @FXML
    private Label lblEstado;

    @FXML
    private Label lblPorcentaje;

    @FXML
    private ProgressBar barraProgreso;

    @FXML
    private Button btnIniciar;

    @FXML
    private Button btnDetener;

    @FXML
    private Button btnReiniciar;

    // Hilo para el proceso de la práctica básica
    private Thread hiloProceso;
    private int progresoActual = 0;

    // =========================================================================
    // Componentes FXML - Monitor Empresarial Concurrente
    // =========================================================================
    // Proceso Clientes (100 registros)
    @FXML
    private Label lblEstadoClientes;

    @FXML
    private Label lblPorcentajeClientes;

    @FXML
    private ProgressBar barraProgresoClientes;

    @FXML
    private Button btnIniciarClientes;

    @FXML
    private Button btnDetenerClientes;

    @FXML
    private Button btnReiniciarClientes;

    private Thread hiloClientes;
    private static final int TOTAL_CLIENTES = 100;
    private static final int TIEMPO_CLIENTE_MS = 80; // 80 ms por cliente (total ~8 s)
    private int progresoClientes = 0;

    // Proceso Productos (50 registros)
    @FXML
    private Label lblEstadoProductos;

    @FXML
    private Label lblPorcentajeProductos;

    @FXML
    private ProgressBar barraProgresoProductos;

    @FXML
    private Button btnIniciarProductos;

    @FXML
    private Button btnDetenerProductos;

    @FXML
    private Button btnReiniciarProductos;

    private Thread hiloProductos;
    private static final int TOTAL_PRODUCTOS = 50;
    private static final int TIEMPO_PRODUCTO_MS = 140; // 140 ms por producto (total ~7 s)
    private int progresoProductos = 0;

    // =========================================================================
    // MÉTODOS - PROCESO INDIVIDUAL
    // =========================================================================

    /**
     * Inicia o reanuda el procesamiento en segundo plano utilizando Thread y Runnable.
     */
    @FXML
    private void iniciarProceso() {
        if (barraProgreso == null) {
            iniciarClientes();
            return;
        }
        // Evitar iniciar si ya se está ejecutando
        if (hiloProceso != null && hiloProceso.isAlive()) {
            return;
        }

        // Si ya completó los 100 registros, reiniciar desde cero
        if (progresoActual >= 100) {
            progresoActual = 0;
            if (barraProgreso != null) barraProgreso.setProgress(0);
            if (lblPorcentaje != null) lblPorcentaje.setText("0%");
        }

        if (lblEstado != null) {
            lblEstado.setText(progresoActual > 0 
                ? "Reanudando registros (" + progresoActual + "%)..." 
                : "Procesando registros...");
        }
        if (btnIniciar != null) btnIniciar.setDisable(true);
        if (btnDetener != null) btnDetener.setDisable(false);
        if (btnReiniciar != null) btnReiniciar.setDisable(true);

        final int puntoInicio = progresoActual + 1;

        Runnable tarea = () -> {
            try {
                for (int i = puntoInicio; i <= 100; i++) {
                    // Verificación de interrupción cooperativa
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }

                    // Simulación del tiempo de procesamiento por registro
                    Thread.sleep(100);

                    final int avance = i;
                    progresoActual = avance;

                    // Actualización segura de la interfaz JavaFX
                    Platform.runLater(() -> {
                        if (barraProgreso != null) barraProgreso.setProgress(avance / 100.0);
                        if (lblPorcentaje != null) lblPorcentaje.setText(avance + "%");
                    });
                }

                // Notificación de finalización exitosa
                Platform.runLater(() -> {
                    if (lblEstado != null) lblEstado.setText("Proceso finalizado con éxito");
                    habilitarBotones();
                });

            } catch (InterruptedException e) {
                // Restablecer el estado de interrupción del hilo
                Thread.currentThread().interrupt();

                // Notificación visual de detención con avance actual guardado
                Platform.runLater(() -> {
                    if (lblEstado != null) lblEstado.setText("Proceso detenido en " + progresoActual + "%");
                    habilitarBotones();
                });
            }
        };

        hiloProceso = new Thread(tarea);
        hiloProceso.setName("Hilo-Procesamiento");
        // Hilo demonio para que no impida el cierre de la JVM
        hiloProceso.setDaemon(true);
        hiloProceso.start();
    }

    /**
     * Solicita la detención controlada del hilo mediante interrupt().
     */
    @FXML
    private void detenerProceso() {
        if (barraProgreso == null) {
            detenerClientes();
            return;
        }
        if (hiloProceso != null && hiloProceso.isAlive()) {
            if (btnDetener != null) btnDetener.setDisable(true);
            if (lblEstado != null) lblEstado.setText("Solicitando detención...");
            hiloProceso.interrupt();
        }
    }

    /**
     * Reinicia los controles al estado inicial (0%).
     */
    @FXML
    private void reiniciarProceso() {
        if (barraProgreso == null) {
            reiniciarClientes();
            return;
        }
        if (hiloProceso != null && hiloProceso.isAlive()) {
            return;
        }
        progresoActual = 0;
        if (barraProgreso != null) barraProgreso.setProgress(0);
        if (lblPorcentaje != null) lblPorcentaje.setText("0%");
        if (lblEstado != null) lblEstado.setText("Esperando inicio");
        habilitarBotones();
    }

    /**
     * Restablece el estado de los botones de la práctica base.
     */
    private void habilitarBotones() {
        btnIniciar.setDisable(false);
        btnDetener.setDisable(true);
        btnReiniciar.setDisable(false);
    }

    // =========================================================================
    // PASO 6: Identificar el bloqueo de la interfaz gráfica (Paso 6)
    // =========================================================================

    /**
     * Demostración de proceso bloqueante ejecutado directamente en el JavaFX
     * Application Thread. Durante los 5 segundos de espera, la interfaz se congela,
     * no responde a clics, ni puede ser redibujada por el sistema operativo.
     */
    @FXML
    private void procesoBloqueante() {
        lblEstado.setText("Procesando en hilo principal (¡UI bloqueada!)...");
        
        // Mensaje en consola para registro
        System.out.println("[ALERTA] Iniciando proceso bloqueante en JavaFX Application Thread...");

        try {
            // Este sleep() congela el hilo de la interfaz gráfica
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        lblEstado.setText("Finalizado (se desbloqueó la interfaz)");
        System.out.println("[ALERTA] Proceso bloqueante finalizado.");
    }

    /**
     * Muestra una ventana de diálogo explicativa con el análisis del Paso 6.
     */
    @FXML
    private void mostrarExplicacionBloqueo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Diagnóstico Técnico - Bloqueo de JavaFX Application Thread");
        alert.setHeaderText("¿Por qué no debemos ejecutar procesos prolongados en eventos de JavaFX?");
        alert.setContentText(
            "JavaFX utiliza un modelo de un solo hilo para la interfaz gráfica (JavaFX Application Thread).\n\n" +
            "1. Si se ejecuta una tarea pesada o Thread.sleep() en este hilo, se bloquea la cola de eventos.\n" +
            "2. La aplicación deja de repintarse (pantalla congelada), no procesa clics del ratón ni pulsaciones de teclas.\n" +
            "3. El sistema operativo marca la ventana como 'No responde'.\n\n" +
            "Solución: Delegar el trabajo pesado a un hilo secundario (Thread / Runnable) y usar Platform.runLater() " +
            "únicamente para actualizar los controles visuales."
        );
        alert.showAndWait();
    }

    // =========================================================================
    // PROCESAMIENTO CONCURRENTE (CLIENTES Y PRODUCTOS)
    // =========================================================================

    /**
     * Inicia o reanuda el hilo para procesamiento de clientes (100 registros).
     */
    @FXML
    private void iniciarClientes() {
        if (hiloClientes != null && hiloClientes.isAlive()) {
            return;
        }

        // Si ya completó los 100 registros, reiniciar desde cero
        if (progresoClientes >= TOTAL_CLIENTES) {
            progresoClientes = 0;
            if (barraProgresoClientes != null) barraProgresoClientes.setProgress(0);
            if (lblPorcentajeClientes != null) lblPorcentajeClientes.setText("0%");
        }

        if (lblEstadoClientes != null) {
            lblEstadoClientes.setText(progresoClientes > 0 
                ? "Reanudando clientes (" + progresoClientes + "/" + TOTAL_CLIENTES + ")..." 
                : "Procesando " + TOTAL_CLIENTES + " clientes...");
        }
        if (btnIniciarClientes != null) btnIniciarClientes.setDisable(true);
        if (btnDetenerClientes != null) btnDetenerClientes.setDisable(false);
        if (btnReiniciarClientes != null) btnReiniciarClientes.setDisable(true);

        final int puntoInicio = progresoClientes + 1;

        Runnable tareaClientes = () -> {
            try {
                for (int i = puntoInicio; i <= TOTAL_CLIENTES; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }

                    Thread.sleep(TIEMPO_CLIENTE_MS);

                    final int avance = i;
                    progresoClientes = avance;
                    final double porcentaje = (double) avance / TOTAL_CLIENTES;

                    Platform.runLater(() -> {
                        if (barraProgresoClientes != null) barraProgresoClientes.setProgress(porcentaje);
                        if (lblPorcentajeClientes != null) {
                            lblPorcentajeClientes.setText(String.format("%d%% (%d/%d)", 
                                    Math.round(porcentaje * 100), avance, TOTAL_CLIENTES));
                        }
                    });
                }

                Platform.runLater(() -> {
                    if (lblEstadoClientes != null) lblEstadoClientes.setText("¡Clientes procesados con éxito!");
                    if (btnIniciarClientes != null) btnIniciarClientes.setDisable(false);
                    if (btnDetenerClientes != null) btnDetenerClientes.setDisable(true);
                    if (btnReiniciarClientes != null) btnReiniciarClientes.setDisable(false);
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    if (lblEstadoClientes != null) {
                        lblEstadoClientes.setText("Proceso detenido (" + progresoClientes + "/" + TOTAL_CLIENTES + ")");
                    }
                    if (btnIniciarClientes != null) btnIniciarClientes.setDisable(false);
                    if (btnDetenerClientes != null) btnDetenerClientes.setDisable(true);
                    if (btnReiniciarClientes != null) btnReiniciarClientes.setDisable(false);
                });
            }
        };

        hiloClientes = new Thread(tareaClientes);
        hiloClientes.setName("Hilo-Clientes");
        hiloClientes.setDaemon(true);
        hiloClientes.start();
    }

    /**
     * Detiene controladamente el hilo de procesamiento de clientes.
     */
    @FXML
    private void detenerClientes() {
        if (hiloClientes != null && hiloClientes.isAlive()) {
            if (btnDetenerClientes != null) btnDetenerClientes.setDisable(true);
            if (lblEstadoClientes != null) lblEstadoClientes.setText("Solicitando detención de clientes...");
            hiloClientes.interrupt();
        }
    }

    /**
     * Reinicia los controles de clientes al estado inicial (0%).
     */
    @FXML
    private void reiniciarClientes() {
        if (hiloClientes != null && hiloClientes.isAlive()) {
            return;
        }
        progresoClientes = 0;
        if (barraProgresoClientes != null) barraProgresoClientes.setProgress(0);
        if (lblPorcentajeClientes != null) lblPorcentajeClientes.setText("0%");
        if (lblEstadoClientes != null) lblEstadoClientes.setText("En espera");
        if (btnIniciarClientes != null) btnIniciarClientes.setDisable(false);
        if (btnDetenerClientes != null) btnDetenerClientes.setDisable(true);
        if (btnReiniciarClientes != null) btnReiniciarClientes.setDisable(false);
    }

    /**
     * Inicia o reanuda el hilo para procesamiento de productos (50 registros).
     */
    @FXML
    private void iniciarProductos() {
        if (hiloProductos != null && hiloProductos.isAlive()) {
            return;
        }

        // Si ya completó los 50 registros, reiniciar desde cero
        if (progresoProductos >= TOTAL_PRODUCTOS) {
            progresoProductos = 0;
            if (barraProgresoProductos != null) barraProgresoProductos.setProgress(0);
            if (lblPorcentajeProductos != null) lblPorcentajeProductos.setText("0%");
        }

        if (lblEstadoProductos != null) {
            lblEstadoProductos.setText(progresoProductos > 0 
                ? "Reanudando productos (" + progresoProductos + "/" + TOTAL_PRODUCTOS + ")..." 
                : "Procesando " + TOTAL_PRODUCTOS + " productos...");
        }
        if (btnIniciarProductos != null) btnIniciarProductos.setDisable(true);
        if (btnDetenerProductos != null) btnDetenerProductos.setDisable(false);
        if (btnReiniciarProductos != null) btnReiniciarProductos.setDisable(true);

        final int puntoInicio = progresoProductos + 1;

        Runnable tareaProductos = () -> {
            try {
                for (int i = puntoInicio; i <= TOTAL_PRODUCTOS; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }

                    Thread.sleep(TIEMPO_PRODUCTO_MS);

                    final int avance = i;
                    progresoProductos = avance;
                    final double porcentaje = (double) avance / TOTAL_PRODUCTOS;

                    Platform.runLater(() -> {
                        if (barraProgresoProductos != null) barraProgresoProductos.setProgress(porcentaje);
                        if (lblPorcentajeProductos != null) {
                            lblPorcentajeProductos.setText(String.format("%d%% (%d/%d)", 
                                    Math.round(porcentaje * 100), avance, TOTAL_PRODUCTOS));
                        }
                    });
                }

                Platform.runLater(() -> {
                    if (lblEstadoProductos != null) lblEstadoProductos.setText("¡Productos procesados con éxito!");
                    if (btnIniciarProductos != null) btnIniciarProductos.setDisable(false);
                    if (btnDetenerProductos != null) btnDetenerProductos.setDisable(true);
                    if (btnReiniciarProductos != null) btnReiniciarProductos.setDisable(false);
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    if (lblEstadoProductos != null) {
                        lblEstadoProductos.setText("Proceso detenido (" + progresoProductos + "/" + TOTAL_PRODUCTOS + ")");
                    }
                    if (btnIniciarProductos != null) btnIniciarProductos.setDisable(false);
                    if (btnDetenerProductos != null) btnDetenerProductos.setDisable(true);
                    if (btnReiniciarProductos != null) btnReiniciarProductos.setDisable(false);
                });
            }
        };

        hiloProductos = new Thread(tareaProductos);
        hiloProductos.setName("Hilo-Productos");
        hiloProductos.setDaemon(true);
        hiloProductos.start();
    }

    /**
     * Detiene controladamente el hilo de procesamiento de productos.
     */
    @FXML
    private void detenerProductos() {
        if (hiloProductos != null && hiloProductos.isAlive()) {
            if (btnDetenerProductos != null) btnDetenerProductos.setDisable(true);
            if (lblEstadoProductos != null) lblEstadoProductos.setText("Solicitando detención de productos...");
            hiloProductos.interrupt();
        }
    }

    /**
     * Reinicia los controles de productos al estado inicial (0%).
     */
    @FXML
    private void reiniciarProductos() {
        if (hiloProductos != null && hiloProductos.isAlive()) {
            return;
        }
        progresoProductos = 0;
        if (barraProgresoProductos != null) barraProgresoProductos.setProgress(0);
        if (lblPorcentajeProductos != null) lblPorcentajeProductos.setText("0%");
        if (lblEstadoProductos != null) lblEstadoProductos.setText("En espera");
        if (btnIniciarProductos != null) btnIniciarProductos.setDisable(false);
        if (btnDetenerProductos != null) btnDetenerProductos.setDisable(true);
        if (btnReiniciarProductos != null) btnReiniciarProductos.setDisable(false);
    }

    /**
     * Inicia simultáneamente ambos procesos (Clientes y Productos).
     */
    @FXML
    private void iniciarAmbos() {
        iniciarClientes();
        iniciarProductos();
    }

    /**
     * Detiene simultáneamente ambos procesos.
     */
    @FXML
    private void detenerAmbos() {
        detenerClientes();
        detenerProductos();
    }

    /**
     * Reinicia simultáneamente ambos procesos si no están activos.
     */
    @FXML
    private void reiniciarAmbos() {
        reiniciarClientes();
        reiniciarProductos();
    }

    // =========================================================================
    // CIERRE SEGURO DE LA APLICACIÓN (Ciclo de Vida de los Hilos)
    // =========================================================================

    /**
     * Detiene todos los hilos en ejecución cuando se cierra la ventana principal.
     * Garantiza el cumplimiento del indicador IL3.3.
     */
    public void detenerAlCerrar() {
        System.out.println("[INFO] Cerrando ventana y deteniendo hilos activos...");

        if (hiloProceso != null && hiloProceso.isAlive()) {
            hiloProceso.interrupt();
        }
        if (hiloClientes != null && hiloClientes.isAlive()) {
            hiloClientes.interrupt();
        }
        if (hiloProductos != null && hiloProductos.isAlive()) {
            hiloProductos.interrupt();
        }
    }
}
