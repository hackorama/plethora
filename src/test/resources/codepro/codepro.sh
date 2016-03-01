#java -classpath .:/cygdrive/c/eclipse/plugins  -jar `cygpath -m /cygdrive/c/eclipse/plugins/org.eclipse.equinox.launcher_1.2.0.v20110502.jar` -clean -noupdate -application org.eclipse.ant.core.antRunner -data  . -verbose -file codepro.xml 
java -classpath .:/cygdrive/c/eclipse/plugins  -jar `cygpath -m /cygdrive/c/eclipse/plugins/org.eclipse.equinox.launcher_1.2.0.v20110502.jar` -clean -noupdate -application org.eclipse.ant.core.antRunner -data ./tmp -verbose -buildfile codepro.xml 

