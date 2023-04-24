import com.fasterxml.jackson.core.JsonFactory;
import controlP5.app.ControlP5;
import controlP5.ControlEvent;
import controlP5.controller.chart.*;
import controlP5.controller.textfield.*;
import jp.ncl.time.Time;
import net.sorauta.network.udp.packet.UdpPacketInfo;
import net.sorauta.network.udp.recorder.UdpRecorder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

final int RECEIVER_PORT = 6454;
final String RECORDER_FILENAME = "record_data.json";

UdpRecorder recorder;

ControlP5 cp5;
Chart myChart;

void settings() {
  System.setProperty("logging.dir", sketchPath("log"));
  PropertyConfigurator.configure(dataPath("log4j.properties"));

  size(1280, 480);
}

void setup() {
  textSize(16);

  recorder = new UdpRecorder(this);
  recorder.setReceiverPort(RECEIVER_PORT);
  recorder.setup();
  recorder.setRecorderFilePath(dataPath(RECORDER_FILENAME));

  cp5 = new ControlP5(this);
  myChart = cp5.addChart("dataflow")
               .setPosition(20, 100)
               .setSize(width - 40, 100)
               .setRange(-20, 20)
               .setView(Chart.LINE)
               .setStrokeWeight(1.5)
               .setLabel("")
               ;
  myChart.addDataSet("incoming");
  myChart.setData("incoming", new float[100]);

  int xPos = width - 120;
  cp5.addButton("update")
     .setValue(0)
     .setPosition(xPos, 25)
     .setSize(100, 30)
     ;
  xPos -= 170;
  cp5.addTextfield("targetPort")
     .setPosition(xPos, 25)
     .setSize(150, 30)
     ;
  xPos -= 170;
  cp5.addTextfield("targetIpAddress")
     .setPosition(xPos,25)
     .setSize(150, 30)
     ;
}

void draw() {
  background(0);
  fill(255);

  boolean updatedFlag = recorder.update();
  
  myChart.push("incoming", (updatedFlag ? 10 : 0));

  // info
  text("ReceiverPort: " + recorder.getReceiverPort(), 20, 40);
  text("Records: " + recorder.getCurrentPlayerIndex() + " / " + (recorder.getNumRecords() - 1), 20, 60);
  text("Recorder Status: " + recorder.getRecorderStatus(), 20, 80);
  
  // detail
  UdpPacketInfo lastPacketInfo = recorder.getLastPacketInfo();
  if (lastPacketInfo != null) {
    String s = lastPacketInfo.toString();
    s = s.substring(1, s.length() - 1);
    String[] str = s.split("\\]\\[");
    text("Packet Info: \r\n" + join(str, "\r\n"), 20, 220);
  }

  // shortcut
  text("[L] Load from file ", 20, height - 120);
  text("[R] Record ", 20, height - 100);
  text("[P] Play ", 20, height - 80);
  text("[S] Stop ", 20, height - 60);
  text("[V] Save record data to file ", 20, height - 40);
}

void keyPressed() {
  if (keyCode == 'L') {
    println("load");
    recorder.loadRecordList();
  }
  else if (keyCode == 'R') {
    println("record");
    recorder.startToRecord();
  }
  else if (keyCode == 'P') {
    println("play");
    recorder.startToPlay();
  }
  else if (keyCode == 'S') {
    println("stop");
    recorder.stop();
  }
  else if (keyCode == 'V') {
    println("save");
    recorder.saveRecordList();
    recorder.stop();
  }
}

void update(int _theValue) {
  try {
    String targetIpAddress = cp5.get(Textfield.class,"targetIpAddress").getText();
    String targetPort = cp5.get(Textfield.class,"targetPort").getText();
    if (!targetIpAddress.equals("") && !targetPort.equals("")) {
      recorder.setTargetIpAddress(targetIpAddress);
      recorder.setTargetPort(Integer.parseInt(targetPort));
    }
  }
  catch (Exception e) {
    println(e);
  }
}
