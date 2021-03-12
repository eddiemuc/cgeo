package cgeo.geocaching.brouter.util;

public interface ProgressListener
{
  public void updateProgress( String progress );

  public boolean isCanceled();
}
