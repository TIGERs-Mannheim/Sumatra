/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 *
 * *********************************************************
 */
Die Dateien in diesem Ordner enthalten die Coding-Richtlinien f�r das Projekt.
Wenn ihr das Projekt ganz normal auscheckt, sollten diese standardm��ig aktiviert
sein (siehe '.settings' Ordner; zu �ndern unter Rechtsklick(Projekt)->Properties->
Java Code Style), hier nur nochmal der Vollst�ndigkeit halber.

Zusatz (NicolaiO): Die Coding-Richtlinien haben sich inzwischen mehrmals geändert.
Mit Ausnahme der javatemplates wird alles automatisch mit den project-settings
aktiviert, es ist also nichts weiter zutun. Die XML-Dateien bitte ignorieren,
ich lasse sie der Vollständigkeit halber da.
Für die Java Templates gilt weiterhin, dass diese wie unten beschrieben importiert
werden müssen!

Die meisten Sachen lassen sich auf schon getippten Code - oder auch ganze
package-Strukturen - anwenden (die Shortcuts sind deshalb gleich mitangegeben)

* format.xml:
	Einstellungen f�r den eclipse Java-Code-Formatierer (Ctrl+Shift+F)
	
* codetemplates.xml:
	Beim Anlegen neuer Dateien/Typen/Methoden etc. einzuf�gende Kommentare o.�., ist
	per default aktiviert.
	
* cleanup.xml:
	Definiert was bei der Aktion "Clean up" alles ausgef�hrt werden soll. Sehr m�chtig,
	aber dementsprechend auch mit Vorsicht zu genie�en, man will seinen Code ja auch
	wiedererkennen;-)
	Deswegen ist das bei uns auf:
		(s. Rechtsklick(Projekt)->Properties->Java Code Style->Clean Up)
	beschr�nkt
	
	!Sollte man sich einen Shortcut f�r anlegen, ich habs auf (Ctrl+Alt+C)
	(Einzustellen unter Window->Preferences->General->Keys, nach "Clean Up" filtern,
	einfach Key-Binding angeben, Click(Apply))
	
* javatemplates.xml:
	Hier sind ein paar Templates, die das Leben des Programmiers einfacher machen sollen.
	(Das sind die Erg�nzungen, die bei Strg+Leer aufpoppen)
	Im Moment gibts:
	- (Sumatra-Observable)
		- observable_list
		- observable_list+notify
		- observable_notify
	- singleton
	- log (Log4j)
	
	Vor Benutzung m�sst ihr die �ber Window->Preferences->Java->Editor->Templates->Import...
	importieren. (Tipp: Unten den Haken bei "Use code formatter" wegmachen, selsamer Weise
	siehts dann besser aus...)
	Vorschlag: Sortiert die "Template Proposals" unter (...)Editor->Content Assist->Advanced
	(untere Liste) ganz nach oben, dann m�sst ihr nach Strg+Leer nicht noch gro� selektieren.