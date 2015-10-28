package com.tripad.cootrack.handler;

import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;

import com.tripad.cootrack.utility.OpenApiUtils;

public class RefreshTokenHandler extends BaseActionHandler {

  protected JSONObject execute(Map<String, Object> parameters, String data) {
    String hasil = "";
    try {
      OpenApiUtils utils = new OpenApiUtils();

      // hasil = utils.getToken().getMessage();
      hasil = utils.getListMonitoring();

      // String isProcessed = "N";

      // get the data as json
      // final JSONObject jsonData = new JSONObject(data);
      // final JSONArray headerIds = jsonData.getJSONArray("headers");
      //
      // // iterate over the headersid
      // for (int i = 0; i < headerIds.length(); i++) {
      // final String headerId = headerIds.getString(i);
      //
      // // get the order
      // final GudUjianDinas gudUjianDinas = OBDal.getInstance().get(GudUjianDinas.class, headerId);
      //
      // // and add its grand totalheaderId
      // isProcessed = gudUjianDinas.isProcessed().toString();
      // }

      // create the result
      JSONObject json = new JSONObject();
      json.put("jawaban", hasil);

      // and return it
      return json;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }
}