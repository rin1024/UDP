package net.sorauta.network.udp.packet;

/** 受信時刻、送信元情報、パケットを保持するためのクラス */
public class UdpPacketInfo {
  private long receivedMillis;
  private byte[] packetData;
  private String senderIpAddress;
  private int senderPort;

  public UdpPacketInfo(
      long _receivedMillis, byte[] _packetData, String _senderIpAddress, int _senderPort) {
    receivedMillis = _receivedMillis;
    packetData = _packetData;
    senderIpAddress = _senderIpAddress;
    senderPort = _senderPort;
  }

  public long getReceivedMillis() {
    return receivedMillis;
  }

  public byte[] getPacketData() {
    return packetData;
  }

  public String getSenderIpAddress() {
    return senderIpAddress;
  }

  public int getSenderPort() {
    return senderPort;
  }

  @Override
  public String toString() {
    String outString = "";

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
