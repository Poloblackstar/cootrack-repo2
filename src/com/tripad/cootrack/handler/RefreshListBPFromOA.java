/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.tripad.cootrack.handler;

import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;

import com.tripad.cootrack.utility.OpenApiUtils;
import com.tripad.cootrack.utility.ResponseResultToDB;

/**
 *
 * @author mfachmirizal
 */
public class RefreshListBPFromOA extends BaseActionHandler {
    protected JSONObject execute(Map<String, Object> parameters, String data) {
        String hasil = "";
        JSONObject json = new JSONObject();
        OpenApiUtils utils = new OpenApiUtils();
        
        // ArrayList<String> tempIdDataServer = new ArrayList<String>();
        try {
            if (utils.getCurrentPassword() != null ) {
                JSONObject hasilRetrieve = utils.requestListChildAccount(null);
                hasil = hasilRetrieve.get("msg").toString();
                
                if (hasil.length() == 0) {
                    //check, bila punya children maka tampung dlu
                    // Retrieve seluruh Data dari OpenAPi Ke BP
                    new ResponseResultToDB().validateBPList(hasilRetrieve);
                    
                    // JSONArray dataList = (JSONArray) hasilRetrieve.get("children");
                    
                }
                // new CustomJsonErrorResponse("5555", "Fatal protocol violation :
                // "+e.getMessage()).getErrResponse();
                
                json.put("jawaban", hasil);
                
            } else {
                json.put("jawaban", "This Openbravo user not synchronized with OpenAPI !");
            }
            // and return it
            return json;
        } catch (JSONException e) {
            System.out.println("MASUK JSONEXCEPTION");
            json.put("jawaban", e.getMessage());
        } finally {
            return json;
        }
    }
}
