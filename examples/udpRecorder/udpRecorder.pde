import com.fasterxml.jackson.core.JsonFactory;
import controlP5.app.ControlP5;
import controlP5.ControlEvent;
import controlP5.controller.chart.*;
import jp.ncl.time.Time;
import net.sorauta.network.udp.packet.UdpPacketInfo;
import net.sorauta.network.udp.recorder.UdpRecorder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

String RECORDER_FILENAME = "record_data.json";

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
  recorder.setup();
  recorder.setRecorderFilePath(dataPath(RECORDER_FILENAME));

  cp5 = new ControlP5(this);
  myChart = cp5.addChart("dataflow")
               .setPosition(20, 80)
               .setSize(width - 40, 100)
               .setRange(-20, 20)
               .setView(Chart.LINE)
               .setStrokeWeight(1.5)
               .setLabel("")
               ;

  myChart.addDataSet("incoming");
  myChart.setData("incoming", new float[100]);
}

void draw() {
  background(0);
  fill(255);

  boolean updatedFlag = recorder.update();
  
  myChart.push("incoming", (updatedFlag ? 10 : 0));

  // info
  text("Records: " + recorder.getCurrentPlayerIndex() + " / " + (recorder.getNumRecords() - 1), 20, 40);
  text("Recorder Status: " + recorder.getRecorderStatus(), 20, 60);
  
  // detail
  UdpPacketInfo lastPacketInfo = recorder.getLastPacketInfo();
  if (lastPacketInfo != null) {
    String s = lastPacketInfo.toString();
    s = s.substring(1, s.length() - 1);
    String[] str = s.split("\\]\\[");
    text("Packet Info: \r\n" + join(str, "\r\n"), 20, 200);
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

void mouseClicked() {
  // recorder.dumpRecordList();
}
