Tutorial
=========

SourceCode findet sich im Module <i>phynixx-tutorial</i>

In eine Datei können fortlaufend Strings geschrieben werden. Diese Schreiboperationen sollen transaktional unterstützt werden, so dass auch eine Rollback möglich ist.

Im Rahmen einer Transaktion soll bei einem Rollback der Stand der Datei bei Beginn der Transaktion wiederhergestellt werden.

Folgender Testfall beschreibt die Arbeit mit dieser Klasse <code>org.csc.phynixx.tutorial.TAEnabledUTFWriter</code>

   package org.csc.phynixx.tutorial;
   . . . 
   public void testTAEnabledUTFWriter() throws Exception {
   
      File file = this.tmpDir.assertExitsFile("my_test.tmp");
      
      TAEnabledUTFWriter writer = TAEnabledUTFWriter.createWriter(file);
      try {
          // schreibe zwei String in die datei
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
    
Diese Funktionalität soll transaktional unterstützt werden. Dazu muss sie im ersten Schritt das Interface <code></code>

   
