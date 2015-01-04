# Varianten der Transaktionsverarbeitung

## Last Resource Gambit
In vielen Fällen kann man sich mit dem so genannten mit dem Verfahren helfen, das in the Last Resource Gambit bezeichnet wird. Die Transaktionssteuerung weiß um die einzelnen Charakteristika der Persistenzsysteme und stellt ihre Verarbeitungslogik darauf ein.
 
Ist z.B. nur ein nicht XA-kompatible Persistenzmedium an der Transaktion beteiltigt, so wird nachdem prepare an alle anderen beteiligten Resourcen das commit der Nicht-kompatiblen gerufen und anschließend das commit der XA-Resourcen.
Dieses Verfahren löst zwar auch nicht das oben beschrieben Problem der irreversiblen Aktionen, die während des *prepare phase* fehlschlagen, aber dasjenige des rollbacks  nach erfolgreicher prepare-Phase.
 
## *Logging in presumed abort Algorithmus*
Der presumed abort - Algorithmus ist in  [gupta-1997] definiert.
> „. . . , the 2PC protocol requires transmission of several messages and force-writing of several log records. A variant of the 2PC protocol, called presumed abort (PA), attempts to reduce these overheads by requiring all cohorts to follow a "in the no information case, abort" rule.“
 
*presumed abort* bedeutet als Konsequenz, dass der Transaktionmanager die Transaktion nur vor und während der eigentlichen *committing phase *( prepare,commit ) protokollieren muss. Insbesondere müssen weder *rollback data* noch Informationen vor der *committing phase * protokolliert werden. Es werden ebenfalls keine Informationen für Transaktionen  geloggt, die zu 1PC optimiert wurden.

Das Verfahren *presumed abort* ist das gängige Verfahren, nach dem Transaktionsmanager den Stand der aktuellen Transaktionen protokollieren.
 
Dieses Verfahren setzt im Falle eines Abbruchs voraus, dass alle eingesetzten ResourceManager in der Lage sind, ihre Resourcen zu restaurieren (recover). Der TM übernimmt nur eine Koordination der abgebrochenen Transaktion, wenn diese während der committing phase ( 2PC mit prepare/commit) abgebrochen ist.
 
Ein Logging-Verfahren, dass diese Eigenschaft unterstützt, hat im wesentliche folgende Funktionalität.
1. Bevor ein Commit gestartet wird, werden alle beteiligten XAResourcen geloggt, so dass diese beim Wiederanlauf referenzierbar sind.
2. Es werden keinerlei inhaltliche Informationen über den XAResourcen durch den Transaktionmanager geloggt. Diese ist Aufgabe des Resourcemanagers.
3. Falls die *committing phase* erfolgreich abgeschlossen wird, so sind die Recoveryinformationen (siehe 1) unnötigt und könnten gelöscht werden
4. Falls der Transaktionmanager während der *commiting phase* abstürtzt, so kann auf Basis der *rollforward data* die Situation zum Zeitpunkt das Abbruchs wiederhergestellt werden. Dies allerdings nur aus Sicht des Transaktionmanagers (siehe unten).
5. XAResourcen müssen ihre Transaktion-Informationen, die zur Integration in die Transaktion notwendigen Informationen wie XID) erst ab der prepare-Phase loggen. Zuvor allerdings müssen evtl. Informationen für recovery/rollback geloggt werden.
6. Wiederherstellung einer XAResource umfasst auch die Informationen, um die Referenz des TM auf diese XAResource (siehe 1) aufzulösen. Dazu ist in der Regel zu untersuchen, wie der gewählte TM die XAResource referenziert (z.B. via toString() wie JOTM).
7. *XAResource.recover()* liefert alle wiederherstellbaren XAResources des RM. Dabei können diejenigen, die nicht an wiederherstellbaren Transaktion beteiligt sind, ignoriert werden.
 
Ein Logging-Verfahren, dass diese Eigenschaft unterstützt, hat im wesentliche folgende Funktionalität.

1. performante, konkurrierende Protokollierung (selbstverständlich)
2. dauerhafte und atomare Protokollierung
3. Bündelung der Records der *committing phase* , nur ein I/O-Zugriff pro Start einer *committing phase*
4. Ignorierung/Löschen/Überscheiben von Logrecords, die zu erfolgreich abgeschlossenene Transaktionen  gehören. Diese sollen im Log keinen Platz blockieren (garbage collection für LogRecords).

