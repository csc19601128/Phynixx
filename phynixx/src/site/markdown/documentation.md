Dokumentation Phynixx
=====================


## Logging ##
Phynixx setzt bei Logging auf _logback_ (siehe [http://logback.qos.ch/index.html] (http://logback.qos.ch/index.html) )
Um Phynixx kann mittels des Projekts SLF4J ( [http://www.slf4j.org/manual.html](http://www.slf4j.org/manual.html) ) an Ihr Logging angepasst werden . 
Dazu müssen die Aufrufe an das logback-Loggingsystem mittels der Bridge _logback-classic.jar_ nach SLF4J umgeleitet werden.

Wird diese Bridge eingesetzt, so muss 

<pre>
&lt;dependency&gt;
  &lt;groupId&gt;org.csc&lt;/groupId&gt;
  &lt;artifactId&gt;phynixx-common&lt;/artifactId&gt;
  &lt;version&gt;2.0.0&lt;version&gt;
  &lt;exclusions&gt;
    &lt;exclusion&gt;
       &lt;groupId&gt;ch.qos.logback&lt;/groupId&gt;
       &lt;artifactId&gt;logback-classic&lt;/artifactId&gt;
    &lt;/exclusion&gt;
  &lt;/exclusions&gt;
&lt;/dependency&gt;
</pre>


