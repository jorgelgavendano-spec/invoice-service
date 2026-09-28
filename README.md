# invoice-service
Servicio para timbrado de CFDIs 

API que recibe solicitudes para timbrar una factura, las manda a un PAC simulado (que a veces tarda o falla) y evita timbrar dos veces la misma factura si la solicitud se reenvía.


Cómo correrlo

Necesitas Java 21, Maven y PostgreSQL. RabbitMQ solo hace falta para /api/timbradosCola.

Crea una base de datos en PostgreSQL y ajusta src/main/resources/application.yml con tu URL, usuario y contraseña (y los datos de RabbitMQ si lo usas).
Las tablas las crea Hibernate al arrancar. Para tener datos de prueba corre insert_facturas.sql (30 facturas sin timbrar, ids 1 al 30).

El proyecto se arranca en bash con el comando
mvn spring-boot:run

para las pruebas es mvn test.

La ruta donde se levanta es http://localhost:8080.


Endpoint principal
POST /api/timbrados

json para la peticion
{ "external_id": "req-abc-123", "invoice_id": 1, "amount": 1500.50 }

Caso	HTTP
Primer intento, el PAC responde bien 201 con status, external_id y uuid_fiscal
Mismo external_id reenviado	200 con el mismo resultado de la primera vez
Primer intento, el PAC falla 422 con status: "fallida" y el error
invoice_id que no existe 404 con mensaje
Payload inválido (falta un campo, amount no numérico, etc.)	400 con mensaje
La factura ya está timbrada (o en proceso) con otro external_id	409
La factura se está timbrando en este momento (menos de 5 s)	202

También hay POST /api/timbradosCola, que hace lo mismo que el timbrados pero de forma asíncrona: marca la factura como "proceso", manda el mensaje a RabbitMQ y responde 202. Un worker consume el mensaje, llama al PAC y guarda el resultado. Lo hice como extra.

Cómo se evita el doble timbrado
Mismo external_id: si ya existe un timbrado exitoso, regreso el resultado guardado y no vuelvo a llamar al PAC.
Otro external_id para la misma factura: si ya está timbrada o en proceso, respondo 409. El external_id identifica el intento, pero lo que no se debe duplicar es la factura.
Si el intento anterior falló, dejo reintentar (con el mismo o con otro external_id), porque una falla del PAC suele ser temporal.
Estado "proceso": antes de llamar al PAC marco la factura como "proceso" con la fecha. Si llega otro request en menos de 5 segundos, respondo 202 en vez de timbrar de nuevo. Pasados 5 segundos lo tomo como un intento caído y permito reintentar.
La columna external_id es única en la base de datos.


PAC simulado
FakePacApi es una clase en memoria, sin llamadas HTTP. A veces espera 3 segundos y falla más o menos 1 de cada 3 veces (con timeout o un error de datos). Las fallas responden 422. Lo inyecto por constructor para poder mockearlo en las pruebas.

Reportes
GET /api/reportes/facturacion-por-cliente: total facturado y total pagado por cliente, incluyendo los que no tienen pagos.
GET /api/reportes/facturas-vencidas-con-saldo: facturas vencidas cuyo total menos la suma de pagos es mayor a cero.


Pruebas

28 pruebas con JUnit 5 y Mockito, sin necesidad de base de datos ni RabbitMQ:

TimbradoServiceImplTest: lógica del servicio (404, 201, 422, duplicado, 409, en proceso, reintentos).
TimbradoWorkerTest: el consumidor de la cola.
TimbradoControllerTest: códigos HTTP y validaciones (400) con MockMvc.

Pregunta de diseño

Si el job que timbra una factura se ejecuta dos veces por accidente (por ejemplo, un reinicio del worker a mitad del proceso), ¿cómo evitarías que se timbre dos veces?

El mejor metodo a mano por asi decirlo, para evitar doble timbrado, es que en cuanto llega el proceso de timbrado si la factura existe se le actualiza un campo llamado bloqueo, esto con un 1 para ver que esta encendido un bloque ,
posteriormente se manda validar todo lo correspondiente de la factura y se pone en proceso de timbrado, al terminar de validar y mandar timbrar la factura a PAC, se actualiza el bloqueo a 0(apagado), y se actualiza el campo estatus.
Esto evitaria que si entran dos peticiones una tras otra se manden a timbrar a mbas, ya que en cuanto llega se mete un bloqueo paara no modificar.

Algo mas que se le debe agregar, esla configuracion de un limite de tiempo para este bloqueo, que si se vuelve a mandar la factura y el campo proceso tiene mas de 5min que se actulizo, fecha que ya se inserta, se de por cancelada la factura para que no quede eternamente en proceso.