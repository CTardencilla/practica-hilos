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

    // =========================================================================
    // Componentes FXML - Reto Final: Monitor Empresarial Concurrente (Paso 11)
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

    // =========================================================================
    // MÉTODOS - PRÁCTICA BASE (Pasos 4 y 5)
    // =========================================================================

    /**
     * Inicia el procesamiento en segundo plano utilizando Thread y Runnable.
     */
    @FXML
    private void iniciarProceso() {
        // Evitar iniciar si ya se está ejecutando
        if (hiloProceso != null && hiloProceso.isAlive()) {
            return;
        }

        barraProgreso.setProgress(0);
        lblPorcentaje.setText("0%");
        lblEstado.setText("Procesando registros...");
        btnIniciar.setDisable(true);
        btnDetener.setDisable(false);
        btnReiniciar.setDisable(true);

        Runnable tarea = () -> {
            try {
                for (int i = 1; i <= 100; i++) {
                    // Verificación de interrupción cooperativa
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }

                    // Simulación del tiempo de procesamiento por registro
                    Thread.sleep(100);

                    final int avance = i;
                    // Actualización segura de la interfaz JavaFX
                    Platform.runLater(() -> {
                        barraProgreso.setProgress(avance / 100.0);
                        lblPorcentaje.setText(avance + "%");
                    });
                }

                // Notificación de finalización exitosa
                Platform.runLater(() -> {
                    lblEstado.setText("Proceso finalizado");
                    habilitarBotones();
                });

            } catch (InterruptedException e) {
                // Restablecer el estado de interrupción del hilo
                Thread.currentThread().interrupt();

                // Notificación visual de detención
                Platform.runLater(() -> {
                    lblEstado.setText("Proceso detenido");
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
        if (hiloProceso != null && hiloProceso.isAlive()) {
            btnDetener.setDisable(true);
            lblEstado.setText("Solicitando detención...");
            hiloProceso.interrupt();
        }
    }

    /**
     * Reinicia los controles al estado inicial.
     */
    @FXML
    private void reiniciarProceso() {
        if (hiloProceso != null && hiloProceso.isAlive()) {
            return;
        }
        barraProgreso.setProgress(0);
        lblPorcentaje.setText("0%");
        lblEstado.setText("Esperando inicio");
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
    // RETO FINAL: PROCESOS CONCURRENTES (Paso 11)
    // Procesamiento concurrente de Clientes (100) y Productos (50)
    // =========================================================================

    /**
     * Inicia el hilo para procesamiento de clientes (100 registros).
     */
    @FXML
    private void iniciarClientes() {
        if (hiloClientes != null && hiloClientes.isAlive()) {
            return;
        }

        barraProgresoClientes.setProgress(0);
        lblPorcentajeClientes.setText("0%");
        lblEstadoClientes.setText("Procesando " + TOTAL_CLIENTES + " clientes...");
        btnIniciarClientes.setDisable(true);
        btnDetenerClientes.setDisable(false);
        btnReiniciarClientes.setDisable(true);

        Runnable tareaClientes = () -> {
            try {
                for (int i = 1; i <= TOTAL_CLIENTES; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }

                    Thread.sleep(TIEMPO_CLIENTE_MS);

                    final int avance = i;
                    final double porcentaje = (double) avance / TOTAL_CLIENTES;

                    Platform.runLater(() -> {
                        barraProgresoClientes.setProgress(porcentaje);
                        lblPorcentajeClientes.setText(String.format("%d%% (%d/%d)", 
                                Math.round(porcentaje * 100), avance, TOTAL_CLIENTES));
                    });
                }

                Platform.runLater(() -> {
                    lblEstadoClientes.setText("¡Clientes procesados con éxito!");
                    btnIniciarClientes.setDisable(false);
                    btnDetenerClientes.setDisable(true);
                    btnReiniciarClientes.setDisable(false);
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    lblEstadoClientes.setText("Procesamiento de clientes detenido");
                    btnIniciarClientes.setDisable(false);
                    btnDetenerClientes.setDisable(true);
                    btnReiniciarClientes.setDisable(false);
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
            btnDetenerClientes.setDisable(true);
            lblEstadoClientes.setText("Solicitando detención de clientes...");
            hiloClientes.interrupt();
        }
    }

    /**
     * Reinicia los controles de clientes al estado inicial.
     */
    @FXML
    private void reiniciarClientes() {
        if (hiloClientes != null && hiloClientes.isAlive()) {
            return;
        }
        barraProgresoClientes.setProgress(0);
        lblPorcentajeClientes.setText("0%");
        lblEstadoClientes.setText("En espera");
        btnIniciarClientes.setDisable(false);
        btnDetenerClientes.setDisable(true);
        btnReiniciarClientes.setDisable(false);
    }

    /**
     * Inicia el hilo para procesamiento de productos (50 registros).
     */
    @FXML
    private void iniciarProductos() {
        if (hiloProductos != null && hiloProductos.isAlive()) {
            return;
        }

        barraProgresoProductos.setProgress(0);
        lblPorcentajeProductos.setText("0%");
        lblEstadoProductos.setText("Procesando " + TOTAL_PRODUCTOS + " productos...");
        btnIniciarProductos.setDisable(true);
        btnDetenerProductos.setDisable(false);
        btnReiniciarProductos.setDisable(true);

        Runnable tareaProductos = () -> {
            try {
                for (int i = 1; i <= TOTAL_PRODUCTOS; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }

                    Thread.sleep(TIEMPO_PRODUCTO_MS);

                    final int avance = i;
                    final double porcentaje = (double) avance / TOTAL_PRODUCTOS;

                    Platform.runLater(() -> {
                        barraProgresoProductos.setProgress(porcentaje);
                        lblPorcentajeProductos.setText(String.format("%d%% (%d/%d)", 
                                Math.round(porcentaje * 100), avance, TOTAL_PRODUCTOS));
                    });
                }

                Platform.runLater(() -> {
                    lblEstadoProductos.setText("¡Productos procesados con éxito!");
                    btnIniciarProductos.setDisable(false);
                    btnDetenerProductos.setDisable(true);
                    btnReiniciarProductos.setDisable(false);
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    lblEstadoProductos.setText("Procesamiento de productos detenido");
                    btnIniciarProductos.setDisable(false);
                    btnDetenerProductos.setDisable(true);
                    btnReiniciarProductos.setDisable(false);
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
            btnDetenerProductos.setDisable(true);
            lblEstadoProductos.setText("Solicitando detención de productos...");
            hiloProductos.interrupt();
        }
    }

    /**
     * Reinicia los controles de productos al estado inicial.
     */
    @FXML
    private void reiniciarProductos() {
        if (hiloProductos != null && hiloProductos.isAlive()) {
            return;
        }
        barraProgresoProductos.setProgress(0);
        lblPorcentajeProductos.setText("0%");
        lblEstadoProductos.setText("En espera");
        btnIniciarProductos.setDisable(false);
        btnDetenerProductos.setDisable(true);
        btnReiniciarProductos.setDisable(false);
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
