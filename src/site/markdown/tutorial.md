Tutorial
=========

SourceCode findet sich im Module <i>phynixx-tutorial</i>. 


Das Projekt Phynixx bietet eine einfache Möglichkeit für Ressourcen an Transaktionen teilnehmen zu nehmen, seien es lokale oder globale (XA-protokoll, 2PC-) Transaktionen. 
Um eine Vorstellung vom Programmiermodell zu erhalten, stellen Sie sich einer herkömmliche Datenbankverbindung <code>javax.sql.Connection</code> vor. Diese Verbindung ist transaktional und kann, je nach zu grundeliegender DatenSource, auch an XA-Transaktionen teilnehmen.  
Neben der _Connection_ ist die _DateSource_ in ihrer Rolle als _ConnectionFactory_ wichtig.  

Auch im Programmiermodell von Phynixx wird ihre transaktionale Ressource als Connection bezeichnet, ebenso existiert eine ConnectionFactory um die Connections zu erzeugen. 

##  Fahrplan des Tutorials ##
In diesem Tutorial wird beschrieben, wie welche Voraussetzungen geschaffen werden, dass eine Ressource am Transaktionsprotokoll teilnehmen kann. Als Beispiel dient sequemtielles Schreiben in eine Datei. Dies soll transaktional unterstützt werden und solwohl in an lokalen als auch globalen Transaktionen teilnehmen können. 

## Beispiel ##
In eine Datei kann sequentiell geschrieben werden. Diese Schreiboperationen sollen transaktional unterstützt werden, so dass auch eine Rollback möglich ist.
Dazu wird der Bereich des gültigen Inhalts durch eine Positionsangabe relativ zum Dateianfang gegeben. Mittels dieser Positionsangabe wird ein eventuelles Rollback implementiert, denn mittels der Dateiposition kann der Inhalt der Datei relativ zu dieser Position wiederhergestellt werden.  

Aus der Implementierung ergibt sich, dass nur sequentiell in die Datei geschrieben werden kann.

Im Rahmen einer Transaktion soll bei einem Rollback der Stand der Datei zu Beginn der Transaktion wiederhergestellt werden. Die Anfangsdateiposition wird aus der aktuellen Länge der Datei ermittelt. 

Folgender Testfall beschreibt die Arbeit mit dieser Klasse <code>org.csc.phynixx.tutorial.TAEnabledUTFWriter</code>

    package org.csc.phynixx.tutorial;
     . . . 
    public void testTAEnabledUTFWriter() throws Exception {
   
      File file = this.tmpDir.assertExitsFile("my_test.tmp");
      
      TAEnabledUTFWriter writer = TAEnabledUTFWriter.createWriter(file);
      try {
          // schreibe zwei String in die Datei
          writer.write("AA").write("BB");
      } finally {
         // schliesst Datei, laesst aber die Inhalt bestehen.
         writer.close();
       }

       // Liest den Inhalt der Datei wieder ein
       writer = TAEnabledUTFWriter.recoverWriter(file);
       try {
         List<String> content = writer.getContent();
         Assert.assertEquals(2, content.size());
         Assert.assertEquals("AA", content.get(0));
         Assert.assertEquals("BB", content.get(1));
       } finally {
           writer.close();
      }      
    }
*Listing 1 :* Arbeit mit der Ressource <code>TAEnabledUTFWriter</code>
    
## lokale Transaktionen ##

Diese Funktionalität soll transaktional unterstützt werden. Dazu muss sie im ersten Schritt das Interface <code>org.csc.phynixx.connection.IPhynixxConnection</code> unterstützen. Dort wird geregelt, wie die transaktionale Ressource (in unserem Fall <i>TAEnabledUTFWriter</i>) auf die unterschiedlichen Situationen innerhalb einer Transaktionn reagieren soll.

