# Visual Java PathFinder

An Eclipse plug-in to verify Java bytecode ie. .class file using [Java Pathfinder] [JPF] (JPF).

Some of the plug-in features:

  - One click verification for the  Java program
  - Built-in property editor
  - Graphical representation of JPF's output log.

### Modernization of VJP
This plugin  was created on 2008 by Sandro Badame and mentored by Peter Mehlitz. At that time it supported JDK 1.6 and JPF 
1.4 which made the plug-in unable to run on JDK 1.6 + as well as Eclipse Kepler and up. So the aim is to update VJP in order 
to run on the most recent version of Eclipse. 

### Version
2.0.0.15



### Installation

You need to have Eclipse installed.

Then make sure  you have installed Eclipse Plug-in Development (PDE). For more info, click [here].

After that , clone this repositry.

Finally,  copy [jpf.properties] [prop]  file to your local home directory.

```sh
$ cp jpf.properties ~
```

License
----

Eclipse Public License - v 1.0




   [here]: <https://eclipse.org/pde/>    
   [JPF]: <http://babelfish.arc.nasa.gov/trac/jpf>
   [prop]: <https://github.com/saadnaji/VJP/blob/master/jpf.properties> 