Ein hinsichtlich dieser Anforderungen optimiertes Loggingsystem ist offensichtlich nicht optimiert,
recovery/rollback -Informationen für eine XAResource zu unterstützen. Dort muss die Bündelung nicht auf Basis der Records der Committing Phase sondern bzgl. aller während einer Transaktion angefallenen Daten geschehen. Ist die Transaktion abgeschlossen, so sind diese gebündelten Records freizugeben.

### recovery in *presumed abort*
Falls das Persistenzsystem an einer globale Transaktion beteiligt war und die Beteiligung wieder reaktiviert werden muss (nur Zustand *prepared*), so hat sich das restaurierte Persistenzsystem beim Transaktionsmanager für die entsprechende globale Transaktion anzumelden.

Für den Transaktionsmanager ist eine XAResource nur dann beim Recovery interessant, wenn diese an einer Transaktion teilgenommen hat, deren *prepared/committing phase* wiederhergestellt wird.
Ansonsten weiß der Transaktionmanager (siehe* presumed abort * weiter unten) nichts über die XAResource und diese kann sich isoliert wiederherstellen und ein *rollback* durchführen.

Falls diese während der *executing phase* stecken geblieben ist, so hat der Recoverymechanismus des Resourcemanagers für ein korrekte Wiederherstellung des Persistenzsystems zu sorgen.

Ist sie dagegen während der *preparing phase* abgebrochen, ist zu erwarten, dass die XAResource untersucht, ob es eine wiederhergestellte, globale Transaktion gibt, an der sie teilnehmen kann.
Die XAResource wird dieser übergeben, allerdings im Zustand MARK_ROLLBACK. Dadurch ist gewährleistet, dass alle an der Transaktion beteiligten XAResourcen rollbacked werden.

Da allerdings bei Transaktionmanagern die den Recoveryprozess gemäß *presumed abort* unterstützen, globale Transaktionen, welche in der *preparing phase* abbrechen, nicht als wiederherstellbar protokollieren, ist es nicht notwendig, die aufwendige Sonderbehandlung für die Fall vorzusehen.
Auch in diesem Fall kann also der Recoverymanager des XAResource ein *rollback* durchführen.
Insbesondere wird der Zustand *preparing* nicht protokolliert.

Um all die Zustandsinformationen einer Transaktion zuordnen zu können, muss beim Beginn der Beteiligung der XAResource an der Transaktion (Eintritt in executing ) protokolliert werden, mit welcher Identität die XAResource an welcher Transaktion ( identifiziert durch XID) teilnimmt. Dieser Transaktion-Informationen müssen auch allen weiteren LogRecords zugeordnet sein, die sich auf diese XAResource innerhalb dieser Transaktion beziehen.
Stößt der Recoverymanager auf eine XAResource eines solchen Zustands, so zeigt folgende Tabelle, wie zu reagieren ist:

<table>
<tr><td>erster Satz bei XAResource (nur bei XAResource bei Beteiligung an 2PC)</td><td>Um die XAResource der Transaktion zuzuordnen, sind aus dem erstem LogRecord sowohl Identität der XAResource (zur Registrierung beim Transaktionsmanager) und XID zu ermitteln.</td></tr>
<tr><td>Prepared</td><td>Da XAResource für den Transaktionsmanager in Bezug auf Recovery nur nur im Zustand prepared / committing interessant sind, muss sich die XAResource an den Transaktion der wiederhergestellten globalen Transaktion beteiligen , um den TransaKtionsmanager die Transaktion geordnet abzuschliessen.</td></tr>
<tr><td>committing</td><td>Nimmt die XAResource am 2PC teil, so siehe prepared . Ansonsten wird das commit vollständig zu Ende geführt.</td></tr>
<tr><td>keiner der obigen Zustände</td><td>XAResource führt abschliessende Arbeiten aus, um dass rollback/abort abzuschliessen. Es ist keine globale Transaktion notwendig, innerhalb derer die XAResource rollbacked werden muss.</td></tr>
</table>
