package net.sorauta.network.udp.recorder;

/** レコーダーの状態確認用enum */
public enum UdpRecorderStatus {
  /** stop */
  STOPPED(0),
  /** record */
  RECORDING(1),
  /** play */
  PLAYING(2);

  private final int id;

  /**
   * set id
   *
   * @param _id 0 ~ 4
   */
  private UdpRecorderStatus(int _id) {
    this.id = _id;
  }

  /**
   * get id
   *
   * @return id as int
   */
  public int getId() {
    return this.id;
  }

  /**
   * get UdpRecorderStatus id
   *
   * @param _id 0 ~
   * @return UdpRecorderStatus
   */
  public static UdpRecorderStatus from(int _id) {
    for (UdpRecorderStatus recorderStatus : UdpRecorderStatus.values()) {
      if (recorderStatus.getId() == _id) {
        return recorderStatus;
      }
    }
    return STOPPED;
  }

  /**
   * get UdpRecorderStatus from String
   *
   * @param _statusName 取得したいUdpRecorderStatusのStringをセット
   * @return UdpRecorderStatus
   */
  public static UdpRecorderStatus from(String _statusName) {
    for (UdpRecorderStatus recorderStatus : UdpRecorderStatus.values()) {
      if (recorderStatus.toString().equals(_statusName)) {
        return recorderStatus;
      }
    }
    return STOPPED;
  }
}
