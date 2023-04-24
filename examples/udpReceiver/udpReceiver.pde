import com.fasterxml.jackson.core.JsonFactory;
import controlP5.app.ControlP5;
import controlP5.ControlEvent;
import controlP5.controller.chart.*;
import controlP5.controller.textfield.*;
import controlP5.controller.textarea.*;
import java.util.Date;
import jp.ncl.time.Time;
import net.sorauta.network.udp.packet.UdpPacketInfo;
import net.sorauta.network.udp.recorder.UdpRecorder;
import net.sorauta.network.udp.UDP;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

final int RECEIVER_PORT = 51065;
final Logger L = Logger.getLogger(getClass());

UDP udp;
ControlP5 cp5;
Chart myChart;

PFont font = null;
boolean updatedFlag = false;
UdpPacketInfo lastPacketInfo = null;

void settings() {
  System.setProperty("logging.dir", sketchPath("log"));
  PropertyConfigurator.configure(dataPath("log4j.properties"));

  size(1280, 480);
}

void setup() {
  font = createFont("arial", 14);
  textFont(font);

  udp = new UDP(this, RECEIVER_PORT);
  udp.listen(true);

  cp5 = new ControlP5(this);

  int xPos = 20;
  cp5.addTextfield("receiverPort")
    .setPosition(xPos, 25)
    .setSize(150, 30)
    .setText(Integer.toString(RECEIVER_PORT))
    ;
  xPos += 170;
  cp5.addButton("change")
    .setValue(0)
    .setPosition(xPos, 25)
    .setSize(100, 30)
    ;

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

  cp5.addTextarea("packetInfo")
    .setPosition(20, 220)
    .setSize(width - 40, 200)
    .setColor(color(128))
    .setColorBackground(color(255,100))
    .setColorForeground(color(255,100))
    .setFont(font)
    //.setLineHeight(14)
    ;
}

void draw() {
  background(0);
  fill(255);

  myChart.push("incoming", (updatedFlag ? 10 : 0));
  if (updatedFlag == true) {
    updatedFlag = false;
  }

  if (lastPacketInfo != null) {
    String s = lastPacketInfo.toString();
    s = s.substring(1, s.length() - 1);
    String[] str = s.split("\\]\\[");

    cp5.get(Textarea.class, "packetInfo").setText(join(str, "\r\n"));
  }
}

void change(int _theValue) {
  try {
    String receiverPort = cp5.get(Textfield.class, "receiverPort").getText();
    if (!receiverPort.equals("")) {
      udp.close();
      udp.listen(false);
      udp = new UDP(this, Integer.parseInt(receiverPort));
      udp.listen(true);
    }
  }
  catch (Exception e) {
    println(e);
  }
}

/** UDP受信時のイベントハンドラ */
void receive(byte[] _packetData, String _senderIpAddress, int _senderPort) {
  Date currentDate = new Date();
  UdpPacketInfo packetInfo =
    new UdpPacketInfo(currentDate, 0, _packetData, _senderIpAddress, _senderPort);
  // L.debug(packetInfo.toString());

  updatedFlag = true;
  lastPacketInfo = packetInfo;
}
