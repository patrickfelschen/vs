package de.hsos.vs;

/**
 * Interface eines Billboard Adapters.
 * Damit können verschiedene Implementierungen
 * zum Datenaustausch realisiert werden.
 *
 * @author heikerli
 */
public interface BillBoardAdapterIf {
  String readEntries(String caller_ip);

  String readEntry(int idx, String caller_ip);
}
