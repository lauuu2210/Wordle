# Wordle

Proyecto propuesto en la asignatura de Concurrencia y Distribución.

Se trata de una implementación del famoso juego online wordle, el cual funciona con la conexión RMI para conectar cliente-servidor.
El servidor genera una palabra cada cierto tiempo, común a todos los usuarios activos. Cuando este tiempo cambie, la palabra en juego cambia. 
      - Si algún usuario se encuentra jugando la palabra del día anterior, este continuará con la misma.
      - Los usuarios solo podran jugar una palabra al día, como en Wordle original.
      - El servidor es el encargado de realizar todos los controles e intentos.
      
AVISO: Los colores implementados para hacerlo más parecido al original unicamente funcionan ejecutandolos en una terminal Linux o en la terminal de Visual Studio 
o similares.

Realizado en colaboracion con dolimpio.