<table>
<tr><th>Methode</th><th>Beschreibung</th></tr> 
<tr><td>rollback</td><td>Die Datei soll auf den initialen Inhalt zurückgesetzt werden</td></tr>
<tr><td>commit</td><td>Nichts geschieht, da Datei fortlaufend beschrieben wird und damit im Gutfall konsistent ist</td></tr>
<tr><td>reset</td><td>Connection wird neu genutzt und der bisherige Zustand wird verworfen</td></tr>
<tr><td>close</td><td>Connection wird nicht weiter genutzt und freigesetzt</td></tr>
</table>
*Tabelle 1 :*Implementierungen des Interfaces `IPhynixxConnection`
<nbsp;>
Die Funktionalität zum seqnetiellen Schreiben finden sich in <code>TAEnabledUTFWriter</code>, eine Subklasse von <code>IPhynixxConnection</code>.
Dort werden folgende Methoden implementiert
<table>
<tr><th>Methode</th><th>Beschreibung</th></tr> 
<tr><td>open</td><td>Öffnet eine Datei zum schreiben. Es wird die aktuelle Position der Datei als 'rollback'-Information gesichert</td></tr>
<tr><td>resetContent</td><td>Inhalt der Datei wird verworfen. </td></tr>
<tr><td>readContent</td><td>List den Inhalt aus Datei</td></tr>
<tr><td>write</td><td>Es wird ein String in die Datei geschrieben.</td></tr>
</table>
&nbsp;

Die Methoden _open, resetContent, write_ verändern den Zustand der Ressource und müssen daher an einer Transaktion teilnehmen, damit diese korrekt funktionieren. Dies wird durch die Annotation `@RequiresTransaction ` angezeigt.

Um sicherzustellen, dass die Datei auch bei unvorhergesehenem Abbruch der Transaktion wiederherzustellen ist, wird ein persistenter _XADataRecorder_ angefordert. Mittels diesem können Wiederherstellungsinformation gesichert werden.
Dazu muss das Interface _import org.csc.phynixx.connection.IXADataRecorderAware_ implementiert werden. Sobald eine Transaktion geöffnet wird, so wird der eine XARecoder via <code>setXADataRecorder(IXADataRecorder xaDataRecorder)</code> injeziert.

Es ist sichergestellt, dass der IXADataRecorder injeziert wird, unmittelbar bevor die erste Methode aufgerufen wird, welche durch _@RequireTransaction_ annotiert ist. Die Injezierung wird durch den Aufruf dieser Methode ausgelöst.

Das Zusammenspiel innerhalb einer (lokalen) Transaktion ist in der Testklasse <code>TransactionalBehaviourTest</code> zu beobachten.

    @Test
    public void testCommit() throws Exception {

        File file = this.tmpDir.assertExitsFile("my_test.tmp");

        TAEnabledUTFWriter connection = this.connectionFactory.getConnection();
        connection.open(file);
        try {
            connection.write("AA").write("BB");
            connection.commit();
        } finally {
            connection.close();
        }

        TAEnabledUTFWriterImpl recoverWriter = new TAEnabledUTFWriterImpl();
        try {
            recoverWriter.open(file);
            List<String> content = recoverWriter.getContent();
            Assert.assertEquals(2, content.size());
            Assert.assertEquals("AA", content.get(0));
            Assert.assertEquals("BB", content.get(1));

        } finally {
            recoverWriter.close();
        }
    }
*Listing 2 :* Beipiel die Einbindung der Ressource <code>TAEnabledUTFWriter</code> in eine lokale Transaktion

Interessant ist insbesondere das Setup des Tests

    private PhynixxManagedConnectionFactory<TAEnabledUTFWriter> connectionFactory = null;
    
    @Before
    public void setUp() throws Exception {
        // configuring the log-system (e.g. log4j)
        TestUtils.configureLogging();
        this.tmpDir = new TmpDirectory("test");
        
        this.connectionFactory =
                new PhynixxManagedConnectionFactory<TAEnabledUTFWriter>(
                                new TAEnabledUTFWriterFactoryImpl());
        IDataLoggerFactory loggerFactory = new FileChannelDataLoggerFactory(
                                 "ta_enabled", this.tmpDir.getDirectory());
        IPhynixxLoggerSystemStrategy<TAEnabledUTFWriter> strategy = 
                new LoggerPerTransactionStrategy<TAEnabledUTFWriter>(loggerFactory);
        connectionFactory.setLoggerSystemStrategy(strategy);
    }
*Listing 3 :* Setup der Testumgebung

