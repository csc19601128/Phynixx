Tutorial
=========

SourceCode findet sich im Module <i>phynixx-tutorial</i>. 


Das Projekt Phynixx bietet eine einfache Möglichkeit für Ressourcen an Transaktionen teilnehmen zu nehmen, seien es lokale oder globale (XA-protokoll, 2PC-) Transaktionen. 
Um eine Vorstellung vom Programmiermodell zu erhalten, stellen Sie sich eine Datenbank <code>javax.sql.Connection</code> vor, eine herkömmliche Datenbankverbindung. Diese Verbindung ist transaktional und kann, je nach zu grundeliegender DatenSource, auch an XA-Transaktionen teilnehmen.  
Neben der _Connection_ ist die _DateSource_ in ihrer Rolle als _ConnectionFactory_ wichtig.  

Auch im Programmiermodell von Phynixx wird ihre transaktionale Ressource als Connection bezeichnet, ebenso existiert eine ConnectionFactory um die Connections zu erzeugen. 

## Beispiel ##
In eine Datei sollen fortlaufend Strings geschrieben werden können. Diese Schreiboperationen sollen transaktional unterstützt werden, so dass auch eine Rollback möglich ist.
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
*Table 1 :*Implementierungen des Interfaces *IPhynixx*
<nbsp;>
Die Funktionalität zur Erfassung der Strings findet in <code>TAEnabledUTFWriter</code>, eine Subklasse von <code>IPhynixxConnection</code>.
Dort werden folgende Methoden implementiert
<table>
<tr><th>Methode</th><th>Beschreibung</th></tr> 
<tr><td>open</td><td>Öffnet eine Datei zum schreiben. Es wird die aktuelle Position der Datei als 'rollback'-Information gesichert</td></tr>
<tr><td>resetContent</td><td>Inhalt der Datei wird verworfen. </td></tr>
<tr><td>write</td><td>Es wird ein String in die Datei geschrieben.</td></tr>
</table>
&nbsp;

Die Methoden _open, resetContent, write_ verändern den Zustand der Ressource und müssen daher an einer Transaktion teilnehmen, damit diese korrekt funktionieren. Dies wird durch die Annotation <i>@RequiresTransaction</i> angezeigt.

Um sicherzustellen, dass die Datei auch bei unvorhergesehenem Abbruch der Transaktion wiederherzustellen ist, wird ein persistenter _XADataRecorder_ angefordert. Mittels diesem können Wiederherstellungsinformation gesichert werden.
Dazu muss das Interface _import org.csc.phynixx.connection.IXADataRecorderAware_ implementiert werden. Sobald eine Transaktion geöffnet wird, so wird der eine XARecoder via <code>setXADataRecorder(IXADataRecorder xaDataRecorder)</code> injeziert.

Es ist sichergestellt, dass der IXADataRecorder injeziert wird, unmittelbar bevor die erste Methode aufgerufen wird, welche durch _@RequireTransaction_ annotiert ist. Die Injezierung wird durch den Aufruf dieser Methode ausgelöst.

Da Zusammenspiel innerhalb einer (lokalen) Transaktion ist in der Testklasse <code>TransactionalBehaviourTest</code> zu beobachten.

    @Test
    public void testCommit() throws Exception {

        File file = this.tmpDir.assertExitsFile("my_test.tmp");

        TAEnabledUTFWriter connection = this.connectionFactory.getConnection();
        connection.open(file);
        try {
            connection.write("AA").write("BB");
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
        IPhynixxLoggerSystemStrategy strategy = new LoggerPerTransactionStrategy(loggerFactory);
        connectionFactory.setLoggerSystemStrategy(strategy);
    }
*Listing 3 :* Setup der Testumgebung

* Die <code>connectionFactory</code> liefert die Connections der Ressource <code>TAEnabledUTFWriter</code>. Eine Connection in ihrer reinen Form gegeben durch die Implementierung <code>TAEnabledUTFWriterImpl</code> reicht nicht aus, um innerhalb einer Transaktion zu agieren. Da vollständige Management der Integration in eine Transaktion wird durch Implementierungen von <code>IPhynixxManagedConnection&lt;TAEnabledUTFWriter&gt;</code> übernommen. Dazu wird der zughörigen Factory eine Factory übergfebene, welche die reinen Connection erzeugt. 
* Um einen persistenten <code>XADataRecorder</code> injezieren zu können, muss eine eine Persistenzverfahren an die Factory übergeben werden, welches die Wiederherstellungsinformationen sichert. In dieser Strategie wird festgelegt, auf welche Weise die transaktional relevanten Daten gesichert werden. Die Strategie <code>LoggerPerTransactionStrategy</code> erzeugt DataRecoder im Filesystem.
    

    @Test
    public void testRollback() throws Exception {

        File file = this.tmpDir.assertExitsFile("my_test.tmp");

        TAEnabledUTFWriter connection1 = this.connectionFactory.getConnection();
        try {
            connection1.open(file);
            connection1.write("AA").write("BB");
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

## Globale Transaktionen ##


## Integration mit Spring ##

### lokale Transaktionen ###

### globale Transaktionen ###





   
