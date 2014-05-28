
Getting Started
================


Das Projekt <i>Phynixx</i> bietet eine einfache Möglichkeit für Ressourcen an Transaktionen teilnehmen zu nehmen, seien es lokale oder globale (XA-protokoll, 2PC-) Transaktionen.
Stellen Sie sich z.B. jede Form von Ressource vor, deren Zustand sich an dem Ausgang einer Datenbanktransaktion orientiert.
+ Dateioperationen
+ Operationen an Filesysteme
+ Operationen an Archivsystemen ( welche keine eigenes Transaktionshandling anbieten; z.B. Centera von EMC).
+ Implementierung des Command-Pattern nach GoF. Dies enthält eine Form von _rollback_ durch _undo_.

All diese Beispiele stellen transaktionale Ressourcen dar. Sie müssen an lokalen Transaktionen (Transaktionen,.welche sich nur auf diese Ressource beziehen) oder globalen Transaktionen ( 2-PhaseCommit oder auch XA-Transaktionen) teilnehmen teilnehmen können.

Ein solche Ressource muss einen Minimum an Funktionalität bereitsstellen und Phynixx ermöglich ihr die Teilnahme an beiden Transaktionsprotokollen.

Im Tutorial (siehe [Tutorial](tutorial.html)) wird gezeigt, wie das sequentielle Schreiben in eine Datei transaktional mit Phynixx unterstützt wird.

### Was trägt Phynixx bei, um transaktionales Verhalten zu ermöglichen?


 &nbsp;
## Wiederherstellungsinformationen
 
Jede transaktionalen Ressource, welche Informationen ausserhalb der JVM persistiert (Datenbank, Archivsysteme, Dateien, Dateisysteme, ...),  muss sicherstellen, dass sie schlussendlich konsistent ist. Konsistenz muss auch erreicht werden, wenn während der Transaktion das Filesystem, das Netzwerk oder die JVM wegbricht.  
Dazu unterstützt Phynixx transaktionale Ressource,  während der Transaktion zu Informationen sichern, die ihreren konsistenz Zustand beschreiben.
Falls die Transaktion nicht korrekt abgeschlossen wird, so kann die transaktionale Ressource auf Basis dieser Wiederherstellungsinformationen auf einen konsistenten Stand gebracht werden (Recovery Prozess).

Das Verfahren, Wiederherstellungsinformationen zu sichern, muss selbst einige Vorrausetzungen erfüllen. In erster Linie müssen Wiederherstellungsinformationen dauerhaft und atomar gesichert werden. Entweder werden sie verlässlich und vollständig gesichert oder gar nicht. Es muss zu jedem Zeitpunkt offensichtlich sein, welche Wiederherstellungsinormationen gesichert sind.

## lokale Transaktionen
Um an lokalen Transaktionen teilzunehmen, muss nur eine `commit-` und `rollback-`Methode bereitgestellt werden. 

Darüberhinaus steuert Phynixx Aspekte bei, welche ihrer transaktionalen Ressource _out of the box_ zu Gute kommen:

- Bereitstellung eines Persistenzmechanismus für Wiederherstellungsinformtionen
- Teilnahme am Recoveryverfahren
- Unterstützung von _autocommit_
- Unterstützung von _ConnectionPooling_
- Integration in _spring transaktion management_

## 2 PhaseCommit Protokoll
Das 2 PhaseCommit Protokoll ist unumgänglich, wenn Sie ihre transaktionale Ressource gemeinsam mit andern Ressourcen (i.d.R. eine releationale datenbank) in einer Transaktion nutzen wollen. Ein 2PC-fähiger Transaktionsmanager steuert die Transaktion und die Abhängigkeit der transaktionalen Ressourcen untereinander. Für Java ist das 2PC-Protokoll in [JTA] definiert. 

Die Kommunitaktion mit 2PC-fähigen Transaktionsmanagern ist komplex. _Phynixx_ erledigt die gesamte Kommunikation für Sie und macht ihre transaktionale Ressource ohne zusätzliche Programmierung zu einer 2PC-fähigen transaktionalen Ressource. 


&nbsp;

# Viel Spaß mit Phynixx.
