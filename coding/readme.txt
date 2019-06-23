/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 *
 * *********************************************************
 */
Die Dateien in diesem Ordner enthalten die Coding-Richtlinien für das Projekt.
Wenn ihr das Projekt ganz normal auscheckt, sollten diese standardmäßig aktiviert
sein (siehe '.settings' Ordner; zu ändern unter Rechtsklick(Projekt)->Properties->
Java Code Style), hier nur nochmal der Vollständigkeit halber.

Die meisten Sachen lassen sich auf schon getippten Code - oder auch ganze
package-Strukturen - anwenden (die Shortcuts sind deshalb gleich mitangegeben)

* format.xml:
	Einstellungen für den eclipse Java-Code-Formatierer (Ctrl+Shift+F)
	
* codetemplates.xml:
	Beim Anlegen neuer Dateien/Typen/Methoden etc. einzufügende Kommentare o.ä., ist
	per default aktiviert.
	
* cleanup.xml:
	Definiert was bei der Aktion "Clean up" alles ausgeführt werden soll. Sehr mächtig,
	aber dementsprechend auch mit Vorsicht zu genießen, man will seinen Code ja auch
	wiedererkennen;-)
	Deswegen ist das bei uns auf:
		(s. Rechtsklick(Projekt)->Properties->Java Code Style->Clean Up)
	beschränkt
	
	!Sollte man sich einen Shortcut für anlegen, ich habs auf (Ctrl+Alt+C)
	(Einzustellen unter Window->Preferences->General->Keys, nach "Clean Up" filtern,
	einfach Key-Binding angeben, Click(Apply))
	
* javatemplates.xml:
	Hier sind ein paar Templates, die das Leben des Programmiers einfacher machen sollen.
	(Das sind die Ergänzungen, die bei Strg+Leer aufpoppen)
	Im Moment gibts:
	- (Sumatra-Observable)
		- observable_list
		- observable_list+notify
		- observable_notify
	- singleton
	- log (Log4j)
	
	Vor Benutzung müsst ihr die über Window->Preferences->Java->Editor->Templates->Import...
	importieren. (Tipp: Unten den Haken bei "Use code formatter" wegmachen, selsamer Weise
	siehts dann besser aus...)
	Vorschlag: Sortiert die "Template Proposals" unter (...)Editor->Content Assist->Advanced
	(untere Liste) ganz nach oben, dann müsst ihr nach Strg+Leer nicht noch groß selektieren.