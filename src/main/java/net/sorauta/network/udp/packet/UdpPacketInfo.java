package net.sorauta.network.udp.packet;

import java.text.SimpleDateFormat;
import java.util.Date;

/** 受信時刻、送信元情報、パケットを保持するためのクラス */
public class UdpPacketInfo {
  public static final String SDF_PATTERN = "yy-MM-dd HH:mm:ss.S Z";

  private Date receivedDate;
  private long receivedMillis;
  private byte[] packetData;
  private String senderIpAddress;
  private int senderPort;

  /**
   * create new instance
   *
   * @param _receivedDate 受信日時
   * @param _receivedMillis 受信ミリ秒
   * @param _packetData 受信packet
   * @param _senderIpAddress 送信元ip
   * @param _senderPort 送信元port
   */
  public UdpPacketInfo(
      Date _receivedDate,
      long _receivedMillis,
      byte[] _packetData,
      String _senderIpAddress,
      int _senderPort) {
    receivedDate = _receivedDate;
    receivedMillis = _receivedMillis;
    packetData = _packetData;
    senderIpAddress = _senderIpAddress;
    senderPort = _senderPort;
  }

  /**
   * getter
   *
   * @return 受信日時
   */
  public Date getReceivedDate() {
    return receivedDate;
  }

  /**
   * getter
   *
   * @return 受信日時を文字列で
   */
  public String getReceivedDateAsString() {
    return new SimpleDateFormat(SDF_PATTERN).format(receivedDate);
  }

  /**
   * getter
   *
   * @return 受信ミリ秒
   */
  public long getReceivedMillis() {
    return receivedMillis;
  }

  /**
   * getter
   *
   * @return 受信packet
   */
  public byte[] getPacketData() {
    return packetData;
  }

  /**
   * getter
   *
   * @return 送信元ip
   */
  public String getSenderIpAddress() {
    return senderIpAddress;
  }

  /**
   * getter
   *
   * @return 送信元port
   */
  public int getSenderPort() {
    return senderPort;
  }

  /** 文字列に変換 */
  @Override
  public String toString() {
    String outString = "";

    outString += ("[Date: " + getReceivedDateAsString() + "]");
    outString += ("[Millis: " + String.format("%09d", receivedMillis) + "]");
    outString += ("[From: " + senderIpAddress + "@" + senderPort + "]");
    outString += ("[Packet: ");
    for (int i = 0; i < packetData.length - 1; i++) { // -1しているのは、は終端文字を弾くため
      outString += ("" + (char) packetData[i] + " ");
    }
    outString += "]";

    return outString;
  }
}
