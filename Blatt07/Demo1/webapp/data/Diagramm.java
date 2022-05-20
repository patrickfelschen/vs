package de.hsos.vs.web.applets;

import java.awt.Graphics;

/*
 * Das Applet im Src-Code und in kompilierter Form
 * muessen für den aufrufenden Browser zugaenglich sein.
 * Bei dem Netbeans-Projekt bedeutet dies, dass beides
 * unter build/web abgelegt werden muss.
 */
public class Diagramm extends java.applet.Applet {
   double f(double x) {
     return (Math.cos(x/5)+Math.sin(x/7)+2)*getSize().height/4;
   }

   public void paint(Graphics g) {
     for (int x = 0 ; x < getSize().width ; x++) {
        g.drawLine(x, (int)f(x), x+1, (int)f(x + 1));
     }
   }

   public String getAppletInfo() {
     return "Zeichnet Sinussignal mit zwei Frequenzen..";
   }
}
