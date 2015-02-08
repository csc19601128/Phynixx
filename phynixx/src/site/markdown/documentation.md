Dokumentation Phynixx
=====================


## Logging ##
Phynixx setzt bei Logging auf _logback_ (siehe [http://logback.qos.ch/index.html] (http://logback.qos.ch/index.html) ) mit dem Adapter _logback-classic.jar*  (siehe [http://www.eclipse.org/jetty/documentation/current/example-slf4j-multiple-loggers.html](http://www.eclipse.org/jetty/documentation/current/example-slf4j-multiple-loggers.html)

Dieser Adapter wird von Phynixx-Projekten als optionale Dependency angezogen. 
Um Phynixx kann mittels des Projekts SLF4J ( [http://www.slf4j.org/manual.html](http://www.slf4j.org/manual.html) ) an Ihr Logging angepasst werden . 
Ihre Anpassung an SLF4J umfasst zum einen den SLF4J-Adapter und die vin Ihnen benötigten Bridges. Da Phynixx _logback-core_ als LoggingAPI nutzt ist kein zusätzliche Bridge anzugeben.  



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


