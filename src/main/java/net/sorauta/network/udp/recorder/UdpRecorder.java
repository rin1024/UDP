package net.sorauta.network.udp.recorder;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import net.sorauta.network.udp.UDP;
import net.sorauta.network.udp.packet.UdpPacketInfo;
import org.apache.log4j.Logger;
import processing.core.PApplet;

/** レコードするためのクラス */
public class UdpRecorder {
  /** instance of Logger */
  protected final Logger L = Logger.getLogger(getClass());

  private PApplet app;
  private UDP udp;

  private int receiverPort = 6000;
  private String recorderFilePath = "";

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

  /** 更新処理 */
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
    Date currentDate = new Date();

    if (!sameAsLastPackedInfo(_packetData)) {
      UdpPacketInfo packetInfo =
          new UdpPacketInfo(currentDate, currentMillis, _packetData, _senderIpAddress, _senderPort);
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

  /** json形式で保存する */
  public void saveRecordList() {
    // ファイルパスが設定されていない場合、p5のdataフォルダ配下に自動生成
    String recorderFilePath = getRecorderFilePath();
    if (recorderFilePath.equals("")) {
      setRecorderFilePath(app.dataPath("record_data.json"));
    }

    try {
      JsonFactory jsonFactory = new JsonFactory();
      JsonGenerator generator =
          jsonFactory.createJsonGenerator(
              new FileOutputStream(new File(recorderFilePath)), JsonEncoding.UTF8);

      generator.writeStartArray();

      for (Map.Entry<Long, UdpPacketInfo> kv : recordList.entrySet()) {
        Long timeCode = kv.getKey();
        UdpPacketInfo packetInfo = kv.getValue();

        generator.writeStartObject();
        generator.writeStringField("receivedDate", packetInfo.getReceivedDateAsString());
        generator.writeNumberField("receivedMillis", packetInfo.getReceivedMillis());
        generator.writeStringField("senderIpAddress", packetInfo.getSenderIpAddress());
        generator.writeNumberField("senderPort", packetInfo.getSenderPort());

        generator.writeArrayFieldStart("packetData");
        byte[] packets = packetInfo.getPacketData();
        for (byte packet : packets) {
          generator.writeObject(packet);
        }
        generator.writeEndArray();

        generator.writeEndObject();
      }

      generator.writeEndArray();

      // ファイルへの書き出し
      generator.flush();
    } catch (Exception e) {
      L.error(e);
    }
  }

  /**
   * json形式から呼び出す
   *
   * <p>TODO: implement here
   */
  public void loadRecordList() {
    try {
      // 保存データを一旦クリア
      recordList.clear();

      // jsonファイルを読み込む
      ObjectMapper mapper = new ObjectMapper();
      JsonNode rootNode = mapper.readTree(new File(recorderFilePath));

      for (JsonNode node : rootNode) {
        SimpleDateFormat sdf = new SimpleDateFormat(UdpPacketInfo.SDF_PATTERN);
        Date receivedDate = sdf.parse(node.get("receivedDate").asText());
        long receivedMillis = node.get("receivedMillis").asLong();
        String senderIpAddress = node.get("senderIpAddress").asText();
        int senderPort = node.get("senderPort").asInt();

        JsonNode bytesNode = node.get("packetData");
        // byte[] packetData = bytesNode.binaryValue();
        byte[] packetData = new byte[bytesNode.size()];
        try {
          for (int i = 0; i < packetData.length; i++) {
            packetData[i] = (byte) bytesNode.get(i).asInt();
          }
        } catch (Exception e2) {
          L.error("e2: " + e2);
        }

        UdpPacketInfo packetInfo =
            new UdpPacketInfo(
                receivedDate, receivedMillis, packetData, senderIpAddress, senderPort);

        recordList.put(receivedMillis, packetInfo);
      }

    } catch (Exception e) {
      L.error("e: " + e);
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // getter / setter
  ///////////////////////////////////////////////////////////////////////////////////////////////////
  public int getNumRecords() {
    return recordList.size();
  }

  public UdpRecorderStatus getRecorderStatus() {
    return recorderStatus;
  }

  public long getCurrentPlayerKey() {
    return currentPlayerKey;
  }

  public String getRecorderFilePath() {
    return recorderFilePath;
  }

  public void setRecorderFilePath(String _recorderFilePath) {
    recorderFilePath = _recorderFilePath;
  }
}
