/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.tripad.cootrack.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;

import com.tripad.cootrack.data.TmcCar;
import com.tripad.cootrack.utility.OpenApiUtils;
import com.tripad.cootrack.utility.ResponseResultToDB;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

/**
 *
 * @author mfachmirizal
 */
public class RefreshListFilteredStatusCarByImei extends BaseActionHandler {
    private User COOTRACK_USER = OBContext.getOBContext().getUser();
    
    protected JSONObject execute(Map<String, Object> parameters, String data) {
        JSONObject json = new JSONObject();
        String headerId ="";
        String hasil = "";
        String imeiString = "";
        List<TmcCar> carList = null;
        List<String> waveList = new ArrayList<String>();
        OpenApiUtils utils = new OpenApiUtils();
        try {
            //parameter
            final JSONObject jsonData = new JSONObject(data);
            final JSONArray headerIds = jsonData.getJSONArray("headers");
            
            for (int i = 0; i < headerIds.length(); i++) {
                headerId = headerIds.getString(i);
            }
            
            
            // loop dulu list imei
            OBCriteria<TmcCar> tmcCarCriteria = OBDal.getInstance().createCriteria(TmcCar.class);
            tmcCarCriteria.add(Restrictions.eq(TmcCar.PROPERTY_CREATEDBY, COOTRACK_USER));
            int count = 1;
            int waveCount = 0;
            for (TmcCar tmcCar : tmcCarCriteria.list()) {
                imeiString += ("," + tmcCar.getImei());
                // imeiString = imeiString.substring(1);
                try {
                    waveList.set(waveCount, imeiString);
                } catch (IndexOutOfBoundsException iox) {
                    waveList.add(waveCount, imeiString);
                }
                
                if (count % 99 == 0) {
                    imeiString = "";
                    count = 0; //1
                    waveCount++;
                }
                count++;
            }
            
            int cc = 0;
            for (String wave : waveList) {
                //String arr[] = utils.convertToArray(wave.substring(1), ",");
                // requestData disini
                JSONObject hasilRetrieve = utils.requestStatusFilteredCarByImei(wave.substring(1));
                if (hasilRetrieve.get("msg").toString().length() > 0) {
                    hasil = hasilRetrieve.get("msg").toString();
                    break;
                }
                else {
                    new ResponseResultToDB().validateCarStatusList(headerId,hasilRetrieve);
                }              
                Thread.sleep(100);
                cc++;
            }
            
            json.put("jawaban", hasil);
            
            // and return it
            return json;
        } catch (JSONException e) {
            json.put("jawaban", e.getMessage());
        }finally {
            return json;
        }
        
    }
}
