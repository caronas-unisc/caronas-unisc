package br.unisc.caronasuniscegm.rest;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

public class RestErrorHandler {

    public static String getValidationsErrorMessages(Map<String, String> fieldNames,
                                                     VolleyError volleyError) throws JSONException {
        StringBuilder sb = new StringBuilder();

        // A API retorna erros de validação com status code 422. Se o status da resposta não for
        // este, não há mensagem de erro para ser exibida.
        if (volleyError.networkResponse.statusCode != 422)
            return null;

        String jsonText = new String(volleyError.networkResponse.data).trim();
        JSONObject fields = new JSONObject(jsonText);
        Iterator<String> iterator = fields.keys();

        while (iterator.hasNext()) {
            String fieldInternalName = iterator.next();
            String fieldDisplayName = getValueFromKeyOrKey(fieldNames, fieldInternalName);
            JSONArray errors = (JSONArray)fields.get(fieldInternalName);

            for (int i = 0; i < errors.length(); i++) {
                sb.append(fieldDisplayName + " " + errors.getString(i) + "\n");
            }
        }

        return sb.toString();
    }

    private static String getValueFromKeyOrKey(Map<String, String> map, String key) {
        return map.containsKey(key) ? map.get(key) : key;
    }

}
