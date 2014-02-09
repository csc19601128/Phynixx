Technologischer Kontext
==========================

Zustandsübergänge einer TA-gesicherten Resource
------------------------------------------------

In [gupta-1997]  Kap 3.2.1 sind die Zustandsübergänge einer transaktionsgesicherten Ressource  in einem 1/2 phase commit Protokoll beschrieben. (Abort(ing) entspricht dem gängigeren rollback(-ing).)
Bzgl. des 1 phase commit protocols lassen diese wie inAbbildung 1 reduzieren.


![erstes Bild](images/1PhaseCommitWithLogData.png)

Transaktionale Kontext
=======================

Transaktionaler Kontext (oder auch transactional states)  beschreibt die Daten, welche durch eine Commit/Rollback auf einer XAResource betroffen sind. Auf den ersten Auigenschein sollte man meinen, dass XAResource und transaktionaler Konext sich entsprechend, aber tatsächlich ist es so, dass 

* eine XARresource an mehreren Transaktionen teilnehmen kann 
** XAResource nimmt an mehrern Transaktionen unter[JTA 1.1], Chap 3.4.6
** einer XAResource nimmt ein eier 'suspended transaction' teil und wird im einer weiteren nicht suspended transaction genutzt[JTA 1.1] Chap.3.2.3 
* In einer Transaktion werden unterschiedliche XAResourcen zu einem transaktionalen Kontext zusammen gefasst werden
** falls die XAResourcen im Sinne von isSameRM gleich sind, so werden diese zusammen gefasst (start(.., TMJOIN)

Der einer XAResource zugeordnete transaktionale Kontext wird über Transaktion (genauer die XID) qualifiziert.
Mit den Angaben (XAResource, XID) wird der transaktionale Kontext qualifiziert. Es benötigt beide Angaben, um ihn zu bestimmen.



Lokale Transaktionen
=====================

siehe [JTRA 1.1] Chap. 3.4.7 
The resource adapter is encouraged to support the usage of both local and global transactions within the same transactional connection. 
Local transactions are transactions that are started and coordinated by the resource manager internally. The 
XAResource interface is not used for local transactions.
When using the same connection to perform both local and global 
transactions, the following rules apply:
• The local transaction must be committed (or rolled back) before starting a 
global transaction in the connection.
• The global transaction must be disassociated from the connection before any 
local transaction is started.
If a resource adapter does not support mixing local and global transactions 
within the same connection, the resource adapter should throw the resource 
specific exception. For example, java.sql.SQLException is thrown to the 22
application if the resource manager for the underlying RDBMS does not support 
mixing local and global transactions within the same JDBC connection. 
