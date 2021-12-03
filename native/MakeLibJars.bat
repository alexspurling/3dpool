jar cvf ../Webstart/lwjgl/win32lib.jar *.dll
jarsigner -keystore ../Webstart/mykeystore -storepass storepass -keypass keypass ../Webstart/lwjgl/win32lib.jar Alex
jar cvf ../Webstart/lwjgl/linuxlib.jar *.so
jarsigner -keystore ../Webstart/mykeystore -storepass storepass -keypass keypass ../Webstart/lwjgl/linuxlib.jar Alex