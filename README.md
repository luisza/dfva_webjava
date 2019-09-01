# dfva_webjava
Este proyecto es una demostración de como integrar dfva_java en una aplicación web, permite demostrar cómo se realiza la firma.

Está desarrollado con Maven + Hibernate + dfva_java , pero no conecta con una base de datos.

# Instalación 

    Próximamente
    
# Archivos de interés 

Debido a que este es un proyecto de demostración y varios archivos no son necesarios para entender el procedimiento se describen cuales 
debe verificar

- `src/dfva_webjava/java/demo/rest/ControlaFirma.java`  las funciones importantes son:
```
    public ResponseEntity<DfvaRespuesta> firme(..)   // Busca el archivo en disco y lo envía a firmar
    public String check_firma(..)  // verifica el estado de la transacción
```

- `src/dfva_webjava/java/demo/rest/StartController.java` las funciones importantes son:
```
    public String createsign(...)   // Muestra el botón de firma y la ventana de proceso
    public HttpEntity<byte[]>  download(...) // al finalizar descarga el documento firmado
```

-   `src/dfva_webjava/java/demo/DfvaRespuesta.java`  las funciones importantes son:
```
    public class DfvaRespuesta implements Serializable 
    // Esta clase permite serializar la respuesta que se le entrega a dfva_html.
```

# Aspectos importantes 

Este proyecto solo pretende mostrar cómo usar dfva_java, por lo que no detalla algunas particulares de seguridad, por ejemplo es recomendable
guardar variables de control en la sesión para prevenir que entes externos puedan consultar las transacciones.
Además la información se almacena en disco y no en base de datos.

