
Phynixx Logger
===============

Atomares Schreiben von Dateien
-------------------------------
Soll in eine Datei Daten geschrieben werden, so geschieht die sequentiell. Werden bei diesem Schreibvorgang ein Teil der fehlerfrei Daten geschrieben und beim Sxchreiben des anderen teils treten probleme auf, so ist nicht klar, welchen Inhalt die Datei nach diesem Schreiben hat.
Im aktuellen Modul wird eine Klasse bereitgestellt, die atomres Schreiben in eine Datei sicherstellt. Entweder wird alle zu schreibenden Daten uebernommen oder keine.
Die <i>Klasse TAEnabledRandomAccessFile</i> führt in den ersten Stellen einen numerischen Pointer, welcher auf die Positipon innerhalb der datei zeigt, an der der relevante Inhalt endet. Werd en daten geschrieben, so wird erst nach erfolgreichem Schreiben in die Datei dieser Pointer umgesetzt.

In der Klasse <i>FileChannelLogger</i> auf basis dieser von <i>TAEnabledRandomAccessFile</i> ein atomarer Mechanismus bereitsgestellt, definierte Einheiten von Daten atomar zu schreiben und diese Dateneinheiten in einer Push-verfahren wieder auszulesen.

