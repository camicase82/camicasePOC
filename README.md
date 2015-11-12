# camicasePOC
POC worked by me on different techonologies

InboundDinamicGC shows how to dianmically load a spring integration converter from an external jar into an exisitng spring context

to make it work:

1. Create D:\DEVELOPMENT\TMP folder (or update in the code, sorry)
2. place InboundDinamicGC\DinamicConverters.jar on the new folder (this jar contains two converters)
3. Import the project inside eclipse
4. Right click on project, and run on tomcat server
5. Connect to port 9877 using telnet and send a message
6. Point browser to http://localhost:8080/InboundDinamicGC/dinamicSI (here we load the external jar file containing two converters)
7. Check the console, and see that the new converter seams to be loaded (2 converters total)
8. Send a new message on the same telnet terminal, it should show the output of the new converters
