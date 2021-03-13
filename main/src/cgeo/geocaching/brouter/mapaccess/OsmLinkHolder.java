/**
 * Container for routig configs
 *
 * @author ab
 */
package cgeo.geocaching.brouter.mapaccess;

public interface OsmLinkHolder
{
  void setNextForLink( OsmLinkHolder holder );

  OsmLinkHolder getNextForLink();
}
