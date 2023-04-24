package net.sorauta.network.udp.recorder;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import net.sorauta.network.udp.UDP;
import net.sorauta.network.udp.packet.UdpPacketInfo;
import processing.core.PApplet;

/** レコードするためのクラス */
public class UdpRecorder {
  private PApplet app;
  private UDP udp;

  private int receiverPort = 6000;
  private String saveFileName = "record.txt";

  private UdpRecorderStatus recorderStatus = UdpRecorderStatus.STOPPED;

  private TreeMap<Long, UdpPacketInfo> recordList;
  Iterator<Long> recordListIterator;

  // recorder
  private long firstRecordMillis = 0;
  private UdpPacketInfo lastPacketInfo;

  // player
  private long playerStartedMillis = 0;
  private long currentPlayerKey = 0;

  /** create new instance */
  public UdpRecorder(PApplet _app) {
    app = _app;
    recordList = new TreeMap<Long, UdpPacketInfo>();
    recordListIterator = null;
    lastPacketInfo = null;

    udp = new UDP(this, receiverPort);
    // udp.log( true );     // <-- printout the connection activity
    udp.listen(true);
  }

  public void update() {
    if (recorderStatus == UdpRecorderStatus.PLAYING) {
      updatePlaying();
    }
  }

  /** 録画開始 */
  public void startToRecord() {
    // if (firstRecordMillis == 0) {
    //   firstRecordMillis = app.millis();
    // }
    recorderStatus = UdpRecorderStatus.RECORDING;
  }

  /** 再生開始 */
  public void startToPlay() {
    recorderStatus = UdpRecorderStatus.PLAYING;
    playerStartedMillis = app.millis();
    currentPlayerKey = -1;

    recordListIterator = recordList.keySet().iterator();
    if (recordListIterator.hasNext()) {
      currentPlayerKey = (Long) recordListIterator.next();
    }
  }

  /** 停止 */
  public void stop() {
    recorderStatus = UdpRecorderStatus.STOPPED;
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // PLAYER
  ///////////////////////////////////////////////////////////////////////////////////////////////////
  public void updatePlaying() {
    if (recorderStatus != UdpRecorderStatus.PLAYING) {
      return;
    }

    if (recordListIterator.hasNext()) {
      if ((app.millis() - playerStartedMillis) - currentPlayerKey >= 0) {
        currentPlayerKey = (Long) recordListIterator.next();
        System.out.println("currentPlayerKey: " + currentPlayerKey);

        UdpPacketInfo packetInfo = recordList.get(currentPlayerKey);
        udp.send(
            packetInfo.getPacketData(),
            packetInfo.getSenderIpAddress(),
            packetInfo.getSenderPort());
      }
    } else {
      System.out.println("finished to play.");
      recorderStatus = UdpRecorderStatus.STOPPED;
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // RECORDER
  ///////////////////////////////////////////////////////////////////////////////////////////////////
  /** UDP受信時のイベントハンドラ */
  public void receive(byte[] _packetData, String _senderIpAddress, int _senderPort) {
    if (recorderStatus != UdpRecorderStatus.RECORDING) {
      return;
    }

    if (firstRecordMillis == 0) {
      firstRecordMillis = app.millis();
    }
    long currentMillis = app.millis() - firstRecordMillis;

    if (!sameAsLastPackedInfo(_packetData)) {
      UdpPacketInfo packetInfo =
          new UdpPacketInfo(currentMillis, _packetData, _senderIpAddress, _senderPort);
      recordList.put(currentMillis, packetInfo);

      // dump
      PApplet.println(packetInfo.toString());
    }
  }

  /** 最後に受信したpacketと同じか判定する */
  private boolean sameAsLastPackedInfo(byte[] _data) {
    if (lastPacketInfo == null) {
      return false;
    }

    byte[] lastPacketData = lastPacketInfo.getPacketData();
    for (int i = 0; i < lastPacketData.length; i++) {
      if (lastPacketData[i] != _data[i]) {
        return false;
      }
    }

    return true;
  }

  /** 保存されているレコード情報をdumpする */
  public void dumpRecordList() {
    for (Long receivedMillis : recordList.keySet()) {
      UdpPacketInfo packetInfo = recordList.get(receivedMillis);
      System.out.println(packetInfo);
    }
  }

  /**
   * txt形式で保存する
   *
   * <p>TODO: 大量のデータを砂漠にはむいていないので修正必要
   */
  public void saveRecordList() {
    int recordListIndex = 0;
    String[] recordListAsStrings = new String[recordList.size()];
    for (Map.Entry<Long, UdpPacketInfo> kv : recordList.entrySet()) {
      Long timeCode = kv.getKey();
      UdpPacketInfo packetInfo = kv.getValue();

      recordListAsStrings[recordListIndex] = timeCode + ":" + packetInfo.toString();

      recordListIndex++;
    }

    app.saveStrings(saveFileName, recordListAsStrings);
  }

  /**
   * txt形式から呼び出す
   *
   * <p>TODO: implement here
   */
  public void loadRecordList() {
    // TODO: load from text file
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // getter / setter
  ///////////////////////////////////////////////////////////////////////////////////////////////////
  public UdpRecorderStatus getRecorderStatus() {
    return recorderStatus;
  }

  public long getCurrentPlayerKey() {
    return currentPlayerKey;
  }
}
