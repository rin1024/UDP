import com.fasterxml.jackson.core.JsonFactory;
import jp.ncl.time.Time;
import net.sorauta.network.udp.recorder.UdpRecorder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

String RECORDER_FILENAME = "record_data.json";

UdpRecorder recorder;

void settings() {
  System.setProperty("logging.dir", sketchPath("log"));
  PropertyConfigurator.configure(dataPath("log4j.properties"));

  size(640, 480);
}

void setup() {
  textSize(16);

  recorder = new UdpRecorder(this);
  recorder.setRecorderFilePath(dataPath(RECORDER_FILENAME));
}

void draw() {
  background(0);
  fill(255);

  recorder.update();

  // info
  text("Num Records: " + recorder.getNumRecords(), 20, 40);
  text("RecorderStatus: " + recorder.getRecorderStatus(), 20, 60);
  text("CurrentPlayerKey: " + recorder.getCurrentPlayerKey(), 20, 80);

  // shortcut
  text("[L] load from file ", 20, height - 120);
  text("[R] record ", 20, height - 100);
  text("[P] play ", 20, height - 80);
  text("[S] stop ", 20, height - 60);
  text("[V] save record data to file ", 20, height - 40);
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
  recorder.dumpRecordList();
}
