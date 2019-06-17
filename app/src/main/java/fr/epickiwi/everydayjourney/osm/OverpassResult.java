package fr.epickiwi.everydayjourney.osm;

import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import fr.epickiwi.everydayjourney.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OverpassResult {

    private HashMap<Long,Node> nodes = new HashMap<>();
    private HashMap<Long,Way> ways = new HashMap<>();

    public OverpassResult(String jsonOverpassResult) throws JSONException {
        JSONArray elements = new JSONObject(jsonOverpassResult).getJSONArray("elements");

        for(int i = 0; i<elements.length(); i++){
            JSONObject el = elements.getJSONObject(i);
            if(el.getString("type").equals("node")) {
                Node node = new Node(el);
                this.nodes.put(node.getId(), node);
            }
        }

        for(int i = 0; i<elements.length(); i++){
            JSONObject el = elements.getJSONObject(i);
            if(el.getString("type").equals("way")) {
                Way way = new Way(el);
                if(el.has("nodes")) {
                    JSONArray nodes = el.getJSONArray("nodes");
                    for(int y = 0; y<nodes.length(); y++){
                        Node node = this.nodes.get(nodes.getLong(y));
                        if(node != null) {
                            way.addNode(node);
                            this.nodes.remove(node.getId());
                        }
                    }
                }
                ways.put(way.getId(),way);
            }
        }

    }

    public Node[] getNodes() {
        return nodes.values().toArray(new Node[0]);
    }

    public Way[] getWays() {
        return ways.values().toArray(new Way[0]);
    }


    private static OkHttpClient client = new OkHttpClient();

    public static void fromRequest(String overpassMLRequest, Context ctx, final OverpassRequestCallback callback){
        Request rq = null;

        try {

            rq = new Request.Builder()
                    .url(ctx.getString(R.string.overpass_url)+"?data="+ URLEncoder.encode(overpassMLRequest,"utf-8"))
                    .get()
                    .build();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        client.newCall(rq).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if(response.code() != 200){
                        callback.onFailure(new Exception("Request error : "+
                                response.request().url()+" \n"+
                                response.body().string()));
                        return;
                    }

                    OverpassResult result = new OverpassResult(response.body().string());

                    callback.onResponse(result);

                } catch (JSONException e) {
                    callback.onFailure(e);
                }
            }
        });

    }

    public interface OverpassRequestCallback {
        public void onFailure(Exception e);
        public void onResponse(OverpassResult result) throws JSONException;
    }

}
