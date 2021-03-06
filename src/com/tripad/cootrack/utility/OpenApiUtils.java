package com.tripad.cootrack.utility;

/****
 * Oleh : Moch Fachmi Rizal @ Tripad
 */

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Interval;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
//import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;

import com.tripad.cootrack.data.TmcToken;
import com.tripad.cootrack.data.TmcUserSync;
import com.tripad.cootrack.utility.exception.CustomJsonErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenApiUtils {
	
	final private static Logger log = LoggerFactory.getLogger(OpenApiUtils.class);
  // Nanti static var ini hapus
  //public User COOTRACK_USERNAME = OBContext.getOBContext().getUser();// "enduserdahlia";
  CootrackHttpClient con;
  User COOTRACK_USERNAME;

  // static var
  public static final String COOTRACK_GET_TOKEN_URL = "http://api.gpsoo.net/1/auth/access_token?";
  public static final String COOTRACK_GET_TARGET_INFO = "http://api.gpsoo.net/1/account/devinfo?target";
  public static final String COOTRACK_GET_MONITOR_URL_BY_TARGET = "http://api.gpsoo.net/1/account/monitor?target";
  public static final String COOTRACK_GET_TRACKING_URL_BY_IMEI = "http://api.gpsoo.net/1/devices/tracking?map_type=GOOGLE&imeis";

//  public OpenApiUtils(CootrackHttpClient conParam, User user) {
//      System.out.println("Lewat SINI 1");
//    con = conParam;
//    COOTRACK_USERNAME = user;
//  }

//  public OpenApiUtils(CootrackHttpClient conParam) {
//      System.out.println("Lewat SINI 2");
//    con = conParam;
//    COOTRACK_USERNAME = OBContext.getOBContext().getUser();
//  }
  public OpenApiUtils() {
    con = new CootrackHttpClient();
    COOTRACK_USERNAME = OBContext.getOBContext().getUser();
  }

  private JSONObject requestData(String action, String... param)
      throws JSONException, CustomJsonErrorResponseException {
    String commonParam = "";
    TmcToken token = getToken();
    if (token == null) {
      return new CustomJsonErrorResponseException(token.getReturnCode()+"Failed to get Token !")
          .getJSONErrResponse();
    }
    if (token.getReturnCode().equals("0")) {
      commonParam = "access_token=" + token.getToken() + "&account="
          + COOTRACK_USERNAME.getUsername() + "&time=" + getUnixTime();
    } else if (token.getReturnCode().equals("20001") || token.getReturnCode().equals("20004")) {
      return new CustomJsonErrorResponseException(token.getReturnCode(), "Wrong Username / Password !")
          .getJSONErrResponse();
    } else {
      return new CustomJsonErrorResponseException(token.getReturnCode(),
          token.getMessage() + ", Delete Token and try again").getJSONErrResponse();
    }

    String url = "";
    if (action.equals("tracking") || action.equals("trackingbytarget")) {
		if (action.equals("tracking")) {
			url = COOTRACK_GET_TRACKING_URL_BY_IMEI + "=" + param[0] + "&" + commonParam;
		} else {
			url = COOTRACK_GET_MONITOR_URL_BY_TARGET + "=" + param[0] + "&" + commonParam;
			System.out.println("URL : "+url);
			log.error("cetak URL : "+url);
			
		}
    } else if (action.equals("info")) {
      if (param[0] == null) {
        param[0] = encodeString(COOTRACK_USERNAME.getUsername());
      }
      if ("".equals(param[0])) {
        param[0] = encodeString(COOTRACK_USERNAME.getUsername());
      }
      url = COOTRACK_GET_TARGET_INFO + "=" + param[0] + "&" + commonParam;
      
      log.error("cetak URL : "+url);
      //System.out.println("URL : "+url);
    }

    String jsonResponse = "";
    // jsonResponse = new CootrackHttpClient().post2(url);
    jsonResponse = con.post(url);

    JSONObject hasil = new JSONObject(jsonResponse);

    System.out.println("Hasil return : "+hasil.get("ret").toString());
    //auto get token yg expired belum di tambahkan
    if ((hasil.get("ret").toString()).equals("10005")
        || (hasil.get("ret").toString()).equals("10006")) { //token error / not existed
      System.out.println("Token Habis : " + hasil.get("ret").toString());
      deleteToken();
      hasil = requestData(action, param);
    }
    //System.out.println("Lewat Pengecekan Token : "+hasil.get("ret").toString());

    if ((hasil.get("ret").toString()).equals("10101")) { // 10101 : ip limit
      JSONObject hasilRetry;
      do {
        try {
          Thread.sleep(30);
        } catch (InterruptedException e) {
          //return new CustomJsonErrorResponse("5151", "Thread Error : "+e.getMessage()).getJSONErrResponse();
          String strErr = new CustomJsonErrorResponseException("5151", "Thread Error : " + e.getMessage())
              .getStringErrResponse();
          throw new CustomJsonErrorResponseException(strErr);
        }
        hasilRetry = retryRequest(url);
      } while (hasilRetry.get("ret").toString().equals("10101"));
      //return hasilRetry;
      hasil = hasilRetry;
    }

    // ini exception custom
    if (hasil.get("ret").toString().equals("5555")) {
      System.out.println("Masuk 5555 : " + hasil.get("msg").toString());
      throw new CustomJsonErrorResponseException(hasil.get("msg").toString());
    }
    if (hasil.get("ret").toString().equals("10101")) {
      System.out.println("Masuk 10101 : " + hasil.get("msg").toString());
      throw new CustomJsonErrorResponseException(
          hasil.get("msg").toString() + ". Please retry again.");
    }
	  System.out.println("Hasil : " + hasil.toString());
    return hasil;
  }

  private JSONObject retryRequest(String url) {
    String jsonResponse = "";
    //    jsonResponse = new CootrackHttpClient().post2(url);
    jsonResponse = con.post(url);
    JSONObject jsonData = null;
    try {
      System.out.println("Melakukan retry request");
      jsonData = new JSONObject(jsonResponse);
      if (jsonData.get("ret").toString().equals("5555")) {
        System.out.println("Masuk 5555 : " + jsonData.get("msg").toString());
        throw new CustomJsonErrorResponseException(jsonData.get("msg").toString());
      }
      System.out.println("Hasill json retry : " + jsonData.toString());

    } catch (JSONException e) {
      //throw new OBException("Error [OpenApiUtils] ! : " + e.getMessage());// return new
      jsonData = new CustomJsonErrorResponseException("5555", "Error : " + e.getMessage())
          .getJSONErrResponse();
    } finally {
      return jsonData;
    }
  }

  public JSONObject requestListChildAccount(String target)
      throws JSONException, CustomJsonErrorResponseException {
    JSONObject jsonData;
    System.out.println("Target : " + target);
    if (target != null) {
      if (target.contains(" ")) {
        //target = URLEncoder.encode(target, "UTF-8");\
        target = target.replaceAll(" ", "%20");
      }
    }
    jsonData = requestData("info", target);

    return jsonData;
  }

  public JSONObject requestStatusFilteredCarByImei(String method, String param)
      throws JSONException, CustomJsonErrorResponseException {
    JSONObject jsonData;
	if (method.equals("byimei")) {
		jsonData = requestData("tracking", param);
	} else {
		jsonData = requestData("trackingbytarget", param);
		System.out.println("req data = "+jsonData.toString());
	}

    return jsonData;
  }

  public void deleteToken() {
    final OBCriteria<TmcToken> tmcTokenCrit = OBDal.getInstance().createCriteria(TmcToken.class);
    tmcTokenCrit.add(Restrictions.eq(TmcToken.PROPERTY_VALUE, COOTRACK_USERNAME.getUsername()));
    tmcTokenCrit.add(Restrictions.eq(TmcToken.PROPERTY_CREATEDBY, COOTRACK_USERNAME));
    for (TmcToken tokenFromDB : tmcTokenCrit.list()) {
      OBDal.getInstance().remove(tokenFromDB);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    }
  }

  public TmcToken getToken() {
    COOTRACK_USERNAME =  OBContext.getOBContext().getUser();
//    System.out.println("TOKEN INI : "+COOTRACK_USERNAME.getUsername());  
//    System.out.println("TOKEN INI : "+COOTRACK_USERNAME.getUsername());  
    final OBCriteria<TmcToken> tmcTokenCrit = OBDal.getInstance().createCriteria(TmcToken.class);
    tmcTokenCrit.add(Restrictions.eq(TmcToken.PROPERTY_VALUE, COOTRACK_USERNAME.getUsername()));
    tmcTokenCrit.add(Restrictions.eq(TmcToken.PROPERTY_CREATEDBY, COOTRACK_USERNAME));

    if (tmcTokenCrit.count() > 0) {
      for (TmcToken tokenFromDB : tmcTokenCrit.list()) {
        if (tokenFromDB.getMessage().equals("10006")) {// token expired, delete dan dapatkan kembali
          deleteToken();
          getToken();
        } //else if 5555
        else {
          return tokenFromDB;
        }
      }

    } else {
      System.out.println("Token blm ada sebelumnya ");
      // dapatkan token dari server API lewat internet
      TmcToken tmcToken = OBProvider.getInstance().get(TmcToken.class);
      String tokenParam = "account=" + COOTRACK_USERNAME.getUsername() + "&time=" + getUnixTime()
          + "&signature=" + (convertToMd5(getCurrentPassword() + getUnixTime()));
      String getTokenUrl = COOTRACK_GET_TOKEN_URL + tokenParam;

      System.out.println("token url " + getTokenUrl);
      String jsonResponse = con.post(getTokenUrl);
      //      String jsonResponse = new CootrackHttpClient().post2(getTokenUrl);
      JSONObject jsonData = null;

      try {
        jsonData = new JSONObject(jsonResponse);
        //jsonData = checkAndProcessStatusResponse(jsonData);

        tmcToken.setActive(true);
        tmcToken.setValue(COOTRACK_USERNAME.getUsername());
        if (Integer.parseInt(jsonData.get("ret").toString()) == 0) {
          tmcToken.setToken(jsonData.get("access_token").toString());
        } else {
          tmcToken.setToken("-");
        }
        tmcToken.setMessage(jsonData.get("msg").toString());
        tmcToken.setReturnCode((Integer.parseInt(jsonData.get("ret").toString())) + "");

        OBDal.getInstance().save(tmcToken);
        OBDal.getInstance().flush();

        OBDal.getInstance().commitAndClose();

        return tmcToken;

      } catch (JSONException ex) {
        tmcToken.setActive(true);
        tmcToken.setValue(COOTRACK_USERNAME.getUsername());
        tmcToken.setToken("-");
        tmcToken.setMessage(ex.getMessage());
        tmcToken.setReturnCode("55555");
        // Logger.getLogger(OpenApiUtils.class.getName()).log(Level.SEVERE, null, ex);
        return tmcToken;
      }
    }
    return null;
  }

  public long getUnixTime() {
    Date date = new Date();
    long unixTime = (long) date.getTime() / 1000;
    return unixTime;
  }

  private String encodeString(String param) {
    try {
      return URLEncoder.encode(param, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new OBException("Error ! : " + e.getMessage());
    }
  }

  /**
   *
   * @param param1
   *          waktu awal dalam format unix
   * @param param2
   *          waktu akhir dalam format unix
   * @param get
   *          tipe periode yg ingin di dapatkan (years,months,weeks,days,hours,minutes,seconds)
   * @return interval dalam int, dari 2 parameter yg di inputkan
   */
  public int getIntervalFromUnix(long param1, long param2, String get) {
    int hasil = 0;
    try {
      // Interval interval = new Interval( ((long)param1*1000), ((long)param2*1000) );
      Interval interval = new Interval((param1 * 1000L), (param2 * 1000L));
      get = get.toLowerCase();

      // if ("years".equals(get)) {
      // hasil = interval.toPeriod().getYears();
      // }else if ("months".equals(get)) {
      // hasil = interval.toPeriod().getMonths();
      // }else if ("weeks".equals(get)) {
      // hasil = interval.toPeriod().getWeeks();
      // }
      if ("days".equals(get)) {
        // hasil = interval.toPeriod().getDays();
        hasil = interval.toDuration().toStandardDays().getDays();
      } else if ("hours".equals(get)) {
        hasil = interval.toDuration().toStandardHours().getHours();
      } else if ("minutes".equals(get)) {
        hasil = interval.toDuration().toStandardMinutes().getMinutes();
      } else if ("seconds".equals(get)) {
        hasil = interval.toDuration().toStandardSeconds().getSeconds();

      } else {
        throw new OBException(
            get + " bukan parameter yg tepat untuk method : +getIntervalFromUnix");
      }
    } catch (Exception e) {
      hasil = -1;
    }
    return hasil;
  }

  // public String debugTanggal(long param1) {
  // DateTime dt = new DateTime(param1 * 1000L)
  // return null;
  // }

  public String convertToMd5(String param) {
    String password = param;
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5");
      md.update(password.getBytes());
    } catch (NoSuchAlgorithmException na) {
      throw new OBException("Error ! : " + na.getMessage());
    }

    byte byteData[] = md.digest();

    // convert the byte to hex format method 1
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < byteData.length; i++) {
      sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
    }

    // System.out.println("Digest(in hex format):: " + sb.toString());

    // convert the byte to hex format method 2
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < byteData.length; i++) {
      String hex = Integer.toHexString(0xff & byteData[i]);
      if (hex.length() == 1)
        hexString.append('0');
      hexString.append(hex);
    }
    // System.out.println("Digest(in hex format):: " + hexString.toString());
    return hexString.toString();
  }

  public String[] convertToArray(String src, String delim) {
    String hasil[] = src.split(delim);
    return hasil;
  }

  public String getCurrentPassword() {
    OBCriteria<TmcUserSync> tmcUserSyncCrit = OBDal.getInstance().createCriteria(TmcUserSync.class);
    tmcUserSyncCrit.add(Restrictions.eq(TmcUserSync.PROPERTY_USER, COOTRACK_USERNAME));

    if (tmcUserSyncCrit.count() > 0) {
      for (TmcUserSync tmcUserSync : tmcUserSyncCrit.list()) {
        return tmcUserSync.getPassword();
      }
    }
    return null;
  }

  public void deleteCurrentUserSync() {
    OBCriteria<TmcUserSync> tmcUserSync = OBDal.getInstance().createCriteria(TmcUserSync.class);
    tmcUserSync.add(Restrictions.eq(TmcUserSync.PROPERTY_USER, OBContext.getOBContext().getUser()));

    for (TmcUserSync tmcUserSyncList : tmcUserSync.list()) {
      OBDal.getInstance().remove(tmcUserSyncList);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    }
  }
  
  public void closeConnection() {
      con.shutdown();
  }

}