* Die ConnectionFactory `TAEnabledUTFWriterFactoryImpl` liefert die Connections der Ressource <code>TAEnabledUTFWriter</code>. Sie wird zusammen mit `TAEnabledUTFWriterFactory` bereitgestellt.
* Um einen persistenten <code>XADataRecorder</code> injezieren zu können, muss eine eine Persistenzverfahren an die Factory übergeben werden, welches die Wiederherstellungsinformationen sichert. In dieser Strategie wird festgelegt, auf welche Weise die Wiederherstellungsinformationen gesichert werden. Die Strategie <code>LoggerPerTransactionStrategy</code> erzeugt DataRecoder im Filesystem.
* Um einfach temporäre Dateien erzeugen zu können, bietet die Klasse `TmpDirectory` ein Interface, temporäre Datei und Verzeichnisse  zu erzeugen
    

    @Test
    public void testRollback() throws Exception {

        File file = this.tmpDir.assertExitsFile("my_test.tmp");

        TAEnabledUTFWriter connection1 = this.connectionFactory.getConnection();
        try {
            connection1.open(file);
            connection1.write("AA").write("BB");            
            connection1.commit();
        } finally {
            connection1.close();
        }

        TAEnabledUTFWriter connection2 = this.connectionFactory.getConnection();
        try {
            connection2.open(file);
            connection2.write("CC").write("DD");
            connection2.rollback();
        } finally {
            connection2.close();
        }

        TAEnabledUTFWriter recoverWriter = this.connectionFactory.getConnection();
        recoverWriter.open(file);
        try {
            List<String> content = recoverWriter.getContent();
            Assert.assertEquals(2, content.size());
            Assert.assertEquals("AA", content.get(0));
            Assert.assertEquals("BB", content.get(1));

        } finally {
            recoverWriter.close();
        }
    }
*Listing 4 :* Beipiel die Einbindung der Ressource <code>TAEnabledUTFWriter</code> in eine lokale Transaktion mit Rollback

## _managed Connection_
Eine Connection in ihrer reinen Form gegeben durch die Implementierung <code>TAEnabledUTFWriterImpl</code> reicht nicht aus, um innerhalb einer Transaktion zu agieren. 

Ein solche Connection muß zu einer _managed connection_ erweitert werden. Diese Erweiterung steuert viele Aspekte bei, welche eine _connection_ erst zu einer transaktionalen Ressource machen.

Um diese Aspekte zu erhalten, muss eine `PhynixxManagedConnectionFactory` eingesetzt werden. Sie veredelt eine normale Connection zu einer _managed connection_ und damit zu einer transaktionalen Ressource. 

    this.connectionFactory =
                new PhynixxManagedConnectionFactory<TAEnabledUTFWriter>(new TAEnabledUTFWriterFactoryImpl());
*Abbildung 4* : Beispiel einer PhynixxManagedConnectionFactory

Connection, welche mit dieser Factory erzeugt worden sind, sind voll funktiontionsfähige transaktionale Ressourcen und können an lokalen Transaktionen teilnehmen.  Jede _managed connection_ besitzt eine assoziierte _connection_; bei uns vom Typ `TAEnabledUTFWriter`. Bei Bedarf wendet sich die _managed connection_ an diese.  

### Workflow einer _managed connection_
Der Workflow einer  _managed connection_ innerhalb einer Transaktion kann mittels eines Listeners (siehe _Observer Pattern_ [GoF] ) beobachtet werden. Auf diese Weise kann auch die Funktionalität einer _managed connection_ erweitert werden. 

Dazu muss ein Listener vom Typ `IPhynixxManagedConnectionListener` implementiert werden. Um die ManagedConnectionFactory zu beauftragen, jede _managed connection_ mit diesem Listener zu verzieren, wird ihr ein `IPhynixxManagedConnectionDecorator` übergeben werden. Dieser wird bei der Instanzierung einer _managed connection_ gerufen und kann diese wie gewünscht verändern. 


Mittels dies Prinzips kann eine ManagedConnection auch um eigene Aspekte erweitert werden. 

      connectionFactory.addConnectionProxyDecorator(
                 new DumpManagedConnectionListener<TAEnabledUTFWriter>())
*Abbildung 5 *: Beobachten des Workflows einer _managed connection_


`DumpManagedConnectionListener` implementiert sowohl `IPhynixxManagedConnectionListener` als auch `IPhynixxManagedConnectionDecorator` 


## Globale Transaktionen ##


##  Wiederherstellungsinformationen
TBD


## Integration mit Spring

<b>TBD</b>

### lokale Transaktionen

### globale Transaktionen





   
