// via: https://www.gwtcenter.com/handling-version-number-uniformly-by-gradle
package net.sorauta.network.udp;

public class Version {
  public static String version = "0.1-SNAPSHOT";
  public static String buildDate = "2023-04-24 11:48:55";

  /**
   * バージョン情報を返す
   *
   * @return version
   */
  public static String getVersion() {
    return version;
  }

  /**
   * コンパイルした日時を返す
   *
   * @return buildDate
   */
  public static String getBuildDate() {
    return buildDate;
  }
}
