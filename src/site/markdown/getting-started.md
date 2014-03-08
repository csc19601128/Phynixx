
Getting Started
================


Das Projekt <i>Phynixx</i> bietet eine einfache Möglichkeit für Ressourcen an Transaktionen teilnehmen zu nehmen, seien es lokale oder globale (XA-protokoll, 2PC-) Transaktionen.
Stellen Sie sich z.B. jede Form von Ressource vor, deren Zustand sich an dem Ausgang einer Datenbanktransaktion orientiert.
# Dateioperationen
# Operationen an Filesysteme
# Operationen an Archivsystemen ( welche keine eigenes Transaktionshandling anbieten; z.B. Centera von EMC).
# Implementierung des Command-Pattern nach GoF
## dies enthält eine Form von <i>rollback</i> durch <i>undo</i>.

All diese Beispiele stellen transaktionale Ressourcen dar. Sie müssen an lokalen Transaktionen (Transaktionen,.welche sich nur auf diese Ressource beziehen) oder globalen Transaktionen ( 2-Phase oder auch XA-Transaktionen) teilnehmen teilnehmen können.

Ein solle Ressource muss einen Minimum an Funktionalität bereitsstellen und Phynixx ermöglich ihr die Teilnahme an beiden Transaktionsprotokollen.

Im Tutorial (siehe [Tutorial](tutorial.html)) wird gezeigt, wie das sequentielle Schreiben in eine Datei transaktional mit Phynixx unterstützt wird.

Viel Spaß damit.
